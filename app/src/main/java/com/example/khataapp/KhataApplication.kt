package com.example.khataapp

import android.app.Application
import com.example.khataapp.data.local.database.AppDatabase
import com.example.khataapp.data.local.datastore.UserPreferences
import com.example.khataapp.data.repository.AuthRepository
import com.example.khataapp.data.repository.KhataRepository

class KhataApplication : Application() {
    val database        by lazy { AppDatabase.getDatabase(this) }
    val repository      by lazy { KhataRepository(database.customerDao(), database.transactionDao()) }
    val userPreferences by lazy { UserPreferences(this) }
    val authRepository  by lazy { AuthRepository(database.userDao(), userPreferences) }
}
