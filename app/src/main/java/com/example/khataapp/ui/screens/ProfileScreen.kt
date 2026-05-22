package com.example.khataapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.khataapp.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onExport: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel()
) {
    val profile      by viewModel.shopProfile.collectAsState()
    val pinResult    by viewModel.pinChangeResult.collectAsState()
    val profileSaved by viewModel.profileSaved.collectAsState()

    var isEditing     by remember { mutableStateOf(false) }
    var ownerName     by remember(profile) { mutableStateOf(profile.ownerName) }
    var shopName      by remember(profile) { mutableStateOf(profile.shopName) }
    var phone         by remember(profile) { mutableStateOf(profile.phone) }
    var showPinDialog by remember { mutableStateOf(false) }
    val snackbarHost  = remember { SnackbarHostState() }

    LaunchedEffect(pinResult) {
        pinResult?.let { snackbarHost.showSnackbar(it); viewModel.clearPinResult() }
    }
    LaunchedEffect(profileSaved) {
        if (profileSaved) { isEditing = false; snackbarHost.showSnackbar("Profile updated"); viewModel.clearProfileSaved() }
    }

    if (showPinDialog) {
        ChangePinDialog(
            onConfirm = { cur, new, conf -> viewModel.changePin(cur, new, conf) },
            onDismiss = { showPinDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = { viewModel.updateProfile(ownerName, shopName, phone) }) {
                            Icon(Icons.Default.Check, "Save", tint = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost    = { SnackbarHost(snackbarHost) },
        containerColor  = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { AvatarHeader(profile.shopName, profile.ownerName) }

            item {
                SectionCard("Shop Details") {
                    if (isEditing) {
                        ProfileField("Shop Name", shopName, { shopName = it }, KeyboardCapitalization.Words)
                        Spacer(Modifier.height(12.dp))
                        ProfileField("Owner Name", ownerName, { ownerName = it }, KeyboardCapitalization.Words)
                        Spacer(Modifier.height(12.dp))
                        ProfileField("Phone", phone, { phone = it }, KeyboardCapitalization.None, KeyboardType.Phone)
                    } else {
                        ProfileDetail("🏪  Shop Name", profile.shopName.ifEmpty { "—" })
                        Spacer(Modifier.height(8.dp))
                        ProfileDetail("👤  Owner Name", profile.ownerName.ifEmpty { "—" })
                        Spacer(Modifier.height(8.dp))
                        ProfileDetail("📞  Phone", profile.phone.ifEmpty { "—" })
                    }
                }
            }

            item {
                SectionCard("Security") {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .clickable { showPinDialog = true }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("🔒  Change PIN", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("Update your 4-digit access PIN", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("›", fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item {
                SectionCard("Data") {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .clickable { onExport() }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("📤  Export Data", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("Download all transactions as CSV", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("›", fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item {
                SectionCard("About") {
                    ProfileDetail("📱  Version", "1.0.0")
                    Spacer(Modifier.height(8.dp))
                    ProfileDetail("🏢  App", "Khata — Credit Manager")
                }
            }

            item {
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Text("Logout", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun AvatarHeader(shopName: String, ownerName: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
    ) {
        Box(
            modifier = Modifier.size(88.dp).clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = (shopName.firstOrNull() ?: 'K').uppercaseChar().toString(),
                fontSize = 40.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(shopName.ifEmpty { "My Shop" }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(ownerName.ifEmpty { "Owner" }, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ProfileDetail(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ProfileField(
    label: String, value: String, onChange: (String) -> Unit,
    cap: KeyboardCapitalization, keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(value = value, onValueChange = onChange, label = { Text(label) },
        modifier = Modifier.fillMaxWidth(), singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = cap, keyboardType = keyboardType))
}

@Composable
private fun ChangePinDialog(onConfirm: (String, String, String) -> Unit, onDismiss: () -> Unit) {
    var current by remember { mutableStateOf("") }
    var newPin  by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change PIN") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PinInput("Current PIN", current) { if (it.length <= 4 && it.all(Char::isDigit)) current = it }
                PinInput("New PIN",     newPin)  { if (it.length <= 4 && it.all(Char::isDigit)) newPin = it }
                PinInput("Confirm PIN", confirm) { if (it.length <= 4 && it.all(Char::isDigit)) confirm = it }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(current, newPin, confirm); onDismiss() }) { Text("Change") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun PinInput(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onChange, label = { Text(label) },
        modifier = Modifier.fillMaxWidth(), singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword))
}
