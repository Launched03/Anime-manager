@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.animemanager.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.animemanager.core.model.AnimeSortOrder
import com.example.animemanager.core.model.ThemeMode
import com.example.animemanager.core.ui.AnimeChoiceChipRow
import com.example.animemanager.core.ui.AnimeTopBar

@Composable
fun SettingsRoute(
    appVersionName: String,
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreen(
        settings = settings,
        appVersionName = appVersionName,
        onBack = onBack,
        onThemeModeChange = viewModel::updateThemeMode,
        onSortOrderChange = viewModel::updateDefaultSortOrder,
    )
}

@Composable
fun SettingsScreen(
    settings: com.example.animemanager.core.model.AppSettings,
    appVersionName: String,
    onBack: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onSortOrderChange: (AnimeSortOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            AnimeTopBar(
                title = "设置",
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
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(text = "主题模式")
                    AnimeChoiceChipRow(
                        options = ThemeMode.entries,
                        selected = settings.themeMode,
                        onSelect = onThemeModeChange,
                        label = { it.displayLabel() },
                    )
                }
            }
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(text = "默认排序")
                    AnimeChoiceChipRow(
                        options = AnimeSortOrder.entries,
                        selected = settings.defaultSortOrder,
                        onSelect = onSortOrderChange,
                        label = { it.displayLabel() },
                    )
                }
            }
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(text = "版本号")
                    Text(
                        text = "v$appVersionName",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
