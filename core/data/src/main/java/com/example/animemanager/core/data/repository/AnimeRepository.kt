package com.example.animemanager.core.data.repository

import com.example.animemanager.core.model.AnimeDetail
import com.example.animemanager.core.model.AnimeForm
import com.example.animemanager.core.model.AnimeListFilter
import com.example.animemanager.core.model.AnimeStateCounts
import com.example.animemanager.core.model.HomeDashboard
import com.example.animemanager.core.model.WatchState
import com.example.animemanager.core.model.AnimeScheduleInfo
import kotlinx.coroutines.flow.Flow

interface AnimeRepository {
    fun observeHomeDashboard(): Flow<HomeDashboard>
    fun observeAnimeList(filter: AnimeListFilter): Flow<List<com.example.animemanager.core.model.AnimeSummary>>
    fun observeAnimeDetail(animeId: Long): Flow<AnimeDetail?>
    fun observeStateCounts(): Flow<AnimeStateCounts>
    suspend fun upsertAnime(form: AnimeForm): Long
    suspend fun deleteAnime(animeId: Long)
    suspend fun setWatchState(animeId: Long, state: WatchState)
    suspend fun setFavorite(animeId: Long, favorite: Boolean)
    suspend fun updateProgress(animeId: Long, progressEpisode: Int)
    suspend fun upsertSchedule(animeId: Long, schedule: AnimeScheduleInfo)
}
