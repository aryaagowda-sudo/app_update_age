package com.example.appup.notification

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.appup.MainActivity
import com.example.appup.R
import java.util.concurrent.atomic.AtomicInteger

class NotificationHelper(private val context: Context) {

    companion object {
        const val TAG = "NotificationHelper"
        const val CHANNEL_HEALTH_ALERTS = "appguard_health_alerts"
        const val CHANNEL_SECURITY_UPDATES = "appguard_security_updates"

        private const val PREFS_NAME = "appguard_notification_prefs"
        private const val KEY_FCM_TOKEN = "key_fcm_token"

        private val notificationIdCounter = AtomicInteger(1000)

        fun nextNotificationId(): Int = notificationIdCounter.incrementAndGet()
    }

    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Initializes notification channels for Android 8.0+ (API 26+).
     * Creates:
     *  - 'appguard_health_alerts' (High importance, lights, vibration)
     *  - 'appguard_security_updates' (High importance, lights, vibration)
     */
    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val healthChannel = NotificationChannel(
                CHANNEL_HEALTH_ALERTS,
                "App Health Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority alerts for app health drops, dormant apps, and system performance anomalies"
                enableLights(true)
                lightColor = Color.YELLOW
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200, 100, 200)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            val securityChannel = NotificationChannel(
                CHANNEL_SECURITY_UPDATES,
                "Security & Update Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical alerts for security patches, outdated target SDK warnings, and update advisories"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 150, 300)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            notificationManager.createNotificationChannel(healthChannel)
            notificationManager.createNotificationChannel(securityChannel)
            Log.d(TAG, "Notification channels initialized: $CHANNEL_HEALTH_ALERTS, $CHANNEL_SECURITY_UPDATES")
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
    fun requestNotificationPermission(activity: Activity, requestCode: Int = 1001) {
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
     * Posts a local or push health alert notification.
     */
    fun postHealthAlert(
        title: String,
        message: String,
        notificationId: Int = nextNotificationId(),
        appName: String? = null,
        packageName: String? = null,
        extras: Map<String, String> = emptyMap()
    ): Boolean {
        return postHeadsUpAlert(
            channelId = CHANNEL_HEALTH_ALERTS,
            title = title,
            message = message,
            notificationId = notificationId,
            appName = appName,
            packageName = packageName,
            extras = extras
        )
    }

    /**
     * Posts a local or push security & update alert notification.
     */
    fun postSecurityUpdateAlert(
        title: String,
        message: String,
        notificationId: Int = nextNotificationId(),
        appName: String? = null,
        packageName: String? = null,
        extras: Map<String, String> = emptyMap()
    ): Boolean {
        return postHeadsUpAlert(
            channelId = CHANNEL_SECURITY_UPDATES,
            title = title,
            message = message,
            notificationId = notificationId,
            appName = appName,
            packageName = packageName,
            extras = extras
        )
    }

    /**
     * Posts a high-priority heads-up notification with custom payload details.
     */
    fun postHeadsUpAlert(
        channelId: String,
        title: String,
        message: String,
        notificationId: Int = nextNotificationId(),
        appName: String? = null,
        packageName: String? = null,
        extras: Map<String, String> = emptyMap()
    ): Boolean {
        if (!hasNotificationPermission()) {
            Log.w(TAG, "Cannot post notification: POST_NOTIFICATIONS permission not granted")
            return false
        }

        // Ensure channels are created
        createNotificationChannels()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("notification_id", notificationId)
            putExtra("channel_id", channelId)
            if (appName != null) putExtra("app_name", appName)
            if (packageName != null) putExtra("package_name", packageName)
            extras.forEach { (k, v) -> putExtra(k, v) }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val validChannelId = when (channelId) {
            CHANNEL_SECURITY_UPDATES -> CHANNEL_SECURITY_UPDATES
            else -> CHANNEL_HEALTH_ALERTS
        }

        val notification = NotificationCompat.Builder(context, validChannelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message).setBigContentTitle(title))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(
                if (validChannelId == CHANNEL_SECURITY_UPDATES)
                    NotificationCompat.CATEGORY_ALARM
                else
                    NotificationCompat.CATEGORY_RECOMMENDATION
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            Log.i(TAG, "Notification posted successfully [id=$notificationId, channel=$validChannelId, title='$title']")
            return true
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while notifying: ${e.message}")
            return false
        }
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

    /**
     * Persists FCM registration token in SharedPreferences.
     */
    fun saveFcmToken(token: String) {
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
        Log.d(TAG, "FCM token saved successfully")
    }

    /**
     * Retrieves the stored FCM registration token.
     */
    fun getFcmToken(): String? {
        return prefs.getString(KEY_FCM_TOKEN, null)
    }
}
