package com.example.musicplayercompose.feature.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.QueueMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.musicplayercompose.core.navigation.AppNavHost
import com.example.musicplayercompose.core.navigation.Screen
import com.example.musicplayercompose.feature.player.FullPlayerSheet
import com.example.musicplayercompose.feature.player.MiniPlayer
import com.example.musicplayercompose.feature.player.QueueSheet

private data class TopLevelTab(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
)

private val tabs = listOf(
    TopLevelTab(Screen.Home, "Home", Icons.Outlined.Home, Icons.Rounded.Home),
    TopLevelTab(Screen.Library, "Library", Icons.Outlined.LibraryMusic, Icons.Rounded.LibraryMusic),
    TopLevelTab(Screen.Search, "Search", Icons.Outlined.Search, Icons.Rounded.Search),
    TopLevelTab(Screen.Playlists, "Playlists", Icons.Outlined.QueueMusic, Icons.Rounded.QueueMusic),
    TopLevelTab(Screen.Settings, "Settings", Icons.Outlined.Settings, Icons.Rounded.Settings)
)

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val destination = backStack?.destination
    val showNavBar = tabs.any { it.screen.route == destination?.route }

    val snackbar = remember { SnackbarHostState() }
    val isPlayerExpanded by viewModel.isPlayerExpanded.collectAsState()
    val isQueueVisible by viewModel.isQueueVisible.collectAsState()
    val playback by viewModel.playbackState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.messages.collect { message ->
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showSnackbar(message)
        }
    }

    SongActionsHost(viewModel = viewModel, navigate = { navController.navigate(it) }) {
        Scaffold(
            contentWindowInsets = WindowInsets(0),
            snackbarHost = {
                SnackbarHost(snackbar)
            },
            bottomBar = {
                Column(if (showNavBar) Modifier else Modifier.navigationBarsPadding()) {
                    AnimatedVisibility(
                        visible = playback.hasMedia,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut()
                    ) { MiniPlayer(viewModel) }
                    if (showNavBar) {
                        NavigationBar {
                            tabs.forEach { tab ->
                                val selected = destination?.hierarchy?.any { it.route == tab.screen.route } == true
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        navController.navigate(tab.screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(if (selected) tab.selectedIcon else tab.icon, contentDescription = null) },
                                    label = { Text(tab.label) },
                                    modifier = Modifier.semantics { contentDescription = "${tab.label} tab" }
                                )
                            }
                        }
                    }
                }
            }
        ) { padding ->
            AppNavHost(
                navController = navController,
                viewModel = viewModel,
                contentPadding = PaddingValues(bottom = padding.calculateBottomPadding())
            )
        }

        if (isPlayerExpanded) FullPlayerSheet(viewModel)
        if (isQueueVisible) QueueSheet(viewModel)
    }
}
