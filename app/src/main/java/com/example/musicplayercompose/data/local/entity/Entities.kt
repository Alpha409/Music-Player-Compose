package com.example.musicplayercompose.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Media metadata owned by the MediaStore sync. Replaced freely on rescan. */
@Entity(
    tableName = "songs",
    indices = [Index("albumId"), Index("artist"), Index("genre"), Index("dateAdded")]
)
data class SongEntity(
    @PrimaryKey val id: Long,
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
    val folder: String
)

/** User-owned data. Kept separate so a rescan never wipes favourites or history. */
@Entity(tableName = "song_stats")
data class SongStatsEntity(
    @PrimaryKey val songId: Long,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val lastPlayedAt: Long? = null
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"],
    foreignKeys = [ForeignKey(
        entity = PlaylistEntity::class,
        parentColumns = ["id"],
        childColumns = ["playlistId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("songId")]
)
data class PlaylistSongEntity(
    val playlistId: Long,
    val songId: Long,
    val position: Int
)
