package com.example.musicplayercompose.data.media

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import com.example.musicplayercompose.data.local.entity.SongEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val resolver: ContentResolver get() = context.contentResolver

    private val collection: Uri
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

    /** Emits whenever the audio collection changes (new, removed or edited files). */
    fun observeChanges(): Flow<Unit> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }
        }
        resolver.registerContentObserver(collection, true, observer)
        awaitClose { resolver.unregisterContentObserver(observer) }
    }

    /** Reads every music file from MediaStore. Runs on the IO dispatcher. */
    suspend fun scan(): List<SongEntity> = withContext(Dispatchers.IO) {
        val genres = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) legacyGenres() else emptyMap()
        val projection = buildList {
            add(MediaStore.Audio.Media._ID)
            add(MediaStore.Audio.Media.TITLE)
            add(MediaStore.Audio.Media.ARTIST)
            add(MediaStore.Audio.Media.ALBUM)
            add(MediaStore.Audio.Media.ALBUM_ID)
            add(MediaStore.Audio.Media.DURATION)
            add(MediaStore.Audio.Media.TRACK)
            add(MediaStore.Audio.Media.YEAR)
            add(MediaStore.Audio.Media.DATE_ADDED)
            add(MediaStore.Audio.Media.DATE_MODIFIED)
            add(MediaStore.Audio.Media.SIZE)
            @Suppress("DEPRECATION") add(MediaStore.Audio.Media.DATA)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                add(MediaStore.Audio.Media.ALBUM_ARTIST)
                add(MediaStore.Audio.Media.GENRE)
            }
        }.toTypedArray()

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 5000"
        val result = ArrayList<SongEntity>()
        resolver.query(collection, projection, selection, null, null)?.use { c ->
            val id = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val title = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artist = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val album = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumId = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val duration = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val track = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val year = c.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val added = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val modified = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
            val size = c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            @Suppress("DEPRECATION") val data = c.getColumnIndex(MediaStore.Audio.Media.DATA)
            val albumArtist = c.getColumnIndex(MediaStore.Audio.Media.ALBUM_ARTIST)
            val genre = c.getColumnIndex(MediaStore.Audio.Media.GENRE)

            while (c.moveToNext()) {
                val songId = c.getLong(id)
                val aId = c.getLong(albumId)
                // TRACK encodes disc as thousands: 2005 = disc 2, track 5.
                val rawTrack = c.getInt(track)
                val trackNo = (rawTrack % 1000).takeIf { it > 0 }
                val discNo = (rawTrack / 1000).takeIf { it > 0 }
                val path = if (data >= 0) c.getString(data).orEmpty() else ""
                result += SongEntity(
                    id = songId,
                    title = c.getString(title).orEmpty(),
                    artist = c.getString(artist),
                    album = c.getString(album),
                    albumId = aId,
                    albumArtist = if (albumArtist >= 0) c.getString(albumArtist) else null,
                    genre = (if (genre >= 0) c.getString(genre) else genres[songId])?.takeIf { it.isNotBlank() },
                    durationMs = c.getLong(duration),
                    uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId).toString(),
                    artworkUri = ContentUris.withAppendedId(ALBUM_ART_URI, aId).toString(),
                    trackNumber = trackNo,
                    discNumber = discNo,
                    year = c.getInt(year).takeIf { it > 0 },
                    dateAdded = c.getLong(added),
                    dateModified = c.getLong(modified),
                    size = c.getLong(size),
                    folder = path.substringBeforeLast('/', "")
                )
            }
        }
        result
    }

    /** API < 30 has no GENRE column on Media, so join through the Genres table. */
    @Suppress("DEPRECATION")
    private fun legacyGenres(): Map<Long, String> {
        val map = HashMap<Long, String>()
        runCatching {
            resolver.query(
                MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME),
                null, null, null
            )?.use { g ->
                while (g.moveToNext()) {
                    val genreId = g.getLong(0)
                    val name = g.getString(1) ?: continue
                    resolver.query(
                        MediaStore.Audio.Genres.Members.getContentUri("external", genreId),
                        arrayOf(MediaStore.Audio.Genres.Members.AUDIO_ID), null, null, null
                    )?.use { m -> while (m.moveToNext()) map[m.getLong(0)] = name }
                }
            }
        }
        return map
    }

    private companion object {
        val ALBUM_ART_URI: Uri = Uri.parse("content://media/external/audio/albumart")
    }
}
