package com.example.animemanager.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.animemanager.core.data.remote.BangumiSeasonFilter
import com.example.animemanager.core.data.remote.RemoteAnimeDataSource
import com.example.animemanager.core.data.remote.RemoteAnimeSearchResult
import com.example.animemanager.core.data.repository.AnimeRepository
import com.example.animemanager.core.data.repository.DuplicateAnimeException
import com.example.animemanager.core.model.AnimeForm
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeSearchUiState(
    val query: String = "",
    val results: List<RemoteAnimeSearchResult> = emptyList(),
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val error: String? = null,
    val userMessage: String? = null,
    val canLoadMore: Boolean = false,
    val hasSearched: Boolean = false,
)

private data class HomeSearchState(
    val query: String = "",
    val results: List<RemoteAnimeSearchResult> = emptyList(),
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val canLoadMore: Boolean = false,
    val hasSearched: Boolean = false,
    val nextOffset: Int = 0,
    val lastSearch: ParsedHomeSearch? = null,
) {
    fun toUiState(): HomeSearchUiState {
        return HomeSearchUiState(
            query = query,
            results = results,
            loading = loading,
            loadingMore = loadingMore,
            error = error,
            userMessage = message,
            canLoadMore = canLoadMore,
            hasSearched = hasSearched,
        )
    }
}

@HiltViewModel
class HomeSearchViewModel @Inject constructor(
    private val repository: AnimeRepository,
    private val remoteAnimeDataSource: RemoteAnimeDataSource,
) : ViewModel() {
    private val state = MutableStateFlow(HomeSearchState())

    val uiState: StateFlow<HomeSearchUiState> = state
        .map { it.toUiState() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeSearchUiState())

    fun updateQuery(value: String) {
        state.update { it.copy(query = value, error = null) }
    }

    fun search() {
        val parsedSearch = parseHomeSearch(state.value.query)
        if (parsedSearch.keyword.isBlank() && parsedSearch.year == null) {
            state.update { it.copy(error = "请输入番剧名，或输入 2024 春季 这样的年份季度。") }
            return
        }
        loadPage(parsedSearch = parsedSearch, offset = 0, append = false)
    }

    fun loadMore() {
        val current = state.value
        val parsedSearch = current.lastSearch ?: return
        if (current.loading || current.loadingMore || !current.canLoadMore) return
        loadPage(parsedSearch = parsedSearch, offset = current.nextOffset, append = true)
    }

    fun addRemoteAnime(result: RemoteAnimeSearchResult) {
        viewModelScope.launch {
            runCatching {
                repository.upsertAnime(result.mergeInto(AnimeForm()))
            }.onSuccess {
                state.update {
                    it.copy(
                        error = null,
                        message = "已添加《${result.title}》。",
                    )
                }
            }.onFailure { error ->
                state.update {
                    it.copy(
                        message = when (error) {
                            is DuplicateAnimeException -> "《${error.existingTitle}》已经在库中，不能重复添加。"
                            else -> error.message ?: "添加失败，请稍后再试。"
                        },
                    )
                }
            }
        }
    }

    fun consumeUserMessage() {
        state.update { it.copy(message = null) }
    }

    private fun loadPage(
        parsedSearch: ParsedHomeSearch,
        offset: Int,
        append: Boolean,
    ) {
        viewModelScope.launch {
            state.update {
                it.copy(
                    loading = !append,
                    loadingMore = append,
                    error = null,
                    hasSearched = true,
                    lastSearch = parsedSearch,
                    results = if (append) it.results else emptyList(),
                    canLoadMore = if (append) it.canLoadMore else false,
                )
            }
            runCatching {
                remoteAnimeDataSource.searchAnime(
                    keyword = parsedSearch.keyword,
                    year = parsedSearch.year,
                    season = parsedSearch.season,
                    limit = PageSize,
                    offset = offset,
                )
            }.onSuccess { page ->
                state.update { current ->
                    val mergedResults = if (append) {
                        (current.results + page).distinctBy { it.sourceId }
                    } else {
                        page.distinctBy { it.sourceId }
                    }
                    current.copy(
                        loading = false,
                        loadingMore = false,
                        results = mergedResults,
                        error = if (mergedResults.isEmpty()) "没有找到匹配番剧。" else null,
                        canLoadMore = page.size >= PageSize,
                        nextOffset = offset + PageSize,
                    )
                }
            }.onFailure { error ->
                state.update {
                    it.copy(
                        loading = false,
                        loadingMore = false,
                        error = error.message ?: "Bangumi 搜索失败，请检查网络或代理。",
                    )
                }
            }
        }
    }

    private fun parseHomeSearch(input: String): ParsedHomeSearch {
        val yearMatch = YearRegex.find(input)
        val year = yearMatch?.value?.toIntOrNull()
        val season = BangumiSeasonFilter.entries.firstOrNull { seasonFilter ->
            SeasonKeywords.getValue(seasonFilter).any { keyword ->
                input.contains(keyword, ignoreCase = true)
            }
        }

        var keyword = input.trim()
        yearMatch?.value?.let { keyword = keyword.replace(it, "") }
        if (yearMatch != null) {
            keyword = keyword.replace("年", "")
        }
        if (season != null) {
            SeasonKeywords.getValue(season).forEach { seasonKeyword ->
                keyword = keyword.replace(seasonKeyword, "", ignoreCase = true)
            }
        }
        keyword = keyword.replace(Regex("""\s+"""), " ").trim()

        return ParsedHomeSearch(
            keyword = keyword,
            year = year,
            season = season,
        )
    }

    private companion object {
        const val PageSize = 20
        val YearRegex = Regex("""(?:19|20)\d{2}""")
        val SeasonKeywords = mapOf(
            BangumiSeasonFilter.WINTER to listOf("冬季", "冬番", "1月", "1 月", "一月", "winter"),
            BangumiSeasonFilter.SPRING to listOf("春季", "春番", "4月", "4 月", "四月", "spring"),
            BangumiSeasonFilter.SUMMER to listOf("夏季", "夏番", "7月", "7 月", "七月", "summer"),
            BangumiSeasonFilter.AUTUMN to listOf("秋季", "秋番", "10月", "10 月", "十月", "autumn", "fall"),
        )
    }
}

private data class ParsedHomeSearch(
    val keyword: String,
    val year: Int?,
    val season: BangumiSeasonFilter?,
)
