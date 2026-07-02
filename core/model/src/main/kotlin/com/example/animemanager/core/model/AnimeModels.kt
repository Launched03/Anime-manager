package com.example.animemanager.core.model

data class AnimeSummary(
    val id: Long = 0,
    val title: String = "",
    val originalTitle: String? = null,
    val posterRef: String? = null,
    val seriesStatus: SeriesStatus = SeriesStatus.ONGOING,
    val totalEpisodes: Int? = null,
    val updatedEpisodes: Int? = null,
    val seasonYear: Int? = null,
    val seasonName: String? = null,
    val premiereDate: String? = null,
    val watchState: WatchState? = null,
    val isFavorite: Boolean = false,
    val progressEpisode: Int = 0,
    val releaseWeekday: Int? = null,
    val releaseMinuteOfDay: Int? = null,
    val scheduleActive: Boolean = false,
)

data class AnimeDetail(
    val id: Long = 0,
    val title: String = "",
    val originalTitle: String? = null,
    val posterRef: String? = null,
    val synopsis: String? = null,
    val seriesStatus: SeriesStatus = SeriesStatus.ONGOING,
    val totalEpisodes: Int? = null,
    val updatedEpisodes: Int? = null,
    val seasonYear: Int? = null,
    val seasonName: String? = null,
    val premiereDate: String? = null,
    val watchState: WatchState? = null,
    val isFavorite: Boolean = false,
    val progressEpisode: Int = 0,
    val releaseWeekday: Int? = null,
    val releaseMinuteOfDay: Int? = null,
    val releaseTimezone: String? = null,
    val scheduleActive: Boolean = false,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
)

data class AnimeForm(
    val id: Long = 0,
    val title: String = "",
    val originalTitle: String? = null,
    val posterRef: String? = null,
    val synopsis: String? = null,
    val seriesStatus: SeriesStatus = SeriesStatus.ONGOING,
    val totalEpisodes: Int? = null,
    val updatedEpisodes: Int? = null,
    val seasonYear: Int? = null,
    val seasonName: String? = null,
    val premiereDate: String? = null,
    val releaseWeekday: Int? = null,
    val releaseMinuteOfDay: Int? = null,
    val releaseTimezone: String? = null,
    val watchState: WatchState = WatchState.WANT_TO_WATCH,
    val progressEpisode: Int = 0,
    val isFavorite: Boolean = false,
)

data class AnimeListFilter(
    val query: String = "",
    val watchState: WatchState? = null,
    val seriesStatus: SeriesStatus? = null,
    val favoritesOnly: Boolean = false,
    val weekday: Int? = null,
    val sortOrder: AnimeSortOrder = AnimeSortOrder.TITLE_ASC,
)

data class AnimeScheduleInfo(
    val weekday: Int? = null,
    val minuteOfDay: Int? = null,
    val timezone: String? = null,
    val active: Boolean = false,
)

data class HomeDashboard(
    val todayUpdates: List<AnimeSummary> = emptyList(),
    val continueWatching: List<AnimeSummary> = emptyList(),
    val favorites: List<AnimeSummary> = emptyList(),
    val recentlyAdded: List<AnimeSummary> = emptyList(),
)

data class AnimeStateCounts(
    val total: Int = 0,
    val wantToWatch: Int = 0,
    val watching: Int = 0,
    val watched: Int = 0,
    val hold: Int = 0,
    val favorite: Int = 0,
)

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val defaultSortOrder: AnimeSortOrder = AnimeSortOrder.TITLE_ASC,
)
