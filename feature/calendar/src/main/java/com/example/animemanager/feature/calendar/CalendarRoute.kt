@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.animemanager.feature.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.animemanager.core.model.weekdayLabel
import com.example.animemanager.core.ui.AnimeEmptyState
import com.example.animemanager.core.ui.AnimePosterListItem
import com.example.animemanager.core.ui.AnimeTopBar
import com.example.animemanager.core.ui.AnimeWeekdayChips

@Composable
fun CalendarRoute(
    onOpenAnime: (Long) -> Unit,
    onAddAnime: () -> Unit,
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val animeList by viewModel.animeList.collectAsStateWithLifecycle()
    CalendarScreen(
        uiState = uiState,
        animeList = animeList,
        onOpenAnime = onOpenAnime,
        onAddAnime = onAddAnime,
        onWeekdayChange = viewModel::selectWeekday,
        onScheduledOnlyChange = { if (uiState.showOnlyScheduled != it) viewModel.toggleScheduledOnly() },
    )
}

@Composable
fun CalendarScreen(
    uiState: CalendarUiState,
    animeList: List<com.example.animemanager.core.model.AnimeSummary>,
    onOpenAnime: (Long) -> Unit,
    onAddAnime: () -> Unit,
    onWeekdayChange: (Int) -> Unit,
    onScheduledOnlyChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            AnimeTopBar(
                title = "更新日历",
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    RowWithSwitch(
                        label = "仅显示排期",
                        checked = uiState.showOnlyScheduled,
                        onCheckedChange = onScheduledOnlyChange,
                    )
                    AnimeWeekdayChips(
                        selectedWeekday = uiState.selectedWeekday,
                        onSelect = { value -> value?.let(onWeekdayChange) },
                    )
                    Text(text = weekdayLabel(uiState.selectedWeekday))
                }
            }
            if (animeList.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        AnimeEmptyState(
                            title = "当天没有排期番剧",
                            subtitle = "换个星期看看，或者添加新的更新时间",
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
private fun RowWithSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label)
        Spacer(modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
