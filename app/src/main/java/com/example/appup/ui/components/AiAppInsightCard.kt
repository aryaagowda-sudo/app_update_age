package com.example.appup.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appup.data.model.AiAppInsight
import com.example.appup.data.model.AppHealthSummary
import com.example.appup.ui.theme.CyberBackground
import com.example.appup.ui.theme.CyberBorder
import com.example.appup.ui.theme.CyberGreen
import com.example.appup.ui.theme.CyberOrange
import com.example.appup.ui.theme.CyberPrimary
import com.example.appup.ui.theme.CyberRed
import com.example.appup.ui.theme.CyberSecondary
import com.example.appup.ui.theme.CyberSurface
import com.example.appup.ui.theme.CyberSurfaceElevated
import com.example.appup.ui.theme.CyberTextMuted
import com.example.appup.ui.theme.CyberTextPrimary
import com.example.appup.ui.theme.CyberTextSecondary
import com.example.appup.ui.theme.CyberYellow

@Composable
fun AiAppInsightCard(
    app: AppHealthSummary,
    insight: AiAppInsight?,
    isLoading: Boolean,
    errorMessage: String?,
    onGenerate: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0F172A),
            Color(0xFF131D33),
            Color(0xFF181830)
        )
    )

    val borderBrush = Brush.horizontalGradient(
        colors = listOf(
            CyberPrimary,
            CyberSecondary,
            CyberPrimary
        )
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(BorderStroke(1.5.dp, borderBrush), RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFF0D1322)
    ) {
        Box(modifier = Modifier.background(gradientBrush)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with Sparkle Icon and Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyberSecondary.copy(alpha = 0.2f))
                                .border(1.dp, CyberSecondary.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            GeminiSparkleIcon(
                                modifier = Modifier.size(22.dp),
                                color = CyberPrimary
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "AI App Insight",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = CyberTextPrimary,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = CyberSecondary.copy(alpha = 0.25f),
                                    border = BorderStroke(0.5.dp, CyberSecondary)
                                ) {
                                    Text(
                                        text = "GEMINI",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = CyberPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = app.appName,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CyberTextSecondary
                                )
                            )
                        }
                    }

                    // Refresh Button (when insight exists and not loading)
                    if (insight != null && !isLoading) {
                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CyberSurfaceElevated)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Regenerate AI Insight",
                                tint = CyberPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = CyberBorder.copy(alpha = 0.6f), thickness = 1.dp)

                // Error State
                if (errorMessage != null && !isLoading) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CyberRed.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, CyberRed.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = CyberRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodySmall.copy(color = CyberRed)
                            )
                        }
                    }
                }

                // Loading State
                if (isLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = CyberPrimary,
                            modifier = Modifier.size(36.dp),
                            strokeWidth = 3.dp
                        )
                        Text(
                            text = "Analyzing ${app.appName} metadata with Gemini...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = CyberPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = "Evaluating update recency, platform compatibility & lifecycle",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CyberTextMuted
                            )
                        )
                    }
                } else if (insight != null) {
                    // Generated Insight Content
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Section 1: Overview
                        InsightSectionBox(
                            title = "UPDATE STATUS OVERVIEW",
                            titleColor = CyberPrimary
                        ) {
                            Text(
                                text = insight.overview,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = CyberTextPrimary,
                                    lineHeight = 21.sp
                                )
                            )
                        }

                        // Section 2: Why Update Age Matters (Observation)
                        InsightSectionBox(
                            title = "IMPORTANT OBSERVATION: WHY UPDATE AGE MATTERS",
                            titleColor = CyberYellow
                        ) {
                            Text(
                                text = insight.updateAgeSignificance,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = CyberTextSecondary,
                                    lineHeight = 20.sp
                                )
                            )
                        }

                        // Section 3: Attention Assessment
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CyberSurface,
                            border = BorderStroke(1.dp, CyberBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = insight.attentionAssessment,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = CyberTextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when {
                                        insight.attentionAssessment.contains("Nominal", ignoreCase = true) -> CyberGreen.copy(alpha = 0.2f)
                                        insight.attentionAssessment.contains("Low", ignoreCase = true) -> CyberYellow.copy(alpha = 0.2f)
                                        insight.attentionAssessment.contains("Moderate", ignoreCase = true) -> CyberOrange.copy(alpha = 0.2f)
                                        else -> CyberRed.copy(alpha = 0.2f)
                                    }
                                ) {
                                    Text(
                                        text = app.updateCategory.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = when {
                                                insight.attentionAssessment.contains("Nominal", ignoreCase = true) -> CyberGreen
                                                insight.attentionAssessment.contains("Low", ignoreCase = true) -> CyberYellow
                                                insight.attentionAssessment.contains("Moderate", ignoreCase = true) -> CyberOrange
                                                else -> CyberRed
                                            },
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        // Section 4: Recommended Action
                        InsightSectionBox(
                            title = "RECOMMENDED ACTION",
                            titleColor = CyberGreen
                        ) {
                            Text(
                                text = insight.recommendation,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = CyberTextPrimary,
                                    lineHeight = 20.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        // Footer: Timestamp and cache indicator
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Analysis generated: ${insight.formattedTimestamp}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = CyberTextMuted,
                                        fontSize = 11.sp
                                    )
                                )
                                Text(
                                    text = insight.modelSource,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = CyberPrimary.copy(alpha = 0.85f),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                            if (insight.isCached) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = CyberSurfaceElevated,
                                    border = BorderStroke(0.5.dp, CyberBorder)
                                ) {
                                    Text(
                                        text = "Cached",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = CyberTextSecondary,
                                            fontSize = 10.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Not yet generated: Prompt button
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Generate a tailored AI health summary and lifecycle recommendation for ${app.appName} using Google Gemini.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = CyberTextSecondary,
                                lineHeight = 20.sp
                            )
                        )

                        Button(
                            onClick = onGenerate,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberSecondary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                        ) {
                            GeminiSparkleIcon(
                                modifier = Modifier.size(18.dp),
                                color = CyberPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Generate AI Insight",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightSectionBox(
    title: String,
    titleColor: Color,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CyberSurfaceElevated.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, CyberBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = titleColor,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            content()
        }
    }
}

@Composable
fun GeminiSparkleIcon(
    modifier: Modifier = Modifier,
    color: Color = CyberPrimary
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // Draw 4-pointed Gemini star
        val path = Path().apply {
            moveTo(cx, 0f)
            quadraticTo(cx, cy, w, cy)
            quadraticTo(cx, cy, cx, h)
            quadraticTo(cx, cy, 0f, cy)
            quadraticTo(cx, cy, cx, 0f)
            close()
        }

        drawPath(
            path = path,
            color = color
        )
    }
}
