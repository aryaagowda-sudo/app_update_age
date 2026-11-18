package com.example.appup.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun UpdateOwlCard(
    attentionCount: Int,
    oldestApp: AppHealthSummary?,
    onReviewNow: () -> Unit,
    onOpenSettings: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isSnoozed by remember { mutableStateOf(false) }
    var isDismissed by remember { mutableStateOf(false) }
    var unusedAppAnswer by remember { mutableStateOf<Boolean?>(null) } // null = unanswered, true = yes, false = no

    if (isDismissed) return

    val infiniteTransition = rememberInfiniteTransition(label = "OwlFloat")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatY"
    )

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
            // Header: Owl Mascot & Assistant Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .offset(y = floatOffset.dp)
                        .clip(CircleShape)
                        .background(CyberSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    OwlMascotIcon(size = 36.dp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "UpdateOwl",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = CyberTextPrimary
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CyberPrimary.copy(alpha = 0.15f),
                            border = BorderStroke(0.5.dp, CyberPrimary)
                        ) {
                            Text(
                                text = "Smart Reminder",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = CyberPrimary
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = if (isSnoozed) "Reminder snoozed for this session" else "Weekly health & recency monitor",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CyberTextSecondary
                        )
                    )
                }

                if (isSnoozed) {
                    TextButton(onClick = { isSnoozed = false }) {
                        Text("Wake Owl", color = CyberPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (!isSnoozed) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = CyberBorder, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Weekly Reminder Message
                val reminderText = if (attentionCount > 0) {
                    "Hey! You have $attentionCount ${if (attentionCount == 1) "application that needs" else "applications that need"} attention this week."
                } else {
                    "All your applications are currently up to date! UpdateOwl will let you know when an app reaches 90+ days without updates."
                }

                Text(
                    text = reminderText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = CyberTextPrimary,
                        lineHeight = 21.sp
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons: 1. Review Now, 2. Snooze, 3. Dismiss
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (attentionCount > 0) {
                        Button(
                            onClick = onReviewNow,
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberPrimary,
                                contentColor = Color(0xFF090D14)
                            )
                        ) {
                            Text(
                                text = "Review Now",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { isSnoozed = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CyberBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberTextSecondary)
                    ) {
                        Text("Snooze")
                    }

                    OutlinedButton(
                        onClick = { isDismissed = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CyberBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberTextMuted)
                    ) {
                        Text("Dismiss")
                    }
                }

                // Unused Application Prompt Section
                if (oldestApp != null && oldestApp.daysSinceLastUpdate >= 60) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = CyberSurfaceElevated,
                        border = BorderStroke(1.dp, CyberBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "🦉 UNUSED APPLICATION CHECK",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = CyberYellow
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Do you still use \"${oldestApp.appName}\"? It hasn't received an update in ${oldestApp.daysSinceLastUpdate} days.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CyberTextPrimary,
                                    lineHeight = 18.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            if (unusedAppAnswer == null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { unusedAppAnswer = true },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CyberSurface,
                                            contentColor = CyberTextPrimary
                                        ),
                                        border = BorderStroke(1.dp, CyberBorder),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Yes, I use it", style = MaterialTheme.typography.labelSmall)
                                    }

                                    Button(
                                        onClick = { unusedAppAnswer = false },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CyberRedBg,
                                            contentColor = CyberRed
                                        ),
                                        border = BorderStroke(1.dp, CyberRedBorder),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("No, I don't", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            } else if (unusedAppAnswer == true) {
                                Text(
                                    text = "✓ Great! Consider checking Google Play Store to verify it has the latest available release.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = CyberGreen,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            } else {
                                Column {
                                    Text(
                                        text = "Consider uninstalling \"${oldestApp.appName}\" if you no longer need it to save device storage and reduce attack surface.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = CyberOrange,
                                            lineHeight = 18.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = { onOpenSettings(oldestApp.packageName) },
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, CyberOrange.copy(alpha = 0.5f))
                                    ) {
                                        Text("Open Settings to Review", color = CyberOrange, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Clean vector-drawn Owl mascot with animated cyber-glowing eyes
 */
@Composable
fun OwlMascotIcon(
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OwlBlink")
    val eyeScaleY by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(120, delayMillis = 3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Blink"
    )

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Body / Head base
        drawOval(
            color = Color(0xFF1E2838),
            topLeft = Offset(w * 0.12f, h * 0.15f),
            size = Size(w * 0.76f, h * 0.72f)
        )

        // Ear tufts (feathers)
        val leftEar = Path().apply {
            moveTo(w * 0.22f, h * 0.28f)
            lineTo(w * 0.18f, h * 0.08f)
            lineTo(w * 0.38f, h * 0.18f)
            close()
        }
        drawPath(leftEar, color = Color(0xFF1E2838))

        val rightEar = Path().apply {
            moveTo(w * 0.78f, h * 0.28f)
            lineTo(w * 0.82f, h * 0.08f)
            lineTo(w * 0.62f, h * 0.18f)
            close()
        }
        drawPath(rightEar, color = Color(0xFF1E2838))

        // Big round eye frames
        val eyeRadius = w * 0.16f
        val leftEyeCenter = Offset(w * 0.35f, h * 0.44f)
        val rightEyeCenter = Offset(w * 0.65f, h * 0.44f)

        drawCircle(color = Color(0xFF0F141E), radius = eyeRadius, center = leftEyeCenter)
        drawCircle(color = Color(0xFF0F141E), radius = eyeRadius, center = rightEyeCenter)

        // Glowing cyber pupils (animated blink)
        val pupilRadius = eyeRadius * 0.55f
        drawOval(
            color = CyberPrimary,
            topLeft = Offset(leftEyeCenter.x - pupilRadius, leftEyeCenter.y - pupilRadius * eyeScaleY),
            size = Size(pupilRadius * 2, pupilRadius * 2 * eyeScaleY)
        )
        drawOval(
            color = CyberPrimary,
            topLeft = Offset(rightEyeCenter.x - pupilRadius, rightEyeCenter.y - pupilRadius * eyeScaleY),
            size = Size(pupilRadius * 2, pupilRadius * 2 * eyeScaleY)
        )

        // Cute amber beak
        val beakPath = Path().apply {
            moveTo(w * 0.5f, h * 0.48f)
            lineTo(w * 0.44f, h * 0.58f)
            lineTo(w * 0.56f, h * 0.58f)
            close()
        }
        drawPath(beakPath, color = CyberYellow)

        // Subtle breast feather accents
        drawArc(
            color = Color(0xFF2A374E),
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.38f, h * 0.68f),
            size = Size(w * 0.24f, h * 0.12f)
        )
    }
}
