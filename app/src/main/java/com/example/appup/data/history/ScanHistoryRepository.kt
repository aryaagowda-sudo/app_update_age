package com.example.appup.data.history

import android.content.Context
import android.content.SharedPreferences
import com.example.appup.data.model.ScanRecord
import org.json.JSONArray
import org.json.JSONObject

class ScanHistoryRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getScanHistory(): List<ScanRecord> {
        val jsonString = prefs.getString(KEY_SCANS, null) ?: return emptyList()
        val records = mutableListOf<ScanRecord>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                records.add(
                    ScanRecord(
                        id = obj.optString("id"),
                        timestamp = obj.optLong("timestamp"),
                        formattedDateTime = obj.optString("formattedDateTime"),
                        dateHeader = obj.optString("dateHeader"),
                        totalApps = obj.optInt("totalApps"),
                        healthyCount = obj.optInt("healthyCount"),
                        agingCount = obj.optInt("agingCount"),
                        warningCount = obj.optInt("warningCount"),
                        attentionCount = obj.optInt("attentionCount"),
                        healthScore = obj.optInt("healthScore")
                    )
                )
            }
        } catch (_: Exception) {
        }
        return records.sortedByDescending { it.timestamp }
    }

    fun saveScanRecord(record: ScanRecord) {
        try {
            val currentList = getScanHistory().toMutableList()
            // Add new scan at top
            currentList.add(0, record)
            // Keep at most 50 history entries
            val trimmed = currentList.take(50)

            val jsonArray = JSONArray()
            for (item in trimmed) {
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("timestamp", item.timestamp)
                    put("formattedDateTime", item.formattedDateTime)
                    put("dateHeader", item.dateHeader)
                    put("totalApps", item.totalApps)
                    put("healthyCount", item.healthyCount)
                    put("agingCount", item.agingCount)
                    put("warningCount", item.warningCount)
                    put("attentionCount", item.attentionCount)
                    put("healthScore", item.healthScore)
                }
                jsonArray.put(obj)
            }

            prefs.edit().putString(KEY_SCANS, jsonArray.toString()).apply()
        } catch (_: Exception) {
        }
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_SCANS).apply()
    }

    companion object {
        private const val PREFS_NAME = "appguard_scan_history_prefs"
        private const val KEY_SCANS = "scan_history_records"
    }
}
