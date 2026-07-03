package com.example.animemanager.core.data.remote

import com.example.animemanager.core.model.AnimeDetail
import com.example.animemanager.core.model.SeriesStatus
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class BangumiRemoteAnimeDataSource @Inject constructor() : RemoteAnimeDataSource {
    override suspend fun fetchCatalog(): List<AnimeDetail> = emptyList()

    override suspend fun searchAnime(
        keyword: String,
        year: Int?,
        season: BangumiSeasonFilter?,
        limit: Int,
        offset: Int,
    ): List<RemoteAnimeSearchResult> = withContext(Dispatchers.IO) {
        val body = buildSearchBody(keyword = keyword, year = year, season = season)
        val url = URL("$BASE_URL/v0/search/subjects?limit=${limit.coerceIn(1, 50)}&offset=${offset.coerceAtLeast(0)}")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 20_000
            doOutput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("User-Agent", USER_AGENT)
        }

        try {
            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(body.toString())
            }
            val responseCode = connection.responseCode
            val responseText = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader(Charsets.UTF_8).use(BufferedReader::readText)
            } else {
                connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use(BufferedReader::readText).orEmpty()
            }
            if (responseCode !in 200..299) {
                throw IllegalStateException("Bangumi 搜索失败：HTTP $responseCode ${responseText.take(120)}")
            }
            parseSearchResponse(responseText)
        } finally {
            connection.disconnect()
        }
    }

    private fun buildSearchBody(
        keyword: String,
        year: Int?,
        season: BangumiSeasonFilter?,
    ): JSONObject {
        val filter = JSONObject()
            .put("type", JSONArray().put(2))

        val airDateFilters = buildAirDateFilters(year = year, season = season)
        if (airDateFilters.length() > 0) {
            filter.put("air_date", airDateFilters)
        }

        return JSONObject()
            .put("keyword", keyword.trim())
            .put("sort", if (keyword.isBlank() && year != null) "rank" else "match")
            .put("filter", filter)
    }

    private fun buildAirDateFilters(year: Int?, season: BangumiSeasonFilter?): JSONArray {
        if (year == null) return JSONArray()
        val startMonth = season?.startMonth ?: 1
        val start = LocalDate.of(year, startMonth, 1)
        val end = if (season == null) {
            LocalDate.of(year + 1, 1, 1)
        } else {
            YearMonth.of(year, startMonth).plusMonths(3).atDay(1)
        }
        return JSONArray()
            .put(">=$start")
            .put("<$end")
    }

    private fun parseSearchResponse(responseText: String): List<RemoteAnimeSearchResult> {
        val root = JSONObject(responseText)
        val data = root.optJSONArray("data") ?: return emptyList()
        return buildList {
            for (index in 0 until data.length()) {
                val subject = data.optJSONObject(index) ?: continue
                if (subject.optInt("type") != 2) continue
                add(subject.toRemoteAnimeSearchResult())
            }
        }
    }

    private fun JSONObject.toRemoteAnimeSearchResult(): RemoteAnimeSearchResult {
        val name = optStringOrNull("name")
        val nameCn = optStringOrNull("name_cn")
        val airDate = optStringOrNull("date")
        val poster = optJSONObject("images")?.bestPosterUrl()
        val totalEpisodes = optPositiveInt("eps") ?: parseEpisodeCountFromInfobox(optJSONArray("infobox"))
        val seasonYear = airDate?.take(4)?.toIntOrNull()
        val month = airDate?.drop(5)?.take(2)?.toIntOrNull()
        return RemoteAnimeSearchResult(
            sourceId = optLong("id").toString(),
            title = nameCn?.takeIf { it.isNotBlank() } ?: name.orEmpty(),
            originalTitle = name?.takeIf { it.isNotBlank() && it != nameCn },
            synopsis = optStringOrNull("summary"),
            posterRef = poster,
            totalEpisodes = totalEpisodes,
            airDate = airDate,
            seasonYear = seasonYear,
            seasonName = month?.toSeasonName(),
            seriesStatus = inferSeriesStatus(airDate),
        )
    }

    private fun JSONObject.bestPosterUrl(): String? {
        val poster = optStringOrNull("large")
            ?: optStringOrNull("common")
            ?: optStringOrNull("medium")
            ?: optStringOrNull("small")
            ?: optStringOrNull("grid")
        return poster?.let { value ->
            when {
                value.startsWith("//") -> "https:$value"
                else -> value
            }
        }
    }

    private fun parseEpisodeCountFromInfobox(infobox: JSONArray?): Int? {
        if (infobox == null) return null
        val keys = setOf("话数", "話数", "集数", "Episodes", "episodes")
        for (index in 0 until infobox.length()) {
            val item = infobox.optJSONObject(index) ?: continue
            if (item.optString("key") !in keys) continue
            val valueText = item.opt("value")?.toString().orEmpty()
            return Regex("""\d+""").find(valueText)?.value?.toIntOrNull()?.takeIf { it > 0 }
        }
        return null
    }

    private fun inferSeriesStatus(airDate: String?): SeriesStatus {
        val date = airDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return SeriesStatus.ONGOING
        val today = LocalDate.now()
        return when {
            date.isAfter(today) -> SeriesStatus.UPCOMING
            date.plusMonths(6).isBefore(today) -> SeriesStatus.FINISHED
            else -> SeriesStatus.ONGOING
        }
    }

    private fun Int.toSeasonName(): String = when (this) {
        in 1..3 -> "冬季"
        in 4..6 -> "春季"
        in 7..9 -> "夏季"
        in 10..12 -> "秋季"
        else -> ""
    }

    private fun JSONObject.optStringOrNull(key: String): String? {
        return optString(key).trim().takeIf { it.isNotBlank() && it != "null" }
    }

    private fun JSONObject.optPositiveInt(key: String): Int? {
        return optInt(key, 0).takeIf { it > 0 }
    }

    private companion object {
        const val BASE_URL = "https://api.bgm.tv"
        const val USER_AGENT = "AnimeManager/1.0 (local Android app; contact: user)"
    }
}
