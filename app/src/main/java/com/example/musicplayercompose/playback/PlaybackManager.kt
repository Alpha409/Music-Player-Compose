package com.example.musicplayercompose.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.musicplayercompose.domain.model.Song
import com.example.musicplayercompose.domain.repository.MusicRepository
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class PlaybackState(
    val isConnected: Boolean = false,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentSongId: Long? = null,
    val currentIndex: Int = -1,
    val durationMs: Long = 0L,
    val shuffle: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val queue: List<QueueItem> = emptyList(),
    // Shown immediately from the MediaItem, before the Room lookup for the full Song arrives.
    val title: String? = null,
    val artist: String? = null,
    val artworkUri: String? = null
) {
    val hasMedia: Boolean get() = currentSongId != null
}

/**
 * UI-side handle to the playback service. All state comes from a [MediaController], so what the
 * UI shows is always exactly what the service (and therefore the notification) is doing.
 */
@Singleton
class PlaybackManager @Inject constructor(
    @ApplicationContext private val context: Context,
    music: MusicRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private val pending = mutableListOf<(MediaController) -> Unit>()
    private var tickerJob: Job? = null

    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private val _positionMs = MutableStateFlow(0L)
    val positionMs: StateFlow<Long> = _positionMs.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentSong: StateFlow<Song?> = _state
        .map { it.currentSongId }
        .distinctUntilChanged()
        .flatMapLatest { id -> if (id == null) flowOf(null) else music.observeSong(id) }
        .stateIn(scope, SharingStarted.Eagerly, null)

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            refresh(rebuildQueue = events.contains(Player.EVENT_TIMELINE_CHANGED) || !_state.value.isConnected)
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) startTicker() else stopTicker()
            _positionMs.value = controller?.currentPosition?.coerceAtLeast(0L) ?: 0L
        }
    }

    /** Idempotent; safe to call from every screen that needs playback. */
    fun connect() {
        if (controllerFuture != null) return
        val token = SessionToken(context, ComponentName(context, MediaPlayerService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        controllerFuture = future
        future.addListener({
            val c = runCatching { future.get() }.getOrNull()
            if (c == null) {
                controllerFuture = null
                return@addListener
            }
            controller = c
            c.addListener(listener)
            refresh(rebuildQueue = true)
            if (c.isPlaying) startTicker()
            _positionMs.value = c.currentPosition.coerceAtLeast(0L)
            pending.forEach { it(c) }
            pending.clear()
        }, MoreExecutors.directExecutor())
    }

    private fun withController(block: (MediaController) -> Unit) {
        connect()
        controller?.let(block) ?: pending.add(block)
    }

    private fun refresh(rebuildQueue: Boolean) {
        val c = controller ?: return
        val item = c.currentMediaItem
        val previous = _state.value
        _state.value = PlaybackState(
            isConnected = true,
            isPlaying = c.isPlaying,
            isBuffering = c.playbackState == Player.STATE_BUFFERING,
            currentSongId = item?.songId(),
            currentIndex = c.currentMediaItemIndex,
            durationMs = c.duration.takeIf { it != C.TIME_UNSET } ?: 0L,
            shuffle = c.shuffleModeEnabled,
            repeatMode = c.repeatMode,
            queue = if (rebuildQueue) buildQueue(c) else previous.queue,
            title = item?.mediaMetadata?.title?.toString(),
            artist = item?.mediaMetadata?.artist?.toString(),
            artworkUri = item?.mediaMetadata?.artworkUri?.toString()
        )
    }

    private fun buildQueue(c: MediaController): List<QueueItem> = (0 until c.mediaItemCount).map { i ->
        val item = c.getMediaItemAt(i)
        QueueItem(
            index = i,
            songId = item.songId() ?: -1L,
            title = item.mediaMetadata.title?.toString().orEmpty(),
            artist = item.mediaMetadata.artist?.toString().orEmpty(),
            artworkUri = item.mediaMetadata.artworkUri?.toString()
        )
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                controller?.let { _positionMs.value = it.currentPosition.coerceAtLeast(0L) }
                delay(TICK_MS)
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    // region commands

    /** Replaces the queue. [shuffle] = null keeps whatever shuffle mode the user has on. */
    fun playSongs(songs: List<Song>, startIndex: Int = 0, shuffle: Boolean? = null) {
        if (songs.isEmpty()) return
        withController { c ->
            shuffle?.let { c.shuffleModeEnabled = it }
            val start = if (shuffle == true) C.INDEX_UNSET else startIndex.coerceIn(0, songs.lastIndex)
            c.setMediaItems(songs.map { it.toMediaItem() }, start, 0L)
            c.prepare()
            c.play()
        }
    }

    fun playNext(song: Song) {
        withController { c ->
            if (c.mediaItemCount == 0) {
                c.setMediaItem(song.toMediaItem())
                c.prepare()
                c.play()
            } else {
                c.addMediaItem(c.currentMediaItemIndex + 1, song.toMediaItem())
            }
        }
    }

    fun addToQueue(song: Song) {
        withController { c ->
            c.addMediaItem(song.toMediaItem())
            if (c.playbackState == Player.STATE_IDLE) c.prepare()
        }
    }

    fun addToQueue(songs: List<Song>) {
        withController { c -> c.addMediaItems(songs.map { it.toMediaItem() }) }
    }

    fun removeFromQueue(index: Int) = withController { c ->
        if (index in 0 until c.mediaItemCount) c.removeMediaItem(index)
    }

    fun moveQueueItem(from: Int, to: Int) = withController { c ->
        if (from in 0 until c.mediaItemCount && to in 0 until c.mediaItemCount) c.moveMediaItem(from, to)
    }

    fun clearQueue() = withController { c ->
        c.stop()
        c.clearMediaItems()
    }

    fun skipToQueueItem(index: Int) = withController { c ->
        if (index in 0 until c.mediaItemCount) {
            c.seekTo(index, 0L)
            c.play()
        }
    }

    fun togglePlayPause() = withController { c ->
        if (c.isPlaying) {
            c.pause()
        } else {
            if (c.playbackState == Player.STATE_IDLE) c.prepare()
            if (c.playbackState == Player.STATE_ENDED) c.seekTo(0L)
            c.play()
        }
    }

    fun next() = withController { it.seekToNextMediaItem() }
    fun previous() = withController { it.seekToPrevious() }

    fun seekTo(positionMs: Long) = withController {
        it.seekTo(positionMs)
        _positionMs.value = positionMs
    }

    fun seekBy(deltaMs: Long) = withController { c ->
        val target = (c.currentPosition + deltaMs).coerceIn(0L, (c.duration.takeIf { it != C.TIME_UNSET } ?: Long.MAX_VALUE))
        c.seekTo(target)
        _positionMs.value = target
    }

    fun toggleShuffle() = withController { it.shuffleModeEnabled = !it.shuffleModeEnabled }

    fun cycleRepeat() = withController {
        it.repeatMode = when (it.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    // endregion

    private companion object {
        const val TICK_MS = 300L
    }
}
