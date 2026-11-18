package com.example.appup.data.model

import android.graphics.drawable.Drawable

enum class AppRiskLevel {
    SAFE,
    MODERATE,
    HIGH_RISK
}

enum class AppUpdateCategory(
    val label: String,
    val minDays: Long,
    val maxDays: Long
) {
    HEALTHY("Healthy", 0L, 30L),
    AGING("Aging", 31L, 60L),
    WARNING("Warning", 61L, 89L),
    NEEDS_ATTENTION("Needs Attention", 90L, Long.MAX_VALUE)
}

data class SensitivePermission(
    val permission: String,
    val friendlyName: String,
    val description: String,
    val category: String,
    val isGranted: Boolean
)

data class AppHealthSummary(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val versionCode: Long,
    val firstInstallTime: Long,
    val lastUpdateTime: Long,
    val firstInstallDateFormatted: String,
    val lastUpdateDateFormatted: String,
    val daysSinceLastUpdate: Long,
    val updateCategory: AppUpdateCategory = AppUpdateCategory.HEALTHY,
    val recommendation: String = "",
    val isSystemApp: Boolean,
    val targetSdk: Int,
    val isDebuggable: Boolean,
    val allPermissions: List<String>,
    val sensitivePermissions: List<SensitivePermission>,
    val healthScore: Int,
    val riskLevel: AppRiskLevel,
    val riskFactors: List<String>,
    val icon: Drawable? = null
) {
    val daysSinceLastUpdateText: String
        get() = when (daysSinceLastUpdate) {
            0L -> "Updated today"
            1L -> "Updated yesterday"
            else -> "Updated $daysSinceLastUpdate days ago"
        }
}
