package com.example.khataapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.khataapp.KhataApplication
import com.example.khataapp.data.local.entity.Customer
import com.example.khataapp.data.local.entity.CustomerWithBalance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class CustomerSortOrder { NAME_ASC, BALANCE_HIGH, BALANCE_LOW }

class CustomerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as KhataApplication).repository

    val searchQuery = MutableStateFlow("")
    val sortOrder   = MutableStateFlow(CustomerSortOrder.NAME_ASC)

    private val allCustomers: StateFlow<List<CustomerWithBalance>> =
        repository.customersWithBalance.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val customersWithBalance: StateFlow<List<CustomerWithBalance>> =
        combine(allCustomers, searchQuery, sortOrder) { list, query, sort ->
            val filtered = if (query.isBlank()) list
                           else list.filter {
                               it.name.contains(query, ignoreCase = true) ||
                               it.phone.contains(query, ignoreCase = true)
                           }
            when (sort) {
                CustomerSortOrder.NAME_ASC    -> filtered.sortedBy { it.name.lowercase() }
                CustomerSortOrder.BALANCE_HIGH -> filtered.sortedByDescending { it.balance }
                CustomerSortOrder.BALANCE_LOW  -> filtered.sortedBy { it.balance }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCustomer(name: String, phone: String) {
        viewModelScope.launch {
            repository.insertCustomer(Customer(name = name, phone = phone))
        }
    }

    fun updateCustomer(id: Int, name: String, phone: String) {
        viewModelScope.launch {
            repository.insertCustomer(Customer(id = id, name = name, phone = phone))
        }
    }

    fun deleteCustomer(customer: CustomerWithBalance) {
        viewModelScope.launch {
            repository.deleteCustomer(
                Customer(id = customer.id, name = customer.name, phone = customer.phone)
            )
        }
    }
}
