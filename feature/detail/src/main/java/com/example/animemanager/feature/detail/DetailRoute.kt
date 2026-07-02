@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.animemanager.feature.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.animemanager.core.model.SeriesStatus
import com.example.animemanager.core.model.WatchState
import com.example.animemanager.core.model.premiereDateLabel
import com.example.animemanager.core.model.progressLabel
import com.example.animemanager.core.model.scheduleLabel
import com.example.animemanager.core.model.seasonLabel
import com.example.animemanager.core.ui.AnimeEmptyState
import com.example.animemanager.core.ui.AnimeFavoriteToggle
import com.example.animemanager.core.ui.AnimePosterImage
import com.example.animemanager.core.ui.AnimeProgressStepper
import com.example.animemanager.core.ui.AnimeScheduleChip
import com.example.animemanager.core.ui.AnimeChoiceChipRow
import com.example.animemanager.core.ui.AnimeTopBar

@Composable
fun DetailRoute(
    animeId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDeleted: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel(),
) {
    LaunchedEffect(animeId) {
        viewModel.load(animeId)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val anime = uiState.anime
    if (anime == null) {
        DetailScreen(
            anime = null,
            onBack = onBack,
            onEdit = { onEdit(animeId) },
            onDeleteConfirmed = {
                viewModel.deleteAnime(animeId, onDeleted)
            },
            onFavoriteToggle = { },
            onWatchStateChange = { },
            onProgressDecrease = { },
            onProgressIncrease = { },
        )
    } else {
        DetailScreen(
            anime = anime,
            onBack = onBack,
            onEdit = { onEdit(anime.id) },
            onDeleteConfirmed = {
                viewModel.deleteAnime(anime.id, onDeleted)
            },
            onFavoriteToggle = { viewModel.toggleFavorite(anime.id, !anime.isFavorite) },
            onWatchStateChange = { viewModel.setWatchState(anime.id, it) },
            onProgressDecrease = { viewModel.updateProgress(anime.id, anime.progressEpisode - 1) },
            onProgressIncrease = { viewModel.updateProgress(anime.id, anime.progressEpisode + 1) },
        )
    }
}

@Composable
fun DetailScreen(
    anime: com.example.animemanager.core.model.AnimeDetail?,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDeleteConfirmed: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onWatchStateChange: (WatchState) -> Unit,
    onProgressDecrease: () -> Unit,
    onProgressIncrease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            AnimeTopBar(
                title = anime?.title ?: "番剧详情",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (anime != null) {
                        IconButton(onClick = { onEdit(anime.id) }) {
                            Icon(imageVector = Icons.Filled.Edit, contentDescription = "编辑")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(imageVector = Icons.Filled.Delete, contentDescription = "删除")
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        if (anime == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                AnimeEmptyState(
                    title = "番剧不存在",
                    subtitle = "可能已被删除",
                )
            }
        } else {
            val synopsis = anime.synopsis?.takeIf { it.isNotBlank() }
            val canShowSchedule = anime.seriesStatus != SeriesStatus.FINISHED
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    AnimePosterImage(
                        posterRef = anime.posterRef,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f / 3f),
                    )
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = anime.title, style = MaterialTheme.typography.headlineSmall)
                        anime.originalTitle?.takeIf { it.isNotBlank() }?.let {
                            Text(text = it, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(text = seasonLabel(anime.seasonYear, anime.seasonName))
                        Text(text = "开播时间：${premiereDateLabel(anime.premiereDate)}")
                        Text(text = progressLabel(anime.progressEpisode, anime.totalEpisodes))
                        if (canShowSchedule) {
                            Text(text = scheduleLabel(anime.releaseWeekday, anime.releaseMinuteOfDay))
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AnimeFavoriteToggle(
                            favorite = anime.isFavorite,
                            onToggle = onFavoriteToggle,
                        )
                        AssistChip(
                            onClick = { },
                            label = { Text(anime.seriesStatus.displayLabel()) },
                        )
                        if (canShowSchedule) {
                            AnimeScheduleChip(
                                weekday = anime.releaseWeekday,
                                minuteOfDay = anime.releaseMinuteOfDay,
                            )
                        }
                    }
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "观看状态")
                        AnimeChoiceChipRow(
                            options = WatchState.entries,
                            selected = anime.watchState ?: WatchState.WANT_TO_WATCH,
                            onSelect = onWatchStateChange,
                            label = { it.displayLabel() },
                        )
                    }
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "进度")
                        AnimeProgressStepper(
                            progress = anime.progressEpisode,
                            totalEpisodes = anime.totalEpisodes,
                            onDecrease = onProgressDecrease,
                            onIncrease = onProgressIncrease,
                        )
                    }
                }
                if (synopsis != null) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "简介")
                            Text(text = synopsis)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog && anime != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除番剧") },
            text = { Text("删除后会一并清除相关状态。") },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteConfirmed()
                    },
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            },
        )
    }
}
