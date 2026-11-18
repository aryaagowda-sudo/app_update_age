package com.example.appup.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appup.data.analysis.AppAnalysisReport
import com.example.appup.data.model.AppHealthSummary
import com.example.appup.data.model.AppUpdateCategory
import com.example.appup.data.model.DeviceHealth
import com.example.appup.data.model.SystemHealthAudit
import com.example.appup.ui.components.AppHealthCard
import com.example.appup.ui.components.HealthScoreGauge
import com.example.appup.ui.theme.CyberBackground
import com.example.appup.ui.theme.CyberBorder
import com.example.appup.ui.theme.CyberGreen
import com.example.appup.ui.theme.CyberGreenBg
import com.example.appup.ui.theme.CyberGreenBorder
import com.example.appup.ui.theme.CyberPrimary
import com.example.appup.ui.theme.CyberRed
import com.example.appup.ui.theme.CyberRedBg
import com.example.appup.ui.theme.CyberRedBorder
import com.example.appup.ui.theme.CyberSurface
import com.example.appup.ui.theme.CyberSurfaceElevated
import com.example.appup.ui.theme.CyberTextMuted
import com.example.appup.ui.theme.CyberTextPrimary
import com.example.appup.ui.theme.CyberTextSecondary
import com.example.appup.ui.theme.CyberYellow
import com.example.appup.ui.theme.CyberYellowBg
import com.example.appup.ui.theme.CyberYellowBorder

