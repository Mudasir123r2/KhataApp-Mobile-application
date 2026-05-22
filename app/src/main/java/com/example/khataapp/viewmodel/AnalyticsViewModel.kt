package com.example.khataapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.khataapp.KhataApplication
import com.example.khataapp.data.local.entity.CustomerWithBalance
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MonthlyStat(
    val key: String,          // "2026-01"
    val displayName: String,  // "Jan '26"
    val credit: Double,
    val debit: Double,
    val txCount: Int
) {
    val net: Double get() = credit - debit
}

data class AnalyticsSummary(
    val totalCredit: Double       = 0.0,
    val totalDebit: Double        = 0.0,
    val netBalance: Double        = 0.0,
    val totalCustomers: Int       = 0,
    val avgTxAmount: Double       = 0.0,
    val totalTxCount: Int         = 0,
    val mostActiveCustomer: String = "",
    val peakMonth: String          = ""
)

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as KhataApplication).repository

    val monthlyStats: StateFlow<List<MonthlyStat>> = repo.allTransactions
        .map { transactions ->
            val cal     = Calendar.getInstance()
            val keyFmt  = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val dispFmt = SimpleDateFormat("MMM ''yy", Locale.getDefault())

            val grouped = transactions.groupBy { tx ->
                cal.timeInMillis = tx.date
                keyFmt.format(cal.time)
            }

            grouped.entries
                .sortedByDescending { it.key }
                .take(6)
                .map { (key, txList) ->
                    val date = keyFmt.parse(key) ?: Date()
                    MonthlyStat(
                        key         = key,
                        displayName = dispFmt.format(date),
                        credit      = txList.filter { it.type == "credit" }.sumOf { it.amount },
                        debit       = txList.filter { it.type == "debit"  }.sumOf { it.amount },
                        txCount     = txList.size
                    )
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val summary: StateFlow<AnalyticsSummary> =
        combine(
            repo.allTransactions,
            repo.customersWithBalance,
            repo.allCustomers
        ) { txList, customers, allCustomers ->
            val credit   = txList.filter { it.type == "credit" }.sumOf { it.amount }
            val debit    = txList.filter { it.type == "debit"  }.sumOf { it.amount }
            val avgTx    = if (txList.isNotEmpty()) txList.sumOf { it.amount } / txList.size else 0.0

            val mostActive = customers.maxByOrNull { it.totalTransactions }?.name ?: ""

            val keyFmt = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val dispFmt = SimpleDateFormat("MMM ''yy", Locale.getDefault())
            val cal = Calendar.getInstance()
            val peakMonth = txList
                .groupBy { tx -> cal.also { it.timeInMillis = tx.date }.let { keyFmt.format(it.time) } }
                .maxByOrNull { it.value.size }
                ?.key?.let { key -> keyFmt.parse(key)?.let { dispFmt.format(it) } ?: key }
                ?: ""

            AnalyticsSummary(
                totalCredit        = credit,
                totalDebit         = debit,
                netBalance         = credit - debit,
                totalCustomers     = allCustomers.size,
                avgTxAmount        = avgTx,
                totalTxCount       = txList.size,
                mostActiveCustomer = mostActive,
                peakMonth          = peakMonth
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsSummary())

    val topCustomers: StateFlow<List<CustomerWithBalance>> = repo.customersWithBalance
        .map { list -> list.filter { it.totalTransactions > 0 }.sortedByDescending { it.totalTransactions }.take(5) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
