package com.example.appup

import com.example.appup.data.model.AppUsageInfo
import com.example.appup.data.model.AppUsageLevel
import com.example.appup.data.model.RawUsageStat
import com.example.appup.data.monitor.AppUsageService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class AppUsageServiceTest {

    private lateinit var service: AppUsageService
    private val now = 1700000000000L // Fixed reference epoch timestamp

    @Before
    fun setUp() {
        service = AppUsageService()
    }

    // =========================================================================
    // 1. Calculating total foreground time from interval stats
    // =========================================================================

    @Test
    fun testCalculateTotalForegroundTime_singleStat() {
        val stat = RawUsageStat("com.example.app", totalTimeInForeground = 15000L, lastTimeUsed = now)
        val total = service.calculateTotalForegroundTime(listOf(stat))
        assertEquals(15000L, total)
    }

    @Test
    fun testCalculateTotalForegroundTime_multipleStats() {
        val stats = listOf(
            RawUsageStat("com.example.app1", totalTimeInForeground = 10000L, lastTimeUsed = now),
            RawUsageStat("com.example.app2", totalTimeInForeground = 25000L, lastTimeUsed = now),
            RawUsageStat("com.example.app3", totalTimeInForeground = 5000L, lastTimeUsed = now)
        )
        val total = service.calculateTotalForegroundTime(stats)
        assertEquals(40000L, total)
    }

    @Test
    fun testCalculateTotalForegroundTime_emptyList() {
        val total = service.calculateTotalForegroundTime(emptyList())
        assertEquals(0L, total)
    }

    @Test
    fun testCalculateTotalForegroundTime_negativeValuesHandled() {
        val stats = listOf(
            RawUsageStat("com.example.app1", totalTimeInForeground = -500L, lastTimeUsed = now),
            RawUsageStat("com.example.app2", totalTimeInForeground = 20000L, lastTimeUsed = now)
        )
        val total = service.calculateTotalForegroundTime(stats)
        assertEquals(20000L, total)
    }

    @Test
    fun testCalculateTotalForegroundTimeFromDurations() {
        val durations = listOf(5000L, 10000L, -200L, 15000L)
        val total = service.calculateTotalForegroundTimeFromDurations(durations)
        assertEquals(30000L, total)
    }

    // =========================================================================
    // 2. Formatting duration into readable hours/minutes
    // =========================================================================

    @Test
    fun testFormatDuration_zeroAndNegative() {
        assertEquals("0m", service.formatDuration(0L))
        assertEquals("0m", service.formatDuration(-1000L))
    }

    @Test
    fun testFormatDuration_secondsOnlyLessThanMinute() {
        // Less than 60 seconds should indicate < 1m
        assertEquals("< 1m", service.formatDuration(15000L))
        assertEquals("< 1m", service.formatDuration(59000L))
    }

    @Test
    fun testFormatDuration_minutesOnly() {
        val oneMin = 60 * 1000L
        assertEquals("1m", service.formatDuration(oneMin))
        assertEquals("25m", service.formatDuration(25 * oneMin))
        assertEquals("59m", service.formatDuration(59 * oneMin))
    }

    @Test
    fun testFormatDuration_hoursOnly() {
        val oneHour = 3600 * 1000L
        assertEquals("1h", service.formatDuration(oneHour))
        assertEquals("5h", service.formatDuration(5 * oneHour))
    }

    @Test
    fun testFormatDuration_hoursAndMinutes() {
        val duration1 = (2 * 3600 + 45 * 60) * 1000L // 2h 45m
        assertEquals("2h 45m", service.formatDuration(duration1))

        val duration2 = (1 * 3600 + 1 * 60) * 1000L // 1h 1m
        assertEquals("1h 1m", service.formatDuration(duration2))

        val duration3 = (26 * 3600 + 15 * 60) * 1000L // 26h 15m
        assertEquals("26h 15m", service.formatDuration(duration3))
    }

    // =========================================================================
    // 3. Identifying dormant/unused apps
    // =========================================================================

    @Test
    fun testIsAppDormant_activeAppsNotDormant() {
        val fiveDaysAgo = now - TimeUnit.DAYS.toMillis(5)
        assertFalse(service.isAppDormant(fiveDaysAgo, now = now))

        val twentyNineDaysAgo = now - TimeUnit.DAYS.toMillis(29)
        assertFalse(service.isAppDormant(twentyNineDaysAgo, now = now))
    }

    @Test
    fun testIsAppDormant_boundaryCheckAt30Days() {
        val exactly30DaysAgo = now - TimeUnit.DAYS.toMillis(30)
        assertTrue(service.isAppDormant(exactly30DaysAgo, now = now))

        val thirtyOneDaysAgo = now - TimeUnit.DAYS.toMillis(31)
        assertTrue(service.isAppDormant(thirtyOneDaysAgo, now = now))
    }

    @Test
    fun testIsAppDormant_neverUsedOrZeroTimestamp() {
        assertTrue(service.isAppDormant(0L, now = now))
        assertTrue(service.isAppDormant(-1L, now = now))
    }

    @Test
    fun testIdentifyDormantApps_filteringAndSorting() {
        val activeApp = createApp("Active App", totalForeground = 3600000L, lastUsed = now - TimeUnit.DAYS.toMillis(2), isDormant = false, daysSince = 2L)
        val dormantApp1 = createApp("Dormant 45d", totalForeground = 1800000L, lastUsed = now - TimeUnit.DAYS.toMillis(45), isDormant = true, daysSince = 45L)
        val dormantApp2 = createApp("Dormant 90d", totalForeground = 600000L, lastUsed = now - TimeUnit.DAYS.toMillis(90), isDormant = true, daysSince = 90L)
        val neverUsedApp = createApp("Never Used", totalForeground = 0L, lastUsed = 0L, isDormant = true, daysSince = -1L)

        val dormantList = service.identifyDormantApps(listOf(activeApp, dormantApp1, dormantApp2, neverUsedApp))

        assertEquals(3, dormantList.size)
        assertFalse(dormantList.any { it.appName == "Active App" })

        // Never used should be prioritized first, followed by largest daysSinceLastUsed
        assertEquals("Never Used", dormantList[0].appName)
        assertEquals("Dormant 90d", dormantList[1].appName)
        assertEquals("Dormant 45d", dormantList[2].appName)
    }

    // =========================================================================
    // 4. Boundary checks
    // =========================================================================

    @Test
    fun testBoundary_emptyStatsList() {
        val result = service.aggregateRawStats(
            rawStats = emptyList(),
            installedAppMetadata = emptyMap(),
            now = now
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun testBoundary_zeroUsageHandledGracefully() {
        val stat = RawUsageStat("com.example.zero", totalTimeInForeground = 0L, lastTimeUsed = now)
        val result = service.aggregateRawStats(
            rawStats = listOf(stat),
            installedAppMetadata = emptyMap(),
            now = now
        )
        assertEquals(1, result.size)
        val app = result.first()
        assertEquals(0L, app.totalTimeInForeground)
        assertEquals("0m", app.foregroundTimeFormatted)
    }

    @Test
    fun testBoundary_futureTimestampsClockSkew() {
        val futureTime = now + TimeUnit.DAYS.toMillis(10)

        // Days since last used should be clamped to 0
        val days = service.calculateDaysSinceLastUsed(futureTime, now = now)
        assertEquals(0L, days)

        // Future app should not be flagged as dormant
        val isDormant = service.isAppDormant(futureTime, now = now)
        assertFalse(isDormant)

        // Formatting should gracefully say "Just now"
        val formatted = service.formatLastTimeUsed(futureTime, now = now)
        assertEquals("Just now", formatted)
    }

    @Test
    fun testBoundary_daysSinceLastUsed_neverUsed() {
        assertEquals(-1L, service.calculateDaysSinceLastUsed(0L, now = now))
        assertEquals(-1L, service.calculateDaysSinceLastUsed(-500L, now = now))
    }

    // =========================================================================
    // 5. Sorting apps by most/least used
    // =========================================================================

    @Test
    fun testSortAppsByUsage_mostUsedFirst() {
        val appLow = createApp("App Low", totalForeground = 1000L)
        val appHigh = createApp("App High", totalForeground = 50000L)
        val appMid = createApp("App Mid", totalForeground = 25000L)

        val sorted = service.sortAppsByUsage(listOf(appLow, appHigh, appMid), mostUsedFirst = true)

        assertEquals("App High", sorted[0].appName)
        assertEquals("App Mid", sorted[1].appName)
        assertEquals("App Low", sorted[2].appName)
    }

    @Test
    fun testSortAppsByUsage_leastUsedFirst() {
        val appLow = createApp("App Low", totalForeground = 1000L)
        val appHigh = createApp("App High", totalForeground = 50000L)
        val appMid = createApp("App Mid", totalForeground = 25000L)

        val sorted = service.sortAppsByUsage(listOf(appLow, appHigh, appMid), mostUsedFirst = false)

        assertEquals("App Low", sorted[0].appName)
        assertEquals("App Mid", sorted[1].appName)
        assertEquals("App High", sorted[2].appName)
    }

    @Test
    fun testSortAppsByUsage_tieBreakerAlphabetical() {
        val appB = createApp("Beta App", totalForeground = 10000L)
        val appA = createApp("Alpha App", totalForeground = 10000L)

        val sorted = service.sortAppsByUsage(listOf(appB, appA), mostUsedFirst = true)

        assertEquals("Alpha App", sorted[0].appName)
        assertEquals("Beta App", sorted[1].appName)
    }

    @Test
    fun testSortAppsByLastUsed() {
        val appRecent = createApp("Recent", lastUsed = now - 1000L)
        val appOld = createApp("Old", lastUsed = now - TimeUnit.DAYS.toMillis(50))
        val appNever = createApp("Never", lastUsed = 0L)

        val mostRecent = service.sortAppsByLastUsed(listOf(appOld, appRecent, appNever), mostRecentFirst = true)
        assertEquals("Recent", mostRecent[0].appName)
        assertEquals("Old", mostRecent[1].appName)
        assertEquals("Never", mostRecent[2].appName)

        val oldestFirst = service.sortAppsByLastUsed(listOf(appRecent, appOld, appNever), mostRecentFirst = false)
        assertEquals("Never", oldestFirst[0].appName)
        assertEquals("Old", oldestFirst[1].appName)
        assertEquals("Recent", oldestFirst[2].appName)
    }

    // =========================================================================
    // 6. Aggregating raw interval stats across time frames
    // =========================================================================

    @Test
    fun testAggregateRawStats_multipleIntervalsForSamePackage() {
        val pkg = "com.example.chat"
        val intervalStats = listOf(
            RawUsageStat(pkg, totalTimeInForeground = 600000L, lastTimeUsed = now - 50000L),  // 10m
            RawUsageStat(pkg, totalTimeInForeground = 1200000L, lastTimeUsed = now - 20000L), // 20m
            RawUsageStat(pkg, totalTimeInForeground = 1800000L, lastTimeUsed = now - 5000L)   // 30m
        )

        val aggregated = service.aggregateRawStats(intervalStats, now = now)
        assertEquals(1, aggregated.size)

        val result = aggregated.first()
        assertEquals(pkg, result.packageName)
        assertEquals(3600000L, result.totalTimeInForeground) // 10m + 20m + 30m = 60m = 1h
        assertEquals("1h", result.foregroundTimeFormatted)
        assertEquals(now - 5000L, result.lastTimeUsed) // latest timestamp chosen
    }

    @Test
    fun testAggregateRawStats_systemAppFiltering() {
        val metadata = mapOf(
            "com.user.app" to AppUsageService.AppMetadata("com.user.app", "User App", isSystemApp = false),
            "com.system.app" to AppUsageService.AppMetadata("com.system.app", "System App", isSystemApp = true)
        )
        val stats = listOf(
            RawUsageStat("com.user.app", totalTimeInForeground = 50000L, lastTimeUsed = now),
            RawUsageStat("com.system.app", totalTimeInForeground = 20000L, lastTimeUsed = now)
        )

        val withoutSystem = service.aggregateRawStats(stats, metadata, now = now, includeSystemApps = false)
        assertEquals(1, withoutSystem.size)
        assertEquals("User App", withoutSystem.first().appName)

        val withSystem = service.aggregateRawStats(stats, metadata, now = now, includeSystemApps = true)
        assertEquals(2, withSystem.size)
    }

    // =========================================================================
    // 7. Usage level classifications
    // =========================================================================

    @Test
    fun testClassifyUsageLevel() {
        // High: >= 2 hours
        assertEquals(AppUsageLevel.HIGH, service.classifyUsageLevel(2 * 3600 * 1000L, isDormant = false))
        assertEquals(AppUsageLevel.HIGH, service.classifyUsageLevel(3 * 3600 * 1000L, isDormant = false))

        // Moderate: 30m - 2h
        assertEquals(AppUsageLevel.MODERATE, service.classifyUsageLevel(30 * 60 * 1000L, isDormant = false))
        assertEquals(AppUsageLevel.MODERATE, service.classifyUsageLevel(90 * 60 * 1000L, isDormant = false))

        // Low: < 30m
        assertEquals(AppUsageLevel.LOW, service.classifyUsageLevel(15 * 60 * 1000L, isDormant = false))
        assertEquals(AppUsageLevel.LOW, service.classifyUsageLevel(1000L, isDormant = false))

        // Dormant
        assertEquals(AppUsageLevel.DORMANT, service.classifyUsageLevel(50000L, isDormant = true))

        // Unused: 0ms and dormant
        assertEquals(AppUsageLevel.UNUSED, service.classifyUsageLevel(0L, isDormant = true))
    }

    // =========================================================================
    // 8. Total screen time calculation
    // =========================================================================

    @Test
    fun testGetTotalScreenTime() {
        val apps = listOf(
            createApp("App 1", totalForeground = 3600000L), // 1h
            createApp("App 2", totalForeground = 1800000L), // 30m
            createApp("App 3", totalForeground = 600000L)   // 10m
        )
        val total = service.getTotalScreenTime(apps)
        assertEquals(6000000L, total) // 1h 40m = 6,000,000 ms
        assertEquals("1h 40m", service.formatDuration(total))
    }

    // =========================================================================
    // 9. Last used time relative formatting
    // =========================================================================

    @Test
    fun testFormatLastTimeUsed_relativeFormatting() {
        assertEquals("Never used", service.formatLastTimeUsed(0L, now = now))
        assertEquals("Never used", service.formatLastTimeUsed(-100L, now = now))

        // Within 1 minute: "Just now"
        assertEquals("Just now", service.formatLastTimeUsed(now - 30000L, now = now))

        // Within 1 hour: "Xm ago"
        assertEquals("15m ago", service.formatLastTimeUsed(now - TimeUnit.MINUTES.toMillis(15), now = now))

        // Within 24 hours: "Xh ago"
        assertEquals("3h ago", service.formatLastTimeUsed(now - TimeUnit.HOURS.toMillis(3), now = now))

        // 1 day: "Yesterday"
        assertEquals("Yesterday", service.formatLastTimeUsed(now - TimeUnit.DAYS.toMillis(1), now = now))

        // 4 days: "4 days ago"
        assertEquals("4 days ago", service.formatLastTimeUsed(now - TimeUnit.DAYS.toMillis(4), now = now))

        // 14 days: "2 weeks ago"
        assertEquals("2 weeks ago", service.formatLastTimeUsed(now - TimeUnit.DAYS.toMillis(14), now = now))
    }

    private fun createApp(
        appName: String,
        totalForeground: Long = 0L,
        lastUsed: Long = now,
        isDormant: Boolean = false,
        daysSince: Long = 0L
    ): AppUsageInfo {
        return AppUsageInfo(
            packageName = "com.test." + appName.lowercase().replace(" ", ""),
            appName = appName,
            totalTimeInForeground = totalForeground,
            lastTimeUsed = lastUsed,
            lastTimeUsedFormatted = service.formatLastTimeUsed(lastUsed, now = now),
            foregroundTimeFormatted = service.formatDuration(totalForeground),
            isDormant = isDormant,
            daysSinceLastUsed = daysSince,
            usageLevel = service.classifyUsageLevel(totalForeground, isDormant),
            isSystemApp = false,
            icon = null
        )
    }
}
