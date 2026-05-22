package com.example.khataapp.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.khataapp.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val profile by authViewModel.shopProfile.collectAsState()

    var enteredPin by remember { mutableStateOf("") }
    var shake      by remember { mutableStateOf(false) }
    var errorText  by remember { mutableStateOf("") }

    LaunchedEffect(shake) {
        if (shake) { delay(600); shake = false; enteredPin = "" }
    }

    fun onKey(key: String) {
        if (enteredPin.length >= 4) return
        val next = enteredPin + key
        enteredPin = next
        if (next.length == 4) {
            authViewModel.verifyPin(next) { ok ->
                if (ok) onLoginSuccess()
                else { shake = true; errorText = "Incorrect PIN. Try again." }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Spacer(modifier = Modifier.height(48.dp))
            Box(
                modifier = Modifier.size(80.dp).clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) { Text("🔐", fontSize = 36.sp) }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (profile.shopName.isNotEmpty()) profile.shopName else "Khata",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White, fontWeight = FontWeight.Bold
            )
            Text("Welcome back, ${profile.ownerName.ifEmpty { "User" }}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(40.dp))
            Text("Enter your PIN", style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f))
            Spacer(modifier = Modifier.height(20.dp))
            PinDots(count = enteredPin.length, hasError = shake)
            Spacer(modifier = Modifier.height(12.dp))
            if (errorText.isNotEmpty() && !shake) {
                Text(errorText, color = Color(0xFFFFCDD2), style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(32.dp))
            NumPad(onKey = ::onKey, onDelete = {
                if (enteredPin.isNotEmpty()) {
                    enteredPin = enteredPin.dropLast(1)
                    errorText = ""
                }
            })
        }
    }
}

@Composable
private fun PinDots(count: Int, hasError: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(4) { i ->
            val filled = i < count
            val color by animateColorAsState(
                if (hasError) Color(0xFFEF9A9A)
                else if (filled) Color.White
                else Color.White.copy(alpha = 0.3f),
                tween(200), label = "dot"
            )
            Box(
                modifier = Modifier.size(16.dp).clip(CircleShape).background(color)
            )
        }
    }
}

@Composable
private fun NumPad(onKey: (String) -> Unit, onDelete: () -> Unit) {
    val rows = listOf(listOf("1","2","3"), listOf("4","5","6"), listOf("7","8","9"), listOf("","0","⌫"))
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                row.forEach { key ->
                    if (key.isEmpty()) {
                        Box(modifier = Modifier.size(72.dp))
                    } else if (key == "⌫") {
                        Box(
                            modifier = Modifier.size(72.dp).clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .clickable { onDelete() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Delete",
                                tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    } else {
                        Box(
                            modifier = Modifier.size(72.dp).clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .clickable { onKey(key) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(key, fontSize = 24.sp, fontWeight = FontWeight.Medium, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
