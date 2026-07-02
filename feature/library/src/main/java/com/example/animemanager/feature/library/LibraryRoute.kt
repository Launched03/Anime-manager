@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.animemanager.feature.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.animemanager.core.model.AnimeSortOrder
import com.example.animemanager.core.model.SeriesStatus
import com.example.animemanager.core.model.WatchState
import com.example.animemanager.core.ui.AnimeChoiceChipRow
import com.example.animemanager.core.ui.AnimeEmptyState
import com.example.animemanager.core.ui.AnimePosterListItem
import com.example.animemanager.core.ui.AnimeSeriesStatusChips
import com.example.animemanager.core.ui.AnimeTopBar
import com.example.animemanager.core.ui.AnimeWeekdayChips
import com.example.animemanager.core.ui.AnimeWatchStateChips

@Composable
fun LibraryRoute(
    onOpenAnime: (Long) -> Unit,
    onAddAnime: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val animeList by viewModel.animeList.collectAsStateWithLifecycle()
    LibraryScreen(
        uiState = uiState,
        animeList = animeList,
        onOpenAnime = onOpenAnime,
        onAddAnime = onAddAnime,
        onQueryChange = viewModel::updateQuery,
        onWatchStateChange = viewModel::updateWatchState,
        onSeriesStatusChange = viewModel::updateSeriesStatus,
        onFavoritesOnlyChange = viewModel::updateFavoritesOnly,
        onWeekdayChange = viewModel::updateWeekday,
        onSortOrderChange = viewModel::updateSortOrder,
    )
}

@Composable
fun LibraryScreen(
    uiState: LibraryUiState,
    animeList: List<com.example.animemanager.core.model.AnimeSummary>,
    onOpenAnime: (Long) -> Unit,
    onAddAnime: () -> Unit,
    onQueryChange: (String) -> Unit,
    onWatchStateChange: (com.example.animemanager.core.model.WatchState?) -> Unit,
    onSeriesStatusChange: (com.example.animemanager.core.model.SeriesStatus?) -> Unit,
    onFavoritesOnlyChange: (Boolean) -> Unit,
    onWeekdayChange: (Int?) -> Unit,
    onSortOrderChange: (AnimeSortOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    var filtersExpanded by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            AnimeTopBar(
                title = "番剧库",
                actions = {
                    IconButton(onClick = onAddAnime) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = "新增番剧")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                LibrarySearchField(
                    query = uiState.query,
                    onQueryChange = onQueryChange,
                )
            }
            item {
                LibraryFilterHeader(
                    uiState = uiState,
                    expanded = filtersExpanded,
                    onToggle = { filtersExpanded = !filtersExpanded },
                )
            }
            if (filtersExpanded) {
                item {
                    LibraryFilterPanel(
                        uiState = uiState,
                        onWatchStateChange = onWatchStateChange,
                        onSeriesStatusChange = onSeriesStatusChange,
                        onFavoritesOnlyChange = onFavoritesOnlyChange,
                        onWeekdayChange = onWeekdayChange,
                        onSortOrderChange = onSortOrderChange,
                    )
                }
            }
            if (animeList.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        AnimeEmptyState(
                            title = "没有找到番剧",
                            subtitle = "试试换个搜索词或筛选条件",
                        )
                    }
                }
            } else {
                items(animeList, key = { it.id }) { summary ->
                    AnimePosterListItem(
                        summary = summary,
                        onClick = { onOpenAnime(summary.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LibrarySearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("搜索标题") },
        singleLine = true,
        trailingIcon = if (query.isNotBlank()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "清空")
                }
            }
        } else {
            null
        },
    )
}

@Composable
private fun LibraryFilterHeader(
    uiState: LibraryUiState,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(text = "筛选和排序", style = MaterialTheme.typography.titleSmall)
                Text(
                    text = libraryFilterSummary(uiState),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = if (expanded) "收起筛选" else "展开筛选",
            )
        }
    }
}

@Composable
private fun LibraryFilterPanel(
    uiState: LibraryUiState,
    onWatchStateChange: (WatchState?) -> Unit,
    onSeriesStatusChange: (SeriesStatus?) -> Unit,
    onFavoritesOnlyChange: (Boolean) -> Unit,
    onWeekdayChange: (Int?) -> Unit,
    onSortOrderChange: (AnimeSortOrder) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        LibraryFilterGroup(title = "观看状态") {
            AnimeWatchStateChips(selected = uiState.watchState, onSelect = onWatchStateChange)
        }
        LibraryFilterGroup(title = "番剧状态") {
            AnimeSeriesStatusChips(selected = uiState.seriesStatus, onSelect = onSeriesStatusChange)
        }
        LibraryFilterGroup(title = "更新时间") {
            AnimeWeekdayChips(selectedWeekday = uiState.weekday, onSelect = onWeekdayChange)
        }
        LibraryFilterGroup(title = "排序") {
            AnimeChoiceChipRow(
                options = AnimeSortOrder.entries,
                selected = uiState.sortOrder,
                onSelect = onSortOrderChange,
                label = { it.displayLabel() },
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "仅收藏")
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = uiState.favoritesOnly,
                onCheckedChange = onFavoritesOnlyChange,
            )
        }
    }
}

@Composable
private fun LibraryFilterGroup(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title)
        content()
    }
}

private fun libraryFilterSummary(uiState: LibraryUiState): String {
    val filters = buildList {
        uiState.watchState?.let { add(it.displayLabel()) }
        uiState.seriesStatus?.let { add(it.displayLabel()) }
        uiState.weekday?.let { add("更新时间已筛选") }
        if (uiState.favoritesOnly) add("仅收藏")
    }
    val filterText = filters.ifEmpty { listOf("未启用筛选") }.joinToString(" · ")
    return "$filterText · ${uiState.sortOrder.displayLabel()}"
}
