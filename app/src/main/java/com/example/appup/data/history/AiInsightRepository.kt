package com.example.appup.data.history

import android.content.Context
import android.content.SharedPreferences
import com.example.appup.data.model.AiAppInsight
import org.json.JSONObject

class AiInsightRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun getInsight(packageName: String): AiAppInsight? {
        val rawJson = prefs.getString(KEY_PREFIX + packageName, null) ?: return null
        return try {
            val obj = JSONObject(rawJson)
            AiAppInsight(
                packageName = obj.optString("packageName", packageName),
                appName = obj.optString("appName", ""),
                overview = obj.optString("overview", ""),
                updateAgeSignificance = obj.optString("updateAgeSignificance", ""),
                attentionAssessment = obj.optString("attentionAssessment", ""),
                recommendation = obj.optString("recommendation", ""),
                suggestedAction = obj.optString("suggestedAction", ""),
                generatedTimestamp = obj.optLong("generatedTimestamp", 0L),
                formattedTimestamp = obj.optString("formattedTimestamp", ""),
                isCached = true,
                modelSource = obj.optString("modelSource", "Gemini Flash (Firebase AI Logic)")
            )
        } catch (_: Exception) {
            null
        }
    }

    fun saveInsight(insight: AiAppInsight) {
        try {
            val obj = JSONObject().apply {
                put("packageName", insight.packageName)
                put("appName", insight.appName)
                put("overview", insight.overview)
                put("updateAgeSignificance", insight.updateAgeSignificance)
                put("attentionAssessment", insight.attentionAssessment)
                put("recommendation", insight.recommendation)
                put("suggestedAction", insight.suggestedAction)
                put("generatedTimestamp", insight.generatedTimestamp)
                put("formattedTimestamp", insight.formattedTimestamp)
                put("modelSource", insight.modelSource)
            }
            prefs.edit().putString(KEY_PREFIX + insight.packageName, obj.toString()).apply()
        } catch (_: Exception) {
        }
    }

    fun hasInsight(packageName: String): Boolean {
        return prefs.contains(KEY_PREFIX + packageName)
    }

    fun clearInsight(packageName: String) {
        prefs.edit().remove(KEY_PREFIX + packageName).apply()
    }

    companion object {
        private const val PREFS_NAME = "appguard_ai_insights"
        private const val KEY_PREFIX = "insight_"
    }
}
