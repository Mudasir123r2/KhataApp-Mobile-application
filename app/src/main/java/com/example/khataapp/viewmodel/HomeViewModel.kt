package com.example.khataapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.khataapp.KhataApplication
import com.example.khataapp.data.local.entity.CustomerWithBalance
import com.example.khataapp.data.local.entity.Transaction
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class DashboardStats(
    val totalCustomers: Int    = 0,
    val totalCredit: Double    = 0.0,
    val totalDebit: Double     = 0.0,
    val netBalance: Double     = 0.0,
    val totalTransactions: Int = 0
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as KhataApplication).repository
    private val auth = (application as KhataApplication).authRepository

    val shopProfile = auth.shopProfile
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            com.example.khataapp.data.local.datastore.ShopProfile()
        )

    val dashboardStats: StateFlow<DashboardStats> =
        combine(
            repo.allCustomers,
            repo.totalCredit,
            repo.totalDebit,
            repo.totalTransactions
        ) { customers, credit, debit, txCount ->
            DashboardStats(
                totalCustomers    = customers.size,
                totalCredit       = credit,
                totalDebit        = debit,
                netBalance        = credit - debit,
                totalTransactions = txCount
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    val recentTransactions: StateFlow<List<Transaction>> =
        repo.getRecentTransactions(8).stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

    // Customers with positive balance = they owe the shopkeeper
    val topDebtors: StateFlow<List<CustomerWithBalance>> = repo.customersWithBalance
        .map { list -> list.filter { it.balance > 0 }.sortedByDescending { it.balance }.take(5) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val overdueCustomers: StateFlow<List<CustomerWithBalance>> = repo.customersWithBalance
        .map { list -> list.filter { it.balance > 0 }.sortedByDescending { it.balance } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val overdueCount: StateFlow<Int> = repo.customersWithBalance
        .map { list -> list.count { it.balance > 0 } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
}
