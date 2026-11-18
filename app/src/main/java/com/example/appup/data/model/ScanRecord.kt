package com.example.appup.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ScanRecord(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val formattedDateTime: String = formatDateTime(System.currentTimeMillis()),
    val dateHeader: String = formatDateHeader(System.currentTimeMillis()),
    val totalApps: Int,
    val healthyCount: Int,
    val agingCount: Int,
    val warningCount: Int,
    val attentionCount: Int,
    val healthScore: Int
) {
    companion object {
        fun formatDateTime(timeMillis: Long): String {
            return SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(timeMillis))
        }

        fun formatDateHeader(timeMillis: Long): String {
            return SimpleDateFormat("MMMM d", Locale.getDefault()).format(Date(timeMillis)).uppercase(Locale.getDefault())
        }
    }
}
