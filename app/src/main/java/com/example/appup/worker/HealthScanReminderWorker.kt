package com.example.appup.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.appup.data.analysis.AppAnalysisEngine
import com.example.appup.data.history.ScanHistoryRepository
import com.example.appup.data.model.ScanRecord
import com.example.appup.data.monitor.AppSecurityScanner
import com.example.appup.data.monitor.SystemHealthMonitor
import com.example.appup.notifications.AppGuardNotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * HealthScanReminderWorker runs periodically in the background (e.g. daily/weekly).
 * Constrained to run when battery is not low.
 * Inspects device application health and dispatches UpdateOwl notifications or alerts
 * if unpatched or vulnerable applications are found.
 */
class HealthScanReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val TAG = "HealthScanWorker"
        const val WORK_NAME = "AppGuardHealthScanReminderWork"
        const val PREFS_NAME = "appguard_reminder_prefs"
        const val KEY_REMINDER_ENABLED = "key_reminder_enabled"
        const val KEY_SNOOZE_UNTIL = "key_snooze_until"
        const val KEY_INTERVAL_DAYS = "key_interval_days"
        const val DEFAULT_INTERVAL_DAYS = 7L

        /**
         * Builds background work constraints: Battery must not be low.
         */
        fun buildConstraints(): Constraints {
            return Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
        }

        /**
         * Schedules periodic health scan and reminder background work.
         */
        fun schedule(
            context: Context,
            intervalDays: Long = DEFAULT_INTERVAL_DAYS,
            updateExisting: Boolean = true
        ) {
            val constraints = buildConstraints()
            val periodicRequest = PeriodicWorkRequestBuilder<HealthScanReminderWorker>(
                intervalDays, TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .addTag(TAG)
                .build()

            val policy = if (updateExisting) {
                ExistingPeriodicWorkPolicy.UPDATE
            } else {
                ExistingPeriodicWorkPolicy.KEEP
            }

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                policy,
                periodicRequest
            )
            Log.d(TAG, "Scheduled periodic health scan worker every $intervalDays days (policy=$policy)")
        }

        /**
         * Cancels periodic health scan worker.
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "Cancelled periodic health scan worker")
        }

        /**
         * Enqueues an immediate one-shot scan.
         */
        fun runOnceNow(context: Context) {
            val constraints = buildConstraints()
            val oneTimeRequest = OneTimeWorkRequestBuilder<HealthScanReminderWorker>()
                .setConstraints(constraints)
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context).enqueue(oneTimeRequest)
            Log.d(TAG, "Enqueued immediate one-shot health scan worker")
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.i(TAG, "HealthScanReminderWorker started background inspection")

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Check if reminders are enabled by user
        val isReminderEnabled = prefs.getBoolean(KEY_REMINDER_ENABLED, true)
        if (!isReminderEnabled) {
            Log.d(TAG, "Health scan reminders disabled in settings. Skipping notification.")
            return@withContext Result.success()
        }

        // Check if reminders are currently snoozed
        val snoozeUntil = prefs.getLong(KEY_SNOOZE_UNTIL, 0L)
        val currentTime = System.currentTimeMillis()
        if (currentTime < snoozeUntil) {
            Log.d(TAG, "Reminders currently snoozed until $snoozeUntil. Current time: $currentTime. Skipping.")
            return@withContext Result.success()
        }

        try {
            // 1. Inspect installed apps health
            val scanner = AppSecurityScanner(context)
            val scannedApps = scanner.scanInstalledApps()

            val analysisEngine = AppAnalysisEngine()
            val report = analysisEngine.analyze(scannedApps)

            // 2. Persist scan record to local history
            val historyRepo = ScanHistoryRepository(context)
            val scanRecord = ScanRecord(
                totalApps = report.totalApps,
                healthyCount = report.healthyCount,
                agingCount = report.agingCount,
                warningCount = report.warningCount,
                attentionCount = report.attentionCount,
                healthScore = report.upToDateScore
            )
            historyRepo.saveScanRecord(scanRecord)

            val notificationHelper = AppGuardNotificationHelper(context)

            // 3. Trigger Owl reminder notification or alert if unpatched apps exist
            if (report.attentionCount > 0) {
                val oldestApp = report.sortedAppsOldestFirst.firstOrNull()

                // If severely unmaintained (> 180 days), trigger high-priority alert
                if (oldestApp != null && oldestApp.daysSinceLastUpdate >= 180) {
                    notificationHelper.dispatchUnmaintainedAppAlert(
                        appName = oldestApp.appName,
                        daysSinceUpdate = oldestApp.daysSinceLastUpdate,
                        packageName = oldestApp.packageName
                    )
                }

                // Dispatch UpdateOwl periodic reminder
                notificationHelper.dispatchOwlWeeklyReminder(
                    attentionAppsCount = report.attentionCount,
                    oldestAppName = oldestApp?.appName,
                    daysSinceUpdate = oldestApp?.daysSinceLastUpdate
                )
                Log.i(TAG, "Dispatched Owl reminder for ${report.attentionCount} attention apps")
            } else {
                Log.i(TAG, "Device health check clear. All apps are up to date.")
            }

            // 4. Check for anomalous system memory consumption
            val systemMonitor = SystemHealthMonitor(context)
            val devHealth = systemMonitor.getDeviceHealth()
            if (devHealth.ram.usedPercent >= 92 || devHealth.ram.isLowMemory) {
                notificationHelper.dispatchAlertNotification(
                    title = "System Memory Anomaly",
                    message = "RAM usage is critical at ${devHealth.ram.usedPercent}%. Open AppGuard to run memory optimization.",
                    targetTab = 0,
                    alertType = AppGuardNotificationHelper.ALERT_TYPE_MEMORY
                )
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error executing HealthScanReminderWorker: ${e.message}", e)
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
