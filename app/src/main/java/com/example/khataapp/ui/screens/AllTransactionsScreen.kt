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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.khataapp.data.local.entity.TransactionWithCustomer
import com.example.khataapp.ui.theme.CreditGreen
import com.example.khataapp.ui.theme.DebitRed
import com.example.khataapp.viewmodel.AllTransactionsViewModel
import com.example.khataapp.viewmodel.TxTypeFilter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTransactionsScreen(
    onNavigateBack: () -> Unit,
    onCustomerClick: (Int) -> Unit,
    viewModel: AllTransactionsViewModel = viewModel()
) {
    val grouped      by viewModel.grouped.collectAsState()
    val searchQuery  by viewModel.searchQuery.collectAsState()
    val typeFilter   by viewModel.typeFilter.collectAsState()
    val totalCredit  by viewModel.summaryCredit.collectAsState()
    val totalDebit   by viewModel.summaryDebit.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Transactions", fontWeight = FontWeight.Bold) },
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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search by customer or note…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TxTypeFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = typeFilter == filter,
                        onClick  = { viewModel.typeFilter.value = filter },
                        label    = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Given", style = MaterialTheme.typography.labelSmall)
                        Text("₹%.0f".format(totalCredit), fontWeight = FontWeight.Bold,
                            color = CreditGreen, style = MaterialTheme.typography.titleSmall)
                    }
                    Box(modifier = Modifier.width(1.dp).height(32.dp)
                        .background(MaterialTheme.colorScheme.outline))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Received", style = MaterialTheme.typography.labelSmall)
                        Text("₹%.0f".format(totalDebit), fontWeight = FontWeight.Bold,
                            color = DebitRed, style = MaterialTheme.typography.titleSmall)
                    }
                    Box(modifier = Modifier.width(1.dp).height(32.dp)
                        .background(MaterialTheme.colorScheme.outline))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Net", style = MaterialTheme.typography.labelSmall)
                        val net = totalCredit - totalDebit
                        Text("₹%.0f".format(net), fontWeight = FontWeight.Bold,
                            color = if (net >= 0) CreditGreen else DebitRed,
                            style = MaterialTheme.typography.titleSmall)
                    }
                }
            }

            if (grouped.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔍", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "No transactions yet"
                                   else "No results for \"$searchQuery\"",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    grouped.forEach { group ->
                        item(key = "header_${group.label}") {
                            Text(
                                text = group.label,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(group.transactions, key = { "tx_${it.id}" }) { tx ->
                            AllTxRow(tx = tx, onClick = { onCustomerClick(tx.customerId) })
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AllTxRow(tx: TransactionWithCustomer, onClick: () -> Unit) {
    val isCredit = tx.type == "credit"
    val timeFmt  = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape)
                .background(if (isCredit) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = tx.customerName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                fontWeight = FontWeight.Bold,
                color = if (isCredit) CreditGreen else DebitRed
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(tx.customerName, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold)
            Text(
                text = tx.note.ifBlank { if (isCredit) "Credit" else "Debit" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(timeFmt.format(Date(tx.date)), style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Text(
            text = "${if (isCredit) "+" else "-"}₹%.2f".format(tx.amount),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (isCredit) CreditGreen else DebitRed
        )
    }
}
