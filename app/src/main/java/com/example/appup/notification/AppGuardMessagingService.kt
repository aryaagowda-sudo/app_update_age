package com.example.appup.notification

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AppGuardMessagingService : FirebaseMessagingService() {

    companion object {
        const val TAG = "AppGuardFCM"

        /**
         * Gracefully initializes FirebaseApp if google-services.json is absent or
         * if Firebase has not been initialized yet. Ensures compilation and runtime
         * execution succeed without crashes.
         */
        fun initializeFirebaseSilently(context: Context): Boolean {
            return try {
                if (FirebaseApp.getApps(context).isEmpty()) {
                    val options = FirebaseOptions.Builder()
                        .setApplicationId("1:108394829103:android:appguarddemo")
                        .setApiKey("AIzaSyAppGuardDefaultClientKeyPlaceholder")
                        .setProjectId("appguard-protection")
                        .build()
                    FirebaseApp.initializeApp(context.applicationContext, options)
                    Log.i(TAG, "Firebase initialized with fallback configuration.")
                }
                true
            } catch (e: Exception) {
                Log.w(TAG, "Graceful Firebase initialization skipped or failed: ${e.message}")
                false
            }
        }

        /**
         * Requests the current FCM registration token asynchronously, ensuring graceful
         * handling when Firebase is unconfigured or network is unavailable.
         */
        fun fetchFcmToken(context: Context, onTokenResult: (String?) -> Unit) {
            initializeFirebaseSilently(context)
            try {
                FirebaseMessaging.getInstance().token
                    .addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                            onTokenResult(null)
                            return@addOnCompleteListener
                        }
                        val token = task.result
                        Log.d(TAG, "Current FCM registration token: $token")
                        NotificationHelper(context).saveFcmToken(token)
                        onTokenResult(token)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Exception accessing FirebaseMessaging instance: ${e.message}")
                onTokenResult(null)
            }
        }
    }

    private val notificationHelper by lazy { NotificationHelper(this) }

    override fun onCreate() {
        super.onCreate()
        initializeFirebaseSilently(this)
        notificationHelper.createNotificationChannels()
    }

    /**
     * Called when a new FCM registration token is generated.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "onNewToken received: $token")
        notificationHelper.saveFcmToken(token)
    }

    /**
     * Called when an incoming FCM message is received.
     * Handles both notification payloads and data payloads (e.g. app health alerts, update reminders)
     * and triggers high-priority heads-up notifications.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "onMessageReceived from: ${remoteMessage.from}, data=${remoteMessage.data}")

        val notificationTitle = remoteMessage.notification?.title
        val notificationBody = remoteMessage.notification?.body

        // Parse payload through parser
        val parsedPayload = NotificationPayloadParser.parse(
            data = remoteMessage.data,
            notificationTitle = notificationTitle,
            notificationBody = notificationBody
        )

        Log.i(
            TAG,
            "Dispatching heads-up alert [channel=${parsedPayload.channelId}, type=${parsedPayload.alertType}, title='${parsedPayload.title}']"
        )

        notificationHelper.postHeadsUpAlert(
            channelId = parsedPayload.channelId,
            title = parsedPayload.title,
            message = parsedPayload.message,
            appName = parsedPayload.appName,
            packageName = parsedPayload.packageName,
            extras = parsedPayload.extras
        )
    }
}
