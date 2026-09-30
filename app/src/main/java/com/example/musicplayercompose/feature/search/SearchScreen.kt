package com.example.musicplayercompose.feature.search

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.musicplayercompose.core.designsystem.AlbumCard
import com.example.musicplayercompose.core.designsystem.ArtistRow
import com.example.musicplayercompose.core.designsystem.EmptyState
import com.example.musicplayercompose.core.designsystem.SectionHeader
import com.example.musicplayercompose.core.designsystem.SongListItem
import com.example.musicplayercompose.core.navigation.Screen
import com.example.musicplayercompose.feature.main.LocalSongActions
import com.example.musicplayercompose.feature.main.MainViewModel
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.clip

@Composable
fun SearchScreen(
    mainViewModel: MainViewModel,
    navigate: (String) -> Unit,
    contentPadding: PaddingValues,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val playback by mainViewModel.playbackState.collectAsStateWithLifecycle()
    val actions = LocalSongActions.current
    val focus = LocalFocusManager.current

    androidx.compose.foundation.layout.Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Text("Search", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp))
        OutlinedTextField(
            value = query,
            onValueChange = viewModel::onQueryChange,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            singleLine = true,
            shape = MaterialTheme.shapes.extraLarge,
            placeholder = { Text("Songs, artists, albums, genres") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onQueryChange("") }) { Icon(Icons.Rounded.Clear, contentDescription = "Clear search") }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
            )
        )

        when {
            query.isBlank() -> EmptyState(
                Icons.Rounded.Search, "Find your music",
                "Search across every song, album, artist and genre on your device."
            )
            results.isEmpty -> EmptyState(Icons.Rounded.SearchOff, "No results", "Nothing matched “${query.trim()}”. Try a different spelling.")
            else -> LazyColumn(contentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding() + 16.dp)) {
                if (results.artists.isNotEmpty()) {
                    item(key = "h-artists") { SectionHeader("Artists") }
                    items(results.artists, key = { "artist-${it.name}" }) { artist ->
                        ArtistRow(artist, onClick = { navigate(Screen.ArtistDetail.create(artist.name)) })
                    }
                }
                if (results.albums.isNotEmpty()) {
                    item(key = "h-albums") { SectionHeader("Albums") }
                    item(key = "albums-row") {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(results.albums, key = { it.id }) { album ->
                                AlbumCard(album, onClick = { navigate(Screen.AlbumDetail.create(album.id)) })
                            }
                        }
                    }
                }
                if (results.songs.isNotEmpty()) {
                    item(key = "h-songs") { SectionHeader("Songs") }
                    items(results.songs.size, key = { "song-${results.songs[it].id}" }) { index ->
                        val song = results.songs[index]
                        SongListItem(
                            song = song,
                            isCurrent = playback.currentSongId == song.id,
                            isPlaying = playback.isPlaying,
                            onClick = { mainViewModel.playSongs(results.songs, index) },
                            onMore = { actions.openMenu(song) }
                        )
                    }
                }
            }
        }
    }
}
