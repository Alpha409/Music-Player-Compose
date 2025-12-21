package com.example.musicplayercompose.presentation.mymusic

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview

import androidx.navigation.NavController
import com.example.musicplayercompose.R

@Composable
fun MusicScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()){
        MusicItem(navController)
    }
}

@Composable
fun MusicItem(navController: NavController) {
    Row() {
        Icon(painter = painterResource(R.drawable.allsongsplaceholder),contentDescription = null)
    }
}


@Preview
@Composable
fun previewMusicScreen(){
    MusicScreen(navController = NavController(LocalContext.current))

}