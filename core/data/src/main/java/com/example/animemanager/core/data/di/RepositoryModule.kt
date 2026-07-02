package com.example.animemanager.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.animemanager.core.data.remote.BangumiRemoteAnimeDataSource
import com.example.animemanager.core.data.remote.RemoteAnimeDataSource
import com.example.animemanager.core.data.repository.DefaultAnimeRepository
import com.example.animemanager.core.data.repository.AnimeRepository
import com.example.animemanager.core.data.repository.SettingsRepository
import com.example.animemanager.core.data.settings.DataStoreSettingsRepository
import com.example.animemanager.core.data.settings.settingsDataStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAnimeRepository(impl: DefaultAnimeRepository): AnimeRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: DataStoreSettingsRepository): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindRemoteAnimeDataSource(impl: BangumiRemoteAnimeDataSource): RemoteAnimeDataSource

    companion object {
        @Provides
        @Singleton
        fun provideSettingsDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
            return context.settingsDataStore
        }
    }
}
