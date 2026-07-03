@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.animemanager.core.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.rememberLazyListState
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
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
fun AnimeSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "搜索",
    placeholder: String? = null,
    loading: Boolean = false,
    focusRequester: FocusRequester? = null,
    showSearchButton: Boolean = false,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        var textFieldModifier = if (showSearchButton) Modifier.weight(1f) else Modifier.fillMaxWidth()
        if (focusRequester != null) {
            textFieldModifier = textFieldModifier.focusRequester(focusRequester)
        }
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = textFieldModifier,
            label = { Text(label) },
            placeholder = placeholder?.let { text -> { Text(text) } },
            singleLine = true,
            leadingIcon = if (showSearchButton) {
                null
            } else {
                {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                if (!loading) {
                    onSearch()
                }
            }),
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
        if (showSearchButton) {
            IconButton(
                onClick = onSearch,
                enabled = !loading,
            ) {
                Icon(imageVector = Icons.Filled.Search, contentDescription = "搜索")
            }
        }
    }
}

fun Modifier.clearFocusOnTouchDown(
    onTouchDown: () -> Unit,
): Modifier {
    return pointerInput(onTouchDown) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                if (event.changes.any { it.changedToDownIgnoreConsumed() }) {
                    onTouchDown()
                }
            }
        }
    }
}

