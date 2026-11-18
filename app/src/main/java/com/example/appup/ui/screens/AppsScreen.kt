package com.example.appup.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(top = 10.dp)
    ) {
        // 1. Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("Search by name or package...", color = CyberTextMuted) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = CyberPrimary
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
                focusedBorderColor = CyberPrimary,
                unfocusedBorderColor = CyberBorder,
                focusedTextColor = CyberTextPrimary,
                unfocusedTextColor = CyberTextPrimary
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Filter Chips Horizontal Row
        // Requirements: All, Healthy, Aging, Warning, Needs Attention
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterOption == AppFilterOption.ALL,
                onClick = { onFilterChange(AppFilterOption.ALL) },
                label = { Text("All ($totalCount)") },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = CyberSurface,
                    labelColor = CyberTextSecondary,
                    selectedContainerColor = CyberPrimary,
                    selectedLabelColor = Color(0xFF090D14)
                )
            )

            FilterChip(
                selected = filterOption == AppFilterOption.HEALTHY,
                onClick = { onFilterChange(AppFilterOption.HEALTHY) },
                label = { Text("Healthy ($healthyCount)") },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = CyberGreenBg,
                    labelColor = CyberGreen,
                    selectedContainerColor = CyberGreen,
                    selectedLabelColor = Color(0xFF090D14)
                )
            )

            FilterChip(
                selected = filterOption == AppFilterOption.AGING,
                onClick = { onFilterChange(AppFilterOption.AGING) },
                label = { Text("Aging ($agingCount)") },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = CyberYellowBg,
                    labelColor = CyberYellow,
                    selectedContainerColor = CyberYellow,
                    selectedLabelColor = Color(0xFF090D14)
                )
            )

            FilterChip(
                selected = filterOption == AppFilterOption.WARNING,
                onClick = { onFilterChange(AppFilterOption.WARNING) },
                label = { Text("Warning ($warningCount)") },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = CyberOrangeBg,
                    labelColor = CyberOrange,
                    selectedContainerColor = CyberOrange,
                    selectedLabelColor = Color(0xFF090D14)
                )
            )

            FilterChip(
                selected = filterOption == AppFilterOption.ATTENTION,
                onClick = { onFilterChange(AppFilterOption.ATTENTION) },
                label = { Text("Needs Attention ($attentionCount)") },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = CyberRedBg,
                    labelColor = CyberRed,
                    selectedContainerColor = CyberRed,
                    selectedLabelColor = Color(0xFF090D14)
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Sorting Chips Row
        // Requirements: Oldest updated first, Newest updated first, Alphabetical order
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

            FilterChip(
                selected = sortOption == AppSortOption.OLDEST_FIRST,
                onClick = { onSortChange(AppSortOption.OLDEST_FIRST) },
                label = { Text("Oldest First") },
                shape = RoundedCornerShape(10.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = CyberSurface,
                    labelColor = CyberTextSecondary,
                    selectedContainerColor = CyberSurface,
                    selectedLabelColor = CyberPrimary
                ),
                border = BorderStroke(
                    1.dp,
                    if (sortOption == AppSortOption.OLDEST_FIRST) CyberPrimary else CyberBorder
                )
            )

            FilterChip(
                selected = sortOption == AppSortOption.NEWEST_FIRST,
                onClick = { onSortChange(AppSortOption.NEWEST_FIRST) },
                label = { Text("Newest First") },
                shape = RoundedCornerShape(10.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = CyberSurface,
                    labelColor = CyberTextSecondary,
                    selectedContainerColor = CyberSurface,
                    selectedLabelColor = CyberPrimary
                ),
                border = BorderStroke(
                    1.dp,
                    if (sortOption == AppSortOption.NEWEST_FIRST) CyberPrimary else CyberBorder
                )
            )

            FilterChip(
                selected = sortOption == AppSortOption.ALPHABETICAL,
                onClick = { onSortChange(AppSortOption.ALPHABETICAL) },
                label = { Text("Alphabetical") },
                shape = RoundedCornerShape(10.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = CyberSurface,
                    labelColor = CyberTextSecondary,
                    selectedContainerColor = CyberSurface,
                    selectedLabelColor = CyberPrimary
                ),
                border = BorderStroke(
                    1.dp,
                    if (sortOption == AppSortOption.ALPHABETICAL) CyberPrimary else CyberBorder
                )
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

        // 4. Applications List
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
                        onClick = { onAppClick(app) }
                    )
                }
            }
        }
    }
}
