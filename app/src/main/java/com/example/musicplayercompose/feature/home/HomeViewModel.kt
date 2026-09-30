package com.example.musicplayercompose.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayercompose.domain.model.Album
import com.example.musicplayercompose.domain.model.Artist
import com.example.musicplayercompose.domain.model.Playlist
import com.example.musicplayercompose.domain.model.Song
import com.example.musicplayercompose.domain.repository.MusicRepository
import com.example.musicplayercompose.domain.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HomeUiState(
    val recentlyPlayed: List<Song> = emptyList(),
    val recentlyAdded: List<Song> = emptyList(),
    val favorites: List<Song> = emptyList(),
    val mostPlayed: List<Song> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    music: MusicRepository,
    playlists: PlaylistRepository
) : ViewModel() {

    // Each carousel is capped in SQL, so Home never pulls the whole library into memory.
    private val songSections = combine(
        music.observeRecentlyPlayed(CAROUSEL_SIZE),
        music.observeRecentlyAdded(CAROUSEL_SIZE),
        music.observeFavorites().map { it.take(CAROUSEL_SIZE) },
        music.observeMostPlayed(CAROUSEL_SIZE)
    ) { played, added, favorites, most -> listOf(played, added, favorites, most) }

    private val collections = combine(
        playlists.observePlaylists(),
        music.observeAlbums().map { it.take(CAROUSEL_SIZE) },
        music.observeArtists().map { it.take(CAROUSEL_SIZE) }
    ) { p, al, ar -> Triple(p, al, ar) }

    val state: StateFlow<HomeUiState> = combine(songSections, collections) { songs, (p, al, ar) ->
        HomeUiState(songs[0], songs[1], songs[2], songs[3], p, al, ar)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    private companion object {
        const val CAROUSEL_SIZE = 20
    }
}
