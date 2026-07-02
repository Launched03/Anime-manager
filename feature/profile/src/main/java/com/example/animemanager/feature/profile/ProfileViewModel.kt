package com.example.animemanager.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.animemanager.core.data.repository.AnimeRepository
import com.example.animemanager.core.model.AnimeStateCounts
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

data class ProfileUiState(
    val counts: AnimeStateCounts = AnimeStateCounts(),
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    repository: AnimeRepository,
) : ViewModel() {
    val uiState: StateFlow<ProfileUiState> = repository.observeStateCounts()
        .map { counts -> ProfileUiState(counts = counts) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())
}
