package com.example.appup.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import com.example.appup.data.analysis.AppAnalysisEngine
import com.example.appup.data.analysis.AppAnalysisReport
import com.example.appup.data.analysis.GeminiAppAnalysisService
import com.example.appup.data.history.AiInsightRepository
import com.example.appup.data.history.ScanHistoryRepository
import com.example.appup.data.model.AiAppInsight
import com.example.appup.data.model.AppHealthSummary
import com.example.appup.data.model.AppUpdateCategory
import com.example.appup.data.model.DeviceHealth
import com.example.appup.data.model.ScanRecord
import com.example.appup.data.model.SystemHealthAudit
import com.example.appup.data.monitor.AppSecurityScanner
import com.example.appup.data.monitor.SystemHealthMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppSortOption(val label: String) {
    OLDEST_FIRST("Oldest First"),
    NEWEST_FIRST("Newest First"),
    ALPHABETICAL("A to Z")
}

enum class AppFilterOption(val label: String) {
    ALL("All"),
    HEALTHY("Healthy"),
    AGING("Aging"),
    WARNING("Warning"),
    ATTENTION("Needs Attention")
}

data class AppGuardUiState(
    val isLoading: Boolean = true,
    val isOptimizing: Boolean = false,
    val deviceHealth: DeviceHealth? = null,
    val allApps: List<AppHealthSummary> = emptyList(),
    val filteredApps: List<AppHealthSummary> = emptyList(),
    val healthAudit: SystemHealthAudit? = null,
    val analysisReport: AppAnalysisReport? = null,
    val selectedTab: Int = 0, // 0: Home/Dashboard, 1: Apps, 2: Insights, 3: History
    val searchQuery: String = "",
    val filterOption: AppFilterOption = AppFilterOption.ALL,
    val sortOption: AppSortOption = AppSortOption.OLDEST_FIRST,
    val selectedApp: AppHealthSummary? = null,
    val scanHistory: List<ScanRecord> = emptyList(),
    val snackbarMessage: String? = null,
    val lastScanTime: String = "Just now",
    val currentAiInsight: AiAppInsight? = null,
    val isAiInsightLoading: Boolean = false,
    val aiInsightError: String? = null
)

