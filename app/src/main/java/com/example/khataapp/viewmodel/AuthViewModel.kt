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

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data object Success : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = (application as KhataApplication).authRepository

    val isSetupDone: StateFlow<Boolean> = auth.isSetupDone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val shopProfile: StateFlow<ShopProfile> = auth.shopProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ShopProfile())

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun setup(ownerName: String, shopName: String, phone: String, pin: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            if (ownerName.isBlank() || shopName.isBlank() || phone.isBlank() || pin.length < 4) {
                _uiState.value = AuthUiState.Error("Please fill all fields with a 4-digit PIN")
                return@launch
            }
            auth.saveProfile(ownerName.trim(), shopName.trim(), phone.trim(), pin)
            _uiState.value = AuthUiState.Success
        }
    }

    fun verifyPin(pin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = auth.verifyPin(pin).first()
            onResult(result)
        }
    }

    fun updateProfile(ownerName: String, shopName: String, phone: String) {
        viewModelScope.launch {
            auth.updateProfile(ownerName.trim(), shopName.trim(), phone.trim())
            _uiState.value = AuthUiState.Success
        }
    }

    fun changePin(currentPin: String, newPin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val valid = auth.verifyPin(currentPin).first()
            if (!valid) { onResult(false); return@launch }
            auth.changePin(newPin)
            onResult(true)
        }
    }

    fun resetState() { _uiState.value = AuthUiState.Idle }
}
