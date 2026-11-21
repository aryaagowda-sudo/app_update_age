package com.example.appup.notifications

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.appup.MainActivity
import com.example.appup.R
import java.util.concurrent.atomic.AtomicInteger

/**
 * AppGuardNotificationHelper manages high-priority security/performance alert notifications
 * and periodic UpdateOwl reminder notifications.
 */
class AppGuardNotificationHelper(private val context: Context) {

    companion object {
        const val TAG = "AppGuardNotifHelper"

        // Dual Notification Channel IDs
        const val CHANNEL_APPGUARD_ALERTS = "appguard_alerts"
        const val CHANNEL_OWL_REMINDERS = "owl_reminders"

        // Channel Names
        const val CHANNEL_NAME_ALERTS = "AppGuard Alerts"
        const val CHANNEL_NAME_REMINDERS = "Owl Reminders"

        // Intent Extras
        const val EXTRA_DEEP_LINK = "deep_link"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_CHANNEL_ID = "channel_id"
        const val EXTRA_ALERT_TYPE = "alert_type"
        const val EXTRA_APP_NAME = "app_name"
        const val EXTRA_PACKAGE_NAME = "package_name"
        const val EXTRA_TARGET_TAB = "target_tab"
        const val EXTRA_ATTENTION_COUNT = "attention_count"

        // Deep Link URIs
        const val DEEP_LINK_ALERTS = "appguard://alerts"
        const val DEEP_LINK_REMINDERS = "appguard://reminders"
        const val DEEP_LINK_DASHBOARD = "appguard://dashboard"
        const val DEEP_LINK_APPS = "appguard://apps"

        // Alert Types
        const val ALERT_TYPE_MEMORY = "memory_anomaly"
        const val ALERT_TYPE_BATTERY = "battery_drain"
        const val ALERT_TYPE_UNMAINTAINED = "critical_unmaintained"
        const val ALERT_TYPE_REMINDER = "owl_reminder"

        // Notification IDs
        const val NOTIF_ID_REMINDER = 2001
        const val NOTIF_ID_MEMORY_ALERT = 2002
        const val NOTIF_ID_BATTERY_ALERT = 2003
        const val NOTIF_ID_UNMAINTAINED_ALERT = 2004

        private val notificationIdCounter = AtomicInteger(3000)

        fun nextNotificationId(): Int = notificationIdCounter.incrementAndGet()
    }

    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    /**
     * Initializes the dual notification channels for Android 8.0+ (API 26+).
     * 1. 'appguard_alerts': High priority, sound, vibration, red-orange light indicator.
     * 2. 'owl_reminders': Default priority, sound, vibration, cyber-accent indicator.
     */
    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Channel 1: AppGuard Alerts
            val alertChannel = NotificationChannel(
                CHANNEL_APPGUARD_ALERTS,
                CHANNEL_NAME_ALERTS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority alerts for anomalous memory, battery drain, or critical unmaintained apps"
                enableLights(true)
                lightColor = Color.rgb(255, 61, 0) // Cyber Red/Orange
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            // Channel 2: Owl Reminders
            val reminderChannel = NotificationChannel(
                CHANNEL_OWL_REMINDERS,
                CHANNEL_NAME_REMINDERS,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Periodic health check-ins and weekly update reminders from UpdateOwl"
                enableLights(true)
                lightColor = Color.rgb(0, 229, 255) // Cyber Cyan
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 150, 100, 150)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            notificationManager.createNotificationChannel(alertChannel)
            notificationManager.createNotificationChannel(reminderChannel)
            Log.d(TAG, "Initialized channels: $CHANNEL_APPGUARD_ALERTS, $CHANNEL_OWL_REMINDERS")
        }
    }

    /**
     * Checks whether the app has permission to post notifications.
     */
    fun hasNotificationPermission(): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            return false
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Requests POST_NOTIFICATIONS permission on Android 13+ (API 33+).
     */
    fun requestNotificationPermission(activity: Activity, requestCode: Int = 1002) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission()) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    requestCode
                )
            }
        }
    }

    /**
     * Creates a PendingIntent targeting MainActivity with deep link and extras.
     */
    private fun createContentPendingIntent(
        notificationId: Int,
        channelId: String,
        deepLink: String,
        targetTab: Int = 0,
        appName: String? = null,
        packageName: String? = null,
        alertType: String? = null,
        extras: Map<String, String> = emptyMap()
    ): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse(deepLink)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(EXTRA_CHANNEL_ID, channelId)
            putExtra(EXTRA_DEEP_LINK, deepLink)
            putExtra(EXTRA_TARGET_TAB, targetTab)
            if (appName != null) putExtra(EXTRA_APP_NAME, appName)
            if (packageName != null) putExtra(EXTRA_PACKAGE_NAME, packageName)
            if (alertType != null) putExtra(EXTRA_ALERT_TYPE, alertType)
            extras.forEach { (k, v) -> putExtra(k, v) }
        }

        return PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Dispatches a high-priority alert notification (appguard_alerts channel).
     * Used for anomalous memory, battery drain, or critical unmaintained apps.
     */
    fun dispatchAlertNotification(
        title: String,
        message: String,
        notificationId: Int = nextNotificationId(),
        deepLink: String = DEEP_LINK_ALERTS,
        targetTab: Int = 1, // Apps tab
        appName: String? = null,
        packageName: String? = null,
        alertType: String = ALERT_TYPE_UNMAINTAINED,
        extras: Map<String, String> = emptyMap()
    ): Boolean {
        if (!hasNotificationPermission()) {
            Log.w(TAG, "Notification skipped: POST_NOTIFICATIONS permission not granted")
            return false
        }

        createNotificationChannels()

        val pendingIntent = createContentPendingIntent(
            notificationId = notificationId,
            channelId = CHANNEL_APPGUARD_ALERTS,
            deepLink = deepLink,
            targetTab = targetTab,
            appName = appName,
            packageName = packageName,
            alertType = alertType,
            extras = extras
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_APPGUARD_ALERTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
                    .setBigContentTitle(title)
                    .setSummaryText("AppGuard Alert")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setColor(Color.rgb(255, 61, 0)) // Red-Orange indicator
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSubText("AppGuard Security")
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            Log.i(TAG, "Dispatched AppGuard Alert [id=$notificationId, title='$title']")
            true
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while dispatching alert: ${e.message}")
            false
        }
    }

    /**
     * Dispatches a periodic reminder notification (owl_reminders channel).
     * Displays UpdateOwl badge style for health check-ins and weekly update reminders.
     */
    fun dispatchReminderNotification(
        title: String,
        message: String,
        notificationId: Int = NOTIF_ID_REMINDER,
        deepLink: String = DEEP_LINK_REMINDERS,
        targetTab: Int = 0, // Dashboard
        attentionCount: Int = 0,
        appName: String? = null,
        packageName: String? = null,
        extras: Map<String, String> = emptyMap()
    ): Boolean {
        if (!hasNotificationPermission()) {
            Log.w(TAG, "Notification skipped: POST_NOTIFICATIONS permission not granted")
            return false
        }

        createNotificationChannels()

        val pendingIntent = createContentPendingIntent(
            notificationId = notificationId,
            channelId = CHANNEL_OWL_REMINDERS,
            deepLink = deepLink,
            targetTab = targetTab,
            appName = appName,
            packageName = packageName,
            alertType = ALERT_TYPE_REMINDER,
            extras = extras
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_OWL_REMINDERS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
                    .setBigContentTitle(title)
                    .setSummaryText(if (attentionCount > 0) "$attentionCount apps need review" else "All apps healthy")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setColor(Color.rgb(0, 229, 255)) // Cyber Cyan indicator
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSubText("UpdateOwl") // UpdateOwl badge style
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            Log.i(TAG, "Dispatched Owl Reminder [id=$notificationId, title='$title']")
            true
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while dispatching reminder: ${e.message}")
            false
        }
    }

    /**
     * Helper to dispatch anomalous memory usage alert.
     */
    fun dispatchMemoryAnomalyAlert(
        appName: String,
        memoryUsedMb: Long,
        thresholdMb: Long = 500,
        packageName: String? = null,
        notificationId: Int = NOTIF_ID_MEMORY_ALERT
    ): Boolean {
        val title = "High Memory Usage Detected"
        val message = "$appName is consuming ${memoryUsedMb}MB of RAM (threshold: ${thresholdMb}MB). Consider optimizing background processes."
        return dispatchAlertNotification(
            title = title,
            message = message,
            notificationId = notificationId,
            deepLink = DEEP_LINK_ALERTS,
            targetTab = 0,
            appName = appName,
            packageName = packageName,
            alertType = ALERT_TYPE_MEMORY
        )
    }

    /**
     * Helper to dispatch battery drain alert.
     */
    fun dispatchBatteryDrainAlert(
        appName: String,
        drainPercentage: Int,
        packageName: String? = null,
        notificationId: Int = NOTIF_ID_BATTERY_ALERT
    ): Boolean {
        val title = "Battery Drain Warning"
        val message = "$appName has caused $drainPercentage% battery discharge during active background operations."
        return dispatchAlertNotification(
            title = title,
            message = message,
            notificationId = notificationId,
            deepLink = DEEP_LINK_ALERTS,
            targetTab = 0,
            appName = appName,
            packageName = packageName,
            alertType = ALERT_TYPE_BATTERY
        )
    }

    /**
     * Helper to dispatch critical unmaintained app alert.
     */
    fun dispatchUnmaintainedAppAlert(
        appName: String,
        daysSinceUpdate: Long,
        packageName: String? = null,
        notificationId: Int = NOTIF_ID_UNMAINTAINED_ALERT
    ): Boolean {
        val title = "Critical Unmaintained App: $appName"
        val message = "$appName has not received security updates in $daysSinceUpdate days. Vulnerabilities may be unpatched."
        return dispatchAlertNotification(
            title = title,
            message = message,
            notificationId = notificationId,
            deepLink = DEEP_LINK_APPS,
            targetTab = 1,
            appName = appName,
            packageName = packageName,
            alertType = ALERT_TYPE_UNMAINTAINED
        )
    }

    /**
     * Helper to dispatch UpdateOwl weekly health check-in reminder.
     */
    fun dispatchOwlWeeklyReminder(
        attentionAppsCount: Int,
        oldestAppName: String? = null,
        daysSinceUpdate: Long? = null,
        notificationId: Int = NOTIF_ID_REMINDER
    ): Boolean {
        val title = "UpdateOwl Weekly Health Check-In"
        val message = if (attentionAppsCount > 0) {
            val appDetail = if (oldestAppName != null && daysSinceUpdate != null) {
                " (including $oldestAppName unupdated for $daysSinceUpdate days)"
            } else ""
            "You have $attentionAppsCount ${if (attentionAppsCount == 1) "app that needs" else "apps that need"} attention$appDetail. Tap to review security status."
        } else {
            "All your applications are up to date! UpdateOwl is actively keeping your device protected."
        }

        return dispatchReminderNotification(
            title = title,
            message = message,
            notificationId = notificationId,
            deepLink = DEEP_LINK_REMINDERS,
            targetTab = 0,
            attentionCount = attentionAppsCount,
            appName = oldestAppName
        )
    }

    /**
     * Cancels an active notification.
     */
    fun cancelNotification(notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    /**
     * Cancels all notifications posted by AppGuard.
     */
    fun cancelAll() {
        NotificationManagerCompat.from(context).cancelAll()
    }
}