class AppGuardViewModel(application: Application) : AndroidViewModel(application) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val systemMonitor = SystemHealthMonitor(application.applicationContext)
    private val securityScanner = AppSecurityScanner(application.applicationContext)
    private val analysisEngine = AppAnalysisEngine()
    private val historyRepository = ScanHistoryRepository(application.applicationContext)
    private val aiInsightRepository = AiInsightRepository(application.applicationContext)
    private val geminiService = GeminiAppAnalysisService()

    private val _uiState = MutableStateFlow(
        AppGuardUiState(scanHistory = historyRepository.getScanHistory())
    )
    val uiState: StateFlow<AppGuardUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        scope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val (health, report, audit) = withContext(Dispatchers.IO) {
                val devHealth = systemMonitor.getDeviceHealth()
                val scannedApps = securityScanner.scanInstalledApps()
                val analysis = analysisEngine.analyze(scannedApps)
                val computedAudit = securityScanner.computeOverallAudit(devHealth, analysis.sortedAppsOldestFirst)
                Triple(devHealth, analysis, computedAudit)
            }

            // Persist scan to local history
            val newScanRecord = ScanRecord(
                totalApps = report.totalApps,
                healthyCount = report.healthyCount,
                agingCount = report.agingCount,
                warningCount = report.warningCount,
                attentionCount = report.attentionCount,
                healthScore = report.upToDateScore
            )
            withContext(Dispatchers.IO) {
                historyRepository.saveScanRecord(newScanRecord)
            }
            val updatedHistory = withContext(Dispatchers.IO) {
                historyRepository.getScanHistory()
            }

            val currentTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())

            val currentState = _uiState.value
            val filtered = applyFilterAndSort(
                apps = report.sortedAppsOldestFirst,
                query = currentState.searchQuery,
                filter = currentState.filterOption,
                sort = currentState.sortOption
            )

            _uiState.value = currentState.copy(
                isLoading = false,
                deviceHealth = health,
                allApps = report.sortedAppsOldestFirst,
                filteredApps = filtered,
                analysisReport = report,
                healthAudit = audit,
                scanHistory = updatedHistory,
                lastScanTime = currentTime
            )
        }
    }

    fun setSelectedTab(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun setSearchQuery(query: String) {
        val currentState = _uiState.value
        val filtered = applyFilterAndSort(
            apps = currentState.allApps,
            query = query,
            filter = currentState.filterOption,
            sort = currentState.sortOption
        )
        _uiState.value = currentState.copy(
            searchQuery = query,
            filteredApps = filtered
        )
    }

    fun setFilterOption(option: AppFilterOption) {
        val currentState = _uiState.value
        val filtered = applyFilterAndSort(
            apps = currentState.allApps,
            query = currentState.searchQuery,
            filter = option,
            sort = currentState.sortOption
        )
        _uiState.value = currentState.copy(
            filterOption = option,
            filteredApps = filtered
        )
    }

    fun setSortOption(sort: AppSortOption) {
        val currentState = _uiState.value
        val filtered = applyFilterAndSort(
            apps = currentState.allApps,
            query = currentState.searchQuery,
            filter = currentState.filterOption,
            sort = sort
        )
        _uiState.value = currentState.copy(
            sortOption = sort,
            filteredApps = filtered
        )
    }

    fun selectApp(app: AppHealthSummary?) {
        if (app == null) {
            _uiState.value = _uiState.value.copy(
                selectedApp = null,
                currentAiInsight = null,
                isAiInsightLoading = false,
                aiInsightError = null
            )
        } else {
            val cachedInsight = aiInsightRepository.getInsight(app.packageName)
            _uiState.value = _uiState.value.copy(
                selectedApp = app,
                currentAiInsight = cachedInsight,
                isAiInsightLoading = false,
                aiInsightError = null
            )
        }
    }

    fun generateAiInsight(app: AppHealthSummary, forceRefresh: Boolean = false) {
        if (!forceRefresh) {
            val cached = aiInsightRepository.getInsight(app.packageName)
            if (cached != null) {
                _uiState.value = _uiState.value.copy(
                    currentAiInsight = cached,
                    isAiInsightLoading = false,
                    aiInsightError = null
                )
                return
            }
        }

        scope.launch {
            _uiState.value = _uiState.value.copy(
                isAiInsightLoading = true,
                aiInsightError = null
            )

            try {
                val insight = withContext(Dispatchers.IO) {
                    geminiService.generateInsight(app)
                }
                withContext(Dispatchers.IO) {
                    aiInsightRepository.saveInsight(insight)
                }
                _uiState.value = _uiState.value.copy(
                    currentAiInsight = insight,
                    isAiInsightLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAiInsightLoading = false,
                    aiInsightError = e.localizedMessage ?: "Failed to generate AI insight. Check network connection."
                )
            }
        }
    }

    fun runOptimization() {
        if (_uiState.value.isOptimizing) return

        scope.launch {
            _uiState.value = _uiState.value.copy(isOptimizing = true)

            val freedBytes = withContext(Dispatchers.IO) {
                val targets = _uiState.value.allApps
                    .filter { !it.isSystemApp && it.packageName != getApplication<Application>().packageName }
                    .take(15)
                    .map { it.packageName }

                val freed = systemMonitor.performQuickOptimization(targets)
                kotlinx.coroutines.delay(1200)
                freed
            }

            val freedMb = freedBytes / (1024 * 1024)

            val refreshedHealth = withContext(Dispatchers.IO) { systemMonitor.getDeviceHealth() }
            val refreshedAudit = withContext(Dispatchers.IO) {
                securityScanner.computeOverallAudit(refreshedHealth, _uiState.value.allApps)
            }

            _uiState.value = _uiState.value.copy(
                isOptimizing = false,
                deviceHealth = refreshedHealth,
                healthAudit = refreshedAudit,
                snackbarMessage = "Optimization complete! Reclaimed ~$freedMb MB of memory."
            )
        }
    }

    fun dismissSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    private fun applyFilterAndSort(
        apps: List<AppHealthSummary>,
        query: String,
        filter: AppFilterOption,
        sort: AppSortOption
    ): List<AppHealthSummary> {
        val filtered = apps.filter { app ->
            val matchesFilter = when (filter) {
                AppFilterOption.ALL -> true
                AppFilterOption.HEALTHY -> app.updateCategory == AppUpdateCategory.HEALTHY
                AppFilterOption.AGING -> app.updateCategory == AppUpdateCategory.AGING
                AppFilterOption.WARNING -> app.updateCategory == AppUpdateCategory.WARNING
                AppFilterOption.ATTENTION -> app.updateCategory == AppUpdateCategory.NEEDS_ATTENTION
            }

            val matchesQuery = query.isBlank() ||
                    app.appName.contains(query, ignoreCase = true) ||
                    app.packageName.contains(query, ignoreCase = true)

            matchesFilter && matchesQuery
        }

        return when (sort) {
            AppSortOption.OLDEST_FIRST -> filtered.sortedByDescending { it.daysSinceLastUpdate }
            AppSortOption.NEWEST_FIRST -> filtered.sortedBy { it.daysSinceLastUpdate }
            AppSortOption.ALPHABETICAL -> filtered.sortedBy { it.appName.lowercase(Locale.getDefault()) }
        }
    }

    fun openAppSettings(context: Context, packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun openPlayStore(context: Context, packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            } catch (_: Exception) {
            }
        }
    }

    fun launchApp(context: Context, packageName: String) {
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
            }
        } catch (_: Exception) {
        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }
}
