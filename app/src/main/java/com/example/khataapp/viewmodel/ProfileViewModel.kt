package com.example.khataapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.khataapp.KhataApplication
import com.example.khataapp.data.local.datastore.ShopProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = (application as KhataApplication).authRepository

    val shopProfile: StateFlow<ShopProfile> = auth.shopProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ShopProfile())

    private val _pinChangeResult = MutableStateFlow<String?>(null)
    val pinChangeResult: StateFlow<String?> = _pinChangeResult.asStateFlow()

    private val _profileSaved = MutableStateFlow(false)
    val profileSaved: StateFlow<Boolean> = _profileSaved.asStateFlow()

    fun updateProfile(ownerName: String, shopName: String, phone: String) {
        viewModelScope.launch {
            auth.updateProfile(ownerName, shopName, phone)
            _profileSaved.value = true
        }
    }

    fun changePin(current: String, newPin: String, confirm: String) {
        viewModelScope.launch {
            if (newPin != confirm) { _pinChangeResult.value = "New PINs do not match"; return@launch }
            if (newPin.length != 4) { _pinChangeResult.value = "PIN must be 4 digits"; return@launch }
            val valid = auth.verifyPin(current).first()
            if (!valid) { _pinChangeResult.value = "Current PIN is incorrect"; return@launch }
            auth.changePin(newPin)
            _pinChangeResult.value = "PIN changed successfully"
        }
    }

    fun clearPinResult() { _pinChangeResult.value = null }
    fun clearProfileSaved() { _profileSaved.value = false }
}
