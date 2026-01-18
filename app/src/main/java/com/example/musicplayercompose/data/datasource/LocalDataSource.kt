package com.example.musicplayercompose.data.datasource

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import com.example.musicplayercompose.domain.models.Mp3FilesDataClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LocalDataSource @Inject constructor(
    private val context: Context
) {
    fun getMp3Files(): Flow<List<Mp3FilesDataClass>> = flow {
        val mp3List = mutableListOf<Mp3FilesDataClass>()
        val resolver = context.contentResolver
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATE_ADDED
        )
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        withContext(Dispatchers.IO) {
            resolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val dateAddedColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val albumArtProjection = arrayOf(MediaStore.Audio.Albums.ALBUM_ART)
                var count = 0
                while (cursor.moveToNext() && count < 20) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn)
                    val artist = cursor.getString(artistColumn)
                    val album = cursor.getString(albumColumn)
                    val duration = cursor.getLong(durationColumn)
                    val path = cursor.getString(pathColumn)
                    val albumId = cursor.getLong(albumIdColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn)
                    var albumArt: Bitmap? = null
                    resolver.query(
                        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        albumArtProjection,
                        "${MediaStore.Audio.Albums._ID}=?",
                        arrayOf(albumId.toString()),
                        null
                    )?.use { albumCursor ->
                        if (albumCursor.moveToFirst()) {
                            val albumArtPath = albumCursor.getString(
                                albumCursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART)
                            )
                            albumArt = BitmapFactory.decodeFile(albumArtPath)
                        }
                    }
                    mp3List.add(
                        Mp3FilesDataClass(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            duration = duration,
                            path = path,
                            dateAdded = dateAdded
                        )
                    )
                    count++
                }
            }
        }
        emit(mp3List)
    }
}