package com.example.khataapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.khataapp.KhataApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Idle    : AuthUiState
    data object Loading : AuthUiState
    data object Success : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = (application as KhataApplication).authRepository

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val isLoggedIn = auth.isLoggedIn

    fun hasAnyUser(onResult: (Boolean) -> Unit) {
        viewModelScope.launch { onResult(auth.hasAnyUser()) }
    }

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter username and password")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = auth.login(username, password)
            _uiState.value = result.fold(
                onSuccess = { AuthUiState.Success },
                onFailure = { AuthUiState.Error(it.message ?: "Login failed") }
            )
        }
    }

    fun register(
        username: String,
        password: String,
        confirmPassword: String,
        shopName: String,
        ownerName: String,
        phone: String
    ) {
        when {
            username.isBlank()        -> { _uiState.value = AuthUiState.Error("Username is required"); return }
            username.length < 3       -> { _uiState.value = AuthUiState.Error("Username must be at least 3 characters"); return }
            password.length < 6       -> { _uiState.value = AuthUiState.Error("Password must be at least 6 characters"); return }
            password != confirmPassword -> { _uiState.value = AuthUiState.Error("Passwords do not match"); return }
            shopName.isBlank()        -> { _uiState.value = AuthUiState.Error("Shop name is required"); return }
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = auth.register(username, password, shopName, ownerName, phone)
            _uiState.value = result.fold(
                onSuccess = { AuthUiState.Success },
                onFailure = { AuthUiState.Error(it.message ?: "Registration failed") }
            )
        }
    }

    fun getAuthNavigation(onResult: (loggedIn: Boolean, hasUsers: Boolean) -> Unit) {
        viewModelScope.launch {
            val loggedIn = auth.isLoggedIn.first()
            val hasUsers = auth.hasAnyUser()
            onResult(loggedIn, hasUsers)
        }
    }

    fun resetState() { _uiState.value = AuthUiState.Idle }
}
