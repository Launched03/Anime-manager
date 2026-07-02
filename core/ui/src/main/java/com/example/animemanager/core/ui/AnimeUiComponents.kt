@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.animemanager.core.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.animemanager.core.model.AnimeSummary
import com.example.animemanager.core.model.SeriesStatus
import com.example.animemanager.core.model.WatchState
import com.example.animemanager.core.model.progressLabel
import com.example.animemanager.core.model.scheduleLabel
import com.example.animemanager.core.model.seasonLabel
import com.example.animemanager.core.model.weekdayLabel

@Composable
fun AnimeTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(52.dp)
                .padding(start = if (navigationIcon == null) 24.dp else 4.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (navigationIcon != null) {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    navigationIcon()
                }
            }
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = actions,
            )
        }
    }
}

@Composable
fun AnimeSectionHeader(
    title: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        if (actionLabel != null && onActionClick != null) {
            AssistChip(onClick = onActionClick, label = { Text(actionLabel) })
        }
    }
}

@Composable
fun AnimeEmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Image,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(40.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun AnimePosterImage(
    posterRef: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val context = LocalContext.current
    val model = remember(posterRef) {
        posterRef?.trim()?.takeIf { it.isNotBlank() }?.let { value ->
            when {
                value.startsWith("http://", ignoreCase = true) || value.startsWith("https://", ignoreCase = true) -> value
                value.startsWith("content://", ignoreCase = true) || value.startsWith("file://", ignoreCase = true) -> Uri.parse(value)
                value.startsWith("/") -> java.io.File(value)
                else -> value
            }
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        if (model == null) {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.BrokenImage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(36.dp),
                )
            }
        } else {
            AsyncImage(
                model = ImageRequest.Builder(context).data(model).crossfade(false).build(),
                contentDescription = null,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
fun AnimeStatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = value, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AnimePosterListItem(
    summary: AnimeSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            AnimePosterImage(
                posterRef = summary.posterRef,
                modifier = Modifier
                    .size(width = 72.dp, height = 96.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = summary.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                summary.originalTitle?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = seasonLabel(summary.seasonYear, summary.seasonName),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                animeListProgressLabel(summary)?.let { progressText ->
                    Text(
                        text = progressText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (summary.seriesStatus != SeriesStatus.FINISHED) {
                    Text(
                        text = scheduleLabel(summary.releaseWeekday, summary.releaseMinuteOfDay),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    AssistChip(onClick = {}, label = { Text(summary.seriesStatus.displayLabel()) })
                    summary.watchState?.let {
                        AssistChip(onClick = {}, label = { Text(it.displayLabel()) })
                    }
                    if (summary.isFavorite) {
                        AssistChip(onClick = {}, label = { Text("收藏") })
                    }
                }
            }
        }
    }
}

private fun animeListProgressLabel(summary: AnimeSummary): String? {
    return if (summary.watchState == WatchState.WATCHED) {
        summary.totalEpisodes
            ?.takeIf { it >= 0 }
            ?.let { "共${it}集" }
    } else {
        progressLabel(summary.progressEpisode, summary.totalEpisodes)
    }
}

@Composable
fun AnimeProgressStepper(
    progress: Int,
    totalEpisodes: Int?,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedButton(onClick = onDecrease) {
            Icon(imageVector = Icons.Filled.Remove, contentDescription = null)
        }
        Text(
            text = progressLabel(progress, totalEpisodes),
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedButton(onClick = onIncrease) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = null)
        }
    }
}

@Composable
fun AnimeWatchStateChips(
    selected: WatchState?,
    onSelect: (WatchState?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelect(null) },
            label = { Text("全部") },
        )
        WatchState.entries.forEach { state ->
            FilterChip(
                selected = selected == state,
                onClick = { onSelect(state) },
                label = { Text(state.displayLabel()) },
            )
        }
    }
}

@Composable
fun AnimeSeriesStatusChips(
    selected: SeriesStatus?,
    onSelect: (SeriesStatus?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text("全部") })
        SeriesStatus.entries.forEach { status ->
            FilterChip(
                selected = selected == status,
                onClick = { onSelect(status) },
                label = { Text(status.displayLabel()) },
            )
        }
    }
}

@Composable
fun AnimeWeekdayChips(
    selectedWeekday: Int?,
    onSelect: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterChip(selected = selectedWeekday == null, onClick = { onSelect(null) }, label = { Text("全部") })
        for (weekday in 1..7) {
            FilterChip(
                selected = selectedWeekday == weekday,
                onClick = { onSelect(weekday) },
                label = { Text(weekdayLabel(weekday)) },
            )
        }
    }
}

@Composable
fun <T> AnimeChoiceChipRow(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(label(option)) },
            )
        }
    }
}

@Composable
fun AnimePosterHeader(
    posterRef: String?,
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        AnimePosterImage(
            posterRef = posterRef,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
        )
        Column(modifier = Modifier.padding(top = 12.dp)) {
            Text(text = title, style = MaterialTheme.typography.headlineSmall)
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun AnimeScheduleChip(
    weekday: Int?,
    minuteOfDay: Int?,
    modifier: Modifier = Modifier,
) {
    AssistChip(
        onClick = {},
        modifier = modifier,
        label = { Text(scheduleLabel(weekday, minuteOfDay)) },
    )
}

@Composable
fun AnimeFavoriteToggle(
    favorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AssistChip(
        onClick = onToggle,
        modifier = modifier,
        label = { Text(if (favorite) "已收藏" else "收藏") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = if (favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )
}
