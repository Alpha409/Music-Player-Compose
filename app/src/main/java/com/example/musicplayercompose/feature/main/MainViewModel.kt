package com.example.musicplayercompose.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayercompose.domain.model.AppSettings
import com.example.musicplayercompose.domain.model.Playlist
import com.example.musicplayercompose.domain.model.Song
import com.example.musicplayercompose.domain.repository.MusicRepository
import com.example.musicplayercompose.domain.repository.PlaylistRepository
import com.example.musicplayercompose.domain.repository.SettingsRepository
import com.example.musicplayercompose.domain.usecase.SyncLibraryUseCase
import com.example.musicplayercompose.domain.usecase.ToggleFavoriteUseCase
import com.example.musicplayercompose.playback.PlaybackEvents
import com.example.musicplayercompose.playback.PlaybackManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Activity-scoped: owns everything that has to be shared across screens — the playback handle,
 * library sync, global snackbar messages and the actions every song menu needs.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val music: MusicRepository,
    private val playlistRepo: PlaylistRepository,
    private val settingsRepo: SettingsRepository,
    private val playback: PlaybackManager,
    private val syncLibrary: SyncLibraryUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val playbackEvents: PlaybackEvents
) : ViewModel() {

    val settings: StateFlow<AppSettings?> = settingsRepo.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val playbackState = playback.state
    val positionMs = playback.positionMs
    val currentSong = playback.currentSong

    val playlists: StateFlow<List<Playlist>> = playlistRepo.observePlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val songCount: StateFlow<Int?> = music.observeSongCount()
        .map<Int, Int?> { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _hasSynced = MutableStateFlow(false)
    val hasSynced: StateFlow<Boolean> = _hasSynced.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private val _isPlayerExpanded = MutableStateFlow(false)
    val isPlayerExpanded: StateFlow<Boolean> = _isPlayerExpanded.asStateFlow()

    private val _isQueueVisible = MutableStateFlow(false)
    val isQueueVisible: StateFlow<Boolean> = _isQueueVisible.asStateFlow()

    private var syncJob: Job? = null

    init {
        playback.connect()
        viewModelScope.launch { playbackEvents.errors.collect { _messages.emit(it) } }
    }

    // region library sync

    /** Starts (once) a sync that re-runs on MediaStore changes and when excluded folders change. */
    @OptIn(FlowPreview::class)
    fun startLibrarySync() {
        if (syncJob?.isActive == true) return
        syncJob = viewModelScope.launch {
            val triggers = merge(
                music.observeMediaStoreChanges().debounce(1_500),
                // Emits on collection, which doubles as the initial sync.
                settingsRepo.settings.map { it.excludedFolders }.distinctUntilChanged().map { }
            )
            triggers.collectLatest { runSync() }
        }
    }

    fun rescan() {
        viewModelScope.launch { runSync(announce = true) }
    }

    private suspend fun runSync(announce: Boolean = false) {
        _isSyncing.value = true
        try {
            val ok = syncLibrary()
            if (!ok) _messages.emit("Couldn't read your music library. Check that access is allowed.")
            else if (announce) _messages.emit("Library updated")
        } finally {
            _isSyncing.value = false
            _hasSynced.value = true
        }
    }

    // endregion

    // region playback

    fun playSongs(songs: List<Song>, startIndex: Int = 0, shuffle: Boolean? = null) =
        playback.playSongs(songs, startIndex, shuffle)

    fun shuffleAll() {
        viewModelScope.launch {
            val sort = settingsRepo.settings.first().sortOrder
            val all = music.observeSongs(sort).first()
            if (all.isEmpty()) _messages.emit("No songs to play yet") else playback.playSongs(all, shuffle = true)
        }
    }

    fun togglePlayPause() = playback.togglePlayPause()
    fun next() = playback.next()
    fun previous() = playback.previous()
    fun seekTo(ms: Long) = playback.seekTo(ms)
    fun seekBy(ms: Long) = playback.seekBy(ms)
    fun toggleShuffle() = playback.toggleShuffle()
    fun cycleRepeat() = playback.cycleRepeat()

    fun playNext(song: Song) {
        playback.playNext(song)
        _messages.tryEmit("\"${song.displayTitle}\" will play next")
    }

    fun addToQueue(song: Song) {
        playback.addToQueue(song)
        _messages.tryEmit("Added \"${song.displayTitle}\" to queue")
    }

    fun addToQueue(songs: List<Song>) {
        playback.addToQueue(songs)
        _messages.tryEmit("Added ${songs.size} songs to queue")
    }

    fun removeFromQueue(index: Int) = playback.removeFromQueue(index)
    fun moveQueueItem(from: Int, to: Int) = playback.moveQueueItem(from, to)
    fun skipToQueueItem(index: Int) = playback.skipToQueueItem(index)
    fun clearQueue() {
        playback.clearQueue()
        _isQueueVisible.value = false
        _isPlayerExpanded.value = false
    }

    fun setPlayerExpanded(expanded: Boolean) {
        _isPlayerExpanded.value = expanded
    }

    fun setQueueVisible(visible: Boolean) {
        _isQueueVisible.value = visible
    }

    // endregion

    // region song actions

    fun toggleFavorite(songId: Long) {
        viewModelScope.launch { toggleFavoriteUseCase(songId) }
    }

    fun addToPlaylist(playlist: Playlist, songs: List<Song>) {
        viewModelScope.launch {
            val added = playlistRepo.addSongs(playlist.id, songs.map { it.id })
            _messages.emit(
                when {
                    added == 0 -> "Already in \"${playlist.name}\""
                    added == 1 -> "Added to \"${playlist.name}\""
                    else -> "Added $added songs to \"${playlist.name}\""
                }
            )
        }
    }

    fun createPlaylistAndAdd(name: String, songs: List<Song>) {
        viewModelScope.launch {
            val id = playlistRepo.createPlaylist(name)
            if (songs.isNotEmpty()) playlistRepo.addSongs(id, songs.map { it.id })
            _messages.emit("Created \"${name.trim()}\"")
        }
    }

    fun showMessage(message: String) {
        _messages.tryEmit(message)
    }

    // endregion
}
