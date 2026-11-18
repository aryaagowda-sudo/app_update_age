package com.example.appup.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appup.data.model.OverallHealthStatus
import com.example.appup.data.model.SystemHealthAudit
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
import com.example.appup.ui.theme.CyberTextMuted
import com.example.appup.ui.theme.CyberTextPrimary
import com.example.appup.ui.theme.CyberTextSecondary
import com.example.appup.ui.theme.CyberYellow
import com.example.appup.ui.theme.CyberYellowBg
import com.example.appup.ui.theme.CyberYellowBorder

@Composable
fun HealthScoreGauge(
    audit: SystemHealthAudit?,
    lastScanTime: String,
    modifier: Modifier = Modifier
) {
    val score = audit?.overallScore ?: 0
    val status = audit?.status ?: OverallHealthStatus.GOOD

    val targetProgress = (score.toFloat() / 100f).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "scoreProgress"
    )

    val animatedScore by androidx.compose.animation.core.animateIntAsState(
        targetValue = score,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "scoreInt"
    )

    val (scoreColor, statusText, statusBg, statusBorder) = when (status) {
        OverallHealthStatus.EXCELLENT -> Quadruple(
            CyberGreen,
            "SYSTEM SECURE",
            CyberGreenBg,
            CyberGreenBorder
        )
        OverallHealthStatus.GOOD -> Quadruple(
            CyberPrimary,
            "OPTIMAL HEALTH",
            Color(0xFF042436),
            Color(0xFF006B99)
        )
        OverallHealthStatus.ATTENTION_NEEDED -> Quadruple(
            CyberYellow,
            "ATTENTION NEEDED",
            CyberYellowBg,
            CyberYellowBorder
        )
        OverallHealthStatus.CRITICAL -> Quadruple(
            CyberRed,
            "SECURITY ALERT",
            CyberRedBg,
            CyberRedBorder
        )
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = CyberSurface,
        border = BorderStroke(1.dp, CyberBorder),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Circular Progress Score Indicator
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(116.dp)
            ) {
                // Background track in dark charcoal
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(116.dp),
                    color = Color(0xFF1B2230),
                    strokeWidth = 10.dp,
                    strokeCap = StrokeCap.Round
                )
                // Active cybersecurity animated progress
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(116.dp),
                    color = scoreColor,
                    strokeWidth = 10.dp,
                    strokeCap = StrokeCap.Round
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$animatedScore",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 34.sp,
                            color = CyberTextPrimary,
                            letterSpacing = (-1).sp
                        )
                    )
                    Text(
                        text = "HEALTH SCORE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = CyberTextMuted,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(18.dp))

            // Details and badge
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(statusBg)
                        .border(1.dp, statusBorder, CircleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        color = scoreColor,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "AppGuard Health Index",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CyberTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${audit?.totalAppsScanned ?: 0} applications audited",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = CyberTextSecondary
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Last evaluated: $lastScanTime",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CyberTextMuted,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
