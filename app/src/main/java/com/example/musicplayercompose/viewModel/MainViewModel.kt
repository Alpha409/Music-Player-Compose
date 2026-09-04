package com.example.musicplayercompose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayercompose.common.player.AudioPlayerManager
import com.example.musicplayercompose.domain.models.Mp3FilesDataClass
import com.example.musicplayercompose.domain.repository.FavSongsRepo
import com.example.musicplayercompose.domain.repository.GetMusicLocalRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val localRepo: GetMusicLocalRepo,
    private val favRepo: FavSongsRepo,
    private val playerManager: AudioPlayerManager
) : ViewModel() {

    private val _rawLocalSongs = MutableStateFlow<List<Mp3FilesDataClass>>(emptyList())
    val rawLocalSongs: StateFlow<List<Mp3FilesDataClass>> = _rawLocalSongs.asStateFlow()

    private val _favSongs = MutableStateFlow<List<Mp3FilesDataClass>>(emptyList())
    val favSongs: StateFlow<List<Mp3FilesDataClass>> = _favSongs.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isPlayerSheetVisible = MutableStateFlow(false)
    val isPlayerSheetVisible: StateFlow<Boolean> = _isPlayerSheetVisible.asStateFlow()

    // Player state delegated from AudioPlayerManager
    val currentPlayingSong: StateFlow<Mp3FilesDataClass?> = playerManager.currentSong
    val isPlaying: StateFlow<Boolean> = playerManager.isPlaying
    val currentPosition: StateFlow<Long> = playerManager.currentPosition
    val duration: StateFlow<Long> = playerManager.duration
    val isShuffle: StateFlow<Boolean> = playerManager.isShuffle
    val isRepeat: StateFlow<Boolean> = playerManager.isRepeat

    // Filtered songs combining raw list, favorites status, and search query
    val songs: StateFlow<List<Mp3FilesDataClass>> = combine(
        _rawLocalSongs,
        _favSongs,
        _searchQuery
    ) { localList, favList, query ->
        val favIds = favList.map { it.id }.toSet()
        val mappedList = localList.map { song ->
            song.copy(isFav = favIds.contains(song.id))
        }

        if (query.isBlank()) {
            mappedList
        } else {
            mappedList.filter {
                it.title.contains(query, ignoreCase = true) ||
                        (it.artist?.contains(query, ignoreCase = true) == true) ||
                        (it.album?.contains(query, ignoreCase = true) == true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered favorites
    val filteredFavSongs: StateFlow<List<Mp3FilesDataClass>> = combine(
        _favSongs,
        _searchQuery
    ) { favList, query ->
        if (query.isBlank()) {
            favList
        } else {
            favList.filter {
                it.title.contains(query, ignoreCase = true) ||
                        (it.artist?.contains(query, ignoreCase = true) == true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadMusic()
    }

    fun loadMusic() {
        getLocalMp3Files()
        getAllFavSongs()
    }

    fun getLocalMp3Files() {
        viewModelScope.launch(IO) {
            localRepo.getMp3LocalFiles().collect { files ->
                _rawLocalSongs.value = files
            }
        }
    }

    fun getAllFavSongs() {
        viewModelScope.launch(IO) {
            favRepo.getAllFavSongs().collect { list ->
                _favSongs.value = list
            }
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun playSong(song: Mp3FilesDataClass, playlist: List<Mp3FilesDataClass>) {
        playerManager.playSong(song, playlist)
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun playNext() {
        playerManager.playNext()
    }

    fun playPrevious() {
        playerManager.playPrevious()
    }

    fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
    }

    fun toggleShuffle() {
        playerManager.toggleShuffle()
    }

    fun toggleRepeat() {
        playerManager.toggleRepeat()
    }

    fun toggleFavorite(song: Mp3FilesDataClass) {
        viewModelScope.launch(IO) {
            val isCurrentlyFav = _favSongs.value.any { it.id == song.id }
            if (isCurrentlyFav) {
                favRepo.removeFav(song)
            } else {
                favRepo.insertFav(song.copy(isFav = true))
            }
        }
    }

    fun setPlayerSheetVisible(visible: Boolean) {
        _isPlayerSheetVisible.value = visible
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}