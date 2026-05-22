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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = (application as KhataApplication).authRepository

    val shopProfile: StateFlow<ShopProfile> = auth.shopProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ShopProfile())

    private val _passwordChangeResult = MutableStateFlow<String?>(null)
    val passwordChangeResult: StateFlow<String?> = _passwordChangeResult.asStateFlow()

    private val _profileSaved = MutableStateFlow(false)
    val profileSaved: StateFlow<Boolean> = _profileSaved.asStateFlow()

    fun updateProfile(ownerName: String, shopName: String, phone: String) {
        viewModelScope.launch {
            auth.updateProfile(ownerName, shopName, phone)
            _profileSaved.value = true
        }
    }

    fun changePassword(current: String, newPassword: String, confirm: String) {
        viewModelScope.launch {
            when {
                newPassword != confirm    -> { _passwordChangeResult.value = "New passwords do not match"; return@launch }
                newPassword.length < 6   -> { _passwordChangeResult.value = "Password must be at least 6 characters"; return@launch }
                current.isBlank()        -> { _passwordChangeResult.value = "Enter your current password"; return@launch }
            }
            val result = auth.changePassword(current, newPassword)
            _passwordChangeResult.value = result.fold(
                onSuccess = { "Password changed successfully" },
                onFailure = { it.message ?: "Failed to change password" }
            )
        }
    }

    fun clearPasswordResult() { _passwordChangeResult.value = null }
    fun clearProfileSaved()   { _profileSaved.value = false }
}
