package com.example.animemanager.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.animemanager.BuildConfig
import com.example.animemanager.feature.calendar.CalendarRoute
import com.example.animemanager.feature.detail.DetailRoute
import com.example.animemanager.feature.edit.EditRoute
import com.example.animemanager.feature.home.HomeRoute
import com.example.animemanager.feature.home.HomeSearchRoute
import com.example.animemanager.feature.library.LibraryRoute
import com.example.animemanager.feature.profile.ProfileRoute
import com.example.animemanager.feature.profile.SettingsRoute

@Composable
fun AnimeNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route?.substringBefore("/")
    val showBottomBar = currentRoute in animeBottomDestinations.map { it.route }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            if (showBottomBar) {
                AnimeBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AnimeRoutes.Home,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            composable(AnimeRoutes.Home) {
                HomeRoute(
                    onOpenAnime = { navController.navigate(AnimeRoutes.detailRoute(it)) },
                    onAddAnime = { navController.navigate(AnimeRoutes.Edit) },
                    onOpenSearch = { navController.navigate(AnimeRoutes.HomeSearch) },
                )
            }
            composable(AnimeRoutes.HomeSearch) {
                HomeSearchRoute(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(AnimeRoutes.Library) {
                LibraryRoute(
                    onOpenAnime = { navController.navigate(AnimeRoutes.detailRoute(it)) },
                    onAddAnime = { navController.navigate(AnimeRoutes.Edit) },
                )
            }
            composable(AnimeRoutes.Calendar) {
                CalendarRoute(
                    onOpenAnime = { navController.navigate(AnimeRoutes.detailRoute(it)) },
                    onAddAnime = { navController.navigate(AnimeRoutes.Edit) },
                )
            }
            composable(AnimeRoutes.Profile) {
                ProfileRoute(
                    onOpenSettings = { navController.navigate(AnimeRoutes.Settings) },
                )
            }
            composable(AnimeRoutes.Settings) {
                SettingsRoute(
                    appVersionName = BuildConfig.VERSION_NAME,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = "${AnimeRoutes.Detail}/{${AnimeRoutes.AnimeIdArg}}",
                arguments = listOf(navArgument(AnimeRoutes.AnimeIdArg) { type = NavType.LongType }),
            ) { entry ->
                val animeId = entry.arguments?.getLong(AnimeRoutes.AnimeIdArg) ?: return@composable
                DetailRoute(
                    animeId = animeId,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(AnimeRoutes.editRoute(it)) },
                    onDeleted = { navController.popBackStack() },
                )
            }
            composable(AnimeRoutes.Edit) {
                EditRoute(
                    animeId = null,
                    onBack = { navController.popBackStack() },
                    onSaved = { savedId ->
                        navController.navigate(AnimeRoutes.detailRoute(savedId)) {
                            popUpTo(AnimeRoutes.Edit) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(
                route = "${AnimeRoutes.Edit}/{${AnimeRoutes.AnimeIdArg}}",
                arguments = listOf(navArgument(AnimeRoutes.AnimeIdArg) { type = NavType.LongType }),
            ) { entry ->
                val animeId = entry.arguments?.getLong(AnimeRoutes.AnimeIdArg) ?: return@composable
                EditRoute(
                    animeId = animeId,
                    onBack = { navController.popBackStack() },
                    onSaved = {
                        navController.popBackStack()
                    },
                )
            }
        }
    }
}

@Composable
private fun AnimeBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        animeBottomDestinations.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = { onNavigate(destination.route) },
                icon = {
                    Icon(
                        imageVector = if (currentRoute == destination.route) destination.selectedIcon else destination.unselectedIcon,
                        contentDescription = destination.label,
                    )
                },
                label = { Text(destination.label) },
            )
        }
    }
}
