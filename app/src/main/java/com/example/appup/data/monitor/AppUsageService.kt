package com.example.appup.data.monitor

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import com.example.appup.data.model.AppUsageInfo
import com.example.appup.data.model.AppUsageLevel
import com.example.appup.data.model.RawUsageStat
import com.example.appup.data.model.UsageRangeType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

typealias UsageStatsTracker = AppUsageService

class AppUsageService(private val context: Context? = null) {

    companion object {
        const val DORMANT_THRESHOLD_DAYS: Long = 30L
        private const val HIGH_USAGE_THRESHOLD_MS: Long = 2 * 60 * 60 * 1000L // 2 hours
        private const val MODERATE_USAGE_THRESHOLD_MS: Long = 30 * 60 * 1000L // 30 minutes
    }

    /**
     * Checks if PACKAGE_USAGE_STATS permission has been granted via AppOpsManager.
     */
    fun hasUsageStatsPermission(): Boolean {
        val ctx = context ?: return false
        val appOps = ctx.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                ctx.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                ctx.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Creates an Intent directing the user to the Usage Access Settings screen.
     */
    fun getUsageAccessSettingsIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            if (context != null) {
                data = Uri.parse("package:${context.packageName}")
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Queries Android UsageStatsManager directly for raw UsageStats.
     */
    fun queryUsageStats(
        startTime: Long,
        endTime: Long,
        intervalType: Int = UsageStatsManager.INTERVAL_BEST
    ): List<UsageStats> {
        val ctx = context ?: return emptyList()
        val usageStatsManager = ctx.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyList()

        return try {
            val stats = usageStatsManager.queryUsageStats(intervalType, startTime, endTime)
            stats ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Queries and aggregates usage stats into a list of AppUsageInfo.
     */
    fun getAppUsageStats(
        startTime: Long,
        endTime: Long,
        intervalType: Int = UsageStatsManager.INTERVAL_BEST,
        dormantThresholdDays: Long = DORMANT_THRESHOLD_DAYS,
        now: Long = System.currentTimeMillis(),
        includeSystemApps: Boolean = false
    ): List<AppUsageInfo> {
        val rawStats = queryUsageStats(startTime, endTime, intervalType).map {
            RawUsageStat(
                packageName = it.packageName,
                totalTimeInForeground = it.totalTimeInForeground,
                lastTimeUsed = it.lastTimeUsed,
                firstTimeStamp = it.firstTimeStamp,
                lastTimeStamp = it.lastTimeStamp
            )
        }

        val installedAppMetadata = getInstalledAppMetadata()
        return aggregateRawStats(
            rawStats = rawStats,
            installedAppMetadata = installedAppMetadata,
            now = now,
            dormantThresholdDays = dormantThresholdDays,
            includeSystemApps = includeSystemApps
        )
    }

    /**
     * Convenience query for past 24 hours.
     */
    fun getDailyUsage(
        now: Long = System.currentTimeMillis(),
        includeSystemApps: Boolean = false
    ): List<AppUsageInfo> {
        val startTime = now - TimeUnit.DAYS.toMillis(1)
        return getAppUsageStats(startTime, now, UsageStatsManager.INTERVAL_DAILY, now = now, includeSystemApps = includeSystemApps)
    }

    /**
     * Convenience query for past 7 days.
     */
    fun getWeeklyUsage(
        now: Long = System.currentTimeMillis(),
        includeSystemApps: Boolean = false
    ): List<AppUsageInfo> {
        val startTime = now - TimeUnit.DAYS.toMillis(7)
        return getAppUsageStats(startTime, now, UsageStatsManager.INTERVAL_WEEKLY, now = now, includeSystemApps = includeSystemApps)
    }

    /**
     * Convenience query for past 30 days.
     */
    fun getMonthlyUsage(
        now: Long = System.currentTimeMillis(),
        includeSystemApps: Boolean = false
    ): List<AppUsageInfo> {
        val startTime = now - TimeUnit.DAYS.toMillis(30)
        return getAppUsageStats(startTime, now, UsageStatsManager.INTERVAL_MONTHLY, now = now, includeSystemApps = includeSystemApps)
    }

    /**
     * Convenience query based on predefined UsageRangeType.
     */
    fun getUsageForRange(
        range: UsageRangeType,
        now: Long = System.currentTimeMillis(),
        includeSystemApps: Boolean = false
    ): List<AppUsageInfo> {
        val startTime = now - TimeUnit.DAYS.toMillis(range.days.toLong())
        val interval = when (range) {
            UsageRangeType.DAILY -> UsageStatsManager.INTERVAL_DAILY
            UsageRangeType.WEEKLY -> UsageStatsManager.INTERVAL_WEEKLY
            UsageRangeType.MONTHLY -> UsageStatsManager.INTERVAL_MONTHLY
            UsageRangeType.YEARLY -> UsageStatsManager.INTERVAL_YEARLY
        }
        return getAppUsageStats(startTime, now, interval, now = now, includeSystemApps = includeSystemApps)
    }

    /**
     * Aggregates a collection of interval/raw stats by package name.
     */
    fun aggregateRawStats(
        rawStats: List<RawUsageStat>,
        installedAppMetadata: Map<String, AppMetadata> = emptyMap(),
        now: Long = System.currentTimeMillis(),
        dormantThresholdDays: Long = DORMANT_THRESHOLD_DAYS,
        includeSystemApps: Boolean = true
    ): List<AppUsageInfo> {
        if (rawStats.isEmpty() && installedAppMetadata.isEmpty()) {
            return emptyList()
        }

        // Group stats by packageName
        val groupedStats = rawStats.groupBy { it.packageName }
        val allPackageNames = (groupedStats.keys + installedAppMetadata.keys).toSet()

        val results = mutableListOf<AppUsageInfo>()

        for (pkgName in allPackageNames) {
            val statsList = groupedStats[pkgName] ?: emptyList()
            val metadata = installedAppMetadata[pkgName]

            val isSystemApp = metadata?.isSystemApp ?: false
            if (!includeSystemApps && isSystemApp) {
                continue
            }

            val totalForegroundTime = calculateTotalForegroundTime(statsList)
            val latestLastTimeUsed = statsList.map { it.lastTimeUsed }.maxOrNull() ?: 0L

            val appName = metadata?.appName ?: pkgName
            val daysSinceLastUsed = calculateDaysSinceLastUsed(latestLastTimeUsed, now)
            val isDormant = isAppDormant(latestLastTimeUsed, totalForegroundTime, dormantThresholdDays, now)
            val lastUsedFormatted = formatLastTimeUsed(latestLastTimeUsed, now)
            val foregroundFormatted = formatDuration(totalForegroundTime)
            val usageLevel = classifyUsageLevel(totalForegroundTime, isDormant)

            results.add(
                AppUsageInfo(
                    packageName = pkgName,
                    appName = appName,
                    totalTimeInForeground = totalForegroundTime,
                    lastTimeUsed = latestLastTimeUsed,
                    lastTimeUsedFormatted = lastUsedFormatted,
                    foregroundTimeFormatted = foregroundFormatted,
                    isDormant = isDormant,
                    daysSinceLastUsed = daysSinceLastUsed,
                    usageLevel = usageLevel,
                    isSystemApp = isSystemApp,
                    icon = metadata?.icon
                )
            )
        }

        return sortAppsByUsage(results, mostUsedFirst = true)
    }

    /**
     * Calculates total foreground time across multiple stats (e.g. interval records).
     */
    fun calculateTotalForegroundTime(stats: List<RawUsageStat>): Long {
        if (stats.isEmpty()) return 0L
        return stats.fold(0L) { acc, stat ->
            acc + stat.totalTimeInForeground.coerceAtLeast(0L)
        }
    }

    /**
     * Calculates total foreground time from duration millis list.
     */
    fun calculateTotalForegroundTimeFromDurations(durations: List<Long>): Long {
        if (durations.isEmpty()) return 0L
        return durations.fold(0L) { acc, dur ->
            acc + dur.coerceAtLeast(0L)
        }
    }

    /**
     * Formats duration milliseconds into readable hours and minutes:
     * - "0m" for 0 or negative
     * - "< 1m" for < 60s
     * - "${hours}h ${minutes}m"
     * - "${hours}h"
     * - "${minutes}m"
     */
    fun formatDuration(durationMillis: Long): String {
        if (durationMillis <= 0L) return "0m"
        val totalSeconds = durationMillis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60

        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            minutes > 0 -> "${minutes}m"
            else -> "< 1m"
        }
    }

    /**
     * Calculates days elapsed since last usage.
     * Boundary check: future timestamps (clock skew) clamp to 0L.
     * Never used (<= 0L) returns -1L.
     */
    fun calculateDaysSinceLastUsed(lastTimeUsed: Long, now: Long = System.currentTimeMillis()): Long {
        if (lastTimeUsed <= 0L) return -1L
        if (lastTimeUsed >= now) return 0L
        val diff = now - lastTimeUsed
        return TimeUnit.MILLISECONDS.toDays(diff)
    }

    /**
     * Checks if an app is dormant (e.g. unused for > thresholdDays or never used).
     * Boundary check: future timestamps return false.
     */
    fun isAppDormant(
        lastTimeUsed: Long,
        totalForegroundTime: Long = 0L,
        thresholdDays: Long = DORMANT_THRESHOLD_DAYS,
        now: Long = System.currentTimeMillis()
    ): Boolean {
        if (lastTimeUsed <= 0L) return true
        if (lastTimeUsed > now) return false
        val diff = now - lastTimeUsed
        return diff >= TimeUnit.DAYS.toMillis(thresholdDays)
    }

    /**
     * Formats last used timestamp into user-friendly description.
     */
    fun formatLastTimeUsed(lastTimeUsed: Long, now: Long = System.currentTimeMillis()): String {
        if (lastTimeUsed <= 0L) return "Never used"
        if (lastTimeUsed > now) return "Just now"

        val diff = (now - lastTimeUsed).coerceAtLeast(0L)
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val mins = TimeUnit.MILLISECONDS.toMinutes(diff)
                "${mins}m ago"
            }
            diff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                "${hours}h ago"
            }
            days == 1L -> "Yesterday"
            days < 7L -> "${days} days ago"
            days < 30L -> "${days / 7} weeks ago"
            else -> {
                try {
                    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                    sdf.format(Date(lastTimeUsed))
                } catch (_: Exception) {
                    "${days} days ago"
                }
            }
        }
    }

