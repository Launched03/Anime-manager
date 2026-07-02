package com.example.animemanager.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.animemanager.core.data.repository.AnimeRepository
import com.example.animemanager.core.model.AnimeListFilter
import com.example.animemanager.core.model.AnimeSortOrder
import com.example.animemanager.core.model.AnimeSummary
import com.example.animemanager.core.model.currentIsoWeekday
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class CalendarUiState(
    val selectedWeekday: Int = currentIsoWeekday(),
    val showOnlyScheduled: Boolean = true,
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: AnimeRepository,
) : ViewModel() {
    private val state = MutableStateFlow(CalendarUiState())

    val uiState: StateFlow<CalendarUiState> = state.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarUiState())
    val animeList: StateFlow<List<AnimeSummary>> = state
        .flatMapLatest { current ->
            repository.observeAnimeList(
                AnimeListFilter(
                    weekday = if (current.showOnlyScheduled) current.selectedWeekday else null,
                    sortOrder = AnimeSortOrder.NEXT_UPDATE_ASC,
                ),
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectWeekday(value: Int) {
        state.update { it.copy(selectedWeekday = value) }
    }

    fun toggleScheduledOnly() {
        state.update { it.copy(showOnlyScheduled = !it.showOnlyScheduled) }
    }
}
