package com.example.appup.data.model

data class RamInfo(
    val totalBytes: Long,
    val availableBytes: Long,
    val usedBytes: Long,
    val usedPercent: Int,
    val isLowMemory: Boolean,
    val thresholdBytes: Long
)

data class StorageInfo(
    val totalBytes: Long,
    val availableBytes: Long,
    val usedBytes: Long,
    val usedPercent: Int
)

data class BatteryInfo(
    val percentage: Int,
    val isCharging: Boolean,
    val chargingSource: String,
    val healthStatus: String,
    val temperatureCelsius: Float,
    val voltageMv: Int,
    val technology: String
)

data class NetworkInfo(
    val isConnected: Boolean,
    val connectionType: String,
    val isMetered: Boolean,
    val speedKbps: Int
)

data class SystemInfo(
    val deviceName: String,
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val sdkInt: Int,
    val uptimeFormatted: String
)

data class DeviceHealth(
    val ram: RamInfo,
    val storage: StorageInfo,
    val battery: BatteryInfo,
    val network: NetworkInfo,
    val system: SystemInfo
)
