package com.example.appup.data.model

enum class OverallHealthStatus {
    EXCELLENT,
    GOOD,
    ATTENTION_NEEDED,
    CRITICAL
}

enum class IssueSeverity {
    INFO,
    WARNING,
    CRITICAL
}

data class HealthIssue(
    val title: String,
    val description: String,
    val severity: IssueSeverity,
    val actionSuggestion: String
)

data class SystemHealthAudit(
    val overallScore: Int,
    val status: OverallHealthStatus,
    val totalAppsScanned: Int,
    val userAppsCount: Int,
    val highRiskAppsCount: Int,
    val moderateRiskAppsCount: Int,
    val safeAppsCount: Int,
    val issues: List<HealthIssue>,
    val quickRecommendations: List<String>
)
