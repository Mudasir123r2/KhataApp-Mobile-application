package com.example.khataapp.data.local.entity

data class CustomerWithBalance(
    val id: Int,
    val name: String,
    val phone: String,
    val balance: Double,
    val lastTransactionDate: Long? = null,
    val totalTransactions: Int = 0
)
