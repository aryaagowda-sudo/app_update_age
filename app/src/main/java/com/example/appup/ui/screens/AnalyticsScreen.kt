package com.example.appup.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appup.data.analysis.AppAnalysisReport
import com.example.appup.data.model.AppHealthSummary
import com.example.appup.data.model.ScanRecord
import com.example.appup.ui.theme.CyberBackground
import com.example.appup.ui.theme.CyberBorder
import com.example.appup.ui.theme.CyberGreen
import com.example.appup.ui.theme.CyberGreenBg
import com.example.appup.ui.theme.CyberOrange
import com.example.appup.ui.theme.CyberOrangeBg
import com.example.appup.ui.theme.CyberPrimary
import com.example.appup.ui.theme.CyberRed
import com.example.appup.ui.theme.CyberRedBg
import com.example.appup.ui.theme.CyberSurface
import com.example.appup.ui.theme.CyberSurfaceElevated
import com.example.appup.ui.theme.CyberTextMuted
import com.example.appup.ui.theme.CyberTextPrimary
import com.example.appup.ui.theme.CyberTextSecondary
import com.example.appup.ui.theme.CyberYellow
import com.example.appup.ui.theme.CyberYellowBg

@Composable
fun AnalyticsScreen(
    analysisReport: AppAnalysisReport?,
    allApps: List<AppHealthSummary>,
    scanHistory: List<ScanRecord>,
    lastScanTime: String,
    onTriggerScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalApps = analysisReport?.totalApps ?: allApps.size
    val healthyCount = analysisReport?.healthyCount ?: 0
    val agingCount = analysisReport?.agingCount ?: 0
    val warningCount = analysisReport?.warningCount ?: 0
    val attentionCount = analysisReport?.attentionCount ?: 0
    val healthScore = analysisReport?.upToDateScore ?: 0

    // Compare latest two scans for health trends
    val latestScan = scanHistory.firstOrNull()
    val previousScan = scanHistory.getOrNull(1)

    val scoreDelta = if (latestScan != null && previousScan != null) {
        latestScan.healthScore - previousScan.healthScore
    } else null

    val attentionDelta = if (latestScan != null && previousScan != null) {
        latestScan.attentionCount - previousScan.attentionCount
    } else null

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Title Header
        item {
            Column {
                Text(
                    text = "App Health Analytics",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = CyberTextPrimary
                    )
                )
                Text(
                    text = "Historical trends & maintenance progression",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = CyberTextSecondary
                    )
                )
            }
        }

        // 2. Current Health Summary Card
        item {
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
                                text = "CURRENT AUDIT METRICS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = CyberPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Score: $healthScore%",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (healthScore >= 70) CyberGreen else if (healthScore >= 40) CyberYellow else CyberRed
                                )
                            )
                            Text(
                                text = "Last Evaluated: $lastScanTime",
                                style = MaterialTheme.typography.bodySmall.copy(color = CyberTextMuted)
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = CyberSurfaceElevated,
                            border = BorderStroke(1.dp, CyberBorder)
                        ) {
                            Text(
                                text = "$totalApps Apps",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTextPrimary
                                ),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = CyberBorder, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // 4-Column Stat Breakdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AnalyticsStatBadge("Healthy", "$healthyCount", "0–30d", CyberGreen)
                        AnalyticsStatBadge("Aging", "$agingCount", "31–60d", CyberYellow)
                        AnalyticsStatBadge("Warning", "$warningCount", "61–89d", CyberOrange)
                        AnalyticsStatBadge("Attention", "$attentionCount", "90+d", CyberRed)
                    }
                }
            }
        }

        // 3. Scan Trend Line Chart
        item {
            HealthScoreLineChartCard(scanHistory = scanHistory)
        }

        // 4. Historical Comparison Card (if 2+ scans exist)
        if (scoreDelta != null || attentionDelta != null) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = CyberSurface,
                    border = BorderStroke(1.dp, CyberBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "HISTORICAL COMPARISON (vs Previous Scan)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = CyberPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Score Change Badge
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = CyberSurfaceElevated,
                                border = BorderStroke(0.5.dp, CyberBorder)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Health Score", style = MaterialTheme.typography.labelSmall, color = CyberTextMuted)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = when {
                                            scoreDelta == null -> "--"
                                            scoreDelta > 0 -> "+$scoreDelta% Improved"
                                            scoreDelta < 0 -> "$scoreDelta% Decreased"
                                            else -> "0% Steady"
                                        },
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                (scoreDelta ?: 0) > 0 -> CyberGreen
                                                (scoreDelta ?: 0) < 0 -> CyberRed
                                                else -> CyberPrimary
                                            }
                                        )
                                    )
                                }
                            }

                            // Outdated Apps Change Badge
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = CyberSurfaceElevated,
                                border = BorderStroke(0.5.dp, CyberBorder)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Outdated Apps (90+d)", style = MaterialTheme.typography.labelSmall, color = CyberTextMuted)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = when {
                                            attentionDelta == null -> "--"
                                            attentionDelta < 0 -> "$attentionDelta (Reduced)"
                                            attentionDelta > 0 -> "+$attentionDelta (Added)"
                                            else -> "Unchanged"
                                        },
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                (attentionDelta ?: 0) < 0 -> CyberGreen
                                                (attentionDelta ?: 0) > 0 -> CyberOrange
                                                else -> CyberTextSecondary
                                            }
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Previous Scans Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scan History Records (${scanHistory.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CyberTextPrimary
                    )
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CyberSurfaceElevated,
                    border = BorderStroke(1.dp, CyberBorder)
                ) {
                    Text(
                        text = "Locally Saved",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyberPrimary,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (scanHistory.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = CyberSurface,
                    border = BorderStroke(1.dp, CyberBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = CyberPrimary,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No Previous Scans Found",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CyberTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Scan your device to record app health trajectories.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = CyberTextSecondary
                            )
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = onTriggerScan,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberPrimary,
                                contentColor = Color(0xFF090D14)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scan Device Now", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(scanHistory, key = { it.id }) { scan ->
                ScanHistoryCard(scan = scan)
            }
        }
    }
}

@Composable
private fun AnalyticsStatBadge(
    label: String,
    count: String,
    range: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = CyberTextPrimary
            )
        )
        Text(
            text = range,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = CyberTextMuted
            )
        )
    }
}
