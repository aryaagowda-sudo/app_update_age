package com.example.appup.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appup.ui.theme.CyberGreen
import com.example.appup.ui.theme.CyberOrange
import com.example.appup.ui.theme.CyberPrimary
import com.example.appup.ui.theme.CyberRed
import com.example.appup.ui.theme.CyberSecondary
import com.example.appup.ui.theme.CyberYellow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Expressive emotional & activity states for the AppGuard Owl mascot.
 */
enum class OwlState {
    /**
     * Natural smooth breathing (subtle scale oscillation), periodic head tilt,
     * natural double-blinking eye animation.
     */
    IDLE_VIGILANT,

    /**
     * Happy wing-flapping, bouncing bounce animation, glowing cyber-green eyes (#00E676).
     */
    CELEBRATING,

    /**
     * Alert wing-pointing gesture, pulsing cyber-orange/red eyes (#FF9100 / #FF334B),
     * upright vigilant posture.
     */
    ALERT_WARNING,

    /**
     * Closed curved eyes, gentle slow breathing, floating 'Zzz' bubble text animation above head.
     */
    SLEEPING_SNOOZED
}

/**
 * Typealias for interoperability with OwlEmotion naming.
 */
typealias OwlEmotion = OwlState

/**
 * High-fidelity Jetpack Compose Canvas animated Owl mascot with cybersecurity styling:
 * dark feathers, electric cyan/purple accents, sharp glowing eyes, beak, wings,
 * cyber ear tufts, and playful tap interactivity.
 */
