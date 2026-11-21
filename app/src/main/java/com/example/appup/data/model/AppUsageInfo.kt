package com.example.appup.data.model

import android.graphics.drawable.Drawable

enum class AppUsageLevel(val label: String) {
    HIGH("High Usage"),
    MODERATE("Moderate Usage"),
    LOW("Low Usage"),
    DORMANT("Dormant"),
    UNUSED("Never Used")
}

data class RawUsageStat(
    val packageName: String,
    val totalTimeInForeground: Long = 0L,
    val lastTimeUsed: Long = 0L,
    val firstTimeStamp: Long = 0L,
    val lastTimeStamp: Long = 0L
)

enum class UsageRangeType(val label: String, val days: Int) {
    DAILY("Past 24 Hours", 1),
    WEEKLY("Past 7 Days", 7),
    MONTHLY("Past 30 Days", 30),
    YEARLY("Past Year", 365)
}

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val totalTimeInForeground: Long = 0L,
    val lastTimeUsed: Long = 0L,
    val lastTimeUsedFormatted: String = "",
    val foregroundTimeFormatted: String = "",
    val isDormant: Boolean = false,
    val daysSinceLastUsed: Long = 0L,
    val usageLevel: AppUsageLevel = AppUsageLevel.MODERATE,
    val isSystemApp: Boolean = false,
    val icon: Drawable? = null
)
