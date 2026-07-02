package com.example.animemanager.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.animemanager.core.data.local.entity.AnimeEntity
import com.example.animemanager.core.data.local.entity.AnimeScheduleEntity
import com.example.animemanager.core.data.local.entity.UserAnimeStateEntity
import com.example.animemanager.core.data.model.AnimeRow
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimeDao {
    @Transaction
    @Query(
        """
        SELECT
            a.id AS animeId,
            a.title AS title,
            a.originalTitle AS originalTitle,
            a.posterRef AS posterRef,
            a.synopsis AS synopsis,
            a.seriesStatus AS seriesStatus,
            a.totalEpisodes AS totalEpisodes,
            a.updatedEpisodes AS updatedEpisodes,
            a.seasonYear AS seasonYear,
            a.seasonName AS seasonName,
            a.premiereDate AS premiereDate,
            a.createdAt AS createdAt,
            a.updatedAt AS updatedAt,
            u.watchState AS watchState,
            COALESCE(u.progressEpisode, 0) AS progressEpisode,
            COALESCE(u.isFavorite, 0) AS isFavorite,
            u.score AS score,
            u.note AS note,
            s.weekday AS releaseWeekday,
            s.minuteOfDay AS releaseMinuteOfDay,
            s.timezone AS releaseTimezone,
            COALESCE(s.isActive, 0) AS scheduleActive
        FROM anime a
        LEFT JOIN user_anime_state u ON u.anime_id = a.id
        LEFT JOIN anime_schedule s ON s.anime_id = a.id
        WHERE
            (:query = '' OR LOWER(a.title) LIKE '%' || LOWER(:query) || '%' OR LOWER(COALESCE(a.originalTitle, '')) LIKE '%' || LOWER(:query) || '%')
            AND (:watchState IS NULL OR u.watchState = :watchState)
            AND (:seriesStatus IS NULL OR a.seriesStatus = :seriesStatus)
            AND (:favoritesOnly = 0 OR COALESCE(u.isFavorite, 0) = 1)
            AND (:weekday IS NULL OR (s.weekday = :weekday AND COALESCE(s.isActive, 0) = 1))
        """
    )
    fun observeAnimeRows(
        query: String,
        watchState: Int?,
        seriesStatus: Int?,
        favoritesOnly: Boolean,
        weekday: Int?,
    ): Flow<List<AnimeRow>>

    @Transaction
    @Query(
        """
        SELECT
            a.id AS animeId,
            a.title AS title,
            a.originalTitle AS originalTitle,
            a.posterRef AS posterRef,
            a.synopsis AS synopsis,
            a.seriesStatus AS seriesStatus,
            a.totalEpisodes AS totalEpisodes,
            a.updatedEpisodes AS updatedEpisodes,
            a.seasonYear AS seasonYear,
            a.seasonName AS seasonName,
            a.premiereDate AS premiereDate,
            a.createdAt AS createdAt,
            a.updatedAt AS updatedAt,
            u.watchState AS watchState,
            COALESCE(u.progressEpisode, 0) AS progressEpisode,
            COALESCE(u.isFavorite, 0) AS isFavorite,
            u.score AS score,
            u.note AS note,
            s.weekday AS releaseWeekday,
            s.minuteOfDay AS releaseMinuteOfDay,
            s.timezone AS releaseTimezone,
            COALESCE(s.isActive, 0) AS scheduleActive
        FROM anime a
        LEFT JOIN user_anime_state u ON u.anime_id = a.id
        LEFT JOIN anime_schedule s ON s.anime_id = a.id
        WHERE a.id = :animeId
        LIMIT 1
        """
    )
    fun observeAnimeRow(animeId: Long): Flow<AnimeRow?>

    @Transaction
    @Query(
        """
        SELECT
            a.id AS animeId,
            a.title AS title,
            a.originalTitle AS originalTitle,
            a.posterRef AS posterRef,
            a.synopsis AS synopsis,
            a.seriesStatus AS seriesStatus,
            a.totalEpisodes AS totalEpisodes,
            a.updatedEpisodes AS updatedEpisodes,
            a.seasonYear AS seasonYear,
            a.seasonName AS seasonName,
            a.premiereDate AS premiereDate,
            a.createdAt AS createdAt,
            a.updatedAt AS updatedAt,
            u.watchState AS watchState,
            COALESCE(u.progressEpisode, 0) AS progressEpisode,
            COALESCE(u.isFavorite, 0) AS isFavorite,
            u.score AS score,
            u.note AS note,
            s.weekday AS releaseWeekday,
            s.minuteOfDay AS releaseMinuteOfDay,
            s.timezone AS releaseTimezone,
            COALESCE(s.isActive, 0) AS scheduleActive
        FROM anime a
        LEFT JOIN user_anime_state u ON u.anime_id = a.id
        LEFT JOIN anime_schedule s ON s.anime_id = a.id
        ORDER BY a.updatedAt DESC, a.title COLLATE NOCASE ASC
        """
    )
    fun observeAllAnimeRows(): Flow<List<AnimeRow>>

    @Query("SELECT COUNT(*) FROM anime")
    fun observeAnimeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM user_anime_state WHERE watchState = :watchState")
    fun observeWatchStateCount(watchState: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM user_anime_state WHERE isFavorite = 1")
    fun observeFavoriteCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAnime(anime: AnimeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertState(state: UserAnimeStateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSchedule(schedule: AnimeScheduleEntity): Long

    @Query("DELETE FROM anime WHERE id = :animeId")
    suspend fun deleteAnime(animeId: Long)

    @Query("SELECT * FROM anime WHERE id = :animeId LIMIT 1")
    suspend fun getAnimeEntity(animeId: Long): AnimeEntity?

    @Query("SELECT * FROM user_anime_state WHERE anime_id = :animeId LIMIT 1")
    suspend fun getStateEntity(animeId: Long): UserAnimeStateEntity?

    @Query("SELECT * FROM anime_schedule WHERE anime_id = :animeId LIMIT 1")
    suspend fun getScheduleEntity(animeId: Long): AnimeScheduleEntity?
}
