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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.khataapp.ui.theme.CreditGreen
import com.example.khataapp.ui.theme.DebitRed
import com.example.khataapp.viewmodel.AnalyticsViewModel
import com.example.khataapp.viewmodel.MonthlyStat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AnalyticsViewModel = viewModel()
) {
    val monthlyStats by viewModel.monthlyStats.collectAsState()
    val summary      by viewModel.summary.collectAsState()
    val topCustomers by viewModel.topCustomers.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics", fontWeight = FontWeight.Bold) },
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
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Key insights
            item {
                SectionTitle("Key Insights")
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    InsightCard("Total Given",    "₹%.0f".format(summary.totalCredit),   CreditGreen, Color(0xFFE8F5E9), "💸", Modifier.weight(1f))
                    InsightCard("Total Received", "₹%.0f".format(summary.totalDebit),    DebitRed,    Color(0xFFFFEBEE), "💰", Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    InsightCard("Transactions", summary.totalTxCount.toString(), MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer, "📋", Modifier.weight(1f))
                    InsightCard("Avg per Tx",   "₹%.0f".format(summary.avgTxAmount), MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondaryContainer, "📊", Modifier.weight(1f))
                }
            }

            // Highlights
            if (summary.mostActiveCustomer.isNotEmpty() || summary.peakMonth.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("🏆  Highlights", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            if (summary.mostActiveCustomer.isNotEmpty()) {
                                HighlightRow("Most Active Customer", summary.mostActiveCustomer)
                            }
                            if (summary.peakMonth.isNotEmpty()) {
                                HighlightRow("Peak Month", summary.peakMonth)
                            }
                            HighlightRow("Net Balance", if (summary.netBalance >= 0)
                                "₹%.0f (you'll receive)".format(summary.netBalance)
                                else "₹%.0f (you owe)".format(-summary.netBalance))
                        }
                    }
                }
            }

            // Monthly chart
            if (monthlyStats.isNotEmpty()) {
                item {
                    SectionTitle("Last 6 Months — Credit vs Debit")
                    Spacer(Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            val maxVal = monthlyStats.maxOf { maxOf(it.credit, it.debit) }.coerceAtLeast(1.0)
                            monthlyStats.forEach { stat ->
                                MonthBarRow(stat = stat, maxVal = maxVal)
                            }
                        }
                    }
                }
            }

            // Top customers by transactions
            if (topCustomers.isNotEmpty()) {
                item {
                    SectionTitle("Most Active Customers")
                    Spacer(Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            topCustomers.forEachIndexed { idx, customer ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${idx + 1}", style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.width(24.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(customer.name, style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold)
                                        Text("${customer.totalTransactions} transactions",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text("₹%.0f".format(kotlin.math.abs(customer.balance)),
                                        fontWeight = FontWeight.Bold,
                                        color = if (customer.balance >= 0) CreditGreen else DebitRed)
                                }
                                if (idx < topCustomers.lastIndex) {
                                    Box(modifier = Modifier.fillMaxWidth().height(0.5.dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant))
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun MonthBarRow(stat: MonthlyStat, maxVal: Double) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stat.displayName, style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.width(56.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                // Credit bar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .height(10.dp)
                            .fillMaxWidth((stat.credit / maxVal).toFloat().coerceIn(0.01f, 1f))
                            .clip(RoundedCornerShape(5.dp))
                            .background(CreditGreen)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("₹%.0f".format(stat.credit), fontSize = 10.sp, color = CreditGreen,
                        fontWeight = FontWeight.Medium)
                }
                // Debit bar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .height(10.dp)
                            .fillMaxWidth((stat.debit / maxVal).toFloat().coerceIn(0.01f, 1f))
                            .clip(RoundedCornerShape(5.dp))
                            .background(DebitRed)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("₹%.0f".format(stat.debit), fontSize = 10.sp, color = DebitRed,
                        fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun HighlightRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun InsightCard(
    label: String, value: String, textColor: Color, bg: Color,
    emoji: String, modifier: Modifier
) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(0.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(emoji, fontSize = 22.sp)
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold, color = textColor)
            Text(label, style = MaterialTheme.typography.bodySmall,
                color = textColor.copy(alpha = 0.75f))
        }
    }
}
