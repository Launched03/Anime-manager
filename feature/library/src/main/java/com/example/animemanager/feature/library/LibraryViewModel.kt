@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)

package com.example.animemanager.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.animemanager.core.data.repository.AnimeRepository
import com.example.animemanager.core.data.repository.SettingsRepository
import com.example.animemanager.core.model.AnimeListFilter
import com.example.animemanager.core.model.AnimeSortOrder
import com.example.animemanager.core.model.AnimeSummary
import com.example.animemanager.core.model.SeriesStatus
import com.example.animemanager.core.model.WatchState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LibraryUiState(
    val query: String = "",
    val watchState: WatchState? = null,
    val seriesStatus: SeriesStatus? = null,
    val favoritesOnly: Boolean = false,
    val weekday: Int? = null,
    val sortOrder: AnimeSortOrder = AnimeSortOrder.TITLE_ASC,
    val filter: AnimeListFilter = AnimeListFilter(),
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: AnimeRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val state = MutableStateFlow(LibraryUiState())

    init {
        viewModelScope.launch {
            settingsRepository.observeSettings().firstOrNull()?.let { settings ->
                state.update { current -> current.copy(sortOrder = settings.defaultSortOrder) }
            }
        }
    }

    val uiState: StateFlow<LibraryUiState> = state.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())

    val animeList: StateFlow<List<AnimeSummary>> = state
        .map { it.toFilter() }
        .distinctUntilChanged()
        .debounce(200)
        .flatMapLatest { filter -> repository.observeAnimeList(filter) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private fun LibraryUiState.toFilter(): AnimeListFilter {
        return AnimeListFilter(
            query = query,
            watchState = watchState,
            seriesStatus = seriesStatus,
            favoritesOnly = favoritesOnly,
            weekday = weekday,
            sortOrder = sortOrder,
        )
    }

    fun updateQuery(value: String) {
        state.update { it.copy(query = value) }
    }

    fun updateWatchState(value: WatchState?) {
        state.update { it.copy(watchState = value) }
    }

    fun updateSeriesStatus(value: SeriesStatus?) {
        state.update { it.copy(seriesStatus = value) }
    }

    fun updateFavoritesOnly(value: Boolean) {
        state.update { it.copy(favoritesOnly = value) }
    }

    fun updateWeekday(value: Int?) {
        state.update { it.copy(weekday = value) }
    }

    fun updateSortOrder(value: AnimeSortOrder) {
        state.update { it.copy(sortOrder = value) }
    }
}
