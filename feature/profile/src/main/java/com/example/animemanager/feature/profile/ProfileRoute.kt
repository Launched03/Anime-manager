@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.animemanager.feature.profile

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.animemanager.core.ui.AnimeStatTile
import com.example.animemanager.core.ui.AnimeTopBar

@Composable
fun ProfileRoute(
    onOpenSettings: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileScreen(
        uiState = uiState,
        onOpenSettings = onOpenSettings,
    )
}

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stats = listOf(
        "全部" to uiState.counts.total,
        "想看" to uiState.counts.wantToWatch,
        "在看" to uiState.counts.watching,
        "已看" to uiState.counts.watched,
        "搁置" to uiState.counts.hold,
        "收藏" to uiState.counts.favorite,
    )

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            AnimeTopBar(
                title = "我的",
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(imageVector = Icons.Filled.Settings, contentDescription = "设置")
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
                Text(text = "统计")
            }
            items(stats.chunked(2)) { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    rowItems.forEach { (label, value) ->
                        AnimeStatTile(
                            label = label,
                            value = value.toString(),
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            item {
                OutlinedButton(onClick = onOpenSettings) {
                    Text(text = "设置")
                }
            }
        }
    }
}
