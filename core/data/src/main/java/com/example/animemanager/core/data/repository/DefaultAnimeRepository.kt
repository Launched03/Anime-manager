package com.example.animemanager.core.data.repository

import com.example.animemanager.core.data.local.dao.AnimeDao
import com.example.animemanager.core.data.local.entity.AnimeEntity
import com.example.animemanager.core.data.local.entity.AnimeScheduleEntity
import com.example.animemanager.core.data.local.entity.UserAnimeStateEntity
import com.example.animemanager.core.data.model.AnimeRow
import com.example.animemanager.core.model.AnimeDetail
import com.example.animemanager.core.model.AnimeForm
import com.example.animemanager.core.model.AnimeListFilter
import com.example.animemanager.core.model.AnimeScheduleInfo
import com.example.animemanager.core.model.AnimeSortOrder
import com.example.animemanager.core.model.AnimeStateCounts
import com.example.animemanager.core.model.AnimeSummary
import com.example.animemanager.core.model.HomeDashboard
import com.example.animemanager.core.model.SeriesStatus
import com.example.animemanager.core.model.WatchState
import com.example.animemanager.core.model.clampProgress
import com.example.animemanager.core.model.currentIsoWeekday
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class DefaultAnimeRepository @Inject constructor(
    private val animeDao: AnimeDao,
) : AnimeRepository {
    override fun observeHomeDashboard(): Flow<HomeDashboard> {
        return animeDao.observeAllAnimeRows().map { rows ->
            val todayWeekday = currentIsoWeekday()
            HomeDashboard(
                todayUpdates = rows
                    .filter {
                        it.seriesStatus != SeriesStatus.FINISHED.dbValue &&
                            it.scheduleActive &&
                            it.releaseWeekday == todayWeekday
                    }
                    .map { it.toSummary() },
                continueWatching = rows
                    .filter { it.watchState == WatchState.WATCHING.dbValue }
                    .sortedByDescending { it.updatedAt }
                    .map { it.toSummary() },
                favorites = rows
                    .filter { it.isFavorite }
                    .sortedByDescending { it.updatedAt }
                    .map { it.toSummary() },
                recentlyAdded = rows
                    .sortedByDescending { it.createdAt }
                    .map { it.toSummary() },
            )
        }
    }

    override fun observeAnimeList(filter: AnimeListFilter): Flow<List<AnimeSummary>> {
        return animeDao.observeAnimeRows(
            query = filter.query.trim(),
            watchState = filter.watchState?.dbValue,
            seriesStatus = filter.seriesStatus?.dbValue,
            favoritesOnly = filter.favoritesOnly,
            weekday = filter.weekday,
        ).map { rows ->
            rows
                .map { it.toSummary() }
                .let { summaries ->
                    if (filter.weekday == null) {
                        summaries
                    } else {
                        summaries.filter { it.seriesStatus != SeriesStatus.FINISHED }
                    }
                }
                .sortedWith(filter.sortOrder.toComparator())
        }
    }

    override fun observeAnimeDetail(animeId: Long): Flow<AnimeDetail?> {
        return animeDao.observeAnimeRow(animeId).map { row -> row?.toDetail() }
    }

    override fun observeStateCounts(): Flow<AnimeStateCounts> {
        return animeDao.observeAllAnimeRows().map { rows ->
            AnimeStateCounts(
                total = rows.size,
                wantToWatch = rows.count { it.watchState == WatchState.WANT_TO_WATCH.dbValue },
                watching = rows.count { it.watchState == WatchState.WATCHING.dbValue },
                watched = rows.count { it.watchState == WatchState.WATCHED.dbValue },
                hold = rows.count { it.watchState == WatchState.HOLD.dbValue },
                favorite = rows.count { it.isFavorite },
            )
        }
    }

    override suspend fun upsertAnime(form: AnimeForm): Long {
        val now = System.currentTimeMillis()
        val animeId = animeDao.upsertAnime(
            AnimeEntity(
                id = form.id,
                title = form.title.trim(),
                originalTitle = form.originalTitle?.trim().takeUnless { it.isNullOrBlank() },
                posterRef = form.posterRef?.trim().takeUnless { it.isNullOrBlank() },
                synopsis = form.synopsis?.trim().takeUnless { it.isNullOrBlank() },
                seriesStatus = form.seriesStatus.dbValue,
                totalEpisodes = form.totalEpisodes,
                updatedEpisodes = form.updatedEpisodes,
                seasonYear = form.seasonYear,
                seasonName = form.seasonName?.trim().takeUnless { it.isNullOrBlank() },
                premiereDate = form.premiereDate?.trim().takeUnless { it.isNullOrBlank() },
                createdAt = if (form.id == 0L) now else (animeDao.getAnimeEntity(form.id)?.createdAt ?: now),
                updatedAt = now,
            )
        ).let { returnedId ->
            if (form.id != 0L) form.id else returnedId
        }
        val canSchedule = form.seriesStatus != SeriesStatus.FINISHED

        animeDao.upsertState(
            UserAnimeStateEntity(
                animeId = animeId,
                watchState = form.watchState.dbValue,
                progressEpisode = clampProgress(form.progressEpisode, form.totalEpisodes),
                isFavorite = form.isFavorite,
                updatedAt = now,
            )
        )
        animeDao.upsertSchedule(
            AnimeScheduleEntity(
                animeId = animeId,
                weekday = form.releaseWeekday.takeIf { canSchedule },
                minuteOfDay = form.releaseMinuteOfDay.takeIf { canSchedule },
                timezone = form.releaseTimezone ?: "Asia/Shanghai",
                isActive = canSchedule && form.releaseWeekday != null && form.releaseMinuteOfDay != null,
            )
        )
        return animeId
    }

    override suspend fun deleteAnime(animeId: Long) {
        animeDao.deleteAnime(animeId)
    }

    override suspend fun setWatchState(animeId: Long, state: WatchState) {
        val now = System.currentTimeMillis()
        val anime = animeDao.getAnimeEntity(animeId) ?: return
        val currentState = animeDao.getStateEntity(animeId)
        val total = anime.totalEpisodes
        val progress = when (state) {
            WatchState.WANT_TO_WATCH -> 0
            WatchState.WATCHED -> total ?: max(currentState?.progressEpisode ?: 0, 0)
            WatchState.WATCHING -> clampProgress(currentState?.progressEpisode ?: 0, total)
            WatchState.HOLD -> currentState?.progressEpisode ?: 0
        }
        animeDao.upsertState(
            UserAnimeStateEntity(
                animeId = animeId,
                watchState = state.dbValue,
                progressEpisode = clampProgress(progress, total),
                isFavorite = currentState?.isFavorite ?: false,
                score = currentState?.score,
                note = currentState?.note,
                updatedAt = now,
            )
        )
    }

    override suspend fun setFavorite(animeId: Long, favorite: Boolean) {
        val now = System.currentTimeMillis()
        val current = animeDao.getStateEntity(animeId)
        animeDao.upsertState(
            UserAnimeStateEntity(
                animeId = animeId,
                watchState = current?.watchState,
                progressEpisode = current?.progressEpisode ?: 0,
                isFavorite = favorite,
                score = current?.score,
                note = current?.note,
                updatedAt = now,
            )
        )
    }

    override suspend fun updateProgress(animeId: Long, progressEpisode: Int) {
        val now = System.currentTimeMillis()
        val anime = animeDao.getAnimeEntity(animeId) ?: return
        val current = animeDao.getStateEntity(animeId)
        val clamped = clampProgress(progressEpisode, anime.totalEpisodes)
        animeDao.upsertState(
            UserAnimeStateEntity(
                animeId = animeId,
                watchState = current?.watchState ?: WatchState.WATCHING.dbValue,
                progressEpisode = clamped,
                isFavorite = current?.isFavorite ?: false,
                score = current?.score,
                note = current?.note,
                updatedAt = now,
            )
        )
    }

    override suspend fun upsertSchedule(animeId: Long, schedule: AnimeScheduleInfo) {
        animeDao.upsertSchedule(
            AnimeScheduleEntity(
                animeId = animeId,
                weekday = schedule.weekday,
                minuteOfDay = schedule.minuteOfDay,
                timezone = schedule.timezone ?: "Asia/Shanghai",
                isActive = schedule.active,
            )
        )
    }

    private fun AnimeRow.toSummary(): AnimeSummary {
        return AnimeSummary(
            id = animeId,
            title = title,
            originalTitle = originalTitle,
            posterRef = posterRef,
            seriesStatus = SeriesStatus.fromDbValue(seriesStatus),
            totalEpisodes = totalEpisodes,
            updatedEpisodes = updatedEpisodes,
            seasonYear = seasonYear,
            seasonName = seasonName,
            premiereDate = premiereDate,
            watchState = WatchState.fromDbValue(watchState),
            isFavorite = isFavorite,
            progressEpisode = progressEpisode ?: 0,
            releaseWeekday = releaseWeekday,
            releaseMinuteOfDay = releaseMinuteOfDay,
            scheduleActive = scheduleActive,
        )
    }

    private fun AnimeRow.toDetail(): AnimeDetail {
        return AnimeDetail(
            id = animeId,
            title = title,
            originalTitle = originalTitle,
            posterRef = posterRef,
            synopsis = synopsis,
            seriesStatus = SeriesStatus.fromDbValue(seriesStatus),
            totalEpisodes = totalEpisodes,
            updatedEpisodes = updatedEpisodes,
            seasonYear = seasonYear,
            seasonName = seasonName,
            premiereDate = premiereDate,
            watchState = WatchState.fromDbValue(watchState),
            isFavorite = isFavorite,
            progressEpisode = progressEpisode ?: 0,
            releaseWeekday = releaseWeekday,
            releaseMinuteOfDay = releaseMinuteOfDay,
            releaseTimezone = releaseTimezone,
            scheduleActive = scheduleActive,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun AnimeSortOrder.toComparator(): Comparator<AnimeSummary> {
        return when (this) {
            AnimeSortOrder.TITLE_ASC -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.title }
            AnimeSortOrder.UPDATED_DESC -> compareByDescending<AnimeSummary> { it.updatedEpisodes ?: 0 }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.title }
            AnimeSortOrder.NEXT_UPDATE_ASC -> compareBy<AnimeSummary> { it.releaseWeekday ?: Int.MAX_VALUE }
                .thenBy { it.releaseMinuteOfDay ?: Int.MAX_VALUE }
                .thenBy(String.CASE_INSENSITIVE_ORDER) { it.title }
            AnimeSortOrder.STATUS_THEN_TITLE -> compareBy<AnimeSummary> { it.seriesStatus.dbValue }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.title }
        }
    }
}
