package com.example.musicplayercompose.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.musicplayercompose.core.designsystem.AlbumCard
import com.example.musicplayercompose.core.designsystem.Artwork
import com.example.musicplayercompose.core.designsystem.ArtistCircle
import com.example.musicplayercompose.core.designsystem.EmptyState
import com.example.musicplayercompose.core.designsystem.PlaylistCard
import com.example.musicplayercompose.core.designsystem.SongCard
import com.example.musicplayercompose.core.designsystem.carousel
import com.example.musicplayercompose.core.navigation.Screen
import com.example.musicplayercompose.feature.main.LocalSongActions
import com.example.musicplayercompose.feature.main.MainViewModel
import java.util.Calendar

@Composable
fun HomeScreen(
    mainViewModel: MainViewModel,
    navigate: (String) -> Unit,
    contentPadding: PaddingValues,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val ui by viewModel.state.collectAsStateWithLifecycle()
    val playback by mainViewModel.playbackState.collectAsStateWithLifecycle()
    val currentSong by mainViewModel.currentSong.collectAsStateWithLifecycle()
    val songCount by mainViewModel.songCount.collectAsStateWithLifecycle()
    val syncing by mainViewModel.isSyncing.collectAsStateWithLifecycle()
    val hasSynced by mainViewModel.hasSynced.collectAsStateWithLifecycle()
    val actions = LocalSongActions.current

    when {
        songCount == 0 && (syncing || !hasSynced) -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Scanning your music…", style = MaterialTheme.typography.bodyLarge)
            }
        }
        songCount == 0 -> EmptyState(
            Icons.Rounded.LibraryMusic,
            "No music found",
            "We couldn't find any songs on this device. Add some music, then scan again.",
            actionLabel = "Scan storage",
            onAction = mainViewModel::rescan
        )
        else -> LazyColumn(contentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding() + 16.dp)) {
            item(key = "header") {
                Column(Modifier.statusBarsPadding().padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp)) {
                    Text(greeting(), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("What do you want to hear?", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = mainViewModel::shuffleAll) {
                        Icon(Icons.Rounded.Shuffle, null)
                        Text("Shuffle all", Modifier.padding(start = 8.dp))
                    }
                }
            }

            if (playback.hasMedia) {
                item(key = "continue") {
                    ContinueListeningCard(
                        title = playback.title.orEmpty(),
                        artist = playback.artist.orEmpty(),
                        artworkUri = playback.artworkUri,
                        isPlaying = playback.isPlaying,
                        onToggle = mainViewModel::togglePlayPause,
                        onOpen = { mainViewModel.setPlayerExpanded(true) }
                    )
                }
            }

            carousel("Recently played", ui.recentlyPlayed, { "rp-${it.id}" }) { i, song ->
                SongCard(song, onClick = { mainViewModel.playSongs(ui.recentlyPlayed, i) }, onLongClick = { actions.openMenu(song) })
            }
            carousel("Recently added", ui.recentlyAdded, { "ra-${it.id}" }) { i, song ->
                SongCard(song, onClick = { mainViewModel.playSongs(ui.recentlyAdded, i) }, onLongClick = { actions.openMenu(song) })
            }
            carousel("Favorites", ui.favorites, { "fav-${it.id}" }) { i, song ->
                SongCard(song, onClick = { mainViewModel.playSongs(ui.favorites, i) }, onLongClick = { actions.openMenu(song) })
            }
            carousel("Most played", ui.mostPlayed, { "mp-${it.id}" }) { i, song ->
                SongCard(song, onClick = { mainViewModel.playSongs(ui.mostPlayed, i) }, onLongClick = { actions.openMenu(song) })
            }
            carousel(
                "Playlists", ui.playlists, { "pl-${it.id}" },
                actionLabel = "See all", onAction = { navigate(Screen.Playlists.route) }
            ) { _, playlist -> PlaylistCard(playlist, onClick = { navigate(Screen.PlaylistDetail.create(playlist.id)) }) }
            carousel(
                "Albums", ui.albums, { "al-${it.id}" },
                actionLabel = "See all", onAction = { navigate(Screen.Library.route) }
            ) { _, album -> AlbumCard(album, onClick = { navigate(Screen.AlbumDetail.create(album.id)) }) }
            carousel("Artists", ui.artists, { "ar-${it.name}" }) { _, artist ->
                ArtistCircle(artist, onClick = { navigate(Screen.ArtistDetail.create(artist.name)) })
            }
        }
    }
}

@Composable
private fun ContinueListeningCard(
    title: String,
    artist: String,
    artworkUri: String?,
    isPlaying: Boolean,
    onToggle: () -> Unit,
    onOpen: () -> Unit
) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("Continue listening", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 8.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable(onClickLabel = "Open player", onClick = onOpen)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Artwork(artworkUri, Modifier.size(72.dp), RoundedCornerShape(16.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(artist, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            FilledIconButton(onClick = onToggle, modifier = Modifier.size(52.dp)) {
                Icon(
                    if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Resume"
                )
            }
        }
    }
}

private fun greeting(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 5..11 -> "Good morning"
    in 12..17 -> "Good afternoon"
    else -> "Good evening"
}
