package com.example.khataapp.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "khata_prefs")

data class ShopProfile(
    val ownerName: String  = "",
    val shopName: String   = "",
    val phone: String      = "",
    val isSetupDone: Boolean = false
)

class UserPreferences(private val context: Context) {

    companion object {
        private val OWNER_NAME    = stringPreferencesKey("owner_name")
        private val SHOP_NAME     = stringPreferencesKey("shop_name")
        private val PHONE         = stringPreferencesKey("phone")
        private val PIN_HASH      = stringPreferencesKey("pin_hash")
        private val SETUP_DONE    = booleanPreferencesKey("setup_done")
    }

    val shopProfile: Flow<ShopProfile> = context.dataStore.data.map { prefs ->
        ShopProfile(
            ownerName   = prefs[OWNER_NAME]  ?: "",
            shopName    = prefs[SHOP_NAME]   ?: "",
            phone       = prefs[PHONE]       ?: "",
            isSetupDone = prefs[SETUP_DONE]  ?: false
        )
    }

    val isSetupDone: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SETUP_DONE] ?: false
    }

    suspend fun saveProfile(ownerName: String, shopName: String, phone: String, pin: String) {
        context.dataStore.edit { prefs ->
            prefs[OWNER_NAME] = ownerName
            prefs[SHOP_NAME]  = shopName
            prefs[PHONE]      = phone
            prefs[PIN_HASH]   = hashPin(pin)
            prefs[SETUP_DONE] = true
        }
    }

    suspend fun updateProfile(ownerName: String, shopName: String, phone: String) {
        context.dataStore.edit { prefs ->
            prefs[OWNER_NAME] = ownerName
            prefs[SHOP_NAME]  = shopName
            prefs[PHONE]      = phone
        }
    }

    suspend fun changePin(newPin: String) {
        context.dataStore.edit { prefs ->
            prefs[PIN_HASH] = hashPin(newPin)
        }
    }

    fun verifyPin(pin: String): Flow<Boolean> = context.dataStore.data.map { prefs ->
        val stored = prefs[PIN_HASH] ?: return@map false
        stored == hashPin(pin)
    }

    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(pin.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}
