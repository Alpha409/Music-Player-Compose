package com.example.musicplayercompose.playback

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.example.musicplayercompose.domain.model.Song
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

fun Song.toMediaItem(): MediaItem = MediaItem.Builder()
    .setMediaId(id.toString())
    .setUri(uri)
    .setMediaMetadata(
        MediaMetadata.Builder()
            .setTitle(displayTitle)
            .setArtist(displayArtist)
            .setAlbumTitle(displayAlbum)
            .setArtworkUri(artworkUri?.let(Uri::parse))
            .setIsBrowsable(false)
            .setIsPlayable(true)
            .build()
    )
    .build()

fun MediaItem.songId(): Long? = mediaId.toLongOrNull()

data class QueueItem(
    val index: Int,
    val songId: Long,
    val title: String,
    val artist: String,
    val artworkUri: String?
)

/** One-way channel from the playback service to the UI for user-visible problems. */
@Singleton
class PlaybackEvents @Inject constructor() {
    private val _errors = MutableSharedFlow<String>(extraBufferCapacity = 8, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val errors: SharedFlow<String> = _errors.asSharedFlow()
    fun emitError(message: String) {
        _errors.tryEmit(message)
    }
}
