package com.example.appup.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appup.data.analysis.AppAnalysisReport
import com.example.appup.data.model.AppHealthSummary
import com.example.appup.ui.theme.CyberBorder
import com.example.appup.ui.theme.CyberGreen
import com.example.appup.ui.theme.CyberOrange
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

@Composable
fun SecurityCoachCard(
    analysisReport: AppAnalysisReport?,
    allApps: List<AppHealthSummary>,
    onReviewApp: (AppHealthSummary) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalApps = analysisReport?.totalApps ?: allApps.size
    val attentionCount = analysisReport?.attentionCount ?: 0
    val agingCount = analysisReport?.agingCount ?: 0
    val oldestApp = analysisReport?.sortedAppsOldestFirst?.firstOrNull() ?: allApps.firstOrNull()

    var showWhyItMatters by remember { mutableStateOf(false) }
    var selectedQuestionIndex by remember { mutableStateOf<Int?>(null) }

    val coachMessage = when {
        attentionCount > 0 && oldestApp != null -> {
            "Your device needs some attention!\n\n" +
            "You have $attentionCount ${if (attentionCount == 1) "application" else "applications"} that haven't been updated in over 90 days.\n\n" +
            "Your oldest application \"${oldestApp.appName}\" hasn't been updated in ${oldestApp.daysSinceLastUpdate} days. I recommend reviewing it first."
        }
        agingCount > 0 -> {
            "Your application posture is generally stable, but you have $agingCount aging applications (31–60 days since update). Reviewing them periodically ensures optimal compatibility."
        }
        else -> {
            "Excellent maintenance! All installed applications are updated within the last 30 days. No urgent actions needed."
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        color = CyberSurface,
        border = BorderStroke(1.dp, CyberBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: Assistant Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberPrimary.copy(alpha = 0.15f))
                        .border(1.dp, CyberPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🛡️",
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "SecurityCoach",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = CyberTextPrimary
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CyberGreen.copy(alpha = 0.15f),
                            border = BorderStroke(0.5.dp, CyberGreen)
                        ) {
                            Text(
                                text = "OFFLINE ASSISTANT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = CyberGreen
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Local rule-based intelligence & advice",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CyberTextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = CyberBorder, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Speech Bubble / Advice Content
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = CyberSurfaceElevated,
                border = BorderStroke(1.dp, CyberBorder)
            ) {
                Text(
                    text = coachMessage,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = CyberTextPrimary,
                        lineHeight = 22.sp
                    ),
                    modifier = Modifier.padding(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Action Buttons: "Review Now" & "Why Does This Matter?"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (oldestApp != null) {
                    Button(
                        onClick = { onReviewApp(oldestApp) },
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberPrimary,
                            contentColor = Color(0xFF090D14)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Review Now",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                OutlinedButton(
                    onClick = { showWhyItMatters = !showWhyItMatters },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (showWhyItMatters) CyberPrimary else CyberBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (showWhyItMatters) CyberPrimary else CyberTextSecondary
                    )
                ) {
                    Text(
                        text = if (showWhyItMatters) "Hide Details" else "Why It Matters",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            // Expandable "Why Does This Matter?" Educational Section
            AnimatedVisibility(
                visible = showWhyItMatters,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF131B2B),
                        border = BorderStroke(1.dp, CyberPrimary.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Why App Recency Matters",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CyberPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "• Active Maintenance: Developers regularly publish security patches, bug fixes, and compatibility improvements for newer Android versions.\n" +
                                       "• Reduced Attack Surface: Applications that are neglected for months may contain unpatched vulnerabilities discovered after their release.\n" +
                                       "• Clarification: Update age is an indicator for review, not proof of an active exploit. Some stable utilities rarely need updates.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CyberTextSecondary,
                                    lineHeight = 19.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Predefined Interactive Questions Section
            Text(
                text = "Ask SecurityCoach:",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = CyberTextMuted
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            val questions = listOf(
                "What should I update first?",
                "Why is this application flagged?",
                "How is my health score calculated?",
                "How can I improve my app health?"
            )

            val answers = listOf(
                // 1. What should I update first?
                if (oldestApp != null) {
                    "Prioritise \"${oldestApp.appName}\" (${oldestApp.daysSinceLastUpdate} days since update), followed by any apps in the 'Needs Attention' category (90+ days without updates)."
                } else {
                    "Your applications are up to date! Check the Applications tab for complete status."
                },
                // 2. Why is this application flagged?
                "Applications are classified by days elapsed since PackageInfo.lastUpdateTime: 0–30d is Healthy, 31–60d is Aging, 61–89d is Warning, and 90+d is Needs Attention. Flagged apps simply haven't received recent maintenance.",
                // 3. How is my health score calculated?
                "Health Score = (Up-to-date Applications / Total Applications) × 100. Any application updated within the last 90 days is counted as up-to-date.",
                // 4. How can I improve my app health?
                "Open Google Play Store > Manage apps & device > 'Updates available', tap 'Update all', and consider uninstalling apps you haven't opened in several months."
            )

            questions.forEachIndexed { index, question ->
                val isSelected = selectedQuestionIndex == index
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            selectedQuestionIndex = if (isSelected) null else index
                        },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) CyberSurfaceElevated else Color.Transparent,
                    border = BorderStroke(0.5.dp, if (isSelected) CyberPrimary.copy(alpha = 0.5f) else CyberBorder.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "❓ $question",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) CyberPrimary else CyberTextPrimary
                                )
                            )
                            Text(
                                text = if (isSelected) "▲" else "▼",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = CyberTextMuted
                                )
                            )
                        }

                        if (isSelected) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = answers[index],
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CyberTextSecondary,
                                    lineHeight = 18.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
