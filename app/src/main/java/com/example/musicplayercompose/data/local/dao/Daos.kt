package com.example.musicplayercompose.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.musicplayercompose.data.local.entity.PlaylistEntity
import com.example.musicplayercompose.data.local.entity.PlaylistSongEntity
import com.example.musicplayercompose.data.local.entity.SongEntity
import com.example.musicplayercompose.data.local.entity.SongStatsEntity
import kotlinx.coroutines.flow.Flow

data class SongRow(
    @Embedded val song: SongEntity,
    val isFavorite: Boolean?,
    val playCount: Int?,
    val lastPlayedAt: Long?
)

data class AlbumRow(
    val id: Long,
    val title: String?,
    val artist: String?,
    val artworkUri: String?,
    val year: Int?,
    val songCount: Int
)

data class ArtistRow(
    val name: String?,
    val albumCount: Int,
    val songCount: Int,
    val artworkUri: String?
)

data class GenreRow(val name: String, val songCount: Int)

data class PlaylistRow(
    val id: Long,
    val name: String,
    val description: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val songCount: Int
)

data class SongStamp(val id: Long, val dateModified: Long)

private const val SONG_SELECT =
    "SELECT songs.*, st.isFavorite AS isFavorite, st.playCount AS playCount, st.lastPlayedAt AS lastPlayedAt " +
        "FROM songs LEFT JOIN song_stats st ON st.songId = songs.id "

private const val ESCAPE = " ESCAPE '\\' "

@Dao
interface SongDao {
    // sort: 0 title, 1 artist, 2 album, 3 date added (newest first), 4 duration (longest first)
    @Query(
        SONG_SELECT + "ORDER BY " +
            "CASE WHEN :sort = 0 THEN songs.title END COLLATE NOCASE ASC, " +
            "CASE WHEN :sort = 1 THEN songs.artist END COLLATE NOCASE ASC, " +
            "CASE WHEN :sort = 2 THEN songs.album END COLLATE NOCASE ASC, " +
            "CASE WHEN :sort = 3 THEN songs.dateAdded END DESC, " +
            "CASE WHEN :sort = 4 THEN songs.durationMs END DESC, " +
            "songs.title COLLATE NOCASE ASC"
    )
    fun observeAll(sort: Int): Flow<List<SongRow>>

    @Query(SONG_SELECT + "WHERE songs.id = :id")
    fun observeById(id: Long): Flow<SongRow?>

    @Query("SELECT COUNT(*) FROM songs")
    fun observeCount(): Flow<Int>

    @Query(SONG_SELECT + "WHERE st.isFavorite = 1 ORDER BY songs.title COLLATE NOCASE ASC")
    fun observeFavorites(): Flow<List<SongRow>>

    @Query(SONG_SELECT + "WHERE st.lastPlayedAt IS NOT NULL ORDER BY st.lastPlayedAt DESC LIMIT :limit")
    fun observeRecentlyPlayed(limit: Int): Flow<List<SongRow>>

    @Query(SONG_SELECT + "ORDER BY songs.dateAdded DESC LIMIT :limit")
    fun observeRecentlyAdded(limit: Int): Flow<List<SongRow>>

    @Query(SONG_SELECT + "WHERE st.playCount > 0 ORDER BY st.playCount DESC, st.lastPlayedAt DESC LIMIT :limit")
    fun observeMostPlayed(limit: Int): Flow<List<SongRow>>

    @Query(
        SONG_SELECT + "WHERE songs.albumId = :albumId " +
            "ORDER BY COALESCE(songs.discNumber, 1), COALESCE(songs.trackNumber, 0), songs.title COLLATE NOCASE"
    )
    fun observeByAlbum(albumId: Long): Flow<List<SongRow>>

    @Query(SONG_SELECT + "WHERE songs.artist = :artist ORDER BY songs.album COLLATE NOCASE, COALESCE(songs.trackNumber, 0)")
    fun observeByArtist(artist: String): Flow<List<SongRow>>

