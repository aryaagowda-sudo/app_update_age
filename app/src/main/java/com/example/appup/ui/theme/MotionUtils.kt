package com.example.appup.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Interactive bounce-press click modifier that animates a scale compression on press
 * and springs back on release using spring physics.
 *
 * @param scaleDown Compression scale factor while pressed (default 0.96f)
 * @param enabled Whether this clickable interaction is active
 * @param onClick Callback triggered upon completed tap
 */
fun Modifier.bounceClickable(
    scaleDown: Float = 0.96f,
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val scale = remember { Animatable(1f) }
    val springSpec = remember {
        spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    }

    this
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
        .pointerInput(enabled) {
            if (!enabled) return@pointerInput
            detectTapGestures(
                onPress = {
                    coroutineScope {
                        launch {
                            scale.animateTo(scaleDown, springSpec)
                        }
                        try {
                            tryAwaitRelease()
                        } finally {
                            withContext(NonCancellable) {
                                scale.animateTo(1f, springSpec)
                            }
                        }
                    }
                },
                onTap = {
                    onClick()
                }
            )
        }
}

/**
 * Overload of [bounceClickable] without [enabled] for concise usage.
 */
fun Modifier.bounceClickable(
    scaleDown: Float = 0.96f,
    onClick: () -> Unit
): Modifier = bounceClickable(
    scaleDown = scaleDown,
    enabled = true,
    onClick = onClick
)

/**
 * Animated linear gradient brush across CyberSurface and CyberPrimary for skeleton cards.
 *
 * @param targetValue The travel distance in pixels for the shimmer animation
 * @param colors The gradient color stops used for the sweep
 */
@Composable
fun shimmerBrush(
    targetValue: Float = 1300f,
    colors: List<Color> = listOf(
        CyberSurface,
        CyberSurfaceElevated,
        CyberPrimary.copy(alpha = 0.28f),
        CyberSurfaceElevated,
        CyberSurface
    )
): Brush {
    val transition = rememberInfiniteTransition(label = "CyberShimmerTransition")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1300,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "CyberShimmerTranslate"
    )

    return Brush.linearGradient(
        colors = colors,
        start = Offset(x = translateAnimation - 450f, y = translateAnimation - 450f),
        end = Offset(x = translateAnimation, y = translateAnimation)
    )
}
