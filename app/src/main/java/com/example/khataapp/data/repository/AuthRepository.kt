package com.example.khataapp.data.repository

import com.example.khataapp.data.local.datastore.ShopProfile
import com.example.khataapp.data.local.datastore.UserPreferences
import kotlinx.coroutines.flow.Flow

class AuthRepository(private val prefs: UserPreferences) {

    val shopProfile: Flow<ShopProfile> = prefs.shopProfile
    val isSetupDone: Flow<Boolean>     = prefs.isSetupDone

    suspend fun saveProfile(ownerName: String, shopName: String, phone: String, pin: String) =
        prefs.saveProfile(ownerName, shopName, phone, pin)

    suspend fun updateProfile(ownerName: String, shopName: String, phone: String) =
        prefs.updateProfile(ownerName, shopName, phone)

    suspend fun changePin(newPin: String) = prefs.changePin(newPin)

    fun verifyPin(pin: String): Flow<Boolean> = prefs.verifyPin(pin)
}
