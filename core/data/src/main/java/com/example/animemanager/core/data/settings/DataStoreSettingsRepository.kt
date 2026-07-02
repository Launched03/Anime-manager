package com.example.animemanager.core.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.example.animemanager.core.data.repository.SettingsRepository
import com.example.animemanager.core.model.AnimeSortOrder
import com.example.animemanager.core.model.AppSettings
import com.example.animemanager.core.model.ThemeMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val THEME_MODE = intPreferencesKey("theme_mode")
private val DEFAULT_SORT = intPreferencesKey("default_sort")

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {
    override fun observeSettings(): Flow<AppSettings> {
        return dataStore.data.map { preferences ->
            AppSettings(
                themeMode = ThemeMode.fromDbValue(preferences[THEME_MODE] ?: ThemeMode.SYSTEM.dbValue),
                defaultSortOrder = AnimeSortOrder.fromDbValue(preferences[DEFAULT_SORT] ?: AnimeSortOrder.TITLE_ASC.dbValue),
            )
        }
    }

    override suspend fun updateThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = themeMode.dbValue
        }
    }

    override suspend fun updateDefaultSortOrder(sortOrder: AnimeSortOrder) {
        dataStore.edit { preferences ->
            preferences[DEFAULT_SORT] = sortOrder.dbValue
        }
    }
}
