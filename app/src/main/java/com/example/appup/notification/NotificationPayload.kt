package com.example.appup.notification

enum class AlertType(val rawValue: String) {
    HEALTH_ALERT("health_alert"),
    SECURITY_UPDATE("security_update"),
    UPDATE_REMINDER("update_reminder"),
    GENERAL("general");

    companion object {
        fun fromString(value: String?): AlertType {
            if (value.isNullOrBlank()) return GENERAL
            val lower = value.lowercase().trim()
            return when {
                lower.contains("health") || lower.contains("anomaly") || lower.contains("aging") -> HEALTH_ALERT
                lower.contains("security") || lower.contains("cve") || lower.contains("vulnerability") -> SECURITY_UPDATE
                lower.contains("update") || lower.contains("patch") || lower.contains("reminder") -> UPDATE_REMINDER
                else -> GENERAL
            }
        }
    }
}

data class AppGuardNotificationPayload(
    val title: String,
    val message: String,
    val channelId: String,
    val alertType: AlertType,
    val appName: String? = null,
    val packageName: String? = null,
    val healthScore: Int? = null,
    val priority: String = "high",
    val extras: Map<String, String> = emptyMap()
)

object NotificationPayloadParser {

    fun parse(
        data: Map<String, String>,
        notificationTitle: String? = null,
        notificationBody: String? = null
    ): AppGuardNotificationPayload {
        val rawType = data["type"] ?: data["alert_type"] ?: data["alertType"] ?: data["category"]
        val alertType = AlertType.fromString(rawType)

        val rawChannel = data["channel_id"] ?: data["channelId"] ?: data["channel"]
        val channelId = when {
            rawChannel == NotificationHelper.CHANNEL_HEALTH_ALERTS || rawChannel == NotificationHelper.CHANNEL_SECURITY_UPDATES -> rawChannel
            alertType == AlertType.SECURITY_UPDATE || alertType == AlertType.UPDATE_REMINDER -> NotificationHelper.CHANNEL_SECURITY_UPDATES
            alertType == AlertType.HEALTH_ALERT -> NotificationHelper.CHANNEL_HEALTH_ALERTS
            // Check title/body keyword heuristics if not specified
            listOfNotNull(notificationTitle, notificationBody, data["title"], data["body"], data["message"]).any { text ->
                val lower = text.lowercase()
                lower.contains("security") || lower.contains("update") || lower.contains("cve") || lower.contains("targetsdk")
            } -> NotificationHelper.CHANNEL_SECURITY_UPDATES
            else -> NotificationHelper.CHANNEL_HEALTH_ALERTS
        }

        val appName = data["app_name"] ?: data["appName"] ?: data["app"]
        val packageName = data["package_name"] ?: data["packageName"] ?: data["package"]
        val scoreStr = data["health_score"] ?: data["healthScore"] ?: data["score"]
        val healthScore = scoreStr?.toIntOrNull()
        val priority = data["priority"] ?: "high"

        val title = when {
            !notificationTitle.isNullOrBlank() -> notificationTitle
            !data["title"].isNullOrBlank() -> data["title"]!!
            !appName.isNullOrBlank() && alertType == AlertType.HEALTH_ALERT -> "App Health Alert: $appName"
            !appName.isNullOrBlank() && (alertType == AlertType.SECURITY_UPDATE || alertType == AlertType.UPDATE_REMINDER) -> "Security Update Needed: $appName"
            alertType == AlertType.SECURITY_UPDATE -> "AppGuard Security Alert"
            alertType == AlertType.UPDATE_REMINDER -> "AppGuard Update Reminder"
            else -> "AppGuard Health Alert"
        }

        val message = when {
            !notificationBody.isNullOrBlank() -> notificationBody
            !data["body"].isNullOrBlank() -> data["body"]!!
            !data["message"].isNullOrBlank() -> data["message"]!!
            !appName.isNullOrBlank() && healthScore != null -> "$appName health score has dropped to $healthScore/100. Review recommended actions."
            !appName.isNullOrBlank() && alertType == AlertType.SECURITY_UPDATE -> "A critical security update is available for $appName. Update immediately."
            !appName.isNullOrBlank() -> "Attention required for $appName. Open AppGuard to inspect."
            alertType == AlertType.HEALTH_ALERT -> "System health audit detected apps requiring maintenance."
            else -> "Important updates and health recommendations are available."
        }

        return AppGuardNotificationPayload(
            title = title,
            message = message,
            channelId = channelId,
            alertType = alertType,
            appName = appName,
            packageName = packageName,
            healthScore = healthScore,
            priority = priority,
            extras = data
        )
    }
}
