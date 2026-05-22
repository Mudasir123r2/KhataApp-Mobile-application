package com.example.khataapp.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.khataapp.KhataApplication
import com.example.khataapp.data.local.entity.Customer
import com.example.khataapp.data.local.entity.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class DateFilterRange { ALL, THIS_WEEK, THIS_MONTH, LAST_3_MONTHS }

class TransactionViewModel(
    application: Application,
    val customerId: Int
) : AndroidViewModel(application) {

    private val repository = (application as KhataApplication).repository

    val dateFilter = MutableStateFlow(DateFilterRange.ALL)

    val customer: StateFlow<Customer?> =
        repository.getCustomerById(customerId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val allTransactions: StateFlow<List<Transaction>> =
        repository.getTransactionsByCustomer(customerId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val transactions: StateFlow<List<Transaction>> =
        combine(allTransactions, dateFilter) { list, filter ->
            val now = System.currentTimeMillis()
            val cutoff = when (filter) {
                DateFilterRange.ALL           -> 0L
                DateFilterRange.THIS_WEEK     -> now - 7L * 24 * 60 * 60 * 1000
                DateFilterRange.THIS_MONTH    -> now - 30L * 24 * 60 * 60 * 1000
                DateFilterRange.LAST_3_MONTHS -> now - 90L * 24 * 60 * 60 * 1000
            }
            if (cutoff == 0L) list else list.filter { it.date >= cutoff }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalBalance: StateFlow<Double> =
        allTransactions
            .map { list -> list.sumOf { t -> if (t.type == "credit") t.amount else -t.amount } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0.0
            )

    fun addTransaction(amount: Double, type: String, note: String, dueDate: Long? = null) {
        viewModelScope.launch {
            repository.insertTransaction(
                Transaction(
                    customerId = customerId,
                    amount = amount,
                    type = type,
                    note = note,
                    date = System.currentTimeMillis(),
                    dueDate = dueDate
                )
            )
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch { repository.deleteTransaction(transaction) }
    }

    fun sendWhatsAppReminder(context: Context, shopName: String) {
        val c = customer.value ?: return
        val balance = totalBalance.value
        if (balance <= 0) return

        val msg = "Hello ${c.name},\n\nThis is a reminder from *$shopName*.\n" +
                  "You have an outstanding balance of *₹%.2f*.\n\n".format(balance) +
                  "Please clear your dues at the earliest convenience.\n\nThank you!"

        val fullPhone = resolvePhone(c.phone)

        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("whatsapp://send?phone=$fullPhone&text=${Uri.encode(msg)}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://wa.me/$fullPhone?text=${Uri.encode(msg)}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e2: Exception) {
                // Last resort: dial
                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${c.phone}")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
        }
    }

    fun shareStatement(context: Context, shopName: String) {
        val c = customer.value ?: return
        val txList = allTransactions.value
        val fmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val sb = StringBuilder()
        sb.appendLine("=== Account Statement ===")
        sb.appendLine("Shop: $shopName")
        sb.appendLine("Customer: ${c.name}  |  Phone: ${c.phone}")
        sb.appendLine("Generated: ${fmt.format(Date())}")
        sb.appendLine("=".repeat(28))
        sb.appendLine()
        txList.forEach { tx ->
            val sign = if (tx.type == "credit") "CR" else "DR"
            sb.appendLine("[${fmt.format(Date(tx.date))}]  $sign  ₹%.2f  ${tx.note.ifBlank { "-" }}".format(tx.amount))
        }
        sb.appendLine()
        sb.appendLine("Net Balance: ₹%.2f".format(totalBalance.value))
        sb.appendLine(
            when {
                totalBalance.value > 0 -> "(Customer owes this amount)"
                totalBalance.value < 0 -> "(You owe this amount)"
                else                   -> "(Settled)"
            }
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            putExtra(Intent.EXTRA_SUBJECT, "Account Statement – ${c.name}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(
            Intent.createChooser(intent, "Share Statement").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    companion object {
        fun resolvePhone(raw: String): String {
            val cleaned    = raw.trim()
            val digitsOnly = cleaned.replace(Regex("[^0-9]"), "")
            return when {
                cleaned.startsWith("+")                                 -> digitsOnly                       // +923XXXXXXXXX → 923XXXXXXXXX
                digitsOnly.startsWith("92") && digitsOnly.length >= 12 -> digitsOnly                       // already 923XXXXXXXXX
                digitsOnly.startsWith("0")                             -> "92${digitsOnly.substring(1)}"   // 03XXXXXXXXX  → 923XXXXXXXXX
                else                                                    -> "92$digitsOnly"                  // 3XXXXXXXXX   → 923XXXXXXXXX
            }
        }

        fun factory(customerId: Int): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    val application =
                        extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                    return TransactionViewModel(application, customerId) as T
                }
            }
    }
}
