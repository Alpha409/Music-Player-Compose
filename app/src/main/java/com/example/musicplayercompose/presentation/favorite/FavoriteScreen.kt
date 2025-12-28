package com.example.musicplayercompose.presentation.favorite

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.musicplayercompose.presentation.mymusic.MusicItem

@Composable
fun FavoriteScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        MusicItem(modifier = Modifier)
    }
}

@Preview
@Composable
fun PreviewMusicScreen() {
    FavoriteScreen()
}