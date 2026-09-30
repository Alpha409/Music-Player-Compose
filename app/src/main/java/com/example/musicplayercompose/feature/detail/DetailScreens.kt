package com.example.musicplayercompose.feature.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.musicplayercompose.core.designsystem.AlbumCard
import com.example.musicplayercompose.core.designsystem.Artwork
import com.example.musicplayercompose.core.designsystem.EmptyState
import com.example.musicplayercompose.core.designsystem.SectionHeader
import com.example.musicplayercompose.core.navigation.Screen
import com.example.musicplayercompose.core.utils.formatTotalDuration
import com.example.musicplayercompose.core.utils.pluralSongs
import com.example.musicplayercompose.domain.model.Song
import com.example.musicplayercompose.feature.library.PlayShuffleRow
import com.example.musicplayercompose.feature.library.songItems
import com.example.musicplayercompose.feature.main.LocalSongActions
import com.example.musicplayercompose.feature.main.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScaffold(
    title: String,
    onBack: () -> Unit,
    songs: List<Song>,
    mainViewModel: MainViewModel,
    contentPadding: PaddingValues,
    artworkUri: String?,
    artworkShape: Shape,
    subtitle: String,
    modifier: Modifier = Modifier,
    extraContent: (androidx.compose.foundation.lazy.LazyListScope.() -> Unit)? = null
) {
    val playback by mainViewModel.playbackState.collectAsStateWithLifecycle()
    val actions = LocalSongActions.current
    var menuOpen by remember { mutableStateOf(false) }

    Column(modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(title, maxLines = 1) },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") }
            },
            actions = {
                IconButton(onClick = { menuOpen = true }, enabled = songs.isNotEmpty()) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "Collection options")
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(text = { Text("Add all to queue") }, onClick = {
                        menuOpen = false
                        mainViewModel.addToQueue(songs)
                    })
                    DropdownMenuItem(text = { Text("Add all to playlist") }, onClick = {
                        menuOpen = false
                        actions.addToPlaylist(songs)
                    })
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )
        if (songs.isEmpty()) {
            EmptyState(Icons.Rounded.Person, "Nothing here", "These songs are no longer in your library.")
            return@Column
        }
        LazyColumn(contentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding() + 16.dp)) {
            item(key = "hero") {
                Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Artwork(
                        artworkUri,
                        Modifier.widthIn(max = 220.dp).fillMaxWidth(0.6f).aspectRatio(1f),
                        artworkShape,
                        fallbackIcon = if (artworkShape == CircleShape) Icons.Rounded.Person else Icons.Rounded.MusicNote
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center, maxLines = 2)
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
            item(key = "controls") {
                PlayShuffleRow(
                    onPlay = { mainViewModel.playSongs(songs, 0, shuffle = false) },
                    onShuffle = { mainViewModel.playSongs(songs, shuffle = true) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            extraContent?.invoke(this)
            songItems(songs, playback, { mainViewModel.playSongs(songs, it) }, actions.openMenu)
        }
    }
}

@Composable
fun AlbumDetailScreen(
    mainViewModel: MainViewModel,
    onBack: () -> Unit,
    contentPadding: PaddingValues,
    viewModel: AlbumDetailViewModel = hiltViewModel()
) {
    val album by viewModel.album.collectAsStateWithLifecycle()
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val total = songs.sumOf { it.durationMs }
    DetailScaffold(
        title = album?.displayTitle ?: "Album",
        onBack = onBack,
        songs = songs,
        mainViewModel = mainViewModel,
        contentPadding = contentPadding,
        artworkUri = album?.artworkUri,
        artworkShape = RoundedCornerShape(24.dp),
        subtitle = listOfNotNull(
            album?.displayArtist,
            album?.year?.toString(),
            "${pluralSongs(songs.size)} · ${formatTotalDuration(total)}"
        ).joinToString(" · ")
    )
}

@Composable
fun ArtistDetailScreen(
    mainViewModel: MainViewModel,
    navigate: (String) -> Unit,
    onBack: () -> Unit,
    contentPadding: PaddingValues,
    viewModel: ArtistDetailViewModel = hiltViewModel()
) {
    val albums by viewModel.albums.collectAsStateWithLifecycle()
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    DetailScaffold(
        title = viewModel.name.ifBlank { "Unknown artist" },
        onBack = onBack,
        songs = songs,
        mainViewModel = mainViewModel,
        contentPadding = contentPadding,
        artworkUri = albums.firstOrNull()?.artworkUri,
        artworkShape = CircleShape,
        subtitle = "${if (albums.size == 1) "1 album" else "${albums.size} albums"} · ${pluralSongs(songs.size)}",
        extraContent = if (albums.isEmpty()) null else {
            {
                item(key = "albums-header") { SectionHeader("Albums") }
                item(key = "albums-row") {
                    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(albums, key = { it.id }) { album ->
                            AlbumCard(album, onClick = { navigate(Screen.AlbumDetail.create(album.id)) })
                        }
                    }
                }
                item(key = "songs-header") { SectionHeader("Songs") }
            }
        }
    )
}

@Composable
fun GenreDetailScreen(
    mainViewModel: MainViewModel,
    onBack: () -> Unit,
    contentPadding: PaddingValues,
    viewModel: GenreDetailViewModel = hiltViewModel()
) {
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    DetailScaffold(
        title = viewModel.name,
        onBack = onBack,
        songs = songs,
        mainViewModel = mainViewModel,
        contentPadding = contentPadding,
        artworkUri = songs.firstOrNull()?.artworkUri,
        artworkShape = RoundedCornerShape(24.dp),
        subtitle = "${pluralSongs(songs.size)} · ${formatTotalDuration(songs.sumOf { it.durationMs })}"
    )
}
