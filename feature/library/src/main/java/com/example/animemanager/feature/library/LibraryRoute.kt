@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.animemanager.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.animemanager.core.model.AnimeSummary
import com.example.animemanager.core.model.AnimeSortOrder
import com.example.animemanager.core.model.SeriesStatus
import com.example.animemanager.core.model.WatchState
import com.example.animemanager.core.model.progressLabel
import com.example.animemanager.core.model.scheduleLabel
import com.example.animemanager.core.model.seasonLabel
import com.example.animemanager.core.ui.AnimeChoiceChipRow
import com.example.animemanager.core.ui.AnimeEmptyState
import com.example.animemanager.core.ui.AnimePosterImage
import com.example.animemanager.core.ui.AnimeSearchField
import com.example.animemanager.core.ui.AnimeSeriesStatusChips
import com.example.animemanager.core.ui.AnimeTopBar
import com.example.animemanager.core.ui.AnimeWeekdayChips
import com.example.animemanager.core.ui.AnimeWatchStateChips
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

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
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    val libraryItems = remember(animeList) {
        animeList.map { it.toLibraryListItemModel() }
    }
    var showFiltersSheet by rememberSaveable { mutableStateOf(false) }
    val clearSearchFocus: () -> Unit = remember(focusManager, keyboardController) {
        {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
        }
    }

    LaunchedEffect(listState, clearSearchFocus) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .collectLatest { isScrolling ->
                if (isScrolling) {
                    clearSearchFocus()
                }
            }
    }

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
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item(contentType = "search") {
                LibrarySearchField(
                    query = uiState.query,
                    onQueryChange = onQueryChange,
                    onSearch = clearSearchFocus,
                )
            }
            item(contentType = "filter_header") {
                LibraryFilterHeader(
                    uiState = uiState,
                    onOpen = {
                        clearSearchFocus()
                        showFiltersSheet = true
                    },
                )
            }
            if (animeList.isEmpty()) {
                item(contentType = "empty") {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        AnimeEmptyState(
                            title = "没有找到番剧",
                            subtitle = "试试换个搜索词或筛选条件",
                        )
                    }
                }
            } else {
                items(
                    items = libraryItems,
                    key = { it.id },
                    contentType = { "anime" },
                ) { item ->
                    LibraryAnimeListItem(
                        item = item,
                        onClick = {
                            clearSearchFocus()
                            onOpenAnime(item.id)
                        },
                    )
                }
            }
        }
    }

    if (showFiltersSheet) {
        LibraryFilterSheet(
            uiState = uiState,
            onDismiss = { showFiltersSheet = false },
            onWatchStateChange = onWatchStateChange,
            onSeriesStatusChange = onSeriesStatusChange,
            onFavoritesOnlyChange = onFavoritesOnlyChange,
            onWeekdayChange = onWeekdayChange,
            onSortOrderChange = onSortOrderChange,
        )
    }
}

@Immutable
private data class LibraryListItemModel(
    val id: Long,
    val title: String,
    val originalTitle: String?,
    val posterRef: String?,
    val metaText: String,
    val statusText: String,
)

private fun AnimeSummary.toLibraryListItemModel(): LibraryListItemModel {
    val seasonText = seasonLabel(seasonYear, seasonName)
    val progressText = libraryProgressLabel(this)
    val scheduleText = if (seriesStatus == SeriesStatus.FINISHED) {
        null
    } else {
        scheduleLabel(releaseWeekday, releaseMinuteOfDay)
    }
    val metaText = listOfNotNull(seasonText, progressText, scheduleText)
        .filter { it.isNotBlank() }
        .joinToString(" · ")
    val statusText = buildList {
        add(seriesStatus.displayLabel())
        watchState?.let { add(it.displayLabel()) }
        if (isFavorite) add("收藏")
    }.joinToString(" · ")

    return LibraryListItemModel(
        id = id,
        title = title,
        originalTitle = originalTitle,
        posterRef = posterRef,
        metaText = metaText,
        statusText = statusText,
    )
}

