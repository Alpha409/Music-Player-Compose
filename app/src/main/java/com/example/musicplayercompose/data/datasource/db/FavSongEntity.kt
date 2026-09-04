package com.example.musicplayercompose.data.datasource.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.musicplayercompose.domain.models.Mp3FilesDataClass

@Entity(tableName = "fav_songs")
data class FavSongEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String?,
    val album: String?,
    val duration: Long,
    val path: String,
    val contentUriString: String = "",
    val albumArtUriString: String? = null,
    val isFav: Boolean = true,
    val dateAdded: Long? = null
) {
    fun toDomain(): Mp3FilesDataClass {
        return Mp3FilesDataClass(
            id = id,
            title = title,
            artist = artist,
            album = album,
            duration = duration,
            path = path,
            contentUriString = contentUriString,
            albumArtUriString = albumArtUriString,
            isFav = isFav,
            dateAdded = dateAdded
        )
    }

    companion object {
        fun fromDomain(model: Mp3FilesDataClass): FavSongEntity {
            return FavSongEntity(
                id = model.id,
                title = model.title,
                artist = model.artist,
                album = model.album,
                duration = model.duration,
                path = model.path,
                contentUriString = model.contentUriString,
                albumArtUriString = model.albumArtUriString,
                isFav = true,
                dateAdded = model.dateAdded
            )
        }
    }
}
