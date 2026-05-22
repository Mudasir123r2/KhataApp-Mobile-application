package com.example.khataapp.viewmodel

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.khataapp.KhataApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as KhataApplication).repository

    val isExporting = MutableStateFlow(false)
    val exportMessage = MutableStateFlow<String?>(null)

    fun exportCsv(context: Context) {
        viewModelScope.launch {
            isExporting.value = true
            try {
                val customers    = repo.allCustomers.first()
                val transactions = repo.allTransactions.first()
                val customerMap  = customers.associateBy { it.id }

                val fmt = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                val sb  = StringBuilder()
                sb.appendLine("Date,Customer Name,Phone,Type,Amount,Note,Due Date")
                transactions.forEach { tx ->
                    val c      = customerMap[tx.customerId]
                    val dueStr = tx.dueDate?.let { fmt.format(Date(it)) } ?: ""
                    sb.appendLine(
                        "${fmt.format(Date(tx.date))},\"${c?.name ?: ""}\",${c?.phone ?: ""},${tx.type}," +
                        "${tx.amount},\"${tx.note}\",\"$dueStr\""
                    )
                }

                val csvContent = sb.toString()
                val fileName   = "khata_export_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}.csv"

                withContext(Dispatchers.IO) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val values = ContentValues().apply {
                            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                            put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                            put(MediaStore.Downloads.IS_PENDING, 1)
                        }
                        val uri = context.contentResolver.insert(
                            MediaStore.Downloads.EXTERNAL_CONTENT_URI, values
                        )
                        uri?.let {
                            context.contentResolver.openOutputStream(it)?.use { stream ->
                                stream.write(csvContent.toByteArray())
                            }
                            values.clear()
                            values.put(MediaStore.Downloads.IS_PENDING, 0)
                            context.contentResolver.update(it, values, null, null)
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        val dir  = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        val file = File(dir, fileName)
                        file.writeText(csvContent)
                    }
                }

                exportMessage.value = "Saved to Downloads: $fileName"
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "✅ Saved to Downloads folder", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                exportMessage.value = "Export failed: ${e.message}"
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                isExporting.value = false
            }
        }
    }

    fun clearMessage() { exportMessage.value = null }
}
