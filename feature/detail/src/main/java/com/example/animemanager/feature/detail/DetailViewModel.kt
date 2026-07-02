@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.animemanager.feature.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.animemanager.core.data.repository.AnimeRepository
import com.example.animemanager.core.model.AnimeDetail
import com.example.animemanager.core.model.WatchState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DetailUiState(
    val anime: AnimeDetail? = null,
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: AnimeRepository,
) : ViewModel() {
    private val animeId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<DetailUiState> = animeId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(DetailUiState())
            } else {
                repository.observeAnimeDetail(id).map { anime -> DetailUiState(anime = anime) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailUiState())

    fun load(animeId: Long) {
        this.animeId.value = animeId
    }

    fun toggleFavorite(animeId: Long, favorite: Boolean) {
        viewModelScope.launch { repository.setFavorite(animeId, favorite) }
    }

    fun setWatchState(animeId: Long, watchState: WatchState) {
        viewModelScope.launch { repository.setWatchState(animeId, watchState) }
    }

    fun updateProgress(animeId: Long, progress: Int) {
        viewModelScope.launch { repository.updateProgress(animeId, progress) }
    }

    fun deleteAnime(animeId: Long, onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteAnime(animeId)
            onDeleted()
        }
    }
}
