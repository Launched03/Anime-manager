package com.example.animemanager.core.data.remote

import com.example.animemanager.core.model.AnimeDetail
import com.example.animemanager.core.model.AnimeForm
import com.example.animemanager.core.model.SeriesStatus

enum class BangumiSeasonFilter(
    val displayLabel: String,
    val startMonth: Int,
) {
    WINTER("冬季 1-3月", 1),
    SPRING("春季 4-6月", 4),
    SUMMER("夏季 7-9月", 7),
    AUTUMN("秋季 10-12月", 10),
}

data class RemoteAnimeSearchResult(
    val sourceId: String,
    val title: String,
    val originalTitle: String? = null,
    val synopsis: String? = null,
    val posterRef: String? = null,
    val totalEpisodes: Int? = null,
    val airDate: String? = null,
    val seasonYear: Int? = null,
    val seasonName: String? = null,
    val seriesStatus: SeriesStatus = SeriesStatus.ONGOING,
) {
    fun mergeInto(form: AnimeForm): AnimeForm {
        return form.copy(
            title = title.ifBlank { form.title },
            originalTitle = originalTitle ?: form.originalTitle,
            posterRef = posterRef ?: form.posterRef,
            synopsis = synopsis ?: form.synopsis,
            seriesStatus = seriesStatus,
            totalEpisodes = totalEpisodes ?: form.totalEpisodes,
            seasonYear = seasonYear ?: form.seasonYear,
            seasonName = seasonName ?: form.seasonName,
            premiereDate = airDate ?: form.premiereDate,
        )
    }
}

interface RemoteAnimeDataSource {
    suspend fun fetchCatalog(): List<AnimeDetail>
    suspend fun searchAnime(
        keyword: String,
        year: Int?,
        season: BangumiSeasonFilter?,
        limit: Int = 20,
    ): List<RemoteAnimeSearchResult>
}
