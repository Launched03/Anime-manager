@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)

package com.example.animemanager.feature.edit

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.animemanager.core.data.remote.BangumiSeasonFilter
import com.example.animemanager.core.data.remote.RemoteAnimeSearchResult
import com.example.animemanager.core.model.AnimeForm
import com.example.animemanager.core.model.SeriesStatus
import com.example.animemanager.core.model.WatchState
import com.example.animemanager.core.model.buildMinuteOfDay
import com.example.animemanager.core.model.splitMinuteOfDay
import com.example.animemanager.core.ui.AnimeChoiceChipRow
import com.example.animemanager.core.ui.AnimePosterImage
import com.example.animemanager.core.ui.AnimeSearchField
import com.example.animemanager.core.ui.AnimeTopBar
import com.example.animemanager.core.ui.clearFocusOnTapOutside
import java.io.File
import java.io.FileOutputStream

@Composable
fun EditRoute(
    animeId: Long?,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
    viewModel: EditViewModel = hiltViewModel(),
) {
    LaunchedEffect(animeId) {
        viewModel.load(animeId)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState.savedAnimeId) {
        val savedId = uiState.savedAnimeId ?: return@LaunchedEffect
        onSaved(savedId)
        viewModel.consumeSavedAnimeId()
    }
    EditScreen(
        uiState = uiState,
        form = uiState.form,
        loading = uiState.loading,
        onBack = onBack,
        onSave = viewModel::save,
        onRemoteSearchQueryChange = viewModel::updateRemoteSearchQuery,
        onRemoteSearchYearChange = viewModel::updateRemoteSearchYear,
        onRemoteSearchSeasonChange = viewModel::updateRemoteSearchSeason,
        onRemoteSearch = viewModel::searchRemoteAnime,
        onRemoteSearchDismiss = viewModel::clearRemoteSearch,
        onRemoteAnimeImport = viewModel::importRemoteAnime,
        onTitleChange = viewModel::updateTitle,
        onOriginalTitleChange = viewModel::updateOriginalTitle,
        onPosterRefChange = viewModel::updatePosterRef,
        onSynopsisChange = viewModel::updateSynopsis,
        onSeriesStatusChange = viewModel::updateSeriesStatus,
        onTotalEpisodesChange = viewModel::updateTotalEpisodes,
        onUpdatedEpisodesChange = viewModel::updateUpdatedEpisodes,
        onSeasonYearChange = viewModel::updateSeasonYear,
        onSeasonNameChange = viewModel::updateSeasonName,
        onPremiereDateChange = viewModel::updatePremiereDate,
        onReleaseWeekdayChange = viewModel::updateReleaseWeekday,
        onReleaseMinuteOfDayChange = viewModel::updateReleaseMinuteOfDay,
        onWatchStateChange = viewModel::updateWatchState,
        onProgressEpisodeChange = viewModel::updateProgressEpisode,
        onFavoriteChange = viewModel::updateFavorite,
    )
}

