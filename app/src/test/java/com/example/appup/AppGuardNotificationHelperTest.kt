package com.example.appup

import com.example.appup.notifications.AppGuardNotificationHelper
import com.example.appup.worker.HealthScanReminderWorker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppGuardNotificationHelperTest {

    @Test
    fun testNotificationChannelConstants() {
        assertEquals("appguard_alerts", AppGuardNotificationHelper.CHANNEL_APPGUARD_ALERTS)
        assertEquals("owl_reminders", AppGuardNotificationHelper.CHANNEL_OWL_REMINDERS)
        assertEquals("AppGuard Alerts", AppGuardNotificationHelper.CHANNEL_NAME_ALERTS)
        assertEquals("Owl Reminders", AppGuardNotificationHelper.CHANNEL_NAME_REMINDERS)
    }

    @Test
    fun testDeepLinkUriFormats() {
        assertEquals("appguard://alerts", AppGuardNotificationHelper.DEEP_LINK_ALERTS)
        assertEquals("appguard://reminders", AppGuardNotificationHelper.DEEP_LINK_REMINDERS)
        assertEquals("appguard://dashboard", AppGuardNotificationHelper.DEEP_LINK_DASHBOARD)
        assertEquals("appguard://apps", AppGuardNotificationHelper.DEEP_LINK_APPS)

        assertTrue(AppGuardNotificationHelper.DEEP_LINK_ALERTS.startsWith("appguard://"))
        assertTrue(AppGuardNotificationHelper.DEEP_LINK_REMINDERS.startsWith("appguard://"))
    }

    @Test
    fun testAlertTypesAndExtras() {
        assertEquals("memory_anomaly", AppGuardNotificationHelper.ALERT_TYPE_MEMORY)
        assertEquals("battery_drain", AppGuardNotificationHelper.ALERT_TYPE_BATTERY)
        assertEquals("critical_unmaintained", AppGuardNotificationHelper.ALERT_TYPE_UNMAINTAINED)
        assertEquals("owl_reminder", AppGuardNotificationHelper.ALERT_TYPE_REMINDER)

        assertEquals("deep_link", AppGuardNotificationHelper.EXTRA_DEEP_LINK)
        assertEquals("notification_id", AppGuardNotificationHelper.EXTRA_NOTIFICATION_ID)
        assertEquals("channel_id", AppGuardNotificationHelper.EXTRA_CHANNEL_ID)
        assertEquals("alert_type", AppGuardNotificationHelper.EXTRA_ALERT_TYPE)
        assertEquals("target_tab", AppGuardNotificationHelper.EXTRA_TARGET_TAB)
    }

    @Test
    fun testNotificationIdGeneration() {
        val id1 = AppGuardNotificationHelper.nextNotificationId()
        val id2 = AppGuardNotificationHelper.nextNotificationId()

        assertTrue(id1 >= 3000)
        assertTrue(id2 > id1)
        assertNotEquals(id1, id2)
    }

    @Test
    fun testWorkerConstantsAndPreferences() {
        assertEquals("AppGuardHealthScanReminderWork", HealthScanReminderWorker.WORK_NAME)
        assertEquals("HealthScanWorker", HealthScanReminderWorker.TAG)
        assertEquals("appguard_reminder_prefs", HealthScanReminderWorker.PREFS_NAME)
        assertEquals("key_reminder_enabled", HealthScanReminderWorker.KEY_REMINDER_ENABLED)
        assertEquals("key_snooze_until", HealthScanReminderWorker.KEY_SNOOZE_UNTIL)
        assertEquals("key_interval_days", HealthScanReminderWorker.KEY_INTERVAL_DAYS)
        assertEquals(7L, HealthScanReminderWorker.DEFAULT_INTERVAL_DAYS)
    }

    @Test
    fun testWorkerConstraintsRequiresBatteryNotLow() {
        val constraints = HealthScanReminderWorker.buildConstraints()
        assertTrue("Constraints must require battery not low", constraints.requiresBatteryNotLow())
        assertFalse("Device does not require charging", constraints.requiresCharging())
    }
}