    @Query(SONG_SELECT + "WHERE songs.genre = :genre ORDER BY songs.title COLLATE NOCASE")
    fun observeByGenre(genre: String): Flow<List<SongRow>>

    @Query(
        SONG_SELECT + "WHERE songs.title LIKE :q $ESCAPE OR songs.artist LIKE :q $ESCAPE " +
            "OR songs.album LIKE :q $ESCAPE OR songs.genre LIKE :q $ESCAPE " +
            "ORDER BY songs.title COLLATE NOCASE LIMIT 200"
    )
    suspend fun search(q: String): List<SongRow>

    @Query(SONG_SELECT + "WHERE songs.id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<SongRow>

    @Query(
        "SELECT albumId AS id, album AS title, COALESCE(MAX(albumArtist), MAX(artist)) AS artist, " +
            "MIN(artworkUri) AS artworkUri, MAX(year) AS year, COUNT(*) AS songCount " +
            "FROM songs GROUP BY albumId ORDER BY album COLLATE NOCASE"
    )
    fun observeAlbums(): Flow<List<AlbumRow>>

    @Query(
        "SELECT albumId AS id, album AS title, COALESCE(MAX(albumArtist), MAX(artist)) AS artist, " +
            "MIN(artworkUri) AS artworkUri, MAX(year) AS year, COUNT(*) AS songCount " +
            "FROM songs WHERE albumId = :id GROUP BY albumId"
    )
    fun observeAlbum(id: Long): Flow<AlbumRow?>

    @Query(
        "SELECT albumId AS id, album AS title, COALESCE(MAX(albumArtist), MAX(artist)) AS artist, " +
            "MIN(artworkUri) AS artworkUri, MAX(year) AS year, COUNT(*) AS songCount " +
            "FROM songs WHERE artist = :artist GROUP BY albumId ORDER BY year DESC, album COLLATE NOCASE"
    )
    fun observeArtistAlbums(artist: String): Flow<List<AlbumRow>>

    @Query(
        "SELECT albumId AS id, album AS title, COALESCE(MAX(albumArtist), MAX(artist)) AS artist, " +
            "MIN(artworkUri) AS artworkUri, MAX(year) AS year, COUNT(*) AS songCount " +
            "FROM songs WHERE album LIKE :q $ESCAPE GROUP BY albumId ORDER BY album COLLATE NOCASE LIMIT 50"
    )
    suspend fun searchAlbums(q: String): List<AlbumRow>

    @Query(
        "SELECT artist AS name, COUNT(DISTINCT albumId) AS albumCount, COUNT(*) AS songCount, " +
            "MIN(artworkUri) AS artworkUri FROM songs GROUP BY artist ORDER BY artist COLLATE NOCASE"
    )
    fun observeArtists(): Flow<List<ArtistRow>>

    @Query(
        "SELECT artist AS name, COUNT(DISTINCT albumId) AS albumCount, COUNT(*) AS songCount, " +
            "MIN(artworkUri) AS artworkUri FROM songs WHERE artist LIKE :q $ESCAPE " +
            "GROUP BY artist ORDER BY artist COLLATE NOCASE LIMIT 50"
    )
    suspend fun searchArtists(q: String): List<ArtistRow>

    @Query(
        "SELECT genre AS name, COUNT(*) AS songCount FROM songs WHERE genre IS NOT NULL AND genre != '' " +
            "GROUP BY genre ORDER BY genre COLLATE NOCASE"
    )
    fun observeGenres(): Flow<List<GenreRow>>

    @Query("SELECT DISTINCT folder FROM songs WHERE folder != '' ORDER BY folder")
    suspend fun folders(): List<String>

    @Query("SELECT id, dateModified FROM songs")
    suspend fun stamps(): List<SongStamp>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(songs: List<SongEntity>)

    @Query("DELETE FROM songs WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM playlist_songs WHERE songId NOT IN (SELECT id FROM songs)")
    suspend fun deleteOrphanPlaylistEntries()
}

@Dao
interface StatsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun ensure(stats: SongStatsEntity): Long

