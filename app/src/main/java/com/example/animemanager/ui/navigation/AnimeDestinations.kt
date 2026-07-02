package com.example.animemanager.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector

object AnimeRoutes {
    const val Home = "home"
    const val Library = "library"
    const val Calendar = "calendar"
    const val Profile = "profile"
    const val Settings = "settings"
    const val Detail = "detail"
    const val Edit = "edit"
    const val AnimeIdArg = "animeId"

    fun detailRoute(animeId: Long): String = "$Detail/$animeId"

    fun editRoute(animeId: Long? = null): String {
        return if (animeId == null) Edit else "$Edit/$animeId"
    }
}

data class AnimeBottomDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

val animeBottomDestinations = listOf(
    AnimeBottomDestination(
        route = AnimeRoutes.Home,
        label = "首页",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
    ),
    AnimeBottomDestination(
        route = AnimeRoutes.Library,
        label = "库",
        selectedIcon = Icons.Filled.VideoLibrary,
        unselectedIcon = Icons.Outlined.VideoLibrary,
    ),
    AnimeBottomDestination(
        route = AnimeRoutes.Calendar,
        label = "日历",
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth,
    ),
    AnimeBottomDestination(
        route = AnimeRoutes.Profile,
        label = "我的",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
    ),
)
