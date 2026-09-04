package com.example.musicplayercompose.presentation.favorite

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicplayercompose.R
import com.example.musicplayercompose.presentation.components.SearchBarComponent
import com.example.musicplayercompose.presentation.components.SongListItem
import com.example.musicplayercompose.ui.theme.AccentPink
import com.example.musicplayercompose.ui.theme.AccentPurple
import com.example.musicplayercompose.ui.theme.DarkBackground
import com.example.musicplayercompose.ui.theme.TextMuted
import com.example.musicplayercompose.ui.theme.TextPrimary
import com.example.musicplayercompose.ui.theme.TextSecondary
import com.example.musicplayercompose.viewModel.MainViewModel

@Composable
fun FavoriteScreen(
    viewModel: MainViewModel
) {
    val favSongs by viewModel.filteredFavSongs.collectAsState()
    val rawFavSongs by viewModel.favSongs.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentPlayingSong by viewModel.currentPlayingSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Favorites",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp
                    ),
                    color = TextPrimary
                )
                Text(
                    text = "${rawFavSongs.size} favorite songs",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            if (favSongs.isNotEmpty()) {
                Button(
                    onClick = {
                        favSongs.firstOrNull()?.let { firstSong ->
                            viewModel.playSong(firstSong, favSongs)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.play),
                        contentDescription = "Play All",
                        tint = TextPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Play All", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (rawFavSongs.isNotEmpty()) {
            SearchBarComponent(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChange(it) },
                placeholderText = "Search in favorites..."
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (favSongs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.favempty),
                        contentDescription = "No Favorites",
                        modifier = Modifier.size(120.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No matches found" else "No favorite songs yet",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Try a different search term" else "Tap the heart on any song to add it here!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                items(favSongs, key = { it.id }) { song ->
                    SongListItem(
                        song = song.copy(isFav = true),
                        isCurrentPlaying = currentPlayingSong?.id == song.id,
                        isPlaying = isPlaying,
                        onClick = { viewModel.playSong(song, favSongs) },
                        onFavoriteToggle = { viewModel.toggleFavorite(song) }
                    )
                }
            }
        }
    }
}