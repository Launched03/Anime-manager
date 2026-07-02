@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.animemanager.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.animemanager.core.model.HomeDashboard
import com.example.animemanager.core.ui.AnimeEmptyState
import com.example.animemanager.core.ui.AnimePosterListItem
import com.example.animemanager.core.ui.AnimeSectionHeader
import com.example.animemanager.core.ui.AnimeTopBar

@Composable
fun HomeRoute(
    onOpenAnime: (Long) -> Unit,
    onAddAnime: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        dashboard = uiState.dashboard,
        onOpenAnime = onOpenAnime,
        onAddAnime = onAddAnime,
    )
}

@Composable
fun HomeScreen(
    dashboard: HomeDashboard,
    onOpenAnime: (Long) -> Unit,
    onAddAnime: () -> Unit,
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
        val sections = listOf(
            "今日更新" to dashboard.todayUpdates,
            "继续追番" to dashboard.continueWatching,
            "收藏夹" to dashboard.favorites,
            "最近添加" to dashboard.recentlyAdded,
        )
        if (sections.all { it.second.isEmpty() }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                AnimeEmptyState(
                    title = "还没有番剧",
                    subtitle = "先添加一部番剧吧",
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                sections.forEach { (title, sectionItems) ->
                    if (sectionItems.isNotEmpty()) {
                        item(key = "${title}_header") {
                            AnimeSectionHeader(title = title)
                        }
                        items(sectionItems, key = { summary -> "${title}_${summary.id}" }) { summary ->
                            AnimePosterListItem(
                                summary = summary,
                                onClick = { onOpenAnime(summary.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}
