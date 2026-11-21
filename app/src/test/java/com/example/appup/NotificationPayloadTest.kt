package com.example.appup

import com.example.appup.notification.AlertType
import com.example.appup.notification.NotificationHelper
import com.example.appup.notification.NotificationPayloadParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationPayloadTest {

    @Test
    fun testHealthAlertPayloadParsing() {
        val data = mapOf(
            "alert_type" to "health_alert",
            "app_name" to "SocialApp",
            "package_name" to "com.example.social",
            "health_score" to "42"
        )

        val payload = NotificationPayloadParser.parse(data)

        assertEquals(AlertType.HEALTH_ALERT, payload.alertType)
        assertEquals(NotificationHelper.CHANNEL_HEALTH_ALERTS, payload.channelId)
        assertEquals("App Health Alert: SocialApp", payload.title)
        assertTrue(payload.message.contains("42/100"))
        assertEquals("SocialApp", payload.appName)
        assertEquals("com.example.social", payload.packageName)
        assertEquals(42, payload.healthScore)
    }

    @Test
    fun testSecurityUpdatePayloadParsing() {
        val data = mapOf(
            "type" to "security_update",
            "app_name" to "BrowserApp",
            "package_name" to "com.example.browser",
            "body" to "Critical vulnerability CVE-2026-1234 detected. Please patch immediately."
        )

        val payload = NotificationPayloadParser.parse(data)

        assertEquals(AlertType.SECURITY_UPDATE, payload.alertType)
        assertEquals(NotificationHelper.CHANNEL_SECURITY_UPDATES, payload.channelId)
        assertEquals("Security Update Needed: BrowserApp", payload.title)
        assertEquals("Critical vulnerability CVE-2026-1234 detected. Please patch immediately.", payload.message)
        assertEquals("BrowserApp", payload.appName)
    }

    @Test
    fun testExplicitChannelAssignment() {
        val data = mapOf(
            "channel_id" to NotificationHelper.CHANNEL_SECURITY_UPDATES,
            "title" to "Custom Security Notice",
            "message" to "Update your device definitions"
        )

        val payload = NotificationPayloadParser.parse(data)

        assertEquals(NotificationHelper.CHANNEL_SECURITY_UPDATES, payload.channelId)
        assertEquals("Custom Security Notice", payload.title)
        assertEquals("Update your device definitions", payload.message)
    }

    @Test
    fun testNotificationPayloadOverridesDataTitleAndBody() {
        val data = mapOf(
            "type" to "health_alert",
            "app_name" to "GameApp",
            "health_score" to "15"
        )

        val payload = NotificationPayloadParser.parse(
            data = data,
            notificationTitle = "Notification Title Priority",
            notificationBody = "Notification Body Priority"
        )

        assertEquals("Notification Title Priority", payload.title)
        assertEquals("Notification Body Priority", payload.message)
        assertEquals(NotificationHelper.CHANNEL_HEALTH_ALERTS, payload.channelId)
        assertEquals("GameApp", payload.appName)
        assertEquals(15, payload.healthScore)
    }

    @Test
    fun testKeywordHeuristicChannelSelection() {
        val data = mapOf(
            "message" to "A new critical security patch is available for installed apps."
        )

        val payload = NotificationPayloadParser.parse(data)

        assertEquals(NotificationHelper.CHANNEL_SECURITY_UPDATES, payload.channelId)
    }

    @Test
    fun testAlertTypeFromString() {
        assertEquals(AlertType.HEALTH_ALERT, AlertType.fromString("health_alert"))
        assertEquals(AlertType.HEALTH_ALERT, AlertType.fromString("aging_apps"))
        assertEquals(AlertType.SECURITY_UPDATE, AlertType.fromString("security_vulnerability"))
        assertEquals(AlertType.UPDATE_REMINDER, AlertType.fromString("patch_reminder"))
        assertEquals(AlertType.GENERAL, AlertType.fromString("unknown_type"))
        assertEquals(AlertType.GENERAL, AlertType.fromString(null))
    }

    @Test
    fun testDefaultFallbackValues() {
        val payload = NotificationPayloadParser.parse(emptyMap())

        assertNotNull(payload.title)
        assertNotNull(payload.message)
        assertEquals(NotificationHelper.CHANNEL_HEALTH_ALERTS, payload.channelId)
        assertNull(payload.appName)
        assertNull(payload.packageName)
        assertNull(payload.healthScore)
    }
}
