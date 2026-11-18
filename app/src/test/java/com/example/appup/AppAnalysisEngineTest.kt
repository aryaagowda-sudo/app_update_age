package com.example.appup

import com.example.appup.data.analysis.AppAnalysisEngine
import com.example.appup.data.model.AppHealthSummary
import com.example.appup.data.model.AppRiskLevel
import com.example.appup.data.model.AppUpdateCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class AppAnalysisEngineTest {

    private lateinit var engine: AppAnalysisEngine
    private val now = 1700000000000L // Reference fixed epoch

    @Before
    fun setUp() {
        engine = AppAnalysisEngine()
    }

    @Test
    fun testUpdateAgeClassificationBoundaries() {
        // 0–30 days: Healthy
        assertEquals(AppUpdateCategory.HEALTHY, engine.classifyUpdateAge(0))
        assertEquals(AppUpdateCategory.HEALTHY, engine.classifyUpdateAge(15))
        assertEquals(AppUpdateCategory.HEALTHY, engine.classifyUpdateAge(30))

        // 31–60 days: Aging
        assertEquals(AppUpdateCategory.AGING, engine.classifyUpdateAge(31))
        assertEquals(AppUpdateCategory.AGING, engine.classifyUpdateAge(45))
        assertEquals(AppUpdateCategory.AGING, engine.classifyUpdateAge(60))

        // 61–89 days: Warning
        assertEquals(AppUpdateCategory.WARNING, engine.classifyUpdateAge(61))
        assertEquals(AppUpdateCategory.WARNING, engine.classifyUpdateAge(75))
        assertEquals(AppUpdateCategory.WARNING, engine.classifyUpdateAge(89))

        // 90+ days: Needs Attention
        assertEquals(AppUpdateCategory.NEEDS_ATTENTION, engine.classifyUpdateAge(90))
        assertEquals(AppUpdateCategory.NEEDS_ATTENTION, engine.classifyUpdateAge(120))
        assertEquals(AppUpdateCategory.NEEDS_ATTENTION, engine.classifyUpdateAge(365))
    }

    @Test
    fun testDaysCalculationEdgeCases() {
        val oneDayMillis = TimeUnit.DAYS.toMillis(1)

        // Normal: 5 days ago
        val fiveDaysAgo = now - (5 * oneDayMillis)
        assertEquals(5L, engine.calculateDaysSinceUpdate(now, fiveDaysAgo))

        // Zero timestamp fallback
        assertEquals(0L, engine.calculateDaysSinceUpdate(now, 0L, 0L))

        // Missing lastUpdateTime but valid firstInstallTime (3 days ago)
        val threeDaysAgo = now - (3 * oneDayMillis)
        assertEquals(3L, engine.calculateDaysSinceUpdate(now, 0L, threeDaysAgo))

        // Clock skew: future timestamp should be clamped to 0
        val futureTime = now + (10 * oneDayMillis)
        assertEquals(0L, engine.calculateDaysSinceUpdate(now, futureTime))
    }

    @Test
    fun testEmptyApplicationsEdgeCase() {
        val report = engine.analyze(emptyList())

        assertEquals(0, report.totalApps)
        assertEquals(0, report.healthyCount)
        assertEquals(0, report.agingCount)
        assertEquals(0, report.warningCount)
        assertEquals(0, report.attentionCount)
        assertEquals(0, report.upToDateApps)
        assertEquals(100, report.upToDateScore) // Safe default, no division by zero
        assertTrue(report.sortedAppsOldestFirst.isEmpty())
        assertTrue(report.globalRecommendations.isNotEmpty())
    }

    @Test
    fun testFullApplicationAnalysisAndMandatoryScore() {
        val oneDayMillis = TimeUnit.DAYS.toMillis(1)

        val app1 = createApp("App Healthy", now - (10 * oneDayMillis))     // 10 days (Healthy)
        val app2 = createApp("App Aging", now - (45 * oneDayMillis))       // 45 days (Aging)
        val app3 = createApp("App Warning", now - (75 * oneDayMillis))     // 75 days (Warning)
        val app4 = createApp("App Attention", now - (120 * oneDayMillis))  // 120 days (Needs Attention)

        val report = engine.analyze(listOf(app1, app2, app3, app4), currentTime = now)

        // Counts
        assertEquals(4, report.totalApps)
        assertEquals(1, report.healthyCount)
        assertEquals(1, report.agingCount)
        assertEquals(1, report.warningCount)
        assertEquals(1, report.attentionCount)

        // Up to date: updated within last 90 days (app1, app2, app3 = 3)
        assertEquals(3, report.upToDateApps)

        // Mandatory score: 3 / 4 * 100 = 75%
        assertEquals(75, report.upToDateScore)

        // Verify sorting: Oldest update first (app4: 120d, app3: 75d, app2: 45d, app1: 10d)
        assertEquals("App Attention", report.sortedAppsOldestFirst[0].appName)
        assertEquals("App Warning", report.sortedAppsOldestFirst[1].appName)
        assertEquals("App Aging", report.sortedAppsOldestFirst[2].appName)
        assertEquals("App Healthy", report.sortedAppsOldestFirst[3].appName)

        // Recommendation for 120 days app
        val attentionRec = report.sortedAppsOldestFirst[0].recommendation
        assertTrue(attentionRec.contains("120 days"))
        assertTrue(attentionRec.contains("Play Store listing"))
    }

    private fun createApp(name: String, lastUpdate: Long): AppHealthSummary {
        return AppHealthSummary(
            packageName = "com.test." + name.lowercase().replace(" ", ""),
            appName = name,
            versionName = "1.0",
            versionCode = 1L,
            firstInstallTime = lastUpdate - 10000L,
            lastUpdateTime = lastUpdate,
            firstInstallDateFormatted = "Jan 01, 2026",
            lastUpdateDateFormatted = "Jan 01, 2026",
            daysSinceLastUpdate = 0L,
            isSystemApp = false,
            targetSdk = 34,
            isDebuggable = false,
            allPermissions = emptyList(),
            sensitivePermissions = emptyList(),
            healthScore = 80,
            riskLevel = AppRiskLevel.SAFE,
            riskFactors = emptyList(),
            icon = null
        )
    }
}
