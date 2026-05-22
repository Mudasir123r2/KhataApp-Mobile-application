package com.example.khataapp.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.khataapp.data.local.entity.CustomerWithBalance
import com.example.khataapp.ui.theme.CreditGreen
import com.example.khataapp.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverdueScreen(
    onNavigateBack: () -> Unit,
    onCustomerClick: (Int) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val overdueList by viewModel.overdueCustomers.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Overdue Customers (${overdueList.size})") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        if (overdueList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("✅", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("All cleared!", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    Text("No outstanding balances", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(overdueList, key = { it.id }) { customer ->
                    OverdueCustomerCard(
                        customer = customer,
                        onClick = { onCustomerClick(customer.id) },
                        onCall = {
                            val uri = Uri.parse("tel:${customer.phone}")
                            context.startActivity(Intent(Intent.ACTION_DIAL, uri))
                        },
                        onWhatsApp = {
                            val fullPhone = com.example.khataapp.viewmodel.TransactionViewModel.resolvePhone(customer.phone)
                            val msg = "Hi ${customer.name}, you have an outstanding balance of ₹%.2f. Please clear it at the earliest.".format(customer.balance)
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("whatsapp://send?phone=$fullPhone&text=${Uri.encode(msg)}")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                })
                            } catch (e: Exception) {
                                context.startActivity(Intent(Intent.ACTION_VIEW,
                                    Uri.parse("https://wa.me/$fullPhone?text=${Uri.encode(msg)}")).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                })
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun OverdueCustomerCard(
    customer: CustomerWithBalance,
    onClick: () -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit
) {
    val daysSince = customer.lastTransactionDate?.let {
        TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - it)
    }
    val fmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = customer.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(customer.name, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold)
                Text(customer.phone, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                daysSince?.let {
                    Text(
                        text = if (it == 0L) "Last transaction: today"
                               else "Last transaction: $it day${if (it == 1L) "" else "s"} ago",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (it > 30) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹%.0f".format(customer.balance),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CreditGreen
                )
                Text("will pay", style = MaterialTheme.typography.labelSmall, color = CreditGreen)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                IconButton(onClick = onCall, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Call, contentDescription = "Call",
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onWhatsApp, modifier = Modifier.size(36.dp)) {
                    Text("W", color = Color(0xFF25D366), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
