package com.example.animemanager.feature.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.animemanager.core.data.remote.BangumiSeasonFilter
import com.example.animemanager.core.data.remote.RemoteAnimeDataSource
import com.example.animemanager.core.data.remote.RemoteAnimeSearchResult
import com.example.animemanager.core.data.repository.AnimeRepository
import com.example.animemanager.core.model.AnimeDetail
import com.example.animemanager.core.model.AnimeForm
import com.example.animemanager.core.model.SeriesStatus
import com.example.animemanager.core.model.WatchState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditUiState(
    val form: AnimeForm = AnimeForm(),
    val loading: Boolean = false,
    val savedAnimeId: Long? = null,
    val remoteSearchQuery: String = "",
    val remoteSearchYear: String = "",
    val remoteSearchSeason: BangumiSeasonFilter? = null,
    val remoteSearchResults: List<RemoteAnimeSearchResult> = emptyList(),
    val remoteSearchLoading: Boolean = false,
    val remoteSearchError: String? = null,
)

@HiltViewModel
class EditViewModel @Inject constructor(
    private val repository: AnimeRepository,
    private val remoteAnimeDataSource: RemoteAnimeDataSource,
) : ViewModel() {
    private val state = MutableStateFlow(EditUiState())

    val uiState: StateFlow<EditUiState> = state.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EditUiState())

    fun load(animeId: Long?) {
        if (animeId == null) {
            state.update { EditUiState() }
            return
        }
        viewModelScope.launch {
            state.update { it.copy(loading = true, savedAnimeId = null) }
            val anime = repository.observeAnimeDetail(animeId).firstOrNull()
            state.update {
                it.copy(
                    form = anime?.toForm() ?: AnimeForm(id = animeId),
                    loading = false,
                    savedAnimeId = null,
                )
            }
        }
    }

    fun updateTitle(value: String) = mutateForm { copy(title = value) }
    fun updateOriginalTitle(value: String) = mutateForm { copy(originalTitle = value) }
    fun updatePosterRef(value: String) = mutateForm { copy(posterRef = value) }
    fun updateSynopsis(value: String) = mutateForm { copy(synopsis = value) }
    fun updateSeriesStatus(value: SeriesStatus) = mutateForm {
        val updated = copy(seriesStatus = value)
        if (value == SeriesStatus.FINISHED) {
            updated.copy(
                releaseWeekday = null,
                releaseMinuteOfDay = null,
                releaseTimezone = null,
            )
        } else {
            updated
        }
    }
    fun updateTotalEpisodes(value: Int?) = mutateForm { copy(totalEpisodes = value) }
    fun updateUpdatedEpisodes(value: Int?) = mutateForm { copy(updatedEpisodes = value) }
    fun updateSeasonYear(value: Int?) = mutateForm { copy(seasonYear = value) }
    fun updateSeasonName(value: String?) = mutateForm { copy(seasonName = value) }
    fun updatePremiereDate(value: String?) = mutateForm { copy(premiereDate = value) }
    fun updateReleaseWeekday(value: Int?) = mutateForm { copy(releaseWeekday = value) }
    fun updateReleaseMinuteOfDay(value: Int?) = mutateForm { copy(releaseMinuteOfDay = value) }
    fun updateReleaseTimezone(value: String?) = mutateForm { copy(releaseTimezone = value) }
    fun updateWatchState(value: WatchState) = mutateForm { copy(watchState = value) }
    fun updateProgressEpisode(value: Int) = mutateForm { copy(progressEpisode = value) }
    fun updateFavorite(value: Boolean) = mutateForm { copy(isFavorite = value) }

    fun updateRemoteSearchQuery(value: String) {
        state.update { it.copy(remoteSearchQuery = value) }
    }

    fun updateRemoteSearchYear(value: String) {
        state.update { it.copy(remoteSearchYear = value.filter(Char::isDigit).take(4)) }
    }

    fun updateRemoteSearchSeason(value: BangumiSeasonFilter?) {
        state.update { it.copy(remoteSearchSeason = value) }
    }

    fun clearRemoteSearch() {
        state.update {
            it.copy(
                remoteSearchResults = emptyList(),
                remoteSearchError = null,
                remoteSearchLoading = false,
            )
        }
    }

    fun searchRemoteAnime() {
        val query = state.value.remoteSearchQuery.trim()
        val year = state.value.remoteSearchYear.toIntOrNull()
        val season = state.value.remoteSearchSeason
        if (query.isBlank() && year == null) {
            state.update { it.copy(remoteSearchError = "请输入番剧名，或者填写年份/季度。") }
            return
        }
        viewModelScope.launch {
            state.update { it.copy(remoteSearchLoading = true, remoteSearchError = null) }
            runCatching {
                remoteAnimeDataSource.searchAnime(
                    keyword = query,
                    year = year,
                    season = season,
                    limit = 30,
                )
            }.onSuccess { results ->
                state.update {
                    it.copy(
                        remoteSearchLoading = false,
                        remoteSearchResults = results,
                        remoteSearchError = if (results.isEmpty()) "没有找到匹配番剧。" else null,
                    )
                }
            }.onFailure { error ->
                state.update {
                    it.copy(
                        remoteSearchLoading = false,
                        remoteSearchError = error.message ?: "Bangumi 搜索失败，请检查网络或代理。",
                    )
                }
            }
        }
    }

    fun importRemoteAnime(result: RemoteAnimeSearchResult) {
        state.update { current ->
            current.copy(
                form = result.mergeInto(current.form),
                remoteSearchError = null,
            )
        }
    }

    fun save() {
        viewModelScope.launch {
            if (state.value.form.title.isBlank()) {
                return@launch
            }
            state.update { it.copy(loading = true) }
            val savedId = repository.upsertAnime(state.value.form)
            state.update {
                it.copy(
                    loading = false,
                    savedAnimeId = savedId,
                    form = it.form.copy(id = savedId),
                )
            }
        }
    }

    fun consumeSavedAnimeId() {
        state.update { it.copy(savedAnimeId = null) }
    }

    private fun mutateForm(block: AnimeForm.() -> AnimeForm) {
        state.update { current -> current.copy(form = current.form.block()) }
    }

    private fun AnimeDetail.toForm(): AnimeForm {
        return AnimeForm(
            id = id,
            title = title,
            originalTitle = originalTitle,
            posterRef = posterRef,
            synopsis = synopsis,
            seriesStatus = seriesStatus,
            totalEpisodes = totalEpisodes,
            updatedEpisodes = updatedEpisodes,
            seasonYear = seasonYear,
            seasonName = seasonName,
            premiereDate = premiereDate,
            releaseWeekday = releaseWeekday,
            releaseMinuteOfDay = releaseMinuteOfDay,
            releaseTimezone = releaseTimezone,
            watchState = watchState ?: WatchState.WANT_TO_WATCH,
            progressEpisode = progressEpisode,
            isFavorite = isFavorite,
        )
    }
}