@Composable
fun DashboardScreen(
    audit: SystemHealthAudit?,
    deviceHealth: DeviceHealth?,
    analysisReport: AppAnalysisReport?,
    allApps: List<AppHealthSummary>,
    isScanning: Boolean = false,
    isOptimizing: Boolean = false,
    lastScanTime: String,
    onScanClick: () -> Unit,
    onOptimizeClick: () -> Unit,
    onAppClick: (AppHealthSummary) -> Unit,
    onViewAllAppsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalApps = analysisReport?.totalApps ?: allApps.size
    val healthyCount = analysisReport?.healthyCount ?: 0
    val agingCount = analysisReport?.agingCount ?: 0
    val attentionCount = analysisReport?.attentionCount ?: 0
    val upToDateScore = analysisReport?.upToDateScore ?: 0
    val upToDateApps = analysisReport?.upToDateApps ?: 0

    // Take exactly the 3 oldest updated applications dynamically
    val oldestThreeApps = (analysisReport?.sortedAppsOldestFirst ?: allApps).take(3)

    val ram = deviceHealth?.ram
    val storage = deviceHealth?.storage
    val battery = deviceHealth?.battery

    val ramUsedGb = ram?.let { String.format("%.1f", it.usedBytes.toDouble() / (1024 * 1024 * 1024)) } ?: "0"
    val ramTotalGb = ram?.let { String.format("%.1f", it.totalBytes.toDouble() / (1024 * 1024 * 1024)) } ?: "0"
    val ramPercent = ram?.usedPercent ?: 0

    val storageUsedGb = storage?.let { String.format("%.1f", it.usedBytes.toDouble() / (1024 * 1024 * 1024)) } ?: "0"
    val storageTotalGb = storage?.let { String.format("%.1f", it.totalBytes.toDouble() / (1024 * 1024 * 1024)) } ?: "0"
    val storagePercent = storage?.usedPercent ?: 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. AppGuard Logo and Brand Header
        item {
            AppGuardDashboardHeader(lastScanTime = lastScanTime)
        }

        // 2. Circular Overall Health Score Indicator
        item {
            HealthScoreGauge(
                audit = audit,
                lastScanTime = lastScanTime
            )
        }

        // 3. Applications Scanned Summary Card
        item {
            ApplicationsScannedSummaryCard(
                totalApps = totalApps,
                upToDateApps = upToDateApps,
                upToDateScore = upToDateScore
            )
        }

        // 4. Three Status Indicator Cards (Green: Healthy, Yellow: Aging, Red: Attention)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatusMetricCard(
                    title = "Healthy",
                    subtitle = "0–30 days",
                    count = healthyCount,
                    accentColor = CyberGreen,
                    bgColor = CyberGreenBg,
                    borderColor = CyberGreenBorder,
                    modifier = Modifier.weight(1f)
                )

                StatusMetricCard(
                    title = "Aging",
                    subtitle = "31–60 days",
                    count = agingCount,
                    accentColor = CyberYellow,
                    bgColor = CyberYellowBg,
                    borderColor = CyberYellowBorder,
                    modifier = Modifier.weight(1f)
                )

                StatusMetricCard(
                    title = "Attention",
                    subtitle = "90+ days",
                    count = attentionCount,
                    accentColor = CyberRed,
                    bgColor = CyberRedBg,
                    borderColor = CyberRedBorder,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 5. Scan Device Action Button
        item {
            Button(
                onClick = onScanClick,
                enabled = !isScanning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberPrimary,
                    contentColor = Color(0xFF090D14)
                ),
                border = BorderStroke(1.dp, Color(0xFF80F2FF))
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF090D14),
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Scanning Installed Applications...",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF090D14)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Scan Device",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }
        }

        // 6. Three Oldest Updated Applications
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Three Oldest Updated Applications",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )
                    )
                    Text(
                        text = "High priority applications requiring maintenance review",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CyberTextSecondary
                        )
                    )
                }

                TextButton(onClick = onViewAllAppsClick) {
                    Text(
                        text = "View All",
                        color = CyberPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        if (oldestThreeApps.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CyberSurface,
                    border = BorderStroke(1.dp, CyberBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No applications detected on device.",
                            color = CyberTextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            items(oldestThreeApps) { app ->
                AppHealthCard(
                    app = app,
                    onClick = { onAppClick(app) }
                )
            }
        }

        // 7. Rule-Based Recommendations Card
        if (analysisReport != null && analysisReport.globalRecommendations.isNotEmpty()) {
            item {
                Text(
                    text = "Security & Maintenance Recommendations",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CyberTextPrimary
                    )
                )
            }

            items(analysisReport.globalRecommendations) { rec ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CyberSurface,
                    border = BorderStroke(1.dp, CyberBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF042436)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = CyberPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = rec,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = CyberTextPrimary,
                                fontWeight = FontWeight.Normal
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 8. Device Hardware Telemetry (RAM, Disk, Battery)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    color = CyberSurface,
                    border = BorderStroke(1.dp, CyberBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "RAM Memory", style = MaterialTheme.typography.labelSmall, color = CyberTextSecondary)
                            Icon(Icons.Default.Settings, contentDescription = null, tint = CyberPrimary, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "$ramPercent%", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = CyberTextPrimary))
                        Text(text = "$ramUsedGb / $ramTotalGb GB", style = MaterialTheme.typography.bodySmall, color = CyberTextMuted)
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    color = CyberSurface,
                    border = BorderStroke(1.dp, CyberBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Internal Disk", style = MaterialTheme.typography.labelSmall, color = CyberTextSecondary)
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = CyberYellow, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "$storagePercent%", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = CyberTextPrimary))
                        Text(text = "$storageUsedGb / $storageTotalGb GB", style = MaterialTheme.typography.bodySmall, color = CyberTextMuted)
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    color = CyberSurface,
                    border = BorderStroke(1.dp, CyberBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Battery", style = MaterialTheme.typography.labelSmall, color = CyberTextSecondary)
                            Icon(Icons.Default.Info, contentDescription = null, tint = CyberGreen, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "${battery?.percentage ?: 0}%", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = CyberTextPrimary))
                        Text(text = "${battery?.temperatureCelsius ?: 0}°C", style = MaterialTheme.typography.bodySmall, color = CyberTextMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun AppGuardDashboardHeader(lastScanTime: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CyberSurface,
        border = BorderStroke(1.dp, CyberBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF042436))
                        .border(1.dp, Color(0xFF006B99), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "AppGuard Logo",
                        tint = CyberPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "AppGuard",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = CyberTextPrimary,
                            letterSpacing = (-0.5).sp
                        )
                    )
                    Text(
                        text = "CYBERSECURITY HEALTH MONITOR",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberPrimary,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF06331E))
                    .border(1.dp, Color(0xFF008940), CircleShape)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(CyberGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ACTIVE",
                        color = CyberGreen,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 9.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ApplicationsScannedSummaryCard(
    totalApps: Int,
    upToDateApps: Int,
    upToDateScore: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CyberSurface,
        border = BorderStroke(1.dp, CyberBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$totalApps Applications Scanned",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )
                    )
                    Text(
                        text = "$upToDateApps of $totalApps apps updated within 90 days",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CyberTextSecondary
                        )
                    )
                }

                Text(
                    text = "$upToDateScore%",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (upToDateScore >= 70) CyberGreen else CyberYellow
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { (upToDateScore / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (upToDateScore >= 70) CyberGreen else CyberYellow,
                trackColor = Color(0xFF1B2230),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun StatusMetricCard(
    title: String,
    subtitle: String,
    count: Int,
    accentColor: Color,
    bgColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = CyberSurface,
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = accentColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = CyberTextPrimary,
                    fontSize = 13.sp
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = CyberTextMuted,
                    fontSize = 10.sp
                )
            )
        }
    }
}
