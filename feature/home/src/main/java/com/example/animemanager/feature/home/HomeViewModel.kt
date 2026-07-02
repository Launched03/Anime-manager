package com.example.animemanager.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.animemanager.core.data.repository.AnimeRepository
import com.example.animemanager.core.model.HomeDashboard
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val dashboard: HomeDashboard = HomeDashboard(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: AnimeRepository,
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = repository.observeHomeDashboard()
        .map { dashboard -> HomeUiState(dashboard = dashboard) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}
