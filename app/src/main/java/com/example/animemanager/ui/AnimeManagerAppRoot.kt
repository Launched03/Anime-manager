package com.example.animemanager.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.animemanager.core.model.ThemeMode
import com.example.animemanager.feature.profile.SettingsViewModel
import com.example.animemanager.ui.navigation.AnimeNavHost
import com.example.animemanager.ui.theme.AnimeManagerTheme

@Composable
fun AnimeManagerAppRoot(
    navController: NavHostController = rememberNavController(),
) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val settings by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val darkTheme = when (settings.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    AnimeManagerTheme(
        darkTheme = darkTheme,
        dynamicColor = false,
    ) {
        AnimeNavHost(navController = navController)
    }
}
