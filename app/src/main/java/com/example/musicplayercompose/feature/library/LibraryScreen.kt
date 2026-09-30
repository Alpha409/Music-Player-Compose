package com.example.musicplayercompose.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.musicplayercompose.core.designsystem.AlbumCard
import com.example.musicplayercompose.core.designsystem.ArtistRow
import com.example.musicplayercompose.core.designsystem.Artwork
import com.example.musicplayercompose.core.designsystem.EmptyState
import com.example.musicplayercompose.core.navigation.Screen
import com.example.musicplayercompose.core.utils.pluralSongs
import com.example.musicplayercompose.domain.model.SortOrder
import com.example.musicplayercompose.feature.main.LocalSongActions
import com.example.musicplayercompose.feature.main.MainViewModel
import kotlinx.coroutines.launch

private val tabTitles = listOf("Songs", "Albums", "Artists", "Genres", "Favorites")

@Composable
fun LibraryScreen(
    mainViewModel: MainViewModel,
    navigate: (String) -> Unit,
    contentPadding: PaddingValues,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val pager = rememberPagerState { tabTitles.size }
    val scope = rememberCoroutineScope()
    val playback by mainViewModel.playbackState.collectAsStateWithLifecycle()
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val albums by viewModel.albums.collectAsStateWithLifecycle()
    val artists by viewModel.artists.collectAsStateWithLifecycle()
    val genres by viewModel.genres.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val isSyncing by mainViewModel.isSyncing.collectAsStateWithLifecycle()
    val actions = LocalSongActions.current
    val bottom = contentPadding.calculateBottomPadding()

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Library", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            if (isSyncing) Text("Scanning…", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
        PrimaryScrollableTabRow(selectedTabIndex = pager.currentPage, edgePadding = 8.dp, divider = {}) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = pager.currentPage == index,
                    onClick = { scope.launch { pager.animateScrollToPage(index) } },
                    text = { Text(title) }
                )
            }
        }
        HorizontalPager(state = pager, modifier = Modifier.fillMaxSize(), key = { it }) { page ->
            when (page) {
                0 -> if (songs.isEmpty()) {
                    EmptyState(Icons.Rounded.LibraryMusic, "No songs yet", "Songs on your device will show up here.")
                } else LazyColumn(contentPadding = PaddingValues(bottom = bottom + 16.dp)) {
                    item(key = "controls", contentType = "controls") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PlayShuffleRow(
                                onPlay = { mainViewModel.playSongs(songs, 0, shuffle = false) },
                                onShuffle = { mainViewModel.playSongs(songs, shuffle = true) },
                                modifier = Modifier.weight(1f)
                            )
                            SortMenu(sortOrder, viewModel::setSortOrder)
                        }
                    }
                    songItems(songs, playback, { mainViewModel.playSongs(songs, it) }, actions.openMenu)
                }

                1 -> if (albums.isEmpty()) {
                    EmptyState(Icons.Rounded.Album, "No albums", "Albums are grouped from your songs' tags.")
                } else LazyVerticalGrid(
                    columns = GridCells.Adaptive(150.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = bottom + 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(albums, key = { it.id }) { album ->
                        AlbumCard(album, onClick = { navigate(Screen.AlbumDetail.create(album.id)) }, width = 150.dp)
                    }
                }

                2 -> if (artists.isEmpty()) {
                    EmptyState(Icons.Rounded.LibraryMusic, "No artists", "Artists are grouped from your songs' tags.")
                } else LazyColumn(contentPadding = PaddingValues(bottom = bottom + 16.dp)) {
                    items(artists, key = { "artist-${it.name}" }) { artist ->
                        ArtistRow(artist, onClick = { navigate(Screen.ArtistDetail.create(artist.name)) })
                    }
                }

                3 -> if (genres.isEmpty()) {
                    EmptyState(Icons.Rounded.LibraryMusic, "No genres", "Genres appear when your songs have genre tags.")
                } else LazyColumn(contentPadding = PaddingValues(bottom = bottom + 16.dp)) {
                    items(genres, key = { "genre-${it.name}" }) { genre ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable(onClickLabel = "Open genre ${genre.name}") { navigate(Screen.GenreDetail.create(genre.name)) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Rounded.LibraryMusic, null, tint = MaterialTheme.colorScheme.onSecondaryContainer) }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(genre.name, style = MaterialTheme.typography.titleMedium)
                                Text(pluralSongs(genre.songCount), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                else -> if (favorites.isEmpty()) {
                    EmptyState(Icons.Rounded.Favorite, "No favorites yet", "Tap the heart in the player to save songs here.")
                } else LazyColumn(contentPadding = PaddingValues(bottom = bottom + 16.dp)) {
                    item(key = "fav-controls", contentType = "controls") {
                        PlayShuffleRow(
                            onPlay = { mainViewModel.playSongs(favorites, 0, shuffle = false) },
                            onShuffle = { mainViewModel.playSongs(favorites, shuffle = true) }
                        )
                    }
                    songItems(favorites, playback, { mainViewModel.playSongs(favorites, it) }, actions.openMenu, keyPrefix = "fav")
                }
            }
        }
    }
}

@Composable
private fun SortMenu(current: SortOrder, onSelect: (SortOrder) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }) {
            Icon(Icons.Rounded.Sort, contentDescription = "Sort songs, currently by ${current.label}")
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            SortOrder.entries.forEach { order ->
                DropdownMenuItem(
                    text = {
                        Text(
                            order.label,
                            color = if (order == current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onSelect(order)
                        open = false
                    }
                )
            }
        }
    }
}
