package com.example.musicplayercompose.domain.models

/*
import androidx.room.Entity
import androidx.room.PrimaryKey

*/

//@Entity(tableName = "fav_songs")
data class Mp3FilesDataClass(
//    @PrimaryKey
    val id: Long,
    val title: String,
    val artist: String?,
    val album: String?,
    val duration: Long,
    val path: String,
    var isFav: Boolean = false,
    val dateAdded: Long? = null
)