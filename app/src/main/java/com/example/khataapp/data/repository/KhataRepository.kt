package com.example.khataapp.data.repository

import com.example.khataapp.data.local.dao.CustomerDao
import com.example.khataapp.data.local.dao.TransactionDao
import com.example.khataapp.data.local.entity.Customer
import com.example.khataapp.data.local.entity.CustomerWithBalance
import com.example.khataapp.data.local.entity.Transaction
import kotlinx.coroutines.flow.Flow

class KhataRepository(
    private val customerDao: CustomerDao,
    private val transactionDao: TransactionDao
) {
    val customersWithBalance: Flow<List<CustomerWithBalance>> =
        customerDao.getCustomersWithBalance()

    val allCustomers: Flow<List<Customer>> =
        customerDao.getAllCustomers()

    val allTransactions: Flow<List<Transaction>> =
        transactionDao.getAllTransactions()

    val allTransactionsWithCustomer: Flow<List<com.example.khataapp.data.local.entity.TransactionWithCustomer>> =
        transactionDao.getTransactionsWithCustomer()

    val totalCredit: Flow<Double>    = transactionDao.getTotalCredit()
    val totalDebit: Flow<Double>     = transactionDao.getTotalDebit()
    val totalTransactions: Flow<Int> = transactionDao.getTotalTransactionCount()

    fun getRecentTransactions(limit: Int = 10): Flow<List<Transaction>> =
        transactionDao.getRecentTransactions(limit)

    fun getCustomerById(customerId: Int): Flow<Customer?> =
        customerDao.getCustomerById(customerId)

    fun getTransactionsByCustomer(customerId: Int): Flow<List<Transaction>> =
        transactionDao.getTransactionsByCustomer(customerId)

    fun getTransactionsByCustomerInRange(customerId: Int, from: Long, to: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByCustomerInRange(customerId, from, to)

    suspend fun insertCustomer(customer: Customer) =
        customerDao.insertCustomer(customer)

    suspend fun deleteCustomer(customer: Customer) =
        customerDao.deleteCustomer(customer)

    suspend fun insertTransaction(transaction: Transaction) =
        transactionDao.insertTransaction(transaction)

    suspend fun deleteTransaction(transaction: Transaction) =
        transactionDao.deleteTransaction(transaction)
}
