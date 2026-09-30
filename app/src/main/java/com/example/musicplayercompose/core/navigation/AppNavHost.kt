package com.example.musicplayercompose.core.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.musicplayercompose.feature.detail.AlbumDetailScreen
import com.example.musicplayercompose.feature.detail.ArtistDetailScreen
import com.example.musicplayercompose.feature.detail.GenreDetailScreen
import com.example.musicplayercompose.feature.home.HomeScreen
import com.example.musicplayercompose.feature.library.LibraryScreen
import com.example.musicplayercompose.feature.main.MainViewModel
import com.example.musicplayercompose.feature.playlists.PlaylistDetailScreen
import com.example.musicplayercompose.feature.playlists.PlaylistsScreen
import com.example.musicplayercompose.feature.search.SearchScreen
import com.example.musicplayercompose.feature.settings.SettingsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModel: MainViewModel,
    contentPadding: PaddingValues
) {
    val navigate: (String) -> Unit = { route -> navController.navigate(route) { launchSingleTop = true } }
    val back: () -> Unit = { navController.popBackStack() }
    val longArg = listOf(navArgument("id") { type = NavType.LongType })
    val stringArg = listOf(navArgument("name") { type = NavType.StringType })

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { fadeOut() }
    ) {
        composable(Screen.Home.route) { HomeScreen(viewModel, navigate, contentPadding) }
        composable(Screen.Library.route) { LibraryScreen(viewModel, navigate, contentPadding) }
        composable(Screen.Search.route) { SearchScreen(viewModel, navigate, contentPadding) }
        composable(Screen.Playlists.route) { PlaylistsScreen(navigate, contentPadding) }
        composable(Screen.Settings.route) { SettingsScreen(viewModel, contentPadding) }

        composable(Screen.AlbumDetail.route, arguments = longArg) { AlbumDetailScreen(viewModel, back, contentPadding) }
        composable(Screen.ArtistDetail.route, arguments = stringArg) { ArtistDetailScreen(viewModel, navigate, back, contentPadding) }
        composable(Screen.GenreDetail.route, arguments = stringArg) { GenreDetailScreen(viewModel, back, contentPadding) }
        composable(Screen.PlaylistDetail.route, arguments = longArg) { PlaylistDetailScreen(viewModel, back, contentPadding) }
    }
}
