package com.example.appup.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appup.ui.theme.CyberBorder
import com.example.appup.ui.theme.CyberPrimary
import com.example.appup.ui.theme.CyberPrimaryDark
import com.example.appup.ui.theme.CyberTextMuted
import com.example.appup.ui.theme.CyberTextPrimary
import com.example.appup.ui.theme.bounceClickable

private data class CyberNavItem(
    val title: String,
    val icon: ImageVector
)

private val NAV_ITEMS = listOf(
    CyberNavItem("Home", Icons.Default.Home),
    CyberNavItem("Applications", Icons.Default.Search),
    CyberNavItem("Insights", Icons.Default.Info),
    CyberNavItem("Analytics", Icons.Default.Refresh)
)

/**
 * CyberFloatingNavBar - A floating glassmorphic navigation dock with capsule styling,
 * animated sliding glowing indicator, and spring physics.
 *
 * @param selectedTab Current active tab index (0 to 3)
 * @param onTabSelected Callback invoked when a tab is selected
 * @param modifier Custom modifier for external layout adjustments
 */
@Composable
fun CyberFloatingNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(28.dp),
                    spotColor = CyberPrimary.copy(alpha = 0.25f),
                    ambientColor = Color.Black
                ),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xEE131822),
            border = BorderStroke(1.dp, CyberBorder.copy(alpha = 0.7f))
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            ) {
                val totalWidth = maxWidth
                val tabWidth = totalWidth / NAV_ITEMS.size

                // Animated sliding glowing pill indicator with spring physics
                val indicatorOffset by animateDpAsState(
                    targetValue = tabWidth * selectedTab.coerceIn(0, NAV_ITEMS.size - 1),
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "CyberNavIndicatorOffset"
                )

                // Glowing Active Pill Indicator
                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffset)
                        .width(tabWidth)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    CyberPrimary.copy(alpha = 0.22f),
                                    CyberPrimaryDark.copy(alpha = 0.08f)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    CyberPrimary.copy(alpha = 0.65f),
                                    CyberPrimary.copy(alpha = 0.15f)
                                )
                            ),
                            shape = RoundedCornerShape(22.dp)
                        )
                ) {
                    // Subtle glowing accent line at the bottom of the active pill
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 3.dp)
                            .size(width = 16.dp, height = 2.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        CyberPrimary.copy(alpha = 0.3f),
                                        CyberPrimary,
                                        CyberPrimary.copy(alpha = 0.3f)
                                    )
                                ),
                                shape = RoundedCornerShape(1.dp)
                            )
                    )
                }

                // Navigation Items Row
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NAV_ITEMS.forEachIndexed { index, item ->
                        val isSelected = selectedTab == index

                        // Selected icon animated scale bounce (1.15f)
                        val iconScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.15f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "NavIconScale_$index"
                        )

                        val iconColor by animateColorAsState(
                            targetValue = if (isSelected) CyberPrimary else CyberTextMuted,
                            animationSpec = tween(durationMillis = 180),
                            label = "NavIconColor_$index"
                        )

                        val textColor by animateColorAsState(
                            targetValue = if (isSelected) CyberTextPrimary else CyberTextMuted,
                            animationSpec = tween(durationMillis = 180),
                            label = "NavTextColor_$index"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(22.dp))
                                .bounceClickable(scaleDown = 0.94f) {
                                    onTabSelected(index)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .graphicsLayer {
                                            scaleX = iconScale
                                            scaleY = iconScale
                                        },
                                    tint = iconColor
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        letterSpacing = 0.2.sp
                                    ),
                                    color = textColor,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
