package com.example.musicplayercompose.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayercompose.core.utils.decodeRouteArg
import com.example.musicplayercompose.domain.model.Album
import com.example.musicplayercompose.domain.model.Song
import com.example.musicplayercompose.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private val Stop = SharingStarted.WhileSubscribed(5_000)

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    savedState: SavedStateHandle,
    music: MusicRepository
) : ViewModel() {
    private val id: Long = checkNotNull(savedState["id"])
    val album: StateFlow<Album?> = music.observeAlbum(id).stateIn(viewModelScope, Stop, null)
    val songs: StateFlow<List<Song>> = music.observeAlbumSongs(id).stateIn(viewModelScope, Stop, emptyList())
}

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    savedState: SavedStateHandle,
    music: MusicRepository
) : ViewModel() {
    val name: String = decodeRouteArg(checkNotNull(savedState["name"]))
    val albums: StateFlow<List<Album>> = music.observeArtistAlbums(name).stateIn(viewModelScope, Stop, emptyList())
    val songs: StateFlow<List<Song>> = music.observeArtistSongs(name).stateIn(viewModelScope, Stop, emptyList())
}

@HiltViewModel
class GenreDetailViewModel @Inject constructor(
    savedState: SavedStateHandle,
    music: MusicRepository
) : ViewModel() {
    val name: String = decodeRouteArg(checkNotNull(savedState["name"]))
    val songs: StateFlow<List<Song>> = music.observeGenreSongs(name).stateIn(viewModelScope, Stop, emptyList())
}
