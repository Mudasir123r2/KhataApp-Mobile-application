package com.example.khataapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.khataapp.data.local.entity.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE customerId = :customerId ORDER BY date DESC")
    fun getTransactionsByCustomer(customerId: Int): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int = 10): Flow<List<Transaction>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE type = 'credit'")
    fun getTotalCredit(): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE type = 'debit'")
    fun getTotalDebit(): Flow<Double>

    @Query("SELECT COUNT(*) FROM transactions")
    fun getTotalTransactionCount(): Flow<Int>

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE customerId = :customerId AND date BETWEEN :from AND :to ORDER BY date DESC")
    fun getTransactionsByCustomerInRange(customerId: Int, from: Long, to: Long): Flow<List<Transaction>>

    @Query("""
        SELECT t.id, t.customerId, c.name AS customerName, c.phone AS customerPhone,
               t.amount, t.type, t.note, t.date, t.dueDate
        FROM transactions t
        INNER JOIN customers c ON t.customerId = c.id
        ORDER BY t.date DESC
    """)
    fun getTransactionsWithCustomer(): Flow<List<com.example.khataapp.data.local.entity.TransactionWithCustomer>>
}
