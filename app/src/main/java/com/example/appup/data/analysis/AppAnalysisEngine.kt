package com.example.appup.data.analysis

import com.example.appup.data.model.AppHealthSummary
import com.example.appup.data.model.AppUpdateCategory

data class AppAnalysisReport(
    val totalApps: Int,
    val healthyCount: Int,
    val agingCount: Int,
    val warningCount: Int,
    val attentionCount: Int,
    val upToDateApps: Int,
    val upToDateScore: Int,
    val sortedAppsOldestFirst: List<AppHealthSummary>,
    val globalRecommendations: List<String>
)

class AppAnalysisEngine {

    companion object {
        const val HEALTHY_MAX_DAYS = 30L
        const val AGING_MAX_DAYS = 60L
        const val WARNING_MAX_DAYS = 89L
        const val UP_TO_DATE_THRESHOLD_DAYS = 90L
    }

    /**
     * Accurately calculates the number of days elapsed since the given update timestamp.
     * Handles edge cases such as missing timestamps, clock skew (future timestamps), and epoch zeroes.
     */
    fun calculateDaysSinceUpdate(
        currentTime: Long = System.currentTimeMillis(),
        lastUpdateTime: Long,
        firstInstallTime: Long = 0L
    ): Long {
        val effectiveTimestamp = when {
            lastUpdateTime > 0L -> lastUpdateTime
            firstInstallTime > 0L -> firstInstallTime
            else -> return 0L
        }

        // Clamp against future clock skew
        val diffMillis = (currentTime - effectiveTimestamp).coerceAtLeast(0L)
        return diffMillis / (1000L * 60L * 60L * 24L)
    }

    /**
     * Classifies an application based on update age:
     * - 0–30 days: Healthy
     * - 31–60 days: Aging
     * - 61–89 days: Warning
     * - 90+ days: Needs Attention
     */
    fun classifyUpdateAge(days: Long): AppUpdateCategory {
        return when {
            days <= HEALTHY_MAX_DAYS -> AppUpdateCategory.HEALTHY
            days <= AGING_MAX_DAYS -> AppUpdateCategory.AGING
            days <= WARNING_MAX_DAYS -> AppUpdateCategory.WARNING
            else -> AppUpdateCategory.NEEDS_ATTENTION
        }
    }

    /**
     * Generates a clear, actionable rule-based recommendation for an individual application.
     */
    fun generateAppRecommendation(appName: String, days: Long, category: AppUpdateCategory): String {
        return when (category) {
            AppUpdateCategory.NEEDS_ATTENTION ->
                "$appName has not been updated for $days days. Check its Play Store listing."
            AppUpdateCategory.WARNING ->
                "$appName has not been updated for $days days. Approaching outdated status."
            AppUpdateCategory.AGING ->
                "$appName was updated $days days ago. Moderate update frequency."
            AppUpdateCategory.HEALTHY ->
                if (days == 0L) "$appName was updated today (Healthy)." else "$appName was updated $days days ago (Healthy)."
        }
    }

    /**
     * Performs complete analysis on a list of applications:
     * 1. Calculates update age accurately
     * 2. Classifies each app into Healthy, Aging, Warning, Needs Attention
     * 3. Sorts applications by oldest update first
     * 4. Calculates total apps and category counts
     * 5. Computes mandatory score: (Up-to-date apps / Total apps) * 100
     * 6. Generates rule-based global recommendations
     * Handles zero applications and date calculation edge cases properly.
     */
    fun analyze(
        apps: List<AppHealthSummary>,
        currentTime: Long = System.currentTimeMillis()
    ): AppAnalysisReport {
        if (apps.isEmpty()) {
            return AppAnalysisReport(
                totalApps = 0,
                healthyCount = 0,
                agingCount = 0,
                warningCount = 0,
                attentionCount = 0,
                upToDateApps = 0,
                upToDateScore = 100, // Safe default for empty list
                sortedAppsOldestFirst = emptyList(),
                globalRecommendations = listOf(
                    "No applications detected to analyze.",
                    "Ensure appropriate package visibility permissions are granted."
                )
            )
        }

        // Re-evaluate update age and classification with current time
        val evaluatedApps = apps.map { app ->
            val days = calculateDaysSinceUpdate(
                currentTime = currentTime,
                lastUpdateTime = app.lastUpdateTime,
                firstInstallTime = app.firstInstallTime
            )
            val category = classifyUpdateAge(days)
            val rec = generateAppRecommendation(app.appName, days, category)

            app.copy(
                daysSinceLastUpdate = days,
                updateCategory = category,
                recommendation = rec
            )
        }

        // Sort: Oldest update first (largest daysSinceLastUpdate first)
        val sorted = evaluatedApps.sortedWith(
            compareByDescending<AppHealthSummary> { it.daysSinceLastUpdate }
                .thenBy { it.appName.lowercase() }
        )

        val totalApps = sorted.size
        val healthyCount = sorted.count { it.updateCategory == AppUpdateCategory.HEALTHY }
        val agingCount = sorted.count { it.updateCategory == AppUpdateCategory.AGING }
        val warningCount = sorted.count { it.updateCategory == AppUpdateCategory.WARNING }
        val attentionCount = sorted.count { it.updateCategory == AppUpdateCategory.NEEDS_ATTENTION }

        // Up-to-date apps defined as updated within the last 90 days (Healthy + Aging + Warning)
        val upToDateApps = healthyCount + agingCount + warningCount

        // Mandatory score: Up-to-date apps / Total apps * 100
        val upToDateScore = if (totalApps > 0) {
            ((upToDateApps.toDouble() / totalApps.toDouble()) * 100.0).toInt().coerceIn(0, 100)
        } else {
            100
        }

        // Rule-based aggregate recommendations
        val recommendations = mutableListOf<String>()

        if (attentionCount > 0) {
            if (attentionCount >= 3) {
                recommendations.add("Several applications ($attentionCount) need attention.")
            } else {
                recommendations.add("$attentionCount application(s) need immediate attention.")
            }

            // Specific highlight for the oldest app
            val oldest = sorted.firstOrNull { it.updateCategory == AppUpdateCategory.NEEDS_ATTENTION }
            if (oldest != null) {
                recommendations.add(
                    "${oldest.appName} has not been updated for ${oldest.daysSinceLastUpdate} days. Check its Play Store listing."
                )
            }
        }

        if (warningCount > 0) {
            recommendations.add(
                "$warningCount application(s) are in the Warning stage (61–89 days without updates)."
            )
        }

        if (agingCount > 0 && attentionCount == 0 && warningCount == 0) {
            recommendations.add(
                "$agingCount application(s) are Aging (31–60 days) - keep an eye out for newer releases."
            )
        }

        if (upToDateScore >= 90) {
            recommendations.add("High update health! $upToDateApps of $totalApps applications are up to date ($upToDateScore%).")
        } else {
            recommendations.add("Update health is at $upToDateScore%. Updating neglected apps reduces vulnerability exposure.")
        }

        return AppAnalysisReport(
            totalApps = totalApps,
            healthyCount = healthyCount,
            agingCount = agingCount,
            warningCount = warningCount,
            attentionCount = attentionCount,
            upToDateApps = upToDateApps,
            upToDateScore = upToDateScore,
            sortedAppsOldestFirst = sorted,
            globalRecommendations = recommendations
        )
    }
}
