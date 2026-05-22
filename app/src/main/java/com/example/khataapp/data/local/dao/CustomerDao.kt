package com.example.khataapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.khataapp.data.local.entity.Customer
import com.example.khataapp.data.local.entity.CustomerWithBalance
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :customerId")
    fun getCustomerById(customerId: Int): Flow<Customer?>

    @Query("""
        SELECT c.id, c.name, c.phone,
        COALESCE(SUM(CASE WHEN t.type = 'credit' THEN t.amount
                         WHEN t.type = 'debit' THEN -t.amount
                         ELSE 0 END), 0.0) AS balance,
        MAX(t.date) AS lastTransactionDate,
        COUNT(t.id) AS totalTransactions
        FROM customers c
        LEFT JOIN transactions t ON c.id = t.customerId
        GROUP BY c.id
        ORDER BY c.name ASC
    """)
    fun getCustomersWithBalance(): Flow<List<CustomerWithBalance>>
}
