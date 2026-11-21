package com.example.appup.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appup.data.model.AppHealthSummary
import com.example.appup.ui.components.CyberFloatingNavBar
import com.example.appup.ui.screens.AnalyticsScreen
import com.example.appup.ui.screens.AppDetailScreen
import com.example.appup.ui.screens.AppsScreen
import com.example.appup.ui.screens.DashboardScreen
import com.example.appup.ui.screens.InsightsScreen
import com.example.appup.ui.theme.CyberBackground
import com.example.appup.ui.theme.CyberBorder
import com.example.appup.ui.theme.CyberPrimary
import com.example.appup.ui.theme.CyberSurface
import com.example.appup.ui.theme.CyberTextPrimary
import com.example.appup.ui.theme.CyberTextSecondary
import com.example.appup.ui.theme.shimmerBrush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppGuardApp(
    viewModel: AppGuardViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Retain last selected app during exit animations
    var lastSelectedApp by remember { mutableStateOf<AppHealthSummary?>(null) }
    if (state.selectedApp != null) {
        lastSelectedApp = state.selectedApp
    }

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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
    ) {
        // Main Screen Scaffold
        Scaffold(
            modifier = Modifier
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
                CyberFloatingNavBar(
                    selectedTab = state.selectedTab,
                    onTabSelected = { viewModel.setSelectedTab(it) }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(CyberBackground)
            ) {
                if (state.isLoading) {
                    CyberSkeletonScreen()
                } else {
                    // Directional tab transition: slide right if target > initial, else slide left, with smooth fade
                    AnimatedContent(
                        targetState = state.selectedTab,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                ) { fullWidth -> fullWidth } + fadeIn(animationSpec = tween(220)))
                                    .togetherWith(
                                        slideOutHorizontally(
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioNoBouncy,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        ) { fullWidth -> -fullWidth } + fadeOut(animationSpec = tween(200))
                                    )
                            } else {
                                (slideInHorizontally(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                ) { fullWidth -> -fullWidth } + fadeIn(animationSpec = tween(220)))
                                    .togetherWith(
                                        slideOutHorizontally(
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioNoBouncy,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        ) { fullWidth -> fullWidth } + fadeOut(animationSpec = tween(200))
                                    )
                            }
                        },
                        label = "TabDirectionalAnimatedContent"
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

        // Full-screen AnimatedVisibility for AppDetailScreen
        AnimatedVisibility(
            visible = state.selectedApp != null,
            enter = slideInHorizontally(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ) { it } + fadeIn(animationSpec = tween(250)),
            exit = slideOutHorizontally(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ) { it } + fadeOut(animationSpec = tween(200)),
            modifier = Modifier.fillMaxSize()
        ) {
            val appToDisplay = state.selectedApp ?: lastSelectedApp
            if (appToDisplay != null) {
                AppDetailScreen(
                    app = appToDisplay,
                    onBack = { viewModel.selectApp(null) },
                    onOpenPlayStore = { pkg -> viewModel.openPlayStore(context, pkg) },
                    onOpenSettings = { pkg -> viewModel.openAppSettings(context, pkg) },
                    onLaunchApp = { pkg -> viewModel.launchApp(context, pkg) },
                    insight = state.currentAiInsight,
                    isInsightLoading = state.isAiInsightLoading,
                    insightError = state.aiInsightError,
                    onGenerateInsight = { viewModel.generateAiInsight(appToDisplay, forceRefresh = false) },
                    onRefreshInsight = { viewModel.generateAiInsight(appToDisplay, forceRefresh = true) }
                )
            }
        }
    }
}

/**
 * Cyber shimmer skeleton screen displayed during state.isLoading,
 * replacing the basic circular spinner with a high-fidelity cyberpunk preview.
 */
@Composable
private fun CyberSkeletonScreen(
    modifier: Modifier = Modifier
) {
    val brush = shimmerBrush()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // Status indicator header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp, top = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(CyberPrimary, CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "ANALYZING INSTALLED APPLICATIONS...",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.1.sp,
                        color = CyberPrimary
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Detecting update timestamps & evaluating security recency",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = CyberTextSecondary
                    )
                )
            }
        }

        // Hero Card Skeleton (Gauge / Health Audit)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(136.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(CyberSurface)
                .border(1.dp, CyberBorder.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circle placeholder for Gauge
                Box(
                    modifier = Modifier
                        .size(82.dp)
                        .clip(CircleShape)
                        .background(brush)
                )
                Spacer(modifier = Modifier.width(18.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.72f)
                            .height(18.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.48f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3 Metric Cards Skeleton Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(74.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberSurface)
                        .border(1.dp, CyberBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.55f)
                                .height(10.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(brush)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.72f)
                                .height(18.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(brush)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // App Card Skeleton List
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(66.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberSurface)
                        .border(1.dp, CyberBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(brush)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.58f)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(brush)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.36f)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(brush)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(width = 50.dp, height = 22.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(brush)
                        )
                    }
                }
            }
        }
    }
}
