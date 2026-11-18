package com.example.appup.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appup.data.model.AppHealthSummary
import com.example.appup.data.model.AppUpdateCategory
import com.example.appup.ui.theme.CyberBorder
import com.example.appup.ui.theme.CyberGreen
import com.example.appup.ui.theme.CyberGreenBg
import com.example.appup.ui.theme.CyberGreenBorder
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailBottomSheet(
    app: AppHealthSummary,
    onDismiss: () -> Unit,
    onOpenSettings: (String) -> Unit,
    onOpenPlayStore: (String) -> Unit,
    onLaunchApp: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    val (categoryColor, categoryBg, categoryBorder) = when (app.updateCategory) {
        AppUpdateCategory.HEALTHY -> Triple(CyberGreen, CyberGreenBg, CyberGreenBorder)
        AppUpdateCategory.AGING -> Triple(CyberYellow, CyberYellowBg, CyberYellowBorder)
        AppUpdateCategory.WARNING -> Triple(CyberYellow, CyberYellowBg, CyberYellowBorder)
        AppUpdateCategory.NEEDS_ATTENTION -> Triple(CyberRed, CyberRedBg, CyberRedBorder)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CyberSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header: Icon, Name, Package
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIconImage(
                    drawable = app.icon,
                    size = 56.dp
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )
                    )
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CyberTextSecondary
                        )
                    )
                    Text(
                        text = "v${app.versionName} (Build ${app.versionCode}) • Target API ${app.targetSdk}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyberTextMuted
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Update Age Classification Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = categoryBg,
                border = BorderStroke(1.dp, categoryBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Status: ${app.updateCategory.label}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = categoryColor
                            )
                        )
                        Text(
                            text = app.daysSinceLastUpdateText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = categoryColor.copy(alpha = 0.85f)
                            )
                        )
                    }

                    Text(
                        text = "${app.healthScore}/100",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = categoryColor
                        )
                    )
                }
            }

            // Recommendation Alert Card
            if (app.recommendation.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = categoryBg,
                    border = BorderStroke(1.dp, categoryBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (app.updateCategory == AppUpdateCategory.NEEDS_ATTENTION) Icons.Default.Warning else Icons.Default.Info,
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = app.recommendation,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = categoryColor
                            )
                        )
                    }
                }
            }

            // Installation & Update Timestamps Card
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CyberSurfaceElevated,
                border = BorderStroke(1.dp, CyberBorder)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "First Installed",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberTextSecondary
                        )
                        Text(
                            text = app.firstInstallDateFormatted,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = CyberTextPrimary
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Last Updated",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberTextSecondary
                        )
                        Text(
                            text = "${app.lastUpdateDateFormatted} (${app.daysSinceLastUpdate} days ago)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = CyberTextPrimary
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "App Type",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberTextSecondary
                        )
                        Text(
                            text = if (app.isSystemApp) "System Pre-installed" else "User Installed",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = CyberTextPrimary
                            )
                        )
                    }
                }
            }

            // Sensitive Permissions Section
            if (app.sensitivePermissions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Sensitive Permissions (${app.sensitivePermissions.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CyberTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    app.sensitivePermissions.forEach { perm ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CyberSurfaceElevated,
                            border = BorderStroke(1.dp, CyberBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = perm.friendlyName,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = CyberTextPrimary
                                        )
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(Color(0xFF042436))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = perm.category,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                color = CyberPrimary
                                            )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = perm.description,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = CyberTextSecondary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Action Buttons
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = CyberBorder)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onLaunchApp(app.packageName) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CyberBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Launch", fontSize = 12.sp)
                }

                Button(
                    onClick = { onOpenPlayStore(app.packageName) },
                    modifier = Modifier.weight(1.3f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberPrimary,
                        contentColor = Color(0xFF090D14)
                    )
                ) {
                    Text(text = "Play Store", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onOpenSettings(app.packageName) },
                    modifier = Modifier.weight(1.3f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberSurfaceElevated,
                        contentColor = CyberTextPrimary
                    ),
                    border = BorderStroke(1.dp, CyberBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = CyberTextPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Settings", fontSize = 12.sp)
                }
            }
        }
    }
}
