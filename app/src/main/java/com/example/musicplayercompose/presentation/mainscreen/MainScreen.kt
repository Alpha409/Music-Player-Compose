package com.example.musicplayercompose.presentation.mainscreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.musicplayercompose.common.bottomnav.BottomNavItem
import com.example.musicplayercompose.presentation.components.FullPlayerSheet
import com.example.musicplayercompose.presentation.components.MiniPlayer
import com.example.musicplayercompose.presentation.favorite.FavoriteScreen
import com.example.musicplayercompose.presentation.home.HomeScreen
import com.example.musicplayercompose.presentation.mymusic.MusicScreen
import com.example.musicplayercompose.presentation.splash.SplashScreen
import com.example.musicplayercompose.ui.theme.AccentPurple
import com.example.musicplayercompose.ui.theme.DarkBackground
import com.example.musicplayercompose.ui.theme.DarkSurface
import com.example.musicplayercompose.ui.theme.TextMuted
import com.example.musicplayercompose.ui.theme.TextPrimary
import com.example.musicplayercompose.viewModel.MainViewModel

val bottomBarRoutes = listOf(
    BottomNavItem.Home.route,
    BottomNavItem.MyMusic.route,
    BottomNavItem.Favorite.route
)

@Composable
fun MainScreen(
    navController: NavHostController,
    viewModel: MainViewModel
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentPlayingSong by viewModel.currentPlayingSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val isPlayerSheetVisible by viewModel.isPlayerSheetVisible.collectAsState()
    val isShuffle by viewModel.isShuffle.collectAsState()
    val isRepeat by viewModel.isRepeat.collectAsState()

    val showBottomBars = currentRoute in bottomBarRoutes

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            if (showBottomBars) {
                Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
                    // MiniPlayer docked above BottomBar
                    AnimatedVisibility(
                        visible = currentPlayingSong != null,
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it })
                    ) {
                        currentPlayingSong?.let { song ->
                            MiniPlayer(
                                song = song,
                                isPlaying = isPlaying,
                                currentPosition = currentPosition,
                                duration = duration,
                                onTogglePlayPause = { viewModel.togglePlayPause() },
                                onNext = { viewModel.playNext() },
                                onOpenFullPlayer = { viewModel.setPlayerSheetVisible(true) }
                            )
                        }
                    }

                    BottomBar(navController = navController, currentRoute = currentRoute)
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            NavHost(
                navController = navController,
                startDestination = "splash"
            ) {
                composable("splash") {
                    SplashScreen(navController)
                }
                composable("home") {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToMyMusic = {
                            navController.navigate(BottomNavItem.MyMusic.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToFavorites = {
                            navController.navigate(BottomNavItem.Favorite.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable("myMusic") {
                    MusicScreen(viewModel = viewModel)
                }
                composable("fav") {
                    FavoriteScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Full Player Modal Sheet
    if (isPlayerSheetVisible) {
        FullPlayerSheet(
            song = currentPlayingSong,
            isPlaying = isPlaying,
            currentPosition = currentPosition,
            duration = duration,
            isShuffle = isShuffle,
            isRepeat = isRepeat,
            onDismiss = { viewModel.setPlayerSheetVisible(false) },
            onTogglePlayPause = { viewModel.togglePlayPause() },
            onNext = { viewModel.playNext() },
            onPrevious = { viewModel.playPrevious() },
            onSeekTo = { viewModel.seekTo(it) },
            onToggleShuffle = { viewModel.toggleShuffle() },
            onToggleRepeat = { viewModel.toggleRepeat() },
            onToggleFavorite = { viewModel.toggleFavorite(it) }
        )
    }
}

@Composable
fun BottomBar(
    navController: NavHostController,
    currentRoute: String?
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.MyMusic,
        BottomNavItem.Favorite
    )

    NavigationBar(
        containerColor = DarkSurface,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(BottomNavItem.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(item.icon),
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AccentPurple,
                    selectedTextColor = AccentPurple,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                    indicatorColor = AccentPurple.copy(alpha = 0.15f)
                )
            )
        }
    }
}