package com.example.musicplayercompose.feature.playlists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayercompose.domain.model.Playlist
import com.example.musicplayercompose.domain.model.Song
import com.example.musicplayercompose.domain.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistsViewModel @Inject constructor(private val repo: PlaylistRepository) : ViewModel() {
    val playlists: StateFlow<List<Playlist>> = repo.observePlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun create(name: String) {
        viewModelScope.launch { repo.createPlaylist(name) }
    }

    fun rename(id: Long, name: String) {
        viewModelScope.launch { repo.renamePlaylist(id, name) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repo.deletePlaylist(id) }
    }
}

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val repo: PlaylistRepository
) : ViewModel() {
    val id: Long = checkNotNull(savedState["id"])

    val playlist: StateFlow<Playlist?> = repo.observePlaylist(id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val songs: StateFlow<List<Song>> = repo.observePlaylistSongs(id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun remove(songId: Long) {
        viewModelScope.launch { repo.removeSong(id, songId) }
    }

    fun move(from: Int, to: Int) {
        val current = songs.value.map { it.id }.toMutableList()
        if (from !in current.indices || to !in current.indices) return
        current.add(to, current.removeAt(from))
        viewModelScope.launch { repo.reorder(id, current) }
    }

    fun rename(name: String) {
        viewModelScope.launch { repo.renamePlaylist(id, name) }
    }

    fun delete() {
        viewModelScope.launch { repo.deletePlaylist(id) }
    }
}
