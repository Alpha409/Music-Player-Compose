package com.example.musicplayercompose.domain.models

import java.util.Locale

data class Mp3FilesDataClass(
    val id: Long,
    val title: String,
    val artist: String?,
    val album: String?,
    val duration: Long,
    val path: String,
    val contentUriString: String = "",
    val albumArtUriString: String? = null,
    var isFav: Boolean = false,
    val dateAdded: Long? = null
) {
    val displayArtist: String
        get() = if (artist.isNullOrBlank() || artist.equals("<unknown>", ignoreCase = true)) {
            "Unknown Artist"
        } else {
            artist
        }

    val displayTitle: String
        get() = if (title.isBlank()) "Untitled Track" else title

    val formattedDuration: String
        get() {
            if (duration <= 0) return "00:00"
            val totalSeconds = duration / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
}