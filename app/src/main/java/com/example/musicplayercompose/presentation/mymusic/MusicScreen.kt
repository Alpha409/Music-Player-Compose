package com.example.musicplayercompose.presentation.mymusic

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import androidx.navigation.NavController
import com.example.musicplayercompose.R
import ir.kaaveh.sdpcompose.sdp

@Composable
fun MusicScreen(navController: NavController) {

    Column(modifier = Modifier.fillMaxSize()) {
        MusicItem(navController, modifier = Modifier)
    }
}

@Composable
fun MusicItem(navController: NavController, modifier: Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.sdp),
        shape = RoundedCornerShape(0.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.background(Color.Black).padding(5.sdp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier.size(50.sdp),
                shape = RoundedCornerShape(10.sdp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.allsongsplaceholder),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(8.sdp))
            Column(
                modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.songnameher),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )

                Text(
                    text = stringResource(R.string.artistnameh),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.heart_empty),
                contentDescription = stringResource(R.string.music),
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp).padding(end = 5.sdp).clickable(true, onClick = )
            )
        }
    }
}


@Preview
@Composable
fun previewMusicScreen() {
    MusicScreen(navController = NavController(LocalContext.current))
}