@Composable
fun EditScreen(
    uiState: EditUiState,
    form: AnimeForm,
    loading: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onRemoteSearchQueryChange: (String) -> Unit,
    onRemoteSearchYearChange: (String) -> Unit,
    onRemoteSearchSeasonChange: (BangumiSeasonFilter?) -> Unit,
    onRemoteSearch: () -> Unit,
    onRemoteSearchDismiss: () -> Unit,
    onRemoteAnimeImport: (RemoteAnimeSearchResult) -> Unit,
    onTitleChange: (String) -> Unit,
    onOriginalTitleChange: (String) -> Unit,
    onPosterRefChange: (String) -> Unit,
    onSynopsisChange: (String) -> Unit,
    onSeriesStatusChange: (SeriesStatus) -> Unit,
    onTotalEpisodesChange: (Int?) -> Unit,
    onUpdatedEpisodesChange: (Int?) -> Unit,
    onSeasonYearChange: (Int?) -> Unit,
    onSeasonNameChange: (String?) -> Unit,
    onPremiereDateChange: (String?) -> Unit,
    onReleaseWeekdayChange: (Int?) -> Unit,
    onReleaseMinuteOfDayChange: (Int?) -> Unit,
    onWatchStateChange: (WatchState) -> Unit,
    onProgressEpisodeChange: (Int) -> Unit,
    onFavoriteChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showRemoteSearch by rememberSaveable { mutableStateOf(false) }
    val posterPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            copyPosterToPrivateDir(context, uri)?.let(onPosterRefChange)
        }
    }
    var releaseTimeText by rememberSaveable(form.id, form.releaseMinuteOfDay) {
        mutableStateOf(formatMinuteOfDay(form.releaseMinuteOfDay))
    }
    LaunchedEffect(form.releaseMinuteOfDay) {
        val formatted = formatMinuteOfDay(form.releaseMinuteOfDay)
        if (releaseTimeText != formatted) {
            releaseTimeText = formatted
        }
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            AnimeTopBar(
                title = if (form.id == 0L) "新建番剧" else "编辑番剧",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onSave, enabled = !loading) {
                        Icon(imageVector = Icons.Filled.Check, contentDescription = "保存")
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
            uiState.saveError?.let { message ->
                item {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnimePosterImage(
                        posterRef = form.posterRef,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f / 3f),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { posterPicker.launch("image/*") }) {
                            Icon(imageVector = Icons.Filled.Image, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("选择海报")
                        }
                        OutlinedButton(onClick = { showRemoteSearch = true }) {
                            Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("在线搜索")
                        }
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "基础信息")
                    OutlinedTextField(
                        value = form.title,
                        onValueChange = onTitleChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("标题") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = form.originalTitle.orEmpty(),
                        onValueChange = onOriginalTitleChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("原名") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = form.posterRef.orEmpty(),
                        onValueChange = onPosterRefChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("海报地址或本地路径") },
                    )
                    OutlinedTextField(
                        value = form.synopsis.orEmpty(),
                        onValueChange = onSynopsisChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("简介") },
                        minLines = 3,
                    )
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "番剧状态")
                    AnimeChoiceChipRow(
                        options = SeriesStatus.entries,
                        selected = form.seriesStatus,
                        onSelect = onSeriesStatusChange,
                        label = { it.displayLabel() },
                    )
                    AnimeChoiceChipRow(
                        options = WatchState.entries,
                        selected = form.watchState,
                        onSelect = onWatchStateChange,
                        label = { it.displayLabel() },
                    )
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "集数与进度")
                    IntField(label = "总集数", value = form.totalEpisodes, onValueChange = onTotalEpisodesChange)
                    IntField(label = "已更新集数", value = form.updatedEpisodes, onValueChange = onUpdatedEpisodesChange)
                    IntField(
                        label = "已看进度",
                        value = form.progressEpisode,
                        onValueChange = { onProgressEpisodeChange(it ?: 0) },
                    )
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "季度信息")
                    IntField(label = "年份", value = form.seasonYear, onValueChange = onSeasonYearChange)
                    OutlinedTextField(
                        value = form.seasonName.orEmpty(),
                        onValueChange = { onSeasonNameChange(it.ifBlank { null }) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("季度") },
                    )
                    OutlinedTextField(
                        value = form.premiereDate.orEmpty(),
                        onValueChange = { onPremiereDateChange(it.trim().ifBlank { null }) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("开播日期 (YYYY-MM-DD)") },
                        singleLine = true,
                    )
                }
            }
            if (form.seriesStatus != SeriesStatus.FINISHED) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = "更新时间")
                        AnimeChoiceChipRow(
                            options = listOf(null, 1, 2, 3, 4, 5, 6, 7),
                            selected = form.releaseWeekday,
                            onSelect = onReleaseWeekdayChange,
                            label = { weekdayLabel(it) },
                        )
                        OutlinedTextField(
                            value = releaseTimeText,
                            onValueChange = { text ->
                                releaseTimeText = text
                                parseMinuteOfDay(text)?.let(onReleaseMinuteOfDayChange)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("时间 (HH:MM)") },
                            singleLine = true,
                        )
                    }
                }
            }
        }
    }

    if (showRemoteSearch) {
        BangumiSearchDialog(
            uiState = uiState,
            onQueryChange = onRemoteSearchQueryChange,
            onYearChange = onRemoteSearchYearChange,
            onSeasonChange = onRemoteSearchSeasonChange,
            onSearch = onRemoteSearch,
            onDismiss = {
                showRemoteSearch = false
                onRemoteSearchDismiss()
            },
            onImport = { result ->
                onRemoteAnimeImport(result)
                showRemoteSearch = false
            },
        )
    }
}

