package com.example.appup.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appup.data.model.ScanRecord
import com.example.appup.ui.theme.CyberBackground
import com.example.appup.ui.theme.CyberBorder
import com.example.appup.ui.theme.CyberGreen
import com.example.appup.ui.theme.CyberGreenBg
import com.example.appup.ui.theme.CyberOrange
import com.example.appup.ui.theme.CyberPrimary
import com.example.appup.ui.theme.CyberRed
import com.example.appup.ui.theme.CyberRedBg
import com.example.appup.ui.theme.CyberSurface
import com.example.appup.ui.theme.CyberSurfaceElevated
import com.example.appup.ui.theme.CyberTextMuted
import com.example.appup.ui.theme.CyberTextPrimary
import com.example.appup.ui.theme.CyberTextSecondary
import com.example.appup.ui.theme.CyberYellow
import com.example.appup.ui.theme.CyberYellowBg

@Composable
fun HistoryScreen(
    scanHistory: List<ScanRecord>,
    onTriggerScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title Header
        item {
            Column {
                Text(
                    text = "Scan History",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = CyberTextPrimary
                    )
                )
                Text(
                    text = "Historical device scans & health score evolution",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = CyberTextSecondary
                    )
                )
            }
        }

        // Health Score Trend Line Chart
        item {
            HealthScoreLineChartCard(scanHistory = scanHistory)
        }

        // Previous Scans Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Previous Scans (${scanHistory.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CyberTextPrimary
                    )
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CyberSurfaceElevated,
                    border = BorderStroke(1.dp, CyberBorder)
                ) {
                    Text(
                        text = "Locally Persisted",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyberPrimary,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (scanHistory.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = CyberSurface,
                    border = BorderStroke(1.dp, CyberBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = CyberPrimary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Previous Scans Found",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CyberTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Scan your device to record and track app health over time.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = CyberTextSecondary
                            )
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onTriggerScan,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberPrimary,
                                contentColor = Color(0xFF090D14)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scan Device Now", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(scanHistory, key = { it.id }) { scan ->
                ScanHistoryCard(scan = scan)
            }
        }
    }
}

@Composable
fun HealthScoreLineChartCard(
    scanHistory: List<ScanRecord>,
    modifier: Modifier = Modifier
) {
    // Reverse so chronological order (oldest -> newest left-to-right) for the graph
    val chronologicalScans = scanHistory.take(10).reversed()

    Surface(
        modifier = modifier.fillMaxWidth(),
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
                Text(
                    text = "HEALTH SCORE TREND",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = CyberPrimary
                    )
                )

                if (chronologicalScans.isNotEmpty()) {
                    Text(
                        text = "Latest: ${chronologicalScans.last().healthScore}%",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CyberGreen
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (chronologicalScans.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (chronologicalScans.isEmpty()) "No scan data available" else "Perform more scans to generate a trend line.",
                        style = MaterialTheme.typography.bodySmall.copy(color = CyberTextMuted)
                    )
                }
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val padding = 24f

                    val usableWidth = width - (padding * 2)
                    val usableHeight = height - (padding * 2)

                    // Draw subtle grid lines
                    val gridColor = Color(0xFF2A3448).copy(alpha = 0.5f)
                    drawLine(gridColor, Offset(padding, padding), Offset(width - padding, padding), strokeWidth = 1f)
                    drawLine(gridColor, Offset(padding, padding + usableHeight / 2), Offset(width - padding, padding + usableHeight / 2), strokeWidth = 1f)
                    drawLine(gridColor, Offset(padding, height - padding), Offset(width - padding, height - padding), strokeWidth = 1f)

                    val points = chronologicalScans.mapIndexed { index, record ->
                        val x = padding + (index.toFloat() / (chronologicalScans.size - 1).coerceAtLeast(1)) * usableWidth
                        val normalizedScore = (record.healthScore.coerceIn(0, 100)) / 100f
                        val y = (height - padding) - (normalizedScore * usableHeight)
                        Offset(x, y)
                    }

                    // Fill Gradient below line
                    val fillPath = Path().apply {
                        moveTo(points.first().x, height - padding)
                        points.forEach { lineTo(it.x, it.y) }
                        lineTo(points.last().x, height - padding)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                CyberPrimary.copy(alpha = 0.25f),
                                Color.Transparent
                            ),
                            startY = padding,
                            endY = height - padding
                        )
                    )

                    // Draw connecting stroke
                    val strokePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }

                    drawPath(
                        path = strokePath,
                        color = CyberPrimary,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw point markers
                    points.forEach { point ->
                        drawCircle(
                            color = CyberPrimary,
                            radius = 4.dp.toPx(),
                            center = point
                        )
                        drawCircle(
                            color = CyberBackground,
                            radius = 2.dp.toPx(),
                            center = point
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScanHistoryCard(
    scan: ScanRecord,
    modifier: Modifier = Modifier
) {
    val scoreColor = when {
        scan.healthScore >= 70 -> CyberGreen
        scan.healthScore >= 40 -> CyberYellow
        else -> CyberRed
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = CyberSurface,
        border = BorderStroke(1.dp, CyberBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Card Header: Date Header & Health Score Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = scan.dateHeader,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp,
                            color = CyberTextPrimary
                        )
                    )
                    Text(
                        text = scan.formattedDateTime,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyberTextMuted
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = scoreColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, scoreColor)
                ) {
                    Text(
                        text = "Score: ${scan.healthScore}%",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = scoreColor
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = CyberBorder, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Breakdown Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ScanStatItem(label = "Total Apps", value = "${scan.totalApps}", color = CyberTextPrimary)
                ScanStatItem(label = "Healthy", value = "${scan.healthyCount}", color = CyberGreen)
                ScanStatItem(label = "Aging", value = "${scan.agingCount}", color = CyberYellow)
                ScanStatItem(label = "Attention", value = "${scan.attentionCount}", color = CyberRed)
            }
        }
    }
}

@Composable
private fun ScanStatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = CyberTextSecondary
            )
        )
    }
}