    @Query("UPDATE song_stats SET isFavorite = 1 - isFavorite WHERE songId = :songId")
    suspend fun flipFavorite(songId: Long)

    @Query("UPDATE song_stats SET playCount = playCount + 1, lastPlayedAt = :now WHERE songId = :songId")
    suspend fun bumpPlay(songId: Long, now: Long)

    @Query("UPDATE song_stats SET isFavorite = 1 WHERE songId = :songId")
    suspend fun markFavorite(songId: Long)

    @Transaction
    suspend fun toggleFavorite(songId: Long) {
        ensure(SongStatsEntity(songId))
        flipFavorite(songId)
    }

    @Transaction
    suspend fun recordPlay(songId: Long, now: Long) {
        ensure(SongStatsEntity(songId))
        bumpPlay(songId, now)
    }

    @Transaction
    suspend fun favorite(songId: Long) {
        ensure(SongStatsEntity(songId))
        markFavorite(songId)
    }
}

@Dao
interface PlaylistDao {
    @Query(
        "SELECT p.id, p.name, p.description, p.createdAt, p.updatedAt, " +
            "(SELECT COUNT(*) FROM playlist_songs ps INNER JOIN songs s ON s.id = ps.songId WHERE ps.playlistId = p.id) AS songCount " +
            "FROM playlists p ORDER BY p.updatedAt DESC"
    )
    fun observePlaylists(): Flow<List<PlaylistRow>>

    @Query(
        "SELECT p.id, p.name, p.description, p.createdAt, p.updatedAt, " +
            "(SELECT COUNT(*) FROM playlist_songs ps INNER JOIN songs s ON s.id = ps.songId WHERE ps.playlistId = p.id) AS songCount " +
            "FROM playlists p WHERE p.id = :id"
    )
    fun observePlaylist(id: Long): Flow<PlaylistRow?>

    @Query(
        "SELECT songs.*, st.isFavorite AS isFavorite, st.playCount AS playCount, st.lastPlayedAt AS lastPlayedAt " +
            "FROM playlist_songs ps INNER JOIN songs ON songs.id = ps.songId " +
            "LEFT JOIN song_stats st ON st.songId = songs.id WHERE ps.playlistId = :id ORDER BY ps.position"
    )
    fun observeSongs(id: Long): Flow<List<SongRow>>

    @Insert
    suspend fun insert(playlist: PlaylistEntity): Long

    @Query("UPDATE playlists SET name = :name, updatedAt = :now WHERE id = :id")
    suspend fun rename(id: Long, name: String, now: Long)

    @Query("UPDATE playlists SET updatedAt = :now WHERE id = :id")
    suspend fun touch(id: Long, now: Long)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT songId FROM playlist_songs WHERE playlistId = :id")
    suspend fun songIds(id: Long): List<Long>

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM playlist_songs WHERE playlistId = :id")
    suspend fun nextPosition(id: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEntries(entries: List<PlaylistSongEntity>)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :id AND songId = :songId")
    suspend fun removeEntry(id: Long, songId: Long)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :id")
    suspend fun clearEntries(id: Long)

    @Transaction
    suspend fun addSongs(id: Long, songIds: List<Long>, now: Long): Int {
        val existing = songIds(id).toSet()
        val fresh = songIds.distinct().filterNot { it in existing }
        val start = nextPosition(id)
        insertEntries(fresh.mapIndexed { i, songId -> PlaylistSongEntity(id, songId, start + i) })
        if (fresh.isNotEmpty()) touch(id, now)
        return fresh.size
    }

    @Transaction
    suspend fun removeSong(id: Long, songId: Long, now: Long) {
        removeEntry(id, songId)
        // Keep positions dense so later appends and reorders stay consistent.
        val remaining = songIds(id)
        if (remaining.isNotEmpty()) replaceOrder(id, remaining, now)
    }

    @Transaction
    suspend fun replaceOrder(id: Long, ordered: List<Long>, now: Long) {
        clearEntries(id)
        insertEntries(ordered.mapIndexed { i, songId -> PlaylistSongEntity(id, songId, i) })
        touch(id, now)
    }
}
