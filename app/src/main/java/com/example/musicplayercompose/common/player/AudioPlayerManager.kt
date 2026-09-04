package com.example.musicplayercompose.common.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicplayercompose.domain.models.Mp3FilesDataClass
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioPlayerManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(context).build().apply {
            addListener(playerListener)
        }
    }

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    private val _currentSong = MutableStateFlow<Mp3FilesDataClass?>(null)
    val currentSong: StateFlow<Mp3FilesDataClass?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _playlist = MutableStateFlow<List<Mp3FilesDataClass>>(emptyList())
    val playlist: StateFlow<List<Mp3FilesDataClass>> = _playlist.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _isRepeat = MutableStateFlow(false)
    val isRepeat: StateFlow<Boolean> = _isRepeat.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(playing: Boolean) {
            _isPlaying.value = playing
            if (playing) {
                startProgressTracker()
            } else {
                stopProgressTracker()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    _duration.value = exoPlayer.duration.coerceAtLeast(0L)
                }
                Player.STATE_ENDED -> {
                    if (_isRepeat.value) {
                        exoPlayer.seekTo(0)
                        exoPlayer.play()
                    } else {
                        playNext()
                    }
                }
                else -> Unit
            }
        }
    }

    fun playSong(song: Mp3FilesDataClass, list: List<Mp3FilesDataClass>) {
        _playlist.value = list
        _currentSong.value = song

        val uriString = if (song.contentUriString.isNotBlank()) song.contentUriString else song.path
        val mediaItem = MediaItem.fromUri(Uri.parse(uriString))

        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            pause()
        } else {
            resume()
        }
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun resume() {
        if (exoPlayer.playbackState == Player.STATE_IDLE && _currentSong.value != null) {
            _currentSong.value?.let { playSong(it, _playlist.value) }
        } else {
            exoPlayer.play()
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        _currentPosition.value = positionMs
    }

    fun playNext() {
        val currentList = _playlist.value
        if (currentList.isEmpty()) return

        val currentIndex = currentList.indexOfFirst { it.id == _currentSong.value?.id }
        if (_isShuffle.value) {
            val randomIndex = currentList.indices.random()
            playSong(currentList[randomIndex], currentList)
        } else if (currentIndex != -1 && currentIndex < currentList.size - 1) {
            playSong(currentList[currentIndex + 1], currentList)
        } else {
            // Loop back to start
            playSong(currentList.first(), currentList)
        }
    }

    fun playPrevious() {
        val currentList = _playlist.value
        if (currentList.isEmpty()) return

        // If played more than 3 seconds, restart current track
        if (exoPlayer.currentPosition > 3000L) {
            seekTo(0)
            return
        }

        val currentIndex = currentList.indexOfFirst { it.id == _currentSong.value?.id }
        if (currentIndex > 0) {
            playSong(currentList[currentIndex - 1], currentList)
        } else {
            playSong(currentList.last(), currentList)
        }
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun toggleRepeat() {
        _isRepeat.value = !_isRepeat.value
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressJob = scope.launch {
            while (isActive) {
                _currentPosition.value = exoPlayer.currentPosition.coerceAtLeast(0L)
                val dur = exoPlayer.duration
                if (dur > 0L) {
                    _duration.value = dur
                }
                delay(500)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stopProgressTracker()
        exoPlayer.removeListener(playerListener)
        exoPlayer.release()
    }
}