fun Modifier.clearFocusOnTapOutside(
    excludedBoundsInRoot: List<Rect>,
    containerBoundsInRoot: Rect?,
    onOutsideTap: () -> Unit,
): Modifier {
    return pointerInput(excludedBoundsInRoot, containerBoundsInRoot, onOutsideTap) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val down = event.changes.firstOrNull { it.changedToDownIgnoreConsumed() } ?: continue
                val containerBounds = containerBoundsInRoot ?: continue
                val rootPosition = Offset(
                    x = containerBounds.left + down.position.x,
                    y = containerBounds.top + down.position.y,
                )
                if (excludedBoundsInRoot.none { it.contains(rootPosition) }) {
                    onOutsideTap()
                }
            }
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
    decodeSize: DpSize? = null,
    fastThumbnail: Boolean = false,
    showMissingIcon: Boolean = true,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val model = rememberAnimeImageModel(posterRef)
    val shape = RoundedCornerShape(8.dp)
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val requestSize = decodeSize?.let { size ->
        with(density) {
            size.width.roundToPx().coerceAtLeast(1) to size.height.roundToPx().coerceAtLeast(1)
        }
    }
    val imageRequest = remember(context, model, requestSize, fastThumbnail, contentScale) {
        model?.let { value ->
            ImageRequest.Builder(context)
                .data(value)
                .crossfade(false)
                .apply {
                    requestSize?.let { (width, height) -> size(width, height) }
                    if (fastThumbnail) {
                        precision(Precision.INEXACT)
                        scale(if (contentScale == ContentScale.Fit) Scale.FIT else Scale.FILL)
                        allowRgb565(true)
                    }
                }
                .build()
        }
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        if (model == null) {
            if (showMissingIcon) {
                Icon(
                    imageVector = Icons.Filled.BrokenImage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(36.dp),
                )
            }
        } else {
            AsyncImage(
                model = imageRequest,
                contentDescription = null,
                contentScale = contentScale,
                filterQuality = if (fastThumbnail) FilterQuality.Low else FilterQuality.Medium,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
fun AnimeAdaptiveImage(
    posterRef: String?,
    modifier: Modifier = Modifier,
    minAspectRatio: Float = 0.56f,
    maxAspectRatio: Float = 2.2f,
) {
    val context = LocalContext.current
    val model = rememberAnimeImageModel(posterRef)
    var aspectRatio by remember(model) { mutableStateOf(2f / 3f) }

    if (model == null) {
        AnimePosterImage(
            posterRef = null,
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio),
            contentScale = ContentScale.Fit,
        )
    } else {
        AsyncImage(
            model = ImageRequest.Builder(context).data(model).crossfade(false).build(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            onSuccess = { state ->
                val drawable = state.result.drawable
                val width = drawable.intrinsicWidth
                val height = drawable.intrinsicHeight
                if (width > 0 && height > 0) {
                    aspectRatio = (width.toFloat() / height.toFloat())
                        .coerceIn(minAspectRatio, maxAspectRatio)
                }
            },
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
                .clip(RoundedCornerShape(8.dp)),
        )
    }
}

@Composable
private fun rememberAnimeImageModel(posterRef: String?): Any? {
    return remember(posterRef) {
        posterRef?.trim()?.takeIf { it.isNotBlank() }?.let { value ->
            when {
                value.startsWith("http://", ignoreCase = true) || value.startsWith("https://", ignoreCase = true) -> value
                value.startsWith("content://", ignoreCase = true) || value.startsWith("file://", ignoreCase = true) -> Uri.parse(value)
                value.startsWith("/") -> java.io.File(value)
                else -> value
            }
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
    val posterDecodeSize = remember { DpSize(72.dp, 96.dp) }
    val seasonText = remember(summary.seasonYear, summary.seasonName) {
        seasonLabel(summary.seasonYear, summary.seasonName)
    }
    val progressText = remember(summary.watchState, summary.progressEpisode, summary.totalEpisodes) {
        animeListProgressLabel(summary)
    }
    val scheduleText = remember(summary.seriesStatus, summary.releaseWeekday, summary.releaseMinuteOfDay) {
        if (summary.seriesStatus == SeriesStatus.FINISHED) {
            null
        } else {
            scheduleLabel(summary.releaseWeekday, summary.releaseMinuteOfDay)
        }
    }
    val tags = remember(summary.seriesStatus, summary.watchState, summary.isFavorite) {
        buildList {
            add(summary.seriesStatus.displayLabel())
            summary.watchState?.let { add(it.displayLabel()) }
            if (summary.isFavorite) add("收藏")
        }
    }

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
                modifier = Modifier.size(width = 72.dp, height = 96.dp),
                decodeSize = posterDecodeSize,
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
                    text = seasonText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                progressText?.let { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                scheduleText?.let { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    tags.forEach { tag -> AnimeListTag(text = tag) }
                }
            }
        }
    }
}

@Composable
private fun AnimeListTag(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
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
    wrapContent: Boolean = false,
) {
    AnimeFilterChipContainer(
        modifier = modifier,
        wrapContent = wrapContent,
    ) {
        AnimeFilterChip(
            selected = selected == null,
            onClick = { onSelect(null) },
            text = "全部",
        )
        WatchState.entries.forEach { state ->
            AnimeFilterChip(
                selected = selected == state,
                onClick = { onSelect(state) },
                text = state.displayLabel(),
            )
        }
    }
}

@Composable
fun AnimeSeriesStatusChips(
    selected: SeriesStatus?,
    onSelect: (SeriesStatus?) -> Unit,
    modifier: Modifier = Modifier,
    wrapContent: Boolean = false,
) {
    AnimeFilterChipContainer(
        modifier = modifier,
        wrapContent = wrapContent,
    ) {
        AnimeFilterChip(selected = selected == null, onClick = { onSelect(null) }, text = "全部")
        SeriesStatus.entries.forEach { status ->
            AnimeFilterChip(
                selected = selected == status,
                onClick = { onSelect(status) },
                text = status.displayLabel(),
            )
        }
    }
}

@Composable
fun AnimeWeekdayChips(
    selectedWeekday: Int?,
    onSelect: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    includeAll: Boolean = true,
    initialVisibleWeekday: Int? = null,
    wrapContent: Boolean = false,
) {
    if (wrapContent) {
        AnimeFilterChipContainer(
            modifier = modifier,
            wrapContent = true,
        ) {
            if (includeAll) {
                AnimeFilterChip(
                    selected = selectedWeekday == null,
                    onClick = { onSelect(null) },
                    text = "全部",
                )
            }
            WeekdayChipValues.forEach { weekday ->
                AnimeFilterChip(
                    selected = selectedWeekday == weekday,
                    onClick = { onSelect(weekday) },
                    text = weekdayLabel(weekday),
                )
            }
        }
        return
    }

    val initialIndex = remember(includeAll, initialVisibleWeekday) {
        initialWeekdayChipIndex(
            weekday = initialVisibleWeekday,
            includeAll = includeAll,
        )
    }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    LazyRow(
        state = listState,
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (includeAll) {
            item(key = "all") {
                AnimeFilterChip(
                    selected = selectedWeekday == null,
                    onClick = { onSelect(null) },
                    text = "全部",
                )
            }
        }
        items(WeekdayChipValues, key = { it }) { weekday ->
            AnimeFilterChip(
                selected = selectedWeekday == weekday,
                onClick = { onSelect(weekday) },
                text = weekdayLabel(weekday),
            )
        }
    }
}

private fun initialWeekdayChipIndex(
    weekday: Int?,
    includeAll: Boolean,
): Int {
    val selected = weekday?.coerceIn(1, 7) ?: return 0
    return if (includeAll) selected else selected - 1
}

@Composable
fun <T> AnimeChoiceChipRow(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
    wrapContent: Boolean = false,
) {
    AnimeFilterChipContainer(
        modifier = modifier,
        wrapContent = wrapContent,
    ) {
        options.forEach { option ->
            AnimeFilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                text = label(option),
            )
        }
    }
}

private val WeekdayChipValues = (1..7).toList()

@Composable
private fun AnimeFilterChipContainer(
    modifier: Modifier = Modifier,
    wrapContent: Boolean,
    content: @Composable () -> Unit,
) {
    if (wrapContent) {
        FlowRow(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = { content() },
        )
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = { content() },
        )
    }
}

@Composable
private fun AnimeFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(18.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val backgroundColor = if (selected) colorScheme.primaryContainer else colorScheme.surfaceVariant
    val contentColor = if (selected) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant
    val borderColor = if (selected) colorScheme.primary else colorScheme.outlineVariant

    Text(
        text = text,
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        style = MaterialTheme.typography.labelLarge,
        color = contentColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
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
