package com.example.musicplayercompose.presentation.mymusic

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController

@Composable
fun MusicScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()){
        MusicItem(navController)
    }
}



@Composable
fun MusicItem(navController: NavController) {



}


@Preview
@Composable
fun previewMusicScreen(){
    MusicScreen(navController = NavController(LocalContext.current))

}