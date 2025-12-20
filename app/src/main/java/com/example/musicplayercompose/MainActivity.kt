package com.example.musicplayercompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.musicplayercompose.presentation.favorite.FavoriteScreen
import com.example.musicplayercompose.presentation.home.HomeScreen
import com.example.musicplayercompose.presentation.mainscreen.MainScreen
import com.example.musicplayercompose.presentation.mymusic.MusicScreen
import com.example.musicplayercompose.presentation.splash.SplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            NavGraph(navController)
            AppRoot()
        }
    }
}
@Composable
fun AppRoot() {
    val navController = rememberNavController()
    MainScreen(navController)
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination =  "splash"
    ) {
        composable("splash") {
            SplashScreen(navController)
        }
        composable("home") {
            HomeScreen(navController)
        }
        composable("myMusic") { MusicScreen(navController) }
        composable("fav") { FavoriteScreen(navController) }
    }
}


