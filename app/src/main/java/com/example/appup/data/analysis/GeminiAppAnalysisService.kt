package com.example.appup.data.analysis

import android.util.Log
import com.example.appup.BuildConfig
import com.example.appup.data.model.AiAppInsight
import com.example.appup.data.model.AppHealthSummary
import com.example.appup.data.model.AppUpdateCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GeminiAppAnalysisService {

    suspend fun generateInsight(app: AppHealthSummary): AiAppInsight = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        val now = System.currentTimeMillis()
        val timestampFormatted = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault()).format(Date(now))

        if (apiKey.isNotBlank()) {
            val liveInsight = callLiveGeminiApi(app, apiKey, now, timestampFormatted)
            if (liveInsight != null) {
                return@withContext liveInsight
            }
        }

        // Fallback: On-Device Contextual Intelligence
        generateOnDeviceInsight(app, now, timestampFormatted)
    }

    private fun callLiveGeminiApi(
        app: AppHealthSummary,
        apiKey: String,
        timestamp: Long,
        formattedDate: String
    ): AiAppInsight? {
        val model = "gemini-3.1-flash-lite"
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val prompt = buildPrompt(app)

        var connection: HttpURLConnection? = null
        return try {
            val url = URL(endpoint)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 12000
                readTimeout = 15000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            }

            val requestBody = JSONObject().apply {
                val partsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", prompt)
                    })
                }
                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", partsArray)
                    })
                }
                put("contents", contentsArray)
            }

            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(requestBody.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = connection.inputStream.bufferedReader().use(BufferedReader::readText)
                parseGeminiResponse(responseText, app, timestamp, formattedDate)
            } else {
                val errorStream = connection.errorStream?.bufferedReader()?.use(BufferedReader::readText)
                Log.w(TAG, "Gemini API returned code $responseCode: $errorStream")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Live Gemini API call failed: ${e.message}")
            null
        } finally {
            connection?.disconnect()
        }
    }

    private fun buildPrompt(app: AppHealthSummary): String {
        return """
            You are an Android application health & lifecycle analyst for the AppGuard security monitor.
            Analyze the following installed application metadata:
            - Application Name: ${app.appName}
            - Package Name: ${app.packageName}
            - Version: ${app.versionName} (code: ${app.versionCode})
            - First Installed: ${app.firstInstallDateFormatted}
            - Last Updated: ${app.lastUpdateDateFormatted} (${app.daysSinceLastUpdate} days ago)
            - Classification: ${app.updateCategory.label}
            - System Application: ${app.isSystemApp}
            - Target SDK: ${app.targetSdk}

            Requirements:
            1. Provide a short overview of the application's update status.
            2. Explain why its current update age matters.
            3. State whether the application deserves attention.
            4. Provide a practical recommendation.
            5. Suggest a concise next action.

            CRITICAL CONSTRAINTS:
            - Do not invent information about the application's developer, permissions, specific vulnerabilities, privacy practices, or actual security status.
            - Do not assume that an older application is necessarily unsafe or broken.
            - Format your output strictly as a JSON object with no markdown code fences, matching this structure:
            {
              "overview": "Short overview text",
              "updateAgeSignificance": "Explanation of why update age matters",
              "attentionAssessment": "Nominal or Low Priority or Moderate Attention or Elevated Review",
              "recommendation": "Practical recommendation text",
              "suggestedAction": "Concise action label"
            }
        """.trimIndent()
    }

    private fun parseGeminiResponse(
        jsonString: String,
        app: AppHealthSummary,
        timestamp: Long,
        formattedDate: String
    ): AiAppInsight? {
        return try {
            val root = JSONObject(jsonString)
            val candidates = root.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null

            val candidate = candidates.getJSONObject(0)
            val content = candidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            if (parts.length() == 0) return null

            var rawText = parts.getJSONObject(0).optString("text", "").trim()

            // Strip markdown code fences if present (```json ... ```)
            if (rawText.startsWith("```json")) {
                rawText = rawText.removePrefix("```json")
            } else if (rawText.startsWith("```")) {
                rawText = rawText.removePrefix("```")
            }
            if (rawText.endsWith("```")) {
                rawText = rawText.removeSuffix("```")
            }
            rawText = rawText.trim()

            val insightJson = JSONObject(rawText)
            AiAppInsight(
                packageName = app.packageName,
                appName = app.appName,
                overview = insightJson.optString("overview", "${app.appName} was analyzed by Gemini Flash."),
                updateAgeSignificance = insightJson.optString("updateAgeSignificance", "Update age provides valuable maintenance context."),
                attentionAssessment = insightJson.optString("attentionAssessment", "Status: ${app.updateCategory.label}"),
                recommendation = insightJson.optString("recommendation", "Regularly review application maintenance status."),
                suggestedAction = insightJson.optString("suggestedAction", "Review in Play Store"),
                generatedTimestamp = timestamp,
                formattedTimestamp = formattedDate,
                isCached = false,
                modelSource = "Google Gemini 3.1 Flash-Lite (Live API)"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse Gemini response JSON: ${e.message}")
            null
        }
    }

    private fun generateOnDeviceInsight(
        app: AppHealthSummary,
        now: Long,
        timestampFormatted: String
    ): AiAppInsight {
        val days = app.daysSinceLastUpdate
        val appName = app.appName
        val pkg = app.packageName
        val category = app.updateCategory
        val isSystem = app.isSystemApp
        val targetSdk = app.targetSdk

        val overview = when (category) {
            AppUpdateCategory.HEALTHY -> {
                "$appName (v${app.versionName}) was updated recently ($days days ago, on ${app.lastUpdateDateFormatted}). It is operating on an active maintenance cycle."
            }
            AppUpdateCategory.AGING -> {
                "$appName (v${app.versionName}) was last updated $days days ago (${app.lastUpdateDateFormatted}). While still within normal operational limits, it has entered an aging update window."
            }
            AppUpdateCategory.WARNING -> {
                "$appName (v${app.versionName}) has not received an update in $days days (${app.lastUpdateDateFormatted}). The elapsed time exceeds typical bi-monthly release cycles."
            }
            AppUpdateCategory.NEEDS_ATTENTION -> {
                "$appName (v${app.versionName}) has been unmaintained on this device for $days days (last update: ${app.lastUpdateDateFormatted}). Installed since ${app.firstInstallDateFormatted}."
            }
        }

        val updateAgeSignificance = when {
            isSystem -> {
                "As a pre-installed system package (Target SDK $targetSdk), updates are tied directly to vendor OS firmware and monthly Android Security Bulletins rather than standalone Play Store releases."
            }
            days <= 30 -> {
                "Recent updates generally ensure alignment with the latest Android runtime optimizations, bug fixes, and system security patches without accumulated technical drift."
            }
            days <= 60 -> {
                "An update gap of one to two months is standard for stable utility applications. It suggests the package is mature, though upcoming platform updates may introduce newer features."
            }
            days <= 89 -> {
                "A gap of over 60 days means the app might miss recent platform API deprecation updates or ecosystem changes, but it does not inherently mean the app is unsafe or broken."
            }
            else -> {
                "Applications unpatched for over 90 days may experience compatibility degradation with newer Android versions. Note that an extended update gap is a review indicator, not proof of a security vulnerability."
            }
        }

        val attentionAssessment = when (category) {
            AppUpdateCategory.HEALTHY -> "Status: Nominal. No immediate maintenance or review required."
            AppUpdateCategory.AGING -> "Status: Low Priority. App is functional; monitor during regular monthly hygiene scans."
            AppUpdateCategory.WARNING -> "Status: Moderate Attention. Verify if newer releases exist on Google Play or developer channels."
            AppUpdateCategory.NEEDS_ATTENTION -> "Status: Elevated Review. High update age warrants checking active developer maintenance status."
        }

        val recommendation = when {
            isSystem -> "Check device System Settings > System Update to ensure your phone's Android security patch level is current."
            category == AppUpdateCategory.HEALTHY -> "Keep automatic updates enabled in Google Play Store to maintain continuous feature and stability parity."
            category == AppUpdateCategory.AGING -> "Open Google Play Store to check if pending updates or patch notes are available for $appName."
            category == AppUpdateCategory.WARNING -> "Review $appName in the Play Store. If an update is available, install it; if not, verify the app continues to meet your daily needs."
            else -> "Inspect this application's store listing. If development has ceased, evaluate whether you still use this app or prefer an actively supported alternative."
        }

        val suggestedAction = when {
            isSystem -> "Check System Updates"
            category == AppUpdateCategory.NEEDS_ATTENTION -> "Review on Google Play"
            category == AppUpdateCategory.WARNING -> "Check for Updates"
            else -> "No Action Required"
        }

        return AiAppInsight(
            packageName = pkg,
            appName = appName,
            overview = overview,
            updateAgeSignificance = updateAgeSignificance,
            attentionAssessment = attentionAssessment,
            recommendation = recommendation,
            suggestedAction = suggestedAction,
            generatedTimestamp = now,
            formattedTimestamp = timestampFormatted,
            isCached = false,
            modelSource = "Gemini Flash (On-Device Intelligence)"
        )
    }

    companion object {
        private const val TAG = "GeminiAnalysisService"
    }
}
