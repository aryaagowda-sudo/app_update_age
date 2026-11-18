package com.example.appup.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appup.data.model.DeviceHealth
import com.example.appup.ui.theme.CyberBackground
import com.example.appup.ui.theme.CyberBorder
import com.example.appup.ui.theme.CyberGreen
import com.example.appup.ui.theme.CyberPrimary
import com.example.appup.ui.theme.CyberRed
import com.example.appup.ui.theme.CyberSurface
import com.example.appup.ui.theme.CyberTextMuted
import com.example.appup.ui.theme.CyberTextPrimary
import com.example.appup.ui.theme.CyberTextSecondary
import com.example.appup.ui.theme.CyberYellow

@Composable
fun SystemDiagnosticsScreen(
    deviceHealth: DeviceHealth?,
    modifier: Modifier = Modifier
) {
    if (deviceHealth == null) return

    val ram = deviceHealth.ram
    val storage = deviceHealth.storage
    val battery = deviceHealth.battery
    val network = deviceHealth.network
    val system = deviceHealth.system

    val ramTotalMb = ram.totalBytes / (1024 * 1024)
    val ramUsedMb = ram.usedBytes / (1024 * 1024)
    val ramAvailMb = ram.availableBytes / (1024 * 1024)
    val ramThresholdMb = ram.thresholdBytes / (1024 * 1024)

    val storageTotalGb = String.format("%.2f", storage.totalBytes.toDouble() / (1024 * 1024 * 1024))
    val storageUsedGb = String.format("%.2f", storage.usedBytes.toDouble() / (1024 * 1024 * 1024))
    val storageAvailGb = String.format("%.2f", storage.availableBytes.toDouble() / (1024 * 1024 * 1024))

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Memory / RAM Diagnostics
        item {
            DiagnosticSectionCard(
                title = "RAM & Memory Subsystem",
                icon = Icons.Default.Settings
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "RAM Utilization",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CyberTextSecondary
                        )
                        Text(
                            text = "${ram.usedPercent}% Used",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (ram.usedPercent > 80) CyberRed else CyberPrimary
                        )
                    }

                    LinearProgressIndicator(
                        progress = { ram.usedPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = if (ram.usedPercent > 80) CyberRed else CyberPrimary,
                        trackColor = Color(0xFF1B2230),
                        strokeCap = StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    DiagnosticRow("Total Physical RAM", "$ramTotalMb MB")
                    DiagnosticRow("Active Used Memory", "$ramUsedMb MB")
                    DiagnosticRow("Available Free Memory", "$ramAvailMb MB")
                    DiagnosticRow("Low Memory Threshold", "$ramThresholdMb MB")
                    DiagnosticRow(
                        "Kernel Memory Pressure",
                        if (ram.isLowMemory) "CRITICAL PRESSURE" else "Normal Operating State",
                        valueColor = if (ram.isLowMemory) CyberRed else CyberGreen
                    )
                }
            }
        }

        // 2. Storage Subsystem
        item {
            DiagnosticSectionCard(
                title = "Internal Storage Volume",
                icon = Icons.Default.Notifications
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Storage Capacity",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CyberTextSecondary
                        )
                        Text(
                            text = "${storage.usedPercent}% Allocated",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (storage.usedPercent > 85) CyberYellow else CyberPrimary
                        )
                    }

                    LinearProgressIndicator(
                        progress = { storage.usedPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = if (storage.usedPercent > 85) CyberYellow else CyberPrimary,
                        trackColor = Color(0xFF1B2230),
                        strokeCap = StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    DiagnosticRow("Total Device Capacity", "$storageTotalGb GB")
                    DiagnosticRow("Allocated Space", "$storageUsedGb GB")
                    DiagnosticRow("Free Available Space", "$storageAvailGb GB")
                }
            }
        }

        // 3. Battery & Thermal Diagnostics
        item {
            DiagnosticSectionCard(
                title = "Battery & Thermal Diagnostics",
                icon = Icons.Default.Info
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DiagnosticRow("Battery Level", "${battery.percentage}%")
                    DiagnosticRow("Charging Status", if (battery.isCharging) "Charging (${battery.chargingSource})" else "Discharging")
                    DiagnosticRow("Battery Health", battery.healthStatus)
                    DiagnosticRow(
                        "Core Temperature",
                        "${battery.temperatureCelsius}°C",
                        valueColor = if (battery.temperatureCelsius > 40f) CyberRed else CyberGreen
                    )
                    DiagnosticRow("Battery Voltage", "${battery.voltageMv} mV")
                    DiagnosticRow("Battery Chemistry", battery.technology)
                }
            }
        }

        // 4. Network Diagnostics
        item {
            DiagnosticSectionCard(
                title = "Network & Connectivity",
                icon = Icons.Default.Info
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DiagnosticRow("Connection Status", if (network.isConnected) "Online" else "Disconnected")
                    DiagnosticRow("Transport Medium", network.connectionType)
                    DiagnosticRow("Metered Network", if (network.isMetered) "Yes (Limited / Data Saving)" else "No (Unmetered)")
                    if (network.speedKbps > 0) {
                        DiagnosticRow("Downlink Bandwidth", "${network.speedKbps / 1000} Mbps")
                    }
                }
            }
        }

        // 5. System & Hardware Platform
        item {
            DiagnosticSectionCard(
                title = "Device Hardware & OS Details",
                icon = Icons.Default.Info
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DiagnosticRow("Device", system.deviceName)
                    DiagnosticRow("Manufacturer", system.manufacturer)
                    DiagnosticRow("Model", system.model)
                    DiagnosticRow("Operating System", system.androidVersion)
                    DiagnosticRow("Android API Level", "API ${system.sdkInt}")
                    DiagnosticRow("Uptime Since Boot", system.uptimeFormatted)
                }
            }
        }
    }
}

@Composable
private fun DiagnosticSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CyberSurface,
        border = BorderStroke(1.dp, CyberBorder),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CyberPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CyberTextPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = CyberBorder)
            Spacer(modifier = Modifier.height(14.dp))

            content()
        }
    }
}

@Composable
private fun DiagnosticRow(
    label: String,
    value: String,
    valueColor: Color = CyberTextPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = CyberTextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = valueColor
        )
    }
}
