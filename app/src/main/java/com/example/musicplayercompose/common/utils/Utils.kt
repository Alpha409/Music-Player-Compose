package com.example.musicplayercompose.common.utils

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicplayercompose.domain.models.Mp3FilesDataClass

object Utils {
    private var exoPlayer: ExoPlayer? = null
    var currentSong: Mp3FilesDataClass? = null
    /**
     * Initialize ExoPlayer if not already created.
     * Should be called once — usually in Application or first Activity.
     */
    fun initPlayer(context: Context): ExoPlayer {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context.applicationContext).build()
        }
        return exoPlayer!!
    }
    /**
     * Get the existing ExoPlayer instance safely.
     */
    fun getPlayer(): ExoPlayer? = exoPlayer
    /**
     * Prepare and play a given media source.
     */
    fun playMedia(context: Context, uri: Uri) {
        val player = initPlayer(context)
        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }
    /**
     * Pause playback if player is active.
     */
    fun pausePlayer() {
        exoPlayer?.pause()
    }
    /**
     * Stop and release the player completely.
     * Call this when the app is closing or music service stops.
     */
    fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
    }
}