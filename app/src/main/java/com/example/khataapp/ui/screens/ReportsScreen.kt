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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.khataapp.ui.theme.CreditGreen
import com.example.khataapp.ui.theme.DebitRed
import com.example.khataapp.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onViewAnalytics: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val stats    by viewModel.dashboardStats.collectAsState()
    val debtors  by viewModel.topDebtors.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Business Reports", fontWeight = FontWeight.Bold) },
                actions = {
                    androidx.compose.material3.TextButton(onClick = onViewAnalytics) {
                        Text("Analytics →", style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Overview", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ReportSummaryCard("Total Credit Given", "₹%.2f".format(stats.totalCredit),
                        CreditGreen, Color(0xFFE8F5E9), "💸", Modifier.weight(1f))
                    ReportSummaryCard("Total Debit", "₹%.2f".format(stats.totalDebit),
                        DebitRed, Color(0xFFFFEBEE), "💰", Modifier.weight(1f))
                }
            }
            item {
                ReportSummaryCard("Net Balance", "₹%.2f".format(stats.netBalance),
                    if (stats.netBalance >= 0) CreditGreen else DebitRed,
                    if (stats.netBalance >= 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    if (stats.netBalance >= 0) "📈" else "📉", Modifier.fillMaxWidth())
            }
            item {
                CreditVsDebitBar(stats.totalCredit, stats.totalDebit)
            }
            item {
                Text("Customers Who Owe You", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            if (debtors.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🎉", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("All balances cleared!", style = MaterialTheme.typography.bodyMedium,
                                    color = CreditGreen, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            } else {
                itemsIndexed(debtors) { index, customer ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(1.dp)) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(36.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center) {
                                Text("${index + 1}", style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(customer.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(customer.phone, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("₹%.2f".format(customer.balance),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold, color = CreditGreen)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportSummaryCard(
    label: String, value: String, textColor: Color,
    bg: Color, emoji: String, modifier: Modifier
) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(0.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold, color = textColor)
            Text(label, style = MaterialTheme.typography.bodySmall,
                color = textColor.copy(alpha = 0.75f))
        }
    }
}

@Composable
private fun CreditVsDebitBar(credit: Double, debit: Double) {
    val total = credit + debit
    val creditFraction = if (total > 0) (credit / total).toFloat() else 0.5f

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Credit vs Debit", style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().height(20.dp).clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFFFEBEE))) {
                Box(modifier = Modifier.fillMaxWidth(creditFraction).height(20.dp)
                    .clip(RoundedCornerShape(10.dp)).background(CreditGreen))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(CreditGreen))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Credit (${(creditFraction * 100).toInt()}%)",
                        style = MaterialTheme.typography.labelSmall, color = CreditGreen)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(DebitRed))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Debit (${((1 - creditFraction) * 100).toInt()}%)",
                        style = MaterialTheme.typography.labelSmall, color = DebitRed)
                }
            }
        }
    }
}
