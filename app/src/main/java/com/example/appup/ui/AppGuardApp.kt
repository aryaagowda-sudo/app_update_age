package com.example.appup.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appup.ui.screens.AnalyticsScreen
import com.example.appup.ui.screens.AppDetailScreen
import com.example.appup.ui.screens.AppsScreen
import com.example.appup.ui.screens.DashboardScreen
import com.example.appup.ui.screens.InsightsScreen
import com.example.appup.ui.theme.CyberBackground
import com.example.appup.ui.theme.CyberPrimary
import com.example.appup.ui.theme.CyberSurface
import com.example.appup.ui.theme.CyberTextMuted
import com.example.appup.ui.theme.CyberTextPrimary
import com.example.appup.ui.theme.CyberTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppGuardApp(
    viewModel: AppGuardViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Handle back button behavior
    BackHandler(enabled = state.selectedApp != null) {
        viewModel.selectApp(null)
    }

    BackHandler(enabled = state.selectedApp == null && state.selectedTab != 0) {
        viewModel.setSelectedTab(0)
    }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissSnackbar()
        }
    }

    // If an app is selected, show the full Application Details Screen (Module 2)
    if (state.selectedApp != null) {
        AppDetailScreen(
            app = state.selectedApp!!,
            onBack = { viewModel.selectApp(null) },
            onOpenPlayStore = { pkg -> viewModel.openPlayStore(context, pkg) },
            onOpenSettings = { pkg -> viewModel.openAppSettings(context, pkg) },
            onLaunchApp = { pkg -> viewModel.launchApp(context, pkg) },
            insight = state.currentAiInsight,
            isInsightLoading = state.isAiInsightLoading,
            insightError = state.aiInsightError,
            onGenerateInsight = { viewModel.generateAiInsight(state.selectedApp!!, forceRefresh = false) },
            onRefreshInsight = { viewModel.generateAiInsight(state.selectedApp!!, forceRefresh = true) }
        )
        return
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground),
        containerColor = CyberBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (state.selectedTab != 0) {
                TopAppBar(
                    title = {
                        Text(
                            text = when (state.selectedTab) {
                                1 -> "Installed Applications"
                                2 -> "Intelligent Insights"
                                3 -> "App Health Analytics"
                                else -> "AppGuard"
                            },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = CyberTextPrimary
                            )
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.loadData() },
                            enabled = !state.isLoading && !state.isOptimizing
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh health analysis",
                                tint = CyberPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CyberSurface
                    )
                )
            }
        },
        bottomBar = {
            // Section 6 Requirement: Bottom navigation with: 1. Home, 2. Applications, 3. Insights, 4. Analytics
            NavigationBar(
                containerColor = CyberSurface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = state.selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF090D14),
                        selectedTextColor = CyberPrimary,
                        indicatorColor = CyberPrimary,
                        unselectedIconColor = CyberTextMuted,
                        unselectedTextColor = CyberTextMuted
                    )
                )

                NavigationBarItem(
                    selected = state.selectedTab == 1,
                    onClick = { viewModel.setSelectedTab(1) },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Applications") },
                    label = { Text("Applications") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF090D14),
                        selectedTextColor = CyberPrimary,
                        indicatorColor = CyberPrimary,
                        unselectedIconColor = CyberTextMuted,
                        unselectedTextColor = CyberTextMuted
                    )
                )

                NavigationBarItem(
                    selected = state.selectedTab == 2,
                    onClick = { viewModel.setSelectedTab(2) },
                    icon = { Icon(Icons.Default.Info, contentDescription = "Insights") },
                    label = { Text("Insights") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF090D14),
                        selectedTextColor = CyberPrimary,
                        indicatorColor = CyberPrimary,
                        unselectedIconColor = CyberTextMuted,
                        unselectedTextColor = CyberTextMuted
                    )
                )

                NavigationBarItem(
                    selected = state.selectedTab == 3,
                    onClick = { viewModel.setSelectedTab(3) },
                    icon = { Icon(Icons.Default.Refresh, contentDescription = "Analytics") },
                    label = { Text("Analytics") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF090D14),
                        selectedTextColor = CyberPrimary,
                        indicatorColor = CyberPrimary,
                        unselectedIconColor = CyberTextMuted,
                        unselectedTextColor = CyberTextMuted
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CyberBackground)
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(52.dp),
                            color = CyberPrimary,
                            strokeWidth = 4.dp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Analyzing Installed Applications...",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CyberTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Detecting update timestamps & evaluating recency",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = CyberTextSecondary
                            )
                        )
                    }
                }
            } else {
                // Smooth animated transition between tabs
                Crossfade(
                    targetState = state.selectedTab,
                    animationSpec = tween(durationMillis = 220),
                    label = "TabCrossfade"
                ) { currentTab ->
                    when (currentTab) {
                        0 -> DashboardScreen(
                            audit = state.healthAudit,
                            deviceHealth = state.deviceHealth,
                            analysisReport = state.analysisReport,
                            allApps = state.allApps,
                            isScanning = state.isLoading,
                            isOptimizing = state.isOptimizing,
                            lastScanTime = state.lastScanTime,
                            onScanClick = { viewModel.loadData() },
                            onOptimizeClick = { viewModel.runOptimization() },
                            onAppClick = { viewModel.selectApp(it) },
                            onViewAllAppsClick = { viewModel.setSelectedTab(1) },
                            onReviewApp = { viewModel.selectApp(it) },
                            onReviewAttentionApps = {
                                viewModel.setFilterOption(AppFilterOption.ATTENTION)
                                viewModel.setSelectedTab(1)
                            },
                            onOpenAppSettings = { pkg -> viewModel.openAppSettings(context, pkg) }
                        )

                        1 -> AppsScreen(
                            apps = state.filteredApps,
                            allApps = state.allApps,
                            searchQuery = state.searchQuery,
                            filterOption = state.filterOption,
                            sortOption = state.sortOption,
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            onFilterChange = { viewModel.setFilterOption(it) },
                            onSortChange = { viewModel.setSortOption(it) },
                            onAppClick = { viewModel.selectApp(it) }
                        )

                        2 -> InsightsScreen(
                            analysisReport = state.analysisReport,
                            allApps = state.allApps,
                            scanHistory = state.scanHistory,
                            onAppClick = { viewModel.selectApp(it) }
                        )

                        3 -> AnalyticsScreen(
                            analysisReport = state.analysisReport,
                            allApps = state.allApps,
                            scanHistory = state.scanHistory,
                            lastScanTime = state.lastScanTime,
                            onTriggerScan = { viewModel.loadData() }
                        )
                    }
                }
            }
        }
    }
}
