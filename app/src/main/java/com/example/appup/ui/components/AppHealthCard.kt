package com.example.appup.ui.components

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appup.data.model.AppHealthSummary
import com.example.appup.data.model.AppUpdateCategory
import com.example.appup.ui.theme.CyberBorder
import com.example.appup.ui.theme.CyberGreen
import com.example.appup.ui.theme.CyberGreenBg
import com.example.appup.ui.theme.CyberGreenBorder
import com.example.appup.ui.theme.CyberOrange
import com.example.appup.ui.theme.CyberOrangeBg
import com.example.appup.ui.theme.CyberOrangeBorder
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
fun AppHealthCard(
    app: AppHealthSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (categoryColor, categoryBg, categoryBorder) = when (app.updateCategory) {
        AppUpdateCategory.HEALTHY -> Triple(CyberGreen, CyberGreenBg, CyberGreenBorder)
        AppUpdateCategory.AGING -> Triple(CyberYellow, CyberYellowBg, CyberYellowBorder)
        AppUpdateCategory.WARNING -> Triple(CyberOrange, CyberOrangeBg, CyberOrangeBorder)
        AppUpdateCategory.NEEDS_ATTENTION -> Triple(CyberRed, CyberRedBg, CyberRedBorder)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = CyberSurface,
        border = BorderStroke(1.dp, CyberBorder),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon
            AppIconImage(
                drawable = app.icon,
                size = 48.dp
            )

            Spacer(modifier = Modifier.width(14.dp))

            // App Name & Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = CyberTextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (app.isSystemApp) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF1E2638))
                                .border(1.dp, Color(0xFF2C3954), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "SYS",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = CyberTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${app.packageName} • v${app.versionName}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = CyberTextSecondary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Update Age Badge & Elapsed Time
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(categoryBg)
                            .border(1.dp, categoryBorder, CircleShape)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = app.updateCategory.label,
                            color = categoryColor,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Text(
                        text = app.daysSinceLastUpdateText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            color = categoryColor,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Health Score Badge
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${app.healthScore}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = categoryColor
                    )
                )

                Text(
                    text = "score",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        color = CyberTextMuted
                    )
                )
            }
        }
    }
}
