package com.example.animemanager.core.data.model

import androidx.room.ColumnInfo

data class AnimeRow(
    @ColumnInfo(name = "animeId") val animeId: Long,
    @ColumnInfo(name = "sourceId") val sourceId: String?,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "originalTitle") val originalTitle: String?,
    @ColumnInfo(name = "posterRef") val posterRef: String?,
    @ColumnInfo(name = "synopsis") val synopsis: String?,
    @ColumnInfo(name = "seriesStatus") val seriesStatus: Int,
    @ColumnInfo(name = "totalEpisodes") val totalEpisodes: Int?,
    @ColumnInfo(name = "updatedEpisodes") val updatedEpisodes: Int?,
    @ColumnInfo(name = "seasonYear") val seasonYear: Int?,
    @ColumnInfo(name = "seasonName") val seasonName: String?,
    @ColumnInfo(name = "premiereDate") val premiereDate: String?,
    @ColumnInfo(name = "createdAt") val createdAt: Long,
    @ColumnInfo(name = "updatedAt") val updatedAt: Long,
    @ColumnInfo(name = "watchState") val watchState: Int?,
    @ColumnInfo(name = "progressEpisode") val progressEpisode: Int?,
    @ColumnInfo(name = "isFavorite") val isFavorite: Boolean,
    @ColumnInfo(name = "score") val score: Int?,
    @ColumnInfo(name = "note") val note: String?,
    @ColumnInfo(name = "releaseWeekday") val releaseWeekday: Int?,
    @ColumnInfo(name = "releaseMinuteOfDay") val releaseMinuteOfDay: Int?,
    @ColumnInfo(name = "releaseTimezone") val releaseTimezone: String?,
    @ColumnInfo(name = "scheduleActive") val scheduleActive: Boolean,
)
