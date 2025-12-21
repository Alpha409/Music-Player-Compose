package com.example.musicplayercompose.presentation.favorite

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.example.musicplayercompose.presentation.mymusic.MusicItem

@Composable
fun FavoriteScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()) {
        MusicItem(navController)
    }
}

@Preview
@Composable
fun previewMusicScreen() {
    FavoriteScreen(navController = NavController(LocalContext.current))
}