@Composable
fun AnimatedOwl(
    state: OwlState = OwlState.IDLE_VIGILANT,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    showZzz: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    // Tap reaction states (flutter, wink, hop bounce)
    var isFluttering by remember { mutableStateOf(false) }
    var isWinking by remember { mutableStateOf(false) }
    val tapHop = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    val infiniteTransition = rememberInfiniteTransition(label = "OwlTransitions")

    // 1. Natural Breathing Scale
    val breathProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    OwlState.SLEEPING_SNOOZED -> 3200
                    OwlState.ALERT_WARNING -> 1200
                    OwlState.CELEBRATING -> 700
                    OwlState.IDLE_VIGILANT -> 2200
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreathProgress"
    )

    val breathScale = when (state) {
        OwlState.SLEEPING_SNOOZED -> 0.97f + breathProgress * 0.05f
        OwlState.ALERT_WARNING -> 1.01f + breathProgress * 0.035f
        OwlState.CELEBRATING -> 0.98f + breathProgress * 0.06f
        OwlState.IDLE_VIGILANT -> 1.0f + breathProgress * 0.035f
    }

    // 2. Periodic Head Tilt (curious owl head motion)
    val headTiltProgress by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4400
                0f at 0
                -1f at 1100
                -1f at 1600
                0f at 2200
                1f at 3300
                1f at 3800
                0f at 4400
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "HeadTilt"
    )

    val headTiltDegrees = when {
        isWinking -> -3.5f
        state == OwlState.IDLE_VIGILANT -> headTiltProgress * 4.5f
        state == OwlState.SLEEPING_SNOOZED -> 3.5f // Slumped to one side
        state == OwlState.ALERT_WARNING -> headTiltProgress * 2f
        state == OwlState.CELEBRATING -> headTiltProgress * 6.5f
        else -> 0f
    }

    // 3. Natural Double-Blinking Eye Animation
    val doubleBlinkScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4200
                1f at 0
                1f at 2800
                0.08f at 2920 // Blink 1 close
                1f at 3040    // Blink 1 open
                0.08f at 3160 // Blink 2 close
                1f at 3280    // Blink 2 open
                1f at 4200    // Rest open
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "DoubleBlink"
    )

    // 4. Celebrating Bounce & Wing Flap
    val celebrateBounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -9f,
        animationSpec = infiniteRepeatable(
            animation = tween(320, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CelebrateBounce"
    )

    val celebrateWingAngle by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 38f,
        animationSpec = infiniteRepeatable(
            animation = tween(220, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CelebrateWing"
    )

    val flutterWingAngle by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 32f,
        animationSpec = infiniteRepeatable(
            animation = tween(110, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FlutterWing"
    )

    // 5. Alert Warning: Pulsing Eyes & Sonar Ring
    val alertPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AlertPulse"
    )

    val alertSonarRing by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AlertSonar"
    )

    val alertEyeColor = lerpColor(CyberOrange, CyberRed, alertPulse)

    // 6. Floating Zzz Animation
    val zzzProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ZzzProgress"
    )

    val totalBounceY = (if (state == OwlState.CELEBRATING) celebrateBounce else 0f) + tapHop.value

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onClick?.invoke()
                coroutineScope.launch {
                    launch {
                        tapHop.animateTo(-7f, tween(130, easing = FastOutSlowInEasing))
                        tapHop.animateTo(
                            0f,
                            spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    }
                    if (state == OwlState.SLEEPING_SNOOZED) {
                        isWinking = true
                        delay(1200)
                        isWinking = false
                    } else {
                        isFluttering = true
                        isWinking = true
                        delay(1300)
                        isFluttering = false
                        isWinking = false
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = totalBounceY
                    rotationZ = headTiltDegrees
                    scaleX = breathScale
                    scaleY = breathScale
                }
        ) {
            val w = this.size.width
            val h = this.size.height

            // 1. Soft Cyber Ambient Backlight Glow
            val ambientGlowColor = when (state) {
                OwlState.IDLE_VIGILANT -> CyberPrimary.copy(alpha = 0.12f)
                OwlState.CELEBRATING -> CyberGreen.copy(alpha = 0.20f)
                OwlState.ALERT_WARNING -> alertEyeColor.copy(alpha = 0.22f)
                OwlState.SLEEPING_SNOOZED -> CyberSecondary.copy(alpha = 0.08f)
            }
            drawOval(
                color = ambientGlowColor,
                topLeft = Offset(w * 0.08f, h * 0.10f),
                size = Size(w * 0.84f, h * 0.80f)
            )

            // 2. Cyber Claws / Perch
            val clawColor = Color(0xFF141C28)
            val clawTipColor = when (state) {
                OwlState.ALERT_WARNING -> alertEyeColor
                OwlState.CELEBRATING -> CyberGreen
                else -> CyberPrimary
            }
            drawRoundRect(
                color = clawColor,
                topLeft = Offset(w * 0.33f, h * 0.84f),
                size = Size(w * 0.12f, h * 0.07f),
                cornerRadius = CornerRadius(w * 0.04f, h * 0.04f)
            )
            drawCircle(
                color = clawTipColor,
                radius = w * 0.016f,
                center = Offset(w * 0.39f, h * 0.89f)
            )
            drawRoundRect(
                color = clawColor,
                topLeft = Offset(w * 0.55f, h * 0.84f),
                size = Size(w * 0.12f, h * 0.07f),
                cornerRadius = CornerRadius(w * 0.04f, h * 0.04f)
            )
            drawCircle(
                color = clawTipColor,
                radius = w * 0.016f,
                center = Offset(w * 0.61f, h * 0.89f)
            )

            // 3. Cyber Ear Tufts
            val earBaseColor = Color(0xFF17202E)
            val earTipYOffset = when (state) {
                OwlState.ALERT_WARNING -> -h * 0.02f
                OwlState.SLEEPING_SNOOZED -> h * 0.025f
                else -> 0f
            }

            // Left Ear Tuft
            val leftEarPath = Path().apply {
                moveTo(w * 0.21f, h * 0.28f)
                lineTo(w * 0.15f, h * 0.07f + earTipYOffset)
                lineTo(w * 0.37f, h * 0.17f)
                close()
            }
            drawPath(leftEarPath, color = earBaseColor)
            drawLine(
                color = if (state == OwlState.ALERT_WARNING) alertEyeColor else CyberPrimary,
                start = Offset(w * 0.21f, h * 0.28f),
                end = Offset(w * 0.15f, h * 0.07f + earTipYOffset),
                strokeWidth = w * 0.025f,
                cap = StrokeCap.Round
            )

            // Right Ear Tuft
            val rightEarPath = Path().apply {
                moveTo(w * 0.79f, h * 0.28f)
                lineTo(w * 0.85f, h * 0.07f + earTipYOffset)
                lineTo(w * 0.63f, h * 0.17f)
                close()
            }
            drawPath(rightEarPath, color = earBaseColor)
            drawLine(
                color = if (state == OwlState.ALERT_WARNING) alertEyeColor else CyberPrimary,
                start = Offset(w * 0.79f, h * 0.28f),
                end = Offset(w * 0.85f, h * 0.07f + earTipYOffset),
                strokeWidth = w * 0.025f,
                cap = StrokeCap.Round
            )

            // 4. Main Body Silhouette (Gradient dark charcoal capsule)
            val bodyBrush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF222E42),
                    Color(0xFF151D2A),
                    Color(0xFF0D121B)
                ),
                startY = h * 0.14f,
                endY = h * 0.86f
            )
            drawOval(
                brush = bodyBrush,
                topLeft = Offset(w * 0.14f, h * 0.14f),
                size = Size(w * 0.72f, h * 0.72f)
            )
            drawOval(
                color = Color(0xFF2C394E),
                topLeft = Offset(w * 0.14f, h * 0.14f),
                size = Size(w * 0.72f, h * 0.72f),
                style = Stroke(width = w * 0.018f)
            )

            // 5. Breast Feather Armor Plates (Cyber Chevrons)
            drawArc(
                color = Color(0xFF233044),
                startAngle = 10f,
                sweepAngle = 160f,
                useCenter = false,
                topLeft = Offset(w * 0.32f, h * 0.58f),
                size = Size(w * 0.36f, h * 0.16f),
                style = Stroke(width = w * 0.022f, cap = StrokeCap.Round)
            )
            drawArc(
                color = CyberPrimary.copy(alpha = 0.25f),
                startAngle = 15f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(w * 0.36f, h * 0.66f),
                size = Size(w * 0.28f, h * 0.12f),
                style = Stroke(width = w * 0.020f, cap = StrokeCap.Round)
            )
            drawArc(
                color = Color(0xFF1B2433),
                startAngle = 20f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(w * 0.40f, h * 0.73f),
                size = Size(w * 0.20f, h * 0.08f),
                style = Stroke(width = w * 0.018f, cap = StrokeCap.Round)
            )

            // 6. Forehead Cyber Node & Crest
            val nodeColor = when (state) {
                OwlState.ALERT_WARNING -> alertEyeColor
                OwlState.CELEBRATING -> CyberGreen
                else -> CyberPrimary
            }
            val crestDiamond = Path().apply {
                moveTo(w * 0.50f, h * 0.18f)
                lineTo(w * 0.53f, h * 0.22f)
                lineTo(w * 0.50f, h * 0.26f)
                lineTo(w * 0.47f, h * 0.22f)
                close()
            }
            drawPath(crestDiamond, color = Color(0xFF0F141E))
            drawPath(
                crestDiamond,
                color = nodeColor.copy(alpha = 0.5f),
                style = Stroke(width = w * 0.012f)
            )
            drawCircle(
                color = nodeColor,
                radius = w * 0.015f,
                center = Offset(w * 0.50f, h * 0.22f)
            )

            // 7. Wings (Left & Right)
            val currentLeftWingAngle = when {
                isFluttering -> flutterWingAngle
                state == OwlState.CELEBRATING -> celebrateWingAngle
                else -> 0f
            }
            val currentRightWingAngle = when {
                isFluttering -> -flutterWingAngle
                state == OwlState.CELEBRATING -> -celebrateWingAngle
                state == OwlState.ALERT_WARNING -> -42f // Alert wing-pointing gesture!
                else -> 0f
            }

            // Left Wing
            rotate(degrees = currentLeftWingAngle, pivot = Offset(w * 0.20f, h * 0.38f)) {
                val leftWingPath = Path().apply {
                    moveTo(w * 0.20f, h * 0.32f)
                    cubicTo(w * 0.05f, h * 0.42f, w * 0.05f, h * 0.65f, w * 0.20f, h * 0.75f)
                    cubicTo(w * 0.25f, h * 0.64f, w * 0.24f, h * 0.46f, w * 0.20f, h * 0.32f)
                    close()
                }
                drawPath(leftWingPath, color = Color(0xFF17202E))
                drawPath(
                    leftWingPath,
                    color = CyberSecondary.copy(alpha = 0.45f),
                    style = Stroke(width = w * 0.015f)
                )
            }

            // Right Wing
            rotate(degrees = currentRightWingAngle, pivot = Offset(w * 0.80f, h * 0.38f)) {
                val rightWingPath = Path().apply {
                    moveTo(w * 0.80f, h * 0.32f)
                    cubicTo(w * 0.95f, h * 0.42f, w * 0.95f, h * 0.65f, w * 0.80f, h * 0.75f)
                    cubicTo(w * 0.75f, h * 0.64f, w * 0.76f, h * 0.46f, w * 0.80f, h * 0.32f)
                    close()
                }
                drawPath(rightWingPath, color = Color(0xFF17202E))
                drawPath(
                    rightWingPath,
                    color = if (state == OwlState.ALERT_WARNING) alertEyeColor.copy(alpha = 0.75f) else CyberSecondary.copy(alpha = 0.45f),
                    style = Stroke(width = w * 0.015f)
                )
                // Wing tip alert accent in ALERT_WARNING
                if (state == OwlState.ALERT_WARNING) {
                    drawCircle(
                        color = alertEyeColor,
                        radius = w * 0.022f,
                        center = Offset(w * 0.80f, h * 0.75f)
                    )
                }
            }

            // 8. Cyber Eye Sockets
            val eyeRadius = w * 0.155f
            val leftEyeCenter = Offset(w * 0.345f, h * 0.42f)
            val rightEyeCenter = Offset(w * 0.655f, h * 0.42f)

            drawCircle(color = Color(0xFF090D14), radius = eyeRadius, center = leftEyeCenter)
            drawCircle(color = Color(0xFF090D14), radius = eyeRadius, center = rightEyeCenter)

            val socketRimColor = when (state) {
                OwlState.ALERT_WARNING -> alertEyeColor.copy(alpha = 0.5f)
                OwlState.CELEBRATING -> CyberGreen.copy(alpha = 0.4f)
                else -> Color(0xFF263346)
            }
            drawCircle(
                color = socketRimColor,
                radius = eyeRadius,
                center = leftEyeCenter,
                style = Stroke(width = w * 0.018f)
            )
            drawCircle(
                color = socketRimColor,
                radius = eyeRadius,
                center = rightEyeCenter,
                style = Stroke(width = w * 0.018f)
            )

            // 9. Eye Rendering & States
            when {
                // SLEEPING_SNOOZED: Peaceful closed curved eyes
                state == OwlState.SLEEPING_SNOOZED && !isWinking -> {
                    val strokeW = w * 0.032f
                    drawArc(
                        color = CyberPrimary.copy(alpha = 0.85f),
                        startAngle = 20f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(leftEyeCenter.x - eyeRadius * 0.65f, leftEyeCenter.y - eyeRadius * 0.35f),
                        size = Size(eyeRadius * 1.3f, eyeRadius * 0.8f),
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = CyberPrimary.copy(alpha = 0.85f),
                        startAngle = 20f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(rightEyeCenter.x - eyeRadius * 0.65f, rightEyeCenter.y - eyeRadius * 0.35f),
                        size = Size(eyeRadius * 1.3f, eyeRadius * 0.8f),
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                }

                // Interactive Wink
                isWinking -> {
                    val strokeW = w * 0.034f
                    // Left eye: playful closed wink arc
                    drawArc(
                        color = CyberPrimary,
                        startAngle = 195f,
                        sweepAngle = 150f,
                        useCenter = false,
                        topLeft = Offset(leftEyeCenter.x - eyeRadius * 0.65f, leftEyeCenter.y - eyeRadius * 0.25f),
                        size = Size(eyeRadius * 1.3f, eyeRadius * 0.75f),
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = w * 0.016f,
                        center = Offset(leftEyeCenter.x + eyeRadius * 0.45f, leftEyeCenter.y - eyeRadius * 0.35f)
                    )

                    // Right eye: sparkling pupil
                    val pupilR = eyeRadius * 0.60f
                    val eyeColor = if (state == OwlState.ALERT_WARNING) alertEyeColor else if (state == OwlState.CELEBRATING) CyberGreen else CyberPrimary
                    drawCircle(color = eyeColor, radius = pupilR, center = rightEyeCenter)
                    drawCircle(color = Color(0xFF090D14), radius = pupilR * 0.50f, center = rightEyeCenter)
                    drawCircle(
                        color = Color.White,
                        radius = pupilR * 0.28f,
                        center = Offset(rightEyeCenter.x + pupilR * 0.32f, rightEyeCenter.y - pupilR * 0.32f)
                    )
                }

                // CELEBRATING: Glowing cyber-green eyes (#00E676)
                state == OwlState.CELEBRATING -> {
                    val pupilR = eyeRadius * 0.64f
                    drawCircle(color = CyberGreen, radius = pupilR, center = leftEyeCenter)
                    drawCircle(color = CyberGreen, radius = pupilR, center = rightEyeCenter)

                    drawCircle(color = Color(0xFF082215), radius = pupilR * 0.50f, center = leftEyeCenter)
                    drawCircle(color = Color(0xFF082215), radius = pupilR * 0.50f, center = rightEyeCenter)

                    drawCircle(
                        color = Color.White,
                        radius = pupilR * 0.30f,
                        center = Offset(leftEyeCenter.x + pupilR * 0.30f, leftEyeCenter.y - pupilR * 0.30f)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = pupilR * 0.30f,
                        center = Offset(rightEyeCenter.x + pupilR * 0.30f, rightEyeCenter.y - pupilR * 0.30f)
                    )

                    // Happy raised brows
                    drawArc(
                        color = CyberGreen,
                        startAngle = 200f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(leftEyeCenter.x - eyeRadius * 0.8f, leftEyeCenter.y - eyeRadius * 1.05f),
                        size = Size(eyeRadius * 1.6f, eyeRadius * 0.5f),
                        style = Stroke(width = w * 0.02f, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = CyberGreen,
                        startAngle = 200f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(rightEyeCenter.x - eyeRadius * 0.8f, rightEyeCenter.y - eyeRadius * 1.05f),
                        size = Size(eyeRadius * 1.6f, eyeRadius * 0.5f),
                        style = Stroke(width = w * 0.02f, cap = StrokeCap.Round)
                    )
                }

                // ALERT_WARNING: Pulsing cyber-orange/red eyes (#FF9100 / #FF334B) + Sonar ring
                state == OwlState.ALERT_WARNING -> {
                    val pupilR = eyeRadius * 0.54f

                    // Sonar alert ring
                    val sonarRadius = eyeRadius * (0.8f + alertSonarRing * 0.7f)
                    val sonarAlpha = (1f - alertSonarRing) * 0.65f
                    drawCircle(
                        color = alertEyeColor.copy(alpha = sonarAlpha),
                        radius = sonarRadius,
                        center = leftEyeCenter,
                        style = Stroke(width = w * 0.020f)
                    )
                    drawCircle(
                        color = alertEyeColor.copy(alpha = sonarAlpha),
                        radius = sonarRadius,
                        center = rightEyeCenter,
                        style = Stroke(width = w * 0.020f)
                    )

                    drawCircle(color = alertEyeColor, radius = pupilR, center = leftEyeCenter)
                    drawCircle(color = alertEyeColor, radius = pupilR, center = rightEyeCenter)

                    drawCircle(color = Color(0xFF1E0A0E), radius = pupilR * 0.45f, center = leftEyeCenter)
                    drawCircle(color = Color(0xFF1E0A0E), radius = pupilR * 0.45f, center = rightEyeCenter)

                    // Vigilant sharp warning brows
                    drawLine(
                        color = alertEyeColor,
                        start = Offset(w * 0.20f, h * 0.30f),
                        end = Offset(w * 0.44f, h * 0.35f),
                        strokeWidth = w * 0.026f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = alertEyeColor,
                        start = Offset(w * 0.80f, h * 0.30f),
                        end = Offset(w * 0.56f, h * 0.35f),
                        strokeWidth = w * 0.026f,
                        cap = StrokeCap.Round
                    )
                }

                // IDLE_VIGILANT: Natural double-blinking eye animation with electric cyan
                else -> {
                    val pupilR = eyeRadius * 0.56f
                    val eyeHeight = pupilR * doubleBlinkScale

                    drawOval(
                        color = CyberPrimary,
                        topLeft = Offset(leftEyeCenter.x - pupilR, leftEyeCenter.y - eyeHeight),
                        size = Size(pupilR * 2, eyeHeight * 2)
                    )
                    drawOval(
                        color = CyberPrimary,
                        topLeft = Offset(rightEyeCenter.x - pupilR, rightEyeCenter.y - eyeHeight),
                        size = Size(pupilR * 2, eyeHeight * 2)
                    )

                    if (doubleBlinkScale > 0.3f) {
                        drawOval(
                            color = Color(0xFF090D14),
                            topLeft = Offset(leftEyeCenter.x - pupilR * 0.48f, leftEyeCenter.y - eyeHeight * 0.48f),
                            size = Size(pupilR * 0.96f, eyeHeight * 0.96f)
                        )
                        drawOval(
                            color = Color(0xFF090D14),
                            topLeft = Offset(rightEyeCenter.x - pupilR * 0.48f, rightEyeCenter.y - eyeHeight * 0.48f),
                            size = Size(pupilR * 0.96f, eyeHeight * 0.96f)
                        )

                        drawCircle(
                            color = Color.White,
                            radius = pupilR * 0.22f * doubleBlinkScale,
                            center = Offset(leftEyeCenter.x + pupilR * 0.30f, leftEyeCenter.y - eyeHeight * 0.30f)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = pupilR * 0.22f * doubleBlinkScale,
                            center = Offset(rightEyeCenter.x + pupilR * 0.30f, rightEyeCenter.y - eyeHeight * 0.30f)
                        )
                    }

                    // Calm cyber brows
                    drawLine(
                        color = Color(0xFF2E3D52),
                        start = Offset(w * 0.22f, h * 0.31f),
                        end = Offset(w * 0.42f, h * 0.33f),
                        strokeWidth = w * 0.020f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0xFF2E3D52),
                        start = Offset(w * 0.78f, h * 0.31f),
                        end = Offset(w * 0.58f, h * 0.33f),
                        strokeWidth = w * 0.020f,
                        cap = StrokeCap.Round
                    )
                }
            }

            // 10. Faceted Cyber Beak (Amber / Gold)
            val beakTopY = h * 0.46f
            val beakBottomY = h * 0.59f
            val beakLeftX = w * 0.43f
            val beakRightX = w * 0.57f
            val beakCenterX = w * 0.50f

            val leftBeakPath = Path().apply {
                moveTo(beakCenterX, beakTopY)
                lineTo(beakLeftX, h * 0.53f)
                lineTo(beakCenterX, beakBottomY)
                close()
            }
            drawPath(leftBeakPath, color = CyberYellow)

            val rightBeakPath = Path().apply {
                moveTo(beakCenterX, beakTopY)
                lineTo(beakRightX, h * 0.53f)
                lineTo(beakCenterX, beakBottomY)
                close()
            }
            drawPath(rightBeakPath, color = Color(0xFFFF8F00))

            drawCircle(
                color = Color(0xFFFFF9C4),
                radius = w * 0.016f,
                center = Offset(beakCenterX, beakTopY + h * 0.025f)
            )
        }

        // Floating 'Zzz' Animation Overlay for SLEEPING_SNOOZED
        if (state == OwlState.SLEEPING_SNOOZED && showZzz) {
            FloatingZzzOverlay(
                progress = zzzProgress,
                owlSize = size
            )
        }
    }
}

