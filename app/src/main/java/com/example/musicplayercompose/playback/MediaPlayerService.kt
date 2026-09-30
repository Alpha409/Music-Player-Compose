package com.example.musicplayercompose.playback

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.musicplayercompose.MainActivity
import com.example.musicplayercompose.domain.model.AppSettings
import com.example.musicplayercompose.domain.model.SavedPlaybackState
import com.example.musicplayercompose.domain.repository.MusicRepository
import com.example.musicplayercompose.domain.repository.PlaybackStateRepository
import com.example.musicplayercompose.domain.repository.SettingsRepository
import com.example.musicplayercompose.domain.usecase.RecordPlayUseCase
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Owns the ExoPlayer and the MediaSession. Runs as a foreground media service while playing,
 * which is what keeps audio alive with the screen off and drives the notification, lock screen,
 * Bluetooth and headset controls. It also records play history and persists the queue, so both
 * keep working when the UI process has no activity.
 */
@androidx.annotation.OptIn(UnstableApi::class)
@AndroidEntryPoint
class MediaPlayerService : MediaSessionService() {

    @Inject lateinit var music: MusicRepository
    @Inject lateinit var settingsRepo: SettingsRepository
    @Inject lateinit var stateRepo: PlaybackStateRepository
    @Inject lateinit var events: PlaybackEvents
    @Inject lateinit var recordPlay: RecordPlayUseCase

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var session: MediaSession? = null
    private lateinit var player: ExoPlayer

    private var settings = AppSettings()
    private var countedMediaId: String? = null
    private var saveJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(musicAttributes(), true)
            .setHandleAudioBecomingNoisy(true)
            .build()
        player.addListener(playerListener)

        val openApp = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        session = MediaSession.Builder(this, player)
            .setSessionActivity(openApp)
            .setCallback(sessionCallback)
            .build()

        scope.launch { settingsRepo.settings.collect { applySettings(it) } }
        scope.launch { restoreQueueIfEmpty() }
        scope.launch {
            while (isActive) {
                delay(PERIODIC_SAVE_MS)
                if (player.isPlaying) scheduleSave(immediate = true)
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Keep playing in the background; only shut down if there's nothing to keep alive.
        if (!player.playWhenReady || player.mediaItemCount == 0) stopSelf()
    }

    override fun onDestroy() {
        // Snapshot synchronously, persist on a scope that outlives this service.
        snapshot()?.let { state -> ioScope.launch { stateRepo.save(state) } }
        session?.run {
            player.removeListener(playerListener)
            player.release()
            release()
        }
        session = null
        scope.cancel()
        super.onDestroy()
    }

    private fun musicAttributes() = AudioAttributes.Builder()
        .setUsage(C.USAGE_MEDIA)
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .build()

    private fun applySettings(new: AppSettings) {
        settings = new
        player.setAudioAttributes(musicAttributes(), new.handleAudioFocus)
        player.setHandleAudioBecomingNoisy(new.pauseOnHeadsetDisconnect)
    }

    // region persistence

    private fun snapshot(): SavedPlaybackState? {
        if (player.mediaItemCount == 0) return null
        val ids = (0 until player.mediaItemCount).mapNotNull { player.getMediaItemAt(it).songId() }
        if (ids.isEmpty()) return null
        return SavedPlaybackState(
            queueIds = ids,
            index = player.currentMediaItemIndex.coerceIn(0, ids.lastIndex),
            positionMs = player.currentPosition.coerceAtLeast(0L),
            shuffle = player.shuffleModeEnabled,
            repeatMode = player.repeatMode
        )
    }

    private fun scheduleSave(immediate: Boolean = false) {
        saveJob?.cancel()
        saveJob = scope.launch {
            if (!immediate) delay(SAVE_DEBOUNCE_MS)
            snapshot()?.let { stateRepo.save(it) }
        }
    }

    private data class Restored(val items: List<MediaItem>, val index: Int, val positionMs: Long, val state: SavedPlaybackState)

    private suspend fun loadSaved(): Restored? {
        if (!settingsRepo.settings.first().resumePlayback) return null
        val state = stateRepo.load() ?: return null
        val songs = music.getSongsByIds(state.queueIds)
        if (songs.isEmpty()) return null
        // Songs deleted since last run are dropped, so re-find the current track by id.
        val currentId = state.queueIds.getOrNull(state.index)
        val idx = songs.indexOfFirst { it.id == currentId }
        return Restored(
            items = songs.map { it.toMediaItem() },
            index = idx.coerceAtLeast(0),
            positionMs = if (idx >= 0) state.positionMs else 0L,
            state = state
        )
    }

    private suspend fun restoreQueueIfEmpty() {
        val restored = loadSaved() ?: return
        // Read straight from the store: the collector in onCreate may not have delivered yet.
        val current = settingsRepo.settings.first()
        // The UI may already have started something while we were reading from disk.
        if (player.mediaItemCount > 0) return
        player.setMediaItems(restored.items, restored.index, restored.positionMs)
        if (current.rememberShuffleRepeat) {
            player.shuffleModeEnabled = restored.state.shuffle
            player.repeatMode = restored.state.repeatMode
        }
        player.prepare()
        if (current.autoPlayOnResume) player.play()
    }

    // endregion

    private val sessionCallback = object : MediaSession.Callback {
        /** Lets a Bluetooth/headset play button revive the last queue after the process died. */
        override fun onPlaybackResumption(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            val future = SettableFuture.create<MediaSession.MediaItemsWithStartPosition>()
            scope.launch {
                val restored = loadSaved()
                if (restored == null) {
                    future.setException(UnsupportedOperationException("Nothing to resume"))
                } else {
                    future.set(MediaSession.MediaItemsWithStartPosition(restored.items, restored.index, restored.positionMs))
                }
            }
            return future
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) countPlayIfNew() else scheduleSave()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            if (reason != Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED) countedMediaId = null
            if (player.isPlaying) countPlayIfNew()
            scheduleSave()
        }

        override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) = scheduleSave()
        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) = scheduleSave()
        override fun onRepeatModeChanged(repeatMode: Int) = scheduleSave()

        override fun onPlayerError(error: PlaybackException) {
            val name = player.currentMediaItem?.mediaMetadata?.title ?: "this track"
            if (player.hasNextMediaItem()) {
                events.emitError("Couldn't play \"$name\" — skipped to the next track")
                player.seekToNextMediaItem()
                player.prepare()
                player.play()
            } else {
                events.emitError("Couldn't play \"$name\"")
            }
        }
    }

    /** Counts one play per track start (not on every pause/resume). */
    private fun countPlayIfNew() {
        val item = player.currentMediaItem ?: return
        if (item.mediaId == countedMediaId) return
        countedMediaId = item.mediaId
        item.songId()?.let { id -> ioScope.launch { recordPlay(id) } }
    }

    private companion object {
        const val SAVE_DEBOUNCE_MS = 400L
        const val PERIODIC_SAVE_MS = 10_000L
    }
}
