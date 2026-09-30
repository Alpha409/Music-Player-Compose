package com.example.musicplayercompose.domain.model

data class Song(
    val id: Long,
    val title: String,
    val artist: String?,
    val album: String?,
    val albumId: Long,
    val albumArtist: String?,
    val genre: String?,
    val durationMs: Long,
    val uri: String,
    val artworkUri: String?,
    val trackNumber: Int?,
    val discNumber: Int?,
    val year: Int?,
    val dateAdded: Long,
    val dateModified: Long,
    val size: Long,
    val folder: String,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val lastPlayedAt: Long? = null
) {
    val displayTitle: String get() = title.ifBlank { "Untitled track" }
    val displayArtist: String get() = artist.cleanOrNull() ?: "Unknown artist"
    val displayAlbum: String get() = album.cleanOrNull() ?: "Unknown album"
}

data class Album(
    val id: Long,
    val title: String,
    val artist: String?,
    val artworkUri: String?,
    val year: Int?,
    val songCount: Int
) {
    val displayTitle: String get() = title.cleanOrNull() ?: "Unknown album"
    val displayArtist: String get() = artist.cleanOrNull() ?: "Unknown artist"
}

data class Artist(
    val name: String,
    val albumCount: Int,
    val songCount: Int,
    val artworkUri: String?
) {
    val displayName: String get() = name.cleanOrNull() ?: "Unknown artist"
}

data class Genre(val name: String, val songCount: Int)

data class Playlist(
    val id: Long,
    val name: String,
    val description: String?,
    val songCount: Int,
    val createdAt: Long,
    val updatedAt: Long
)

enum class SortOrder(val label: String) {
    TITLE("Title"),
    ARTIST("Artist"),
    ALBUM("Album"),
    DATE_ADDED("Recently added"),
    DURATION("Duration")
}

enum class ThemeMode(val label: String) { SYSTEM("System default"), LIGHT("Light"), DARK("Dark") }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val resumePlayback: Boolean = true,
    val autoPlayOnResume: Boolean = false,
    val rememberShuffleRepeat: Boolean = true,
    val skipDurationSec: Int = 10,
    val sortOrder: SortOrder = SortOrder.TITLE,
    val excludedFolders: Set<String> = emptySet(),
    val pauseOnHeadsetDisconnect: Boolean = true,
    val handleAudioFocus: Boolean = true
)

data class SavedPlaybackState(
    val queueIds: List<Long>,
    val index: Int,
    val positionMs: Long,
    val shuffle: Boolean,
    val repeatMode: Int
)

data class SearchResults(
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList()
) {
    val isEmpty: Boolean get() = songs.isEmpty() && albums.isEmpty() && artists.isEmpty()
}

private fun String?.cleanOrNull(): String? =
    this?.takeUnless { it.isBlank() || it.equals("<unknown>", ignoreCase = true) }
