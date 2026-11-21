package com.example.appup.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appup.data.model.AppHealthSummary
import com.example.appup.data.model.AppUpdateCategory
import com.example.appup.ui.AppFilterOption
import com.example.appup.ui.AppSortOption
import com.example.appup.ui.components.AppHealthCard
import com.example.appup.ui.theme.CyberBackground
import com.example.appup.ui.theme.CyberBorder
import com.example.appup.ui.theme.CyberGreen
import com.example.appup.ui.theme.CyberGreenBg
import com.example.appup.ui.theme.CyberOrange
import com.example.appup.ui.theme.CyberOrangeBg
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
fun AppsScreen(
    apps: List<AppHealthSummary>,
    allApps: List<AppHealthSummary>,
    searchQuery: String,
    filterOption: AppFilterOption,
    sortOption: AppSortOption,
    onSearchChange: (String) -> Unit,
    onFilterChange: (AppFilterOption) -> Unit,
    onSortChange: (AppSortOption) -> Unit,
    onAppClick: (AppHealthSummary) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCount = allApps.size
    val healthyCount = allApps.count { it.updateCategory == AppUpdateCategory.HEALTHY }
    val agingCount = allApps.count { it.updateCategory == AppUpdateCategory.AGING }
    val warningCount = allApps.count { it.updateCategory == AppUpdateCategory.WARNING }
    val attentionCount = allApps.count { it.updateCategory == AppUpdateCategory.NEEDS_ATTENTION }

    // Search field animated focus & border glow state
    var isSearchFocused by remember { mutableStateOf(false) }
    val searchBorderColor by animateColorAsState(
        targetValue = if (isSearchFocused) CyberPrimary else if (searchQuery.isNotEmpty()) CyberPrimary.copy(alpha = 0.65f) else CyberBorder,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "searchBorderColor"
    )
    val searchGlowAlpha by animateFloatAsState(
        targetValue = if (isSearchFocused) 0.4f else if (searchQuery.isNotEmpty()) 0.2f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "searchGlowAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(top = 10.dp)
    ) {
        // 1. Search Bar with animated cyber border glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .then(
                    if (searchGlowAlpha > 0f) {
                        Modifier.border(
                            width = 2.dp,
                            color = CyberPrimary.copy(alpha = searchGlowAlpha),
                            shape = RoundedCornerShape(16.dp)
                        )
                    } else Modifier
                )
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isSearchFocused = it.isFocused },
                placeholder = { Text("Search by name or package...", color = CyberTextMuted) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = if (isSearchFocused || searchQuery.isNotEmpty()) CyberPrimary else CyberTextSecondary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = CyberTextSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CyberSurface,
                    unfocusedContainerColor = CyberSurface,
                    focusedBorderColor = searchBorderColor,
                    unfocusedBorderColor = searchBorderColor,
                    focusedTextColor = CyberTextPrimary,
                    unfocusedTextColor = CyberTextPrimary,
                    cursorColor = CyberPrimary
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Filter Chips Horizontal Row with smooth animated border glows on selection
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CyberFilterChip(
                selected = filterOption == AppFilterOption.ALL,
                onClick = { onFilterChange(AppFilterOption.ALL) },
                label = "All ($totalCount)",
                accentColor = CyberPrimary,
                accentBg = CyberSurface
            )

            CyberFilterChip(
                selected = filterOption == AppFilterOption.HEALTHY,
                onClick = { onFilterChange(AppFilterOption.HEALTHY) },
                label = "Healthy ($healthyCount)",
                accentColor = CyberGreen,
                accentBg = CyberGreenBg
            )

            CyberFilterChip(
                selected = filterOption == AppFilterOption.AGING,
                onClick = { onFilterChange(AppFilterOption.AGING) },
                label = "Aging ($agingCount)",
                accentColor = CyberYellow,
                accentBg = CyberYellowBg
            )

            CyberFilterChip(
                selected = filterOption == AppFilterOption.WARNING,
                onClick = { onFilterChange(AppFilterOption.WARNING) },
                label = "Warning ($warningCount)",
                accentColor = CyberOrange,
                accentBg = CyberOrangeBg
            )

            CyberFilterChip(
                selected = filterOption == AppFilterOption.ATTENTION,
                onClick = { onFilterChange(AppFilterOption.ATTENTION) },
                label = "Needs Attention ($attentionCount)",
                accentColor = CyberRed,
                accentBg = CyberRedBg
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Sorting Chips Row with animated glowing selection borders
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sort:",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = CyberTextMuted
                )
            )

            CyberSortChip(
                selected = sortOption == AppSortOption.OLDEST_FIRST,
                onClick = { onSortChange(AppSortOption.OLDEST_FIRST) },
                label = "Oldest First"
            )

            CyberSortChip(
                selected = sortOption == AppSortOption.NEWEST_FIRST,
                onClick = { onSortChange(AppSortOption.NEWEST_FIRST) },
                label = "Newest First"
            )

            CyberSortChip(
                selected = sortOption == AppSortOption.ALPHABETICAL,
                onClick = { onSortChange(AppSortOption.ALPHABETICAL) },
                label = "Alphabetical"
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Results Summary Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Showing ${apps.size} of $totalCount applications",
                style = MaterialTheme.typography.labelSmall.copy(color = CyberTextMuted)
            )
            Text(
                text = "Tap card for details",
                style = MaterialTheme.typography.labelSmall.copy(color = CyberPrimary)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 4. Applications List with smooth item placement and filtering animations
        if (apps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CyberSurface,
                    border = BorderStroke(1.dp, CyberBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No applications match your criteria",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CyberTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try adjusting your search query or category filter.",
                            style = MaterialTheme.typography.bodySmall.copy(color = CyberTextMuted)
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(apps, key = { it.packageName }) { app ->
                    AppHealthCard(
                        app = app,
                        onClick = { onAppClick(app) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

@Composable
private fun CyberFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    accentColor: Color,
    accentBg: Color,
    selectedContentColor: Color = Color(0xFF090D14),
    modifier: Modifier = Modifier
) {
    val animatedBorderColor by animateColorAsState(
        targetValue = if (selected) accentColor else CyberBorder,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chipBorderColor"
    )
    val animatedGlowAlpha by animateFloatAsState(
        targetValue = if (selected) 0.5f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chipGlowAlpha"
    )
    val animatedBorderWidth by animateDpAsState(
        targetValue = if (selected) 1.5.dp else 1.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chipBorderWidth"
    )

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        },
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = if (accentBg == CyberSurface) CyberSurface else accentBg.copy(alpha = 0.55f),
            labelColor = if (selected) selectedContentColor else if (accentBg == CyberSurface) CyberTextSecondary else accentColor,
            selectedContainerColor = accentColor,
            selectedLabelColor = selectedContentColor
        ),
        border = BorderStroke(
            width = animatedBorderWidth,
            color = animatedBorderColor
        ),
        modifier = modifier.then(
            if (animatedGlowAlpha > 0f) {
                Modifier.border(
                    width = 2.dp,
                    color = accentColor.copy(alpha = animatedGlowAlpha * 0.4f),
                    shape = RoundedCornerShape(12.dp)
                )
            } else Modifier
        )
    )
}

@Composable
private fun CyberSortChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val animatedBorderColor by animateColorAsState(
        targetValue = if (selected) CyberPrimary else CyberBorder,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "sortChipBorderColor"
    )
    val animatedGlowAlpha by animateFloatAsState(
        targetValue = if (selected) 0.45f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "sortChipGlowAlpha"
    )
    val animatedBorderWidth by animateDpAsState(
        targetValue = if (selected) 1.5.dp else 1.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "sortChipBorderWidth"
    )

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        shape = RoundedCornerShape(10.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = CyberSurface,
            labelColor = CyberTextSecondary,
            selectedContainerColor = CyberSurfaceElevated,
            selectedLabelColor = CyberPrimary
        ),
        border = BorderStroke(
            width = animatedBorderWidth,
            color = animatedBorderColor
        ),
        modifier = modifier.then(
            if (animatedGlowAlpha > 0f) {
                Modifier.border(
                    width = 2.dp,
                    color = CyberPrimary.copy(alpha = animatedGlowAlpha * 0.35f),
                    shape = RoundedCornerShape(10.dp)
                )
            } else Modifier
        )
    )
}
