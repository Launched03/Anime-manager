@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)

package com.example.animemanager.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.animemanager.core.data.remote.RemoteAnimeSearchResult
import com.example.animemanager.core.ui.AnimeAdaptiveImage
import com.example.animemanager.core.ui.AnimeEmptyState
import com.example.animemanager.core.ui.AnimePosterImage
import com.example.animemanager.core.ui.AnimeSearchField
import com.example.animemanager.core.ui.AnimeSectionHeader
import com.example.animemanager.core.ui.AnimeTopBar
import com.example.animemanager.core.ui.clearFocusOnTapOutside

@Composable
fun HomeSearchRoute(
    onBack: () -> Unit,
    viewModel: HomeSearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.userMessage) {
        val message = uiState.userMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.consumeUserMessage()
    }

    HomeSearchScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onQueryChange = viewModel::updateQuery,
        onSearch = viewModel::search,
        onLoadMore = viewModel::loadMore,
        onAddRemoteAnime = viewModel::addRemoteAnime,
    )
}

@Composable
fun HomeSearchScreen(
    uiState: HomeSearchUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onLoadMore: () -> Unit,
    onAddRemoteAnime: (RemoteAnimeSearchResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val searchFocusRequester = remember { FocusRequester() }
    var listBoundsInRoot by remember { mutableStateOf<Rect?>(null) }
    var searchBoundsInRoot by remember { mutableStateOf<Rect?>(null) }
    var selectedRemoteSourceId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedRemoteResult = selectedRemoteSourceId?.let { sourceId ->
        uiState.results.firstOrNull { it.sourceId == sourceId }
    }
    val clearSearchFocus: () -> Unit = {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }

    LaunchedEffect(Unit) {
        searchFocusRequester.requestFocus()
        keyboardController?.show()
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0.dp),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AnimeTopBar(
                title = "在线搜索",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .onGloballyPositioned { coordinates ->
                    listBoundsInRoot = coordinates.boundsInRoot()
                }
                .clearFocusOnTapOutside(
                    excludedBoundsInRoot = listOfNotNull(searchBoundsInRoot),
                    containerBoundsInRoot = listBoundsInRoot,
                    onOutsideTap = clearSearchFocus,
                ),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                AnimeSearchField(
                    query = uiState.query,
                    onQueryChange = onQueryChange,
                    onSearch = {
                        clearSearchFocus()
                        onSearch()
                    },
                    label = "在线搜索",
                    placeholder = "番剧名 / 2024 / 2024 春季",
                    loading = uiState.loading,
                    focusRequester = searchFocusRequester,
                    showSearchButton = true,
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        searchBoundsInRoot = coordinates.boundsInRoot()
                    },
                )
            }
            uiState.error?.let { message ->
                item {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            if (uiState.loading) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text(text = "正在连接 Bangumi...")
                    }
                }
            }
            if (uiState.results.isNotEmpty()) {
                item(key = "online_results_header") {
                    AnimeSectionHeader(title = "在线搜索结果（${uiState.results.size}）")
                }
                items(uiState.results, key = { result -> "remote_${result.sourceId}" }) { result ->
                    HomeRemoteResultItem(
                        result = result,
                        onOpenDetail = {
                            clearSearchFocus()
                            selectedRemoteSourceId = result.sourceId
                        },
                        onAdd = { onAddRemoteAnime(result) },
                    )
                }
                item(key = "load_more") {
                    HomeSearchLoadMore(
                        canLoadMore = uiState.canLoadMore,
                        loadingMore = uiState.loadingMore,
                        onLoadMore = onLoadMore,
                    )
                }
            } else if (uiState.hasSearched && !uiState.loading && uiState.error == null) {
                item {
                    AnimeEmptyState(
                        title = "没有找到番剧",
                        subtitle = "试试换个关键词或年份季度",
                    )
                }
            }
        }
    }

    selectedRemoteResult?.let { result ->
        HomeRemoteDetailDialog(
            result = result,
            onDismiss = {
                clearSearchFocus()
                selectedRemoteSourceId = null
            },
            onAdd = {
                clearSearchFocus()
                onAddRemoteAnime(result)
                selectedRemoteSourceId = null
            },
        )
    }
}

@Composable
private fun HomeRemoteResultItem(
    result: RemoteAnimeSearchResult,
    onOpenDetail: () -> Unit,
    onAdd: () -> Unit,
) {
    Surface(
        onClick = onOpenDetail,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            AnimePosterImage(
                posterRef = result.posterRef,
                modifier = Modifier.size(width = 54.dp, height = 72.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                result.originalTitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    result.seasonYear?.let { AssistChip(onClick = {}, label = { Text(it.toString()) }) }
                    result.seasonName?.let { AssistChip(onClick = {}, label = { Text(it) }) }
                    result.airDate?.let { AssistChip(onClick = {}, label = { Text(it) }) }
                    result.totalEpisodes?.let { AssistChip(onClick = {}, label = { Text("共${it}集") }) }
                }
                result.synopsis?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        maxLines = 2,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            OutlinedButton(onClick = onAdd) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("添加")
            }
        }
    }
}

@Composable
private fun HomeSearchLoadMore(
    canLoadMore: Boolean,
    loadingMore: Boolean,
    onLoadMore: () -> Unit,
) {
    if (loadingMore) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "正在加载更多...")
        }
    } else if (canLoadMore) {
        OutlinedButton(
            onClick = onLoadMore,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("加载更多")
        }
    } else {
        Text(
            text = "已显示全部结果",
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun HomeRemoteDetailDialog(
    result: RemoteAnimeSearchResult,
    onDismiss: () -> Unit,
    onAdd: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = result.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    AnimeAdaptiveImage(
                        posterRef = result.posterRef,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                result.originalTitle?.let { originalTitle ->
                    item {
                        Text(
                            text = originalTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        result.seasonYear?.let { AssistChip(onClick = {}, label = { Text(it.toString()) }) }
                        result.seasonName?.let { AssistChip(onClick = {}, label = { Text(it) }) }
                        result.airDate?.let { AssistChip(onClick = {}, label = { Text(it) }) }
                        result.totalEpisodes?.let { AssistChip(onClick = {}, label = { Text("共${it}集") }) }
                        AssistChip(onClick = {}, label = { Text(result.seriesStatus.displayLabel()) })
                    }
                }
                item {
                    Text(
                        text = result.synopsis?.takeIf { it.isNotBlank() } ?: "暂无简介",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        },
        confirmButton = {
            OutlinedButton(onClick = onAdd) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("添加")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("返回")
            }
        },
    )
}
