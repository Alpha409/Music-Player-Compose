package com.example.musicplayercompose.data.repository

import com.example.musicplayercompose.data.local.dao.AlbumRow
import com.example.musicplayercompose.data.local.dao.ArtistRow
import com.example.musicplayercompose.data.local.dao.PlaylistRow
import com.example.musicplayercompose.data.local.dao.SongRow
import com.example.musicplayercompose.domain.model.Album
import com.example.musicplayercompose.domain.model.Artist
import com.example.musicplayercompose.domain.model.Playlist
import com.example.musicplayercompose.domain.model.Song

fun SongRow.toDomain() = Song(
    id = song.id,
    title = song.title,
    artist = song.artist,
    album = song.album,
    albumId = song.albumId,
    albumArtist = song.albumArtist,
    genre = song.genre,
    durationMs = song.durationMs,
    uri = song.uri,
    artworkUri = song.artworkUri,
    trackNumber = song.trackNumber,
    discNumber = song.discNumber,
    year = song.year,
    dateAdded = song.dateAdded,
    dateModified = song.dateModified,
    size = song.size,
    folder = song.folder,
    isFavorite = isFavorite == true,
    playCount = playCount ?: 0,
    lastPlayedAt = lastPlayedAt
)

fun AlbumRow.toDomain() = Album(id, title.orEmpty(), artist, artworkUri, year, songCount)

fun ArtistRow.toDomain() = Artist(name.orEmpty(), albumCount, songCount, artworkUri)

fun PlaylistRow.toDomain() = Playlist(id, name, description, songCount, createdAt, updatedAt)

/** Escapes LIKE wildcards so user input is matched literally (paired with `ESCAPE '\'`). */
fun likePattern(query: String): String =
    "%" + query.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%"