@Composable
private fun BangumiSearchDialog(
    uiState: EditUiState,
    onQueryChange: (String) -> Unit,
    onYearChange: (String) -> Unit,
    onSeasonChange: (BangumiSeasonFilter?) -> Unit,
    onSearch: () -> Unit,
    onDismiss: () -> Unit,
    onImport: (RemoteAnimeSearchResult) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var contentBoundsInRoot by remember { mutableStateOf<Rect?>(null) }
    var queryBoundsInRoot by remember { mutableStateOf<Rect?>(null) }
    var yearBoundsInRoot by remember { mutableStateOf<Rect?>(null) }
    val clearSearchFocus: () -> Unit = {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }
    val search: () -> Unit = {
        clearSearchFocus()
        onSearch()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bangumi 在线搜索") },
        text = {
            Column(
                modifier = Modifier
                    .onGloballyPositioned { coordinates ->
                        contentBoundsInRoot = coordinates.boundsInRoot()
                    }
                    .clearFocusOnTapOutside(
                        excludedBoundsInRoot = listOfNotNull(queryBoundsInRoot, yearBoundsInRoot),
                        containerBoundsInRoot = contentBoundsInRoot,
                        onOutsideTap = clearSearchFocus,
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AnimeSearchField(
                    query = uiState.remoteSearchQuery,
                    onQueryChange = onQueryChange,
                    onSearch = search,
                    label = "番剧名",
                    placeholder = "搜索 Bangumi 番剧",
                    loading = uiState.remoteSearchLoading,
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        queryBoundsInRoot = coordinates.boundsInRoot()
                    },
                )
                OutlinedTextField(
                    value = uiState.remoteSearchYear,
                    onValueChange = onYearChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            yearBoundsInRoot = coordinates.boundsInRoot()
                        },
                    label = { Text("年份，可选") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Search,
                    ),
                    keyboardActions = KeyboardActions(onSearch = { search() }),
                )
                AnimeChoiceChipRow(
                    options = listOf<BangumiSeasonFilter?>(null) + BangumiSeasonFilter.entries,
                    selected = uiState.remoteSearchSeason,
                    onSelect = onSeasonChange,
                    label = { it?.displayLabel ?: "全年" },
                )
                uiState.remoteSearchError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (uiState.remoteSearchLoading) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text(text = "正在连接 Bangumi...")
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.remoteSearchResults, key = { it.sourceId }) { result ->
                        BangumiResultItem(
                            result = result,
                            onClick = { onImport(result) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(
                onClick = search,
                enabled = !uiState.remoteSearchLoading,
            ) {
                Text("搜索")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
    )
}

@Composable
private fun BangumiResultItem(
    result: RemoteAnimeSearchResult,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AnimePosterImage(
                posterRef = result.posterRef,
                modifier = Modifier.size(width = 54.dp, height = 72.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(text = result.title, style = MaterialTheme.typography.titleSmall)
                result.originalTitle?.let {
                    Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        }
    }
}

@Composable
private fun IntField(
    label: String,
    value: Int?,
    onValueChange: (Int?) -> Unit,
) {
    OutlinedTextField(
        value = value?.toString().orEmpty(),
        onValueChange = { text -> onValueChange(text.toIntOrNull()) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

private fun weekdayLabel(value: Int?): String = when (value) {
    null -> "未设置"
    1 -> "周一"
    2 -> "周二"
    3 -> "周三"
    4 -> "周四"
    5 -> "周五"
    6 -> "周六"
    7 -> "周日"
    else -> "未设置"
}

private fun formatMinuteOfDay(minuteOfDay: Int?): String {
    val parts = splitMinuteOfDay(minuteOfDay) ?: return ""
    return "%02d:%02d".format(parts.first, parts.second)
}

private fun parseMinuteOfDay(text: String): Int? {
    val value = text.trim()
    if (value.isBlank()) return null
    val parts = value.split(":")
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    if (hour !in 0..23 || minute !in 0..59) return null
    return buildMinuteOfDay(hour, minute)
}

private fun copyPosterToPrivateDir(context: Context, uri: Uri): String? {
    val inputStream = context.contentResolver.openInputStream(uri) ?: return null
    val mimeType = context.contentResolver.getType(uri)
    val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)?.takeIf { it.isNotBlank() } ?: "jpg"
    val directory = File(context.filesDir, "anime_posters").apply { mkdirs() }
    val file = File(directory, "poster_${System.currentTimeMillis()}.$extension")
    inputStream.use { input ->
        FileOutputStream(file).use { output ->
            input.copyTo(output)
        }
    }
    return file.absolutePath
}
