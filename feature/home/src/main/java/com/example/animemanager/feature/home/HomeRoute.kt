@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.animemanager.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.animemanager.core.model.HomeDashboard
import com.example.animemanager.core.ui.AnimeEmptyState
import com.example.animemanager.core.ui.AnimePosterListItem
import com.example.animemanager.core.ui.AnimeSectionHeader
import com.example.animemanager.core.ui.AnimeTopBar

@Composable
fun HomeRoute(
    onOpenAnime: (Long) -> Unit,
    onAddAnime: () -> Unit,
    onOpenSearch: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        dashboard = uiState.dashboard,
        onOpenAnime = onOpenAnime,
        onAddAnime = onAddAnime,
        onOpenSearch = onOpenSearch,
    )
}

@Composable
fun HomeScreen(
    dashboard: HomeDashboard,
    onOpenAnime: (Long) -> Unit,
    onAddAnime: () -> Unit,
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            AnimeTopBar(
                title = "首页",
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
                HomeSearchEntry(onClick = onOpenSearch)
            }

            if (dashboard.favorites.isNotEmpty()) {
                item(key = "favorites_header") {
                    AnimeSectionHeader(title = "收藏夹")
                }
                items(dashboard.favorites, key = { summary -> "favorite_${summary.id}" }) { summary ->
                    AnimePosterListItem(
                        summary = summary,
                        onClick = { onOpenAnime(summary.id) },
                    )
                }
            } else {
                item {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        AnimeEmptyState(
                            title = "还没有收藏番剧",
                            subtitle = "可以先在线搜索添加，或在详情页点收藏",
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeSearchEntry(
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "在线搜索",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "番剧名 / 2024 / 2024 春季",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