@Composable
private fun LibraryAnimeListItem(
    item: LibraryListItemModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val posterDecodeSize = remember { DpSize(64.dp, 92.dp) }
    val rowShape = remember { RoundedCornerShape(8.dp) }
    val rowColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(112.dp)
            .background(rowColor, rowShape)
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimePosterImage(
            posterRef = item.posterRef,
            modifier = Modifier.size(width = 64.dp, height = 92.dp),
            contentScale = ContentScale.Crop,
            decodeSize = posterDecodeSize,
            fastThumbnail = true,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            item.originalTitle?.takeIf { it.isNotBlank() }?.let { originalTitle ->
                Text(
                    text = originalTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = item.metaText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.statusText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun LibrarySearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimeSearchField(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = onSearch,
        modifier = modifier,
        label = "搜索标题",
        placeholder = "搜索本地番剧",
    )
}

@Composable
private fun LibraryFilterHeader(
    uiState: LibraryUiState,
    onOpen: () -> Unit,
) {
    val summary = remember(
        uiState.watchState,
        uiState.seriesStatus,
        uiState.weekday,
        uiState.favoritesOnly,
        uiState.sortOrder,
    ) {
        libraryFilterSummary(uiState)
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
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
                    text = summary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = "打开筛选",
            )
        }
    }
}

@Composable
private fun LibraryFilterSheet(
    uiState: LibraryUiState,
    onDismiss: () -> Unit,
    onWatchStateChange: (WatchState?) -> Unit,
    onSeriesStatusChange: (SeriesStatus?) -> Unit,
    onFavoritesOnlyChange: (Boolean) -> Unit,
    onWeekdayChange: (Int?) -> Unit,
    onSortOrderChange: (AnimeSortOrder) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(contentType = "filter_sheet_title") {
                Text(text = "筛选和排序", style = MaterialTheme.typography.titleMedium)
            }
            item(contentType = "filter_sheet_watch_state") {
                LibraryFilterGroup(title = "观看状态") {
                    AnimeWatchStateChips(
                        selected = uiState.watchState,
                        onSelect = onWatchStateChange,
                        wrapContent = true,
                    )
                }
            }
            item(contentType = "filter_sheet_series_status") {
                LibraryFilterGroup(title = "番剧状态") {
                    AnimeSeriesStatusChips(
                        selected = uiState.seriesStatus,
                        onSelect = onSeriesStatusChange,
                        wrapContent = true,
                    )
                }
            }
            item(contentType = "filter_sheet_weekday") {
                LibraryFilterGroup(title = "更新时间") {
                    AnimeWeekdayChips(
                        selectedWeekday = uiState.weekday,
                        onSelect = onWeekdayChange,
                        wrapContent = true,
                    )
                }
            }
            item(contentType = "filter_sheet_sort") {
                LibraryFilterGroup(title = "排序") {
                    AnimeChoiceChipRow(
                        options = AnimeSortOrder.entries,
                        selected = uiState.sortOrder,
                        onSelect = onSortOrderChange,
                        label = { it.displayLabel() },
                        wrapContent = true,
                    )
                }
            }
            item(contentType = "filter_sheet_favorites") {
                LibraryFavoritesOnlyRow(
                    favoritesOnly = uiState.favoritesOnly,
                    onFavoritesOnlyChange = onFavoritesOnlyChange,
                )
            }
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

@Composable
private fun LibraryFavoritesOnlyRow(
    favoritesOnly: Boolean,
    onFavoritesOnlyChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "仅收藏")
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            checked = favoritesOnly,
            onCheckedChange = onFavoritesOnlyChange,
        )
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

private fun libraryProgressLabel(summary: AnimeSummary): String? {
    return if (summary.watchState == WatchState.WATCHED) {
        summary.totalEpisodes
            ?.takeIf { it >= 0 }
            ?.let { "共${it}集" }
    } else {
        progressLabel(summary.progressEpisode, summary.totalEpisodes)
    }
}
