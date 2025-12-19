package com.example.musicplayercompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.musicplayercompose.common.bottomnav.BottomNavItem
import com.example.musicplayercompose.presentation.favorite.FavoriteScreen
import com.example.musicplayercompose.presentation.home.HomeScreen
import com.example.musicplayercompose.presentation.mainscreen.MainScreen
import com.example.musicplayercompose.presentation.mymusic.MusicScreen
import com.example.musicplayercompose.presentation.splash.SplashScreen
import com.example.musicplayercompose.ui.theme.MusicPlayerComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            NavGraph(navController)
        }
    }
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
        composable("mainScreen") { MainScreen(navController) }
    }
}


