package com.example.appup.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appup.data.analysis.AppAnalysisReport
import com.example.appup.data.model.AppHealthSummary
import com.example.appup.data.model.AppUpdateCategory
import com.example.appup.data.model.ScanRecord
import com.example.appup.ui.components.AppHealthCard
import com.example.appup.ui.theme.CyberBackground
import com.example.appup.ui.theme.CyberBorder
import com.example.appup.ui.theme.CyberGreen
import com.example.appup.ui.theme.CyberGreenBg
import com.example.appup.ui.theme.CyberGreenBorder
import com.example.appup.ui.theme.CyberOrange
import com.example.appup.ui.theme.CyberOrangeBg
import com.example.appup.ui.theme.CyberOrangeBorder
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
fun InsightsScreen(
    analysisReport: AppAnalysisReport?,
    allApps: List<AppHealthSummary>,
    scanHistory: List<ScanRecord>,
    onAppClick: (AppHealthSummary) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalApps = analysisReport?.totalApps ?: allApps.size
    val healthyCount = analysisReport?.healthyCount ?: 0
    val agingCount = analysisReport?.agingCount ?: 0
    val warningCount = analysisReport?.warningCount ?: 0
    val attentionCount = analysisReport?.attentionCount ?: 0
    val healthScore = analysisReport?.upToDateScore ?: 0

    // Prioritized oldest updated applications
    val oldestApps = (analysisReport?.sortedAppsOldestFirst ?: allApps).take(5)

    // Compare with previous scan if available
    val previousScan = scanHistory.getOrNull(1)
    val scoreComparisonText = when {
        previousScan == null -> "Baseline established on this scan."
        healthScore > previousScan.healthScore -> "Health score improved by +${healthScore - previousScan.healthScore}% since previous scan."
        healthScore < previousScan.healthScore -> "Health score decreased by -${previousScan.healthScore - healthScore}% since previous scan."
        else -> "Health score remains steady compared to your previous scan."
    }

    // Dynamic rule-based recommendations list
    val recommendations = mutableListOf<String>()

    if (attentionCount > 0) {
        recommendations.add("You have $attentionCount ${if (attentionCount == 1) "application" else "applications"} requiring attention (no updates in 90+ days).")
    }

    if (agingCount > 0) {
        recommendations.add("Consider reviewing $agingCount applications that haven't received updates in several months.")
    }

    if (oldestApps.isNotEmpty()) {
        val topOldest = oldestApps.first()
        recommendations.add("\"${topOldest.appName}\" hasn't been updated in ${topOldest.daysSinceLastUpdate} days. Check its Google Play Store listing.")
    }

    recommendations.add(scoreComparisonText)

    if (healthyCount > (totalApps * 0.7)) {
        recommendations.add("Majority (${((healthyCount.toDouble() / totalApps.coerceAtLeast(1)) * 100).toInt()}%) of installed apps are actively maintained within 30 days.")
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Header
        item {
            Column {
                Text(
                    text = "Intelligent Insights",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = CyberTextPrimary
                    )
                )
                Text(
                    text = "Dynamic health analysis & maintenance recommendations",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = CyberTextSecondary
                    )
                )
            }
        }

        // Section: Health Score & Metrics Card
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
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "OVERALL HEALTH SCORE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = CyberPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$healthScore%",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (healthScore >= 70) CyberGreen else if (healthScore >= 40) CyberYellow else CyberRed
                                )
                            )
                            Text(
                                text = "Apps updated within 90 days: ${analysisReport?.upToDateApps ?: 0} / $totalApps",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CyberTextSecondary
                                )
                            )
                        }

                        Box(
                            modifier = Modifier.size(72.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { (healthScore / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxSize(),
                                color = if (healthScore >= 70) CyberGreen else if (healthScore >= 40) CyberYellow else CyberRed,
                                trackColor = CyberBorder,
                                strokeWidth = 8.dp
                            )
                            Text(
                                text = "$healthScore",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTextPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = CyberBorder, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // 4-Column Stat Breakdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InsightStatColumn("Total", "$totalApps", CyberPrimary)
                        InsightStatColumn("Healthy", "$healthyCount", CyberGreen)
                        InsightStatColumn("Aging", "$agingCount", CyberYellow)
                        InsightStatColumn("Attention", "$attentionCount", CyberRed)
                    }
                }
            }
        }

        // Section: Mandatory Security & Health Clarification Notice
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CyberSurfaceElevated,
                border = BorderStroke(1.dp, CyberBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = CyberPrimary,
                        modifier = Modifier
                            .size(22.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Important Analysis Clarification",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = CyberTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Update age is an indicator for reviewing applications, not proof of a security vulnerability or compromise. Applications that receive infrequent updates may still be stable, but checking their developer listing ensures you have the latest bug fixes and patches.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = CyberTextSecondary,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }
        }

        // Section: Rule-Based Recommendations
        item {
            Text(
                text = "Actionable Recommendations",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = CyberTextPrimary
                )
            )
        }

        items(recommendations) { rec ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = CyberSurface,
                border = BorderStroke(1.dp, CyberBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(CyberPrimary)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = rec,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = CyberTextPrimary,
                            lineHeight = 20.sp
                        )
                    )
                }
            }
        }

        // Section: Oldest Updated Applications
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Oldest Updated Applications",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CyberTextPrimary
                    )
                )
                Text(
                    text = "Prioritized",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CyberOrange,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        items(oldestApps) { app ->
            AppHealthCard(
                app = app,
                onClick = { onAppClick(app) }
            )
        }
    }
}

@Composable
private fun InsightStatColumn(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = CyberTextMuted
            )
        )
    }
}
