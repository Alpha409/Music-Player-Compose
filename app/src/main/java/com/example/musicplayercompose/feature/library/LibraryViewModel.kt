package com.example.musicplayercompose.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayercompose.domain.model.Album
import com.example.musicplayercompose.domain.model.Artist
import com.example.musicplayercompose.domain.model.Genre
import com.example.musicplayercompose.domain.model.Song
import com.example.musicplayercompose.domain.model.SortOrder
import com.example.musicplayercompose.domain.repository.MusicRepository
import com.example.musicplayercompose.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    music: MusicRepository,
    private val settings: SettingsRepository
) : ViewModel() {

    val sortOrder: StateFlow<SortOrder> = settings.settings.map { it.sortOrder }.distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SortOrder.TITLE)

    @OptIn(ExperimentalCoroutinesApi::class)
    val songs: StateFlow<List<Song>> = sortOrder.flatMapLatest { music.observeSongs(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val albums: StateFlow<List<Album>> = music.observeAlbums()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val artists: StateFlow<List<Artist>> = music.observeArtists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val genres: StateFlow<List<Genre>> = music.observeGenres()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favorites: StateFlow<List<Song>> = music.observeFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setSortOrder(order: SortOrder) {
        viewModelScope.launch { settings.setSortOrder(order) }
    }
}
