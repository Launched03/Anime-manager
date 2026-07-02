package com.example.animemanager.core.data.repository

import com.example.animemanager.core.model.AppSettings
import com.example.animemanager.core.model.AnimeSortOrder
import com.example.animemanager.core.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun updateThemeMode(themeMode: ThemeMode)
    suspend fun updateDefaultSortOrder(sortOrder: AnimeSortOrder)
}
