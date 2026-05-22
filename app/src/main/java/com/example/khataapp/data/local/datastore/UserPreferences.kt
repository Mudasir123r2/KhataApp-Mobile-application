package com.example.khataapp.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "khata_prefs")

data class ShopProfile(
    val ownerName: String = "",
    val shopName: String  = "",
    val phone: String     = ""
)

class UserPreferences(private val context: Context) {

    companion object {
        private val CURRENT_USER_ID = intPreferencesKey("current_user_id")
    }

    val currentUserId: Flow<Int?> = context.dataStore.data.map { prefs ->
        prefs[CURRENT_USER_ID]?.takeIf { it > 0 }
    }

    suspend fun saveSession(userId: Int) {
        context.dataStore.edit { it[CURRENT_USER_ID] = userId }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.remove(CURRENT_USER_ID) }
    }
}
