package com.example.khataapp.data.local.entity

data class TransactionWithCustomer(
    val id: Int,
    val customerId: Int,
    val customerName: String,
    val customerPhone: String,
    val amount: Double,
    val type: String,
    val note: String,
    val date: Long,
    val dueDate: Long? = null
)