    /**
     * Classifies usage level based on foreground time and dormant status.
     */
    fun classifyUsageLevel(totalForegroundTime: Long, isDormant: Boolean): AppUsageLevel {
        return when {
            isDormant && totalForegroundTime <= 0L -> AppUsageLevel.UNUSED
            isDormant -> AppUsageLevel.DORMANT
            totalForegroundTime >= HIGH_USAGE_THRESHOLD_MS -> AppUsageLevel.HIGH
            totalForegroundTime >= MODERATE_USAGE_THRESHOLD_MS -> AppUsageLevel.MODERATE
            else -> AppUsageLevel.LOW
        }
    }

    /**
     * Filters apps to only dormant ones, sorted by longest dormant first.
     */
    fun identifyDormantApps(
        apps: List<AppUsageInfo>,
        thresholdDays: Long = DORMANT_THRESHOLD_DAYS
    ): List<AppUsageInfo> {
        return apps.filter { it.isDormant }
            .sortedWith(
                compareByDescending<AppUsageInfo> { it.daysSinceLastUsed == -1L } // never used first
                    .thenByDescending { it.daysSinceLastUsed }
                    .thenBy { it.appName.lowercase(Locale.getDefault()) }
            )
    }

    /**
     * Sorts apps by most or least used.
     */
    fun sortAppsByUsage(
        apps: List<AppUsageInfo>,
        mostUsedFirst: Boolean = true
    ): List<AppUsageInfo> {
        return if (mostUsedFirst) {
            apps.sortedWith(
                compareByDescending<AppUsageInfo> { it.totalTimeInForeground }
                    .thenBy { it.appName.lowercase(Locale.getDefault()) }
            )
        } else {
            apps.sortedWith(
                compareBy<AppUsageInfo> { it.totalTimeInForeground }
                    .thenBy { it.appName.lowercase(Locale.getDefault()) }
            )
        }
    }

