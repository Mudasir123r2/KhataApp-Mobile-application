package com.example.khataapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.khataapp.KhataApplication
import com.example.khataapp.data.local.entity.TransactionWithCustomer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

enum class TxTypeFilter { ALL, CREDIT, DEBIT }

data class TxDateGroup(
    val label: String,
    val transactions: List<TransactionWithCustomer>
)

class AllTransactionsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as KhataApplication).repository

    val searchQuery = MutableStateFlow("")
    val typeFilter  = MutableStateFlow(TxTypeFilter.ALL)

    private val allWithCustomer: StateFlow<List<TransactionWithCustomer>> =
        repo.allTransactionsWithCustomer.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

    val filtered: StateFlow<List<TransactionWithCustomer>> =
        combine(allWithCustomer, searchQuery, typeFilter) { list, query, type ->
            list.filter { tx ->
                val matchesQuery = query.isBlank() ||
                    tx.customerName.contains(query, ignoreCase = true) ||
                    tx.note.contains(query, ignoreCase = true)
                val matchesType = when (type) {
                    TxTypeFilter.ALL    -> true
                    TxTypeFilter.CREDIT -> tx.type == "credit"
                    TxTypeFilter.DEBIT  -> tx.type == "debit"
                }
                matchesQuery && matchesType
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val grouped: StateFlow<List<TxDateGroup>> = filtered.map { list ->
        val now     = System.currentTimeMillis()
        val todayMs = now - (now % (24 * 60 * 60 * 1000L))

        val today     = mutableListOf<TransactionWithCustomer>()
        val yesterday = mutableListOf<TransactionWithCustomer>()
        val thisWeek  = mutableListOf<TransactionWithCustomer>()
        val thisMonth = mutableListOf<TransactionWithCustomer>()
        val older     = mutableListOf<TransactionWithCustomer>()

        list.forEach { tx ->
            when {
                tx.date >= todayMs                        -> today.add(tx)
                tx.date >= todayMs - 86_400_000L          -> yesterday.add(tx)
                tx.date >= now - 7L * 86_400_000L         -> thisWeek.add(tx)
                tx.date >= now - 30L * 86_400_000L        -> thisMonth.add(tx)
                else                                      -> older.add(tx)
            }
        }

        buildList {
            if (today.isNotEmpty())     add(TxDateGroup("Today", today))
            if (yesterday.isNotEmpty()) add(TxDateGroup("Yesterday", yesterday))
            if (thisWeek.isNotEmpty())  add(TxDateGroup("This Week", thisWeek))
            if (thisMonth.isNotEmpty()) add(TxDateGroup("This Month", thisMonth))
            if (older.isNotEmpty())     add(TxDateGroup("Older", older))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val summaryCredit: StateFlow<Double> = filtered
        .map { list -> list.filter { it.type == "credit" }.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val summaryDebit: StateFlow<Double> = filtered
        .map { list -> list.filter { it.type == "debit" }.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
}