/**
 * Floating 'Zzz' letters with staggered drift and alpha fading.
 */
@Composable
private fun FloatingZzzOverlay(
    progress: Float,
    owlSize: Dp,
    modifier: Modifier = Modifier
) {
    val zList = listOf(
        Triple(progress, 0.75f, 0.70f),
        Triple((progress + 0.33f) % 1f, 0.95f, 0.85f),
        Triple((progress + 0.66f) % 1f, 1.20f, 1.00f)
    )

    val baseSp = (owlSize.value * 0.22f).coerceIn(8f, 18f)

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        zList.forEach { (p, sizeFactor, maxAlpha) ->
            val alpha = (sin(p * Math.PI.toFloat()) * maxAlpha).coerceIn(0f, 1f)
            val xOffset = (owlSize * 0.45f) + (owlSize * 0.35f * p)
            val yOffset = (owlSize * 0.12f) - (owlSize * 0.45f * p)

            Text(
                text = "Z",
                fontSize = (baseSp * sizeFactor).sp,
                fontWeight = FontWeight.ExtraBold,
                color = CyberPrimary.copy(alpha = alpha),
                modifier = Modifier.offset(x = xOffset, y = yOffset)
            )
        }
    }
}

/**
 * Backward compatibility wrapper delegating to AnimatedOwl.
 */
@Composable
fun OwlMascotIcon(
    size: Dp,
    modifier: Modifier = Modifier,
    state: OwlState = OwlState.IDLE_VIGILANT
) {
    AnimatedOwl(
        state = state,
        size = size,
        modifier = modifier
    )
}

/**
 * Linear color interpolation helper.
 */
private fun lerpColor(start: Color, end: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = start.red + (end.red - start.red) * f,
        green = start.green + (end.green - start.green) * f,
        blue = start.blue + (end.blue - start.blue) * f,
        alpha = start.alpha + (end.alpha - start.alpha) * f
    )
}