    /**
     * Sorts apps by last used timestamp.
     */
    fun sortAppsByLastUsed(
        apps: List<AppUsageInfo>,
        mostRecentFirst: Boolean = true
    ): List<AppUsageInfo> {
        return if (mostRecentFirst) {
            apps.sortedWith(
                compareByDescending<AppUsageInfo> { it.lastTimeUsed }
                    .thenBy { it.appName.lowercase(Locale.getDefault()) }
            )
        } else {
            apps.sortedWith(
                compareBy<AppUsageInfo> { it.lastTimeUsed }
                    .thenBy { it.appName.lowercase(Locale.getDefault()) }
            )
        }
    }

    /**
     * Sums foreground time for all apps in the list.
     */
    fun getTotalScreenTime(apps: List<AppUsageInfo>): Long {
        return apps.fold(0L) { acc, app -> acc + app.totalTimeInForeground.coerceAtLeast(0L) }
    }

    /**
     * Helper to load metadata for installed applications.
     */
    private fun getInstalledAppMetadata(): Map<String, AppMetadata> {
        val ctx = context ?: return emptyMap()
        val pm = ctx.packageManager ?: return emptyMap()

        return try {
            val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getInstalledPackages(0)
            }

            packages.associate { pkg ->
                val appInfo = pkg.applicationInfo
                val isSystem = if (appInfo != null) {
                    (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                            (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                } else false

                val appName = try {
                    appInfo?.loadLabel(pm)?.toString()?.takeIf { it.isNotBlank() } ?: pkg.packageName
                } catch (_: Exception) {
                    pkg.packageName
                }

                val icon = try {
                    appInfo?.loadIcon(pm)
                } catch (_: Exception) {
                    null
                }

                pkg.packageName to AppMetadata(
                    packageName = pkg.packageName,
                    appName = appName,
                    isSystemApp = isSystem,
                    icon = icon
                )
            }
        } catch (_: Exception) {
            emptyMap()
        }
    }

    data class AppMetadata(
        val packageName: String,
        val appName: String,
        val isSystemApp: Boolean = false,
        val icon: android.graphics.drawable.Drawable? = null
    )
}
