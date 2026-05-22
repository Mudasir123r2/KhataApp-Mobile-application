package com.example.khataapp.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.khataapp.data.local.entity.Transaction
import com.example.khataapp.ui.theme.CreditGreen
import com.example.khataapp.ui.theme.DebitRed
import com.example.khataapp.viewmodel.DateFilterRange
import com.example.khataapp.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerStatementScreen(
    customerId: Int,
    onNavigateBack: () -> Unit
) {
    val viewModel: TransactionViewModel = viewModel(
        factory = TransactionViewModel.factory(customerId)
    )
    val context = LocalContext.current

    val customer     by viewModel.customer.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val balance      by viewModel.totalBalance.collectAsState()
    val dateFilter   by viewModel.dateFilter.collectAsState()
    val credit = transactions.filter { it.type == "credit" }.sumOf { it.amount }
    val debit  = transactions.filter { it.type == "debit"  }.sumOf { it.amount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statement – ${customer?.name ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.shareStatement(context, "My Shop")
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Net Balance", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₹%.2f".format(kotlin.math.abs(balance)),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (balance >= 0) CreditGreen else DebitRed
                        )
                        Text(
                            text = when {
                                balance > 0 -> "Customer will pay you"
                                balance < 0 -> "You owe this customer"
                                else        -> "Fully settled"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Given", style = MaterialTheme.typography.labelSmall)
                                Text("₹%.0f".format(credit), fontWeight = FontWeight.Bold,
                                    color = CreditGreen)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Received", style = MaterialTheme.typography.labelSmall)
                                Text("₹%.0f".format(debit), fontWeight = FontWeight.Bold,
                                    color = DebitRed)
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DateFilterRange.entries.forEach { range ->
                        FilterChip(
                            selected = dateFilter == range,
                            onClick = { viewModel.dateFilter.value = range },
                            label = {
                                Text(when (range) {
                                    DateFilterRange.ALL          -> "All"
                                    DateFilterRange.THIS_WEEK    -> "Week"
                                    DateFilterRange.THIS_MONTH   -> "Month"
                                    DateFilterRange.LAST_3_MONTHS -> "3 Months"
                                }, fontSize = 12.sp)
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
            }

            if (transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No transactions in this period",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(transactions, key = { it.id }) { tx ->
                    StatementRow(tx)
                }
            }
        }
    }
}

@Composable
private fun StatementRow(tx: Transaction) {
    val isCredit = tx.type == "credit"
    val fmt = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp)
                .background(
                    if (isCredit) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(if (isCredit) "CR" else "DR", fontSize = 11.sp,
                color = if (isCredit) CreditGreen else DebitRed,
                fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(tx.note.ifBlank { if (isCredit) "Credit" else "Debit" },
                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(fmt.format(Date(tx.date)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            tx.dueDate?.let {
                Text("Due: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFC62828))
            }
        }
        Text(
            text = "${if (isCredit) "+" else "-"}₹%.2f".format(tx.amount),
            style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
            color = if (isCredit) CreditGreen else DebitRed
        )
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}
