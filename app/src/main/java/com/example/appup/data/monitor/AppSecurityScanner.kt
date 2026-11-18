package com.example.appup.data.monitor

import android.Manifest
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import com.example.appup.data.analysis.AppAnalysisEngine
import com.example.appup.data.model.AppHealthSummary
import com.example.appup.data.model.AppRiskLevel
import com.example.appup.data.model.DeviceHealth
import com.example.appup.data.model.HealthIssue
import com.example.appup.data.model.IssueSeverity
import com.example.appup.data.model.OverallHealthStatus
import com.example.appup.data.model.SensitivePermission
import com.example.appup.data.model.SystemHealthAudit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppSecurityScanner(private val context: Context) {

    private val sensitivePermissionMap = mapOf(
        Manifest.permission.ACCESS_FINE_LOCATION to Triple(
            "Precise Location",
            "Pinpoints exact physical GPS location",
            "Location"
        ),
        Manifest.permission.ACCESS_COARSE_LOCATION to Triple(
            "Approximate Location",
            "Accesses network-derived location",
            "Location"
        ),
        Manifest.permission.RECORD_AUDIO to Triple(
            "Microphone Access",
            "Can capture audio and eavesdrop on surroundings",
            "Camera/Mic"
        ),
        Manifest.permission.CAMERA to Triple(
            "Camera Access",
            "Can capture photos and record video",
            "Camera/Mic"
        ),
        Manifest.permission.READ_CONTACTS to Triple(
            "Read Contacts",
            "Accesses private contact lists and addresses",
            "Privacy"
        ),
        Manifest.permission.READ_SMS to Triple(
            "Read SMS",
            "Accesses SMS messages and potential 2FA codes",
            "Privacy"
        ),
        Manifest.permission.SEND_SMS to Triple(
            "Send SMS",
            "Can send outgoing SMS messages silently",
            "Privacy"
        ),
        Manifest.permission.CALL_PHONE to Triple(
            "Direct Call",
            "Can initiate telephone calls without user prompt",
            "Privacy"
        ),
        Manifest.permission.READ_CALL_LOG to Triple(
            "Call History",
            "Reads incoming and outgoing call log records",
            "Privacy"
        ),
        Manifest.permission.READ_EXTERNAL_STORAGE to Triple(
            "Read Storage",
            "Reads photos, documents and stored files",
            "Storage"
        ),
        Manifest.permission.WRITE_EXTERNAL_STORAGE to Triple(
            "Modify Storage",
            "Can write, overwrite, or delete local files",
            "Storage"
        )
    )

    fun scanInstalledApps(): List<AppHealthSummary> {
        val pm = context.packageManager

        // Retrieve installed applications visible to this app
        val packages: List<PackageInfo> = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()))
            } else {
                @Suppress("DEPRECATION")
                pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
            }
        } catch (e: Exception) {
            emptyList()
        }

        if (packages.isEmpty()) {
            return emptyList()
        }

        val summaries = mutableListOf<AppHealthSummary>()
        val currentTime = System.currentTimeMillis()

        for (pkg in packages) {
            try {
                val appInfo = pkg.applicationInfo ?: continue
                val packageName = pkg.packageName ?: continue

                // 2. Application Name
                val appName = try {
                    appInfo.loadLabel(pm)?.toString()?.takeIf { it.isNotBlank() } ?: packageName
                } catch (_: Exception) {
                    packageName
                }

                // 3. Application Icon
                val icon: Drawable? = try {
                    appInfo.loadIcon(pm) ?: pm.defaultActivityIcon
                } catch (_: Exception) {
                    try {
                        pm.defaultActivityIcon
                    } catch (_: Exception) {
                        null
                    }
                }

                // 5. Version Name and Version Code
                val versionName = try {
                    pkg.versionName ?: "1.0"
                } catch (_: Exception) {
                    "1.0"
                }

                val versionCode = try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        pkg.longVersionCode
                    } else {
                        @Suppress("DEPRECATION")
                        pkg.versionCode.toLong()
                    }
                } catch (_: Exception) {
                    0L
                }

                // 6. First installation time
                val firstInstallTime = pkg.firstInstallTime

                // 7. Last update time using PackageInfo.lastUpdateTime
                val lastUpdateTime = pkg.lastUpdateTime

                // 8. Convert timestamps into readable dates
                val firstInstallDateFormatted = formatTimestamp(firstInstallTime)
                val lastUpdateDateFormatted = formatTimestamp(lastUpdateTime)

                // 9. Calculate days since last update
                val daysSinceLastUpdate = if (lastUpdateTime > 0L) {
                    val diff = (currentTime - lastUpdateTime).coerceAtLeast(0L)
                    diff / (1000L * 60L * 60L * 24L)
                } else {
                    0L
                }

                val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                        (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                val isDebuggable = (appInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
                val targetSdk = appInfo.targetSdkVersion

                val requestedPermissions = pkg.requestedPermissions ?: emptyArray()
                val sensitiveList = mutableListOf<SensitivePermission>()
                val riskFactors = mutableListOf<String>()

                var baseScore = 100

                // Sensitive permissions check
                for (perm in requestedPermissions) {
                    val metadata = sensitivePermissionMap[perm]
                    if (metadata != null) {
                        val isGranted = try {
                            context.checkPermission(
                                perm,
                                android.os.Process.myPid(),
                                appInfo.uid
                            ) == PackageManager.PERMISSION_GRANTED || true
                        } catch (_: Exception) {
                            true
                        }

                        sensitiveList.add(
                            SensitivePermission(
                                permission = perm,
                                friendlyName = metadata.first,
                                description = metadata.second,
                                category = metadata.third,
                                isGranted = isGranted
                            )
                        )

                        baseScore -= 6

                        if (perm == Manifest.permission.READ_SMS || perm == Manifest.permission.SEND_SMS) {
                            baseScore -= 10
                            if (!riskFactors.contains("Has direct SMS / 2FA access")) {
                                riskFactors.add("Has direct SMS / 2FA access")
                            }
                        }
                        if (perm == Manifest.permission.RECORD_AUDIO) {
                            if (!riskFactors.contains("Microphone recording access")) {
                                riskFactors.add("Microphone recording access")
                            }
                        }
                        if (perm == Manifest.permission.ACCESS_FINE_LOCATION) {
                            if (!riskFactors.contains("Precise GPS location tracking")) {
                                riskFactors.add("Precise GPS location tracking")
                            }
                        }
                    }
                }

                // Target SDK audit
                if (targetSdk < 31) {
                    baseScore -= 15
                    riskFactors.add("Legacy target SDK (API $targetSdk) - lacks modern runtime sandboxing")
                }

                // Debuggable audit
                if (isDebuggable && !isSystemApp) {
                    baseScore -= 20
                    riskFactors.add("Debuggable build detected - vulnerable to runtime inspection")
                }

                // Dormant / un-updated app audit (e.g. apps un-updated for over a year)
                if (daysSinceLastUpdate > 365 && !isSystemApp) {
                    baseScore -= 10
                    riskFactors.add("Not updated for over a year (${daysSinceLastUpdate} days ago)")
                }

                if (sensitiveList.size >= 5) {
                    riskFactors.add("Excessive sensitive permissions (${sensitiveList.size} sensitive scopes)")
                }

                if (riskFactors.isEmpty()) {
                    riskFactors.add("Standard security profile - no unusual risk flags")
                }

                val finalScore = baseScore.coerceIn(15, 100)
                val riskLevel = when {
                    finalScore >= 80 -> AppRiskLevel.SAFE
                    finalScore >= 60 -> AppRiskLevel.MODERATE
                    else -> AppRiskLevel.HIGH_RISK
                }

                val category = AppAnalysisEngine().classifyUpdateAge(daysSinceLastUpdate)
                val appRecommendation = AppAnalysisEngine().generateAppRecommendation(appName, daysSinceLastUpdate, category)

                summaries.add(
                    AppHealthSummary(
                        packageName = packageName,
                        appName = appName,
                        versionName = versionName,
                        versionCode = versionCode,
                        firstInstallTime = firstInstallTime,
                        lastUpdateTime = lastUpdateTime,
                        firstInstallDateFormatted = firstInstallDateFormatted,
                        lastUpdateDateFormatted = lastUpdateDateFormatted,
                        daysSinceLastUpdate = daysSinceLastUpdate,
                        updateCategory = category,
                        recommendation = appRecommendation,
                        isSystemApp = isSystemApp,
                        targetSdk = targetSdk,
                        isDebuggable = isDebuggable,
                        allPermissions = requestedPermissions.toList(),
                        sensitivePermissions = sensitiveList,
                        healthScore = finalScore,
                        riskLevel = riskLevel,
                        riskFactors = riskFactors,
                        icon = icon
                    )
                )
            } catch (_: Exception) {
                // Gracefully ignore individual package inspection failures
            }
        }

        // Sort: Oldest update first (largest daysSinceLastUpdate first), followed by appName
        return summaries.sortedWith(
            compareByDescending<AppHealthSummary> { it.daysSinceLastUpdate }
                .thenBy { it.appName.lowercase() }
        )
    }

    private fun formatTimestamp(timestamp: Long): String {
        if (timestamp <= 0L) return "Unknown"
        return try {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (_: Exception) {
            "Unknown"
        }
    }

    fun computeOverallAudit(
        deviceHealth: DeviceHealth,
        apps: List<AppHealthSummary>
    ): SystemHealthAudit {
        val userApps = apps.filter { !it.isSystemApp }
        val highRiskApps = userApps.filter { it.riskLevel == AppRiskLevel.HIGH_RISK }
        val moderateRiskApps = userApps.filter { it.riskLevel == AppRiskLevel.MODERATE }
        val safeApps = userApps.filter { it.riskLevel == AppRiskLevel.SAFE }

        val issues = mutableListOf<HealthIssue>()
        val recommendations = mutableListOf<String>()

        // 1. RAM Analysis
        val ramUsedPercent = deviceHealth.ram.usedPercent
        val ramScore = (100 - (ramUsedPercent * 0.7f)).toInt().coerceIn(10, 100)

        if (deviceHealth.ram.isLowMemory || ramUsedPercent > 85) {
            issues.add(
                HealthIssue(
                    title = "High RAM Pressure (${ramUsedPercent}% Used)",
                    description = "Available memory is critically low, which may lead to app freezes and battery drain.",
                    severity = IssueSeverity.CRITICAL,
                    actionSuggestion = "Run Quick Clean to terminate dormant background processes."
                )
            )
            recommendations.add("Free up background RAM using AppGuard Quick Clean.")
        } else if (ramUsedPercent > 70) {
            issues.add(
                HealthIssue(
                    title = "Moderate Memory Usage (${ramUsedPercent}%)",
                    description = "Active apps are utilizing over 70% of available physical memory.",
                    severity = IssueSeverity.WARNING,
                    actionSuggestion = "Close heavy background tasks."
                )
            )
        }

        // 2. Storage Analysis
        val storageUsedPercent = deviceHealth.storage.usedPercent
        val storageScore = (100 - (storageUsedPercent * 0.6f)).toInt().coerceIn(15, 100)

        if (storageUsedPercent > 90) {
            issues.add(
                HealthIssue(
                    title = "Storage Nearly Full (${storageUsedPercent}%)",
                    description = "Less than 10% storage remaining. Device performance and updates will degrade.",
                    severity = IssueSeverity.CRITICAL,
                    actionSuggestion = "Clear application cache and remove unused media."
                )
            )
            recommendations.add("Clear app caches in System Settings to regain free disk space.")
        } else if (storageUsedPercent > 75) {
            issues.add(
                HealthIssue(
                    title = "Storage Over 75% Capacity",
                    description = "Storage volume is filling up.",
                    severity = IssueSeverity.INFO,
                    actionSuggestion = "Review downloaded files."
                )
            )
        }

        // 3. Battery & Thermal Analysis
        val battery = deviceHealth.battery
        var batteryScore = 95
        if (battery.temperatureCelsius > 42.0f) {
            batteryScore -= 30
            issues.add(
                HealthIssue(
                    title = "Battery Overheating (${battery.temperatureCelsius}°C)",
                    description = "Device thermal threshold exceeded. Sustained heat causes battery degradation.",
                    severity = IssueSeverity.CRITICAL,
                    actionSuggestion = "Stop gaming, fast charging, or heavy GPU workloads to cool down."
                )
            )
            recommendations.add("Allow device to cool down below 38°C to preserve battery longevity.")
        } else if (battery.temperatureCelsius > 37.0f) {
            batteryScore -= 10
            issues.add(
                HealthIssue(
                    title = "Warm Battery Temperature (${battery.temperatureCelsius}°C)",
                    description = "Device temperature is elevated.",
                    severity = IssueSeverity.INFO,
                    actionSuggestion = "Avoid intensive multi-tasking in direct sunlight."
                )
            )
        }

        // 4. App Security & Health Analysis
        val avgAppHealth = if (userApps.isNotEmpty()) {
            userApps.map { it.healthScore }.average().toInt()
        } else {
            90
        }

        if (highRiskApps.isNotEmpty()) {
            issues.add(
                HealthIssue(
                    title = "${highRiskApps.size} High-Risk Application(s) Detected",
                    description = "Apps with excessive SMS, background location, or outdated target SDKs found on device.",
                    severity = IssueSeverity.CRITICAL,
                    actionSuggestion = "Inspect risky apps in the App Inspector and revoke sensitive permissions."
                )
            )
            recommendations.add("Review ${highRiskApps.firstOrNull()?.appName ?: "risky app"} permissions in App Inspector.")
        }

        val legacyApps = userApps.filter { it.targetSdk < 31 }
        if (legacyApps.isNotEmpty()) {
            issues.add(
                HealthIssue(
                    title = "${legacyApps.size} Apps Target Outdated Android SDKs",
                    description = "These apps do not support modern privacy safeguards and scoped storage.",
                    severity = IssueSeverity.WARNING,
                    actionSuggestion = "Check Google Play Store for newer app updates."
                )
            )
            recommendations.add("Update legacy apps to maintain current security standards.")
        }

        // Overall Weighted Health Score calculation
        val overall = ((ramScore * 0.25f) + (storageScore * 0.20f) + (batteryScore * 0.20f) + (avgAppHealth * 0.35f)).toInt()
        val finalOverall = overall.coerceIn(10, 100)

        val status = when {
            finalOverall >= 85 -> OverallHealthStatus.EXCELLENT
            finalOverall >= 70 -> OverallHealthStatus.GOOD
            finalOverall >= 50 -> OverallHealthStatus.ATTENTION_NEEDED
            else -> OverallHealthStatus.CRITICAL
        }

        if (recommendations.isEmpty()) {
            recommendations.add("System is operating under optimal health parameters.")
            recommendations.add("All active applications meet recommended security baselines.")
        }

        return SystemHealthAudit(
            overallScore = finalOverall,
            status = status,
            totalAppsScanned = apps.size,
            userAppsCount = userApps.size,
            highRiskAppsCount = highRiskApps.size,
            moderateRiskAppsCount = moderateRiskApps.size,
            safeAppsCount = safeApps.size,
            issues = issues,
            quickRecommendations = recommendations
        )
    }
}
