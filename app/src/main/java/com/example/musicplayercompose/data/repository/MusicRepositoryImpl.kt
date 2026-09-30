package com.example.musicplayercompose.data.repository

import com.example.musicplayercompose.data.local.dao.SongDao
import com.example.musicplayercompose.data.local.dao.StatsDao
import com.example.musicplayercompose.data.media.LegacyFavoritesMigrator
import com.example.musicplayercompose.data.media.MediaStoreDataSource
import com.example.musicplayercompose.domain.model.Album
import com.example.musicplayercompose.domain.model.Artist
import com.example.musicplayercompose.domain.model.Genre
import com.example.musicplayercompose.domain.model.SearchResults
import com.example.musicplayercompose.domain.model.Song
import com.example.musicplayercompose.domain.model.SortOrder
import com.example.musicplayercompose.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val songs: SongDao,
    private val stats: StatsDao,
    private val mediaStore: MediaStoreDataSource,
    private val legacyFavorites: LegacyFavoritesMigrator
) : MusicRepository {

    private val syncMutex = Mutex()

    override fun observeSongs(sort: SortOrder): Flow<List<Song>> =
        songs.observeAll(sort.ordinal).map { rows -> rows.map { it.toDomain() } }

    override fun observeSong(id: Long) = songs.observeById(id).map { it?.toDomain() }
    override fun observeSongCount() = songs.observeCount()
    override fun observeFavorites() = songs.observeFavorites().mapSongs()
    override fun observeRecentlyPlayed(limit: Int) = songs.observeRecentlyPlayed(limit).mapSongs()
    override fun observeRecentlyAdded(limit: Int) = songs.observeRecentlyAdded(limit).mapSongs()
    override fun observeMostPlayed(limit: Int) = songs.observeMostPlayed(limit).mapSongs()
    override fun observeAlbums(): Flow<List<Album>> = songs.observeAlbums().map { r -> r.map { it.toDomain() } }
    override fun observeArtists(): Flow<List<Artist>> = songs.observeArtists().map { r -> r.map { it.toDomain() } }
    override fun observeGenres(): Flow<List<Genre>> =
        songs.observeGenres().map { r -> r.map { Genre(it.name, it.songCount) } }

    override fun observeAlbum(id: Long) = songs.observeAlbum(id).map { it?.toDomain() }
    override fun observeAlbumSongs(albumId: Long) = songs.observeByAlbum(albumId).mapSongs()
    override fun observeArtistAlbums(artist: String) =
        songs.observeArtistAlbums(artist).map { r -> r.map { it.toDomain() } }

    override fun observeArtistSongs(artist: String) = songs.observeByArtist(artist).mapSongs()
    override fun observeGenreSongs(genre: String) = songs.observeByGenre(genre).mapSongs()

    override suspend fun search(query: String): SearchResults {
        val pattern = likePattern(query)
        return SearchResults(
            songs = songs.search(pattern).map { it.toDomain() },
            albums = songs.searchAlbums(pattern).map { it.toDomain() },
            artists = songs.searchArtists(pattern).map { it.toDomain() }
        )
    }

    override suspend fun getSongsByIds(ids: List<Long>): List<Song> {
        if (ids.isEmpty()) return emptyList()
        // SQLite caps bound variables (999 on old Android), so chunk the lookup.
        val byId = ids.distinct().chunked(500).flatMap { songs.getByIds(it) }.associate { it.song.id to it.toDomain() }
        return ids.mapNotNull { byId[it] }
    }

    override suspend fun toggleFavorite(songId: Long) = stats.toggleFavorite(songId)

    override suspend fun recordPlay(songId: Long) = stats.recordPlay(songId, System.currentTimeMillis())

    override suspend fun syncLibrary(excludedFolders: Set<String>): Boolean = syncMutex.withLock {
        legacyFavorites.migrateIfNeeded()
        val scanned = try {
            mediaStore.scan()
        } catch (e: Exception) {
            return@withLock false
        }.filterNot { song -> excludedFolders.any { song.folder == it || song.folder.startsWith("$it/") } }

        val known = songs.stamps().associate { it.id to it.dateModified }
        val scannedIds = scanned.mapTo(HashSet()) { it.id }

        val removed = known.keys.filterNot { it in scannedIds }
        removed.chunked(500).forEach { songs.deleteByIds(it) }

        // Only rewrite rows that are new or whose file changed.
        val changed = scanned.filter { known[it.id] != it.dateModified }
        changed.chunked(500).forEach { songs.upsertAll(it) }

        if (removed.isNotEmpty()) songs.deleteOrphanPlaylistEntries()
        true
    }

    override fun observeMediaStoreChanges() = mediaStore.observeChanges()

    override suspend fun libraryFolders() = songs.folders()

    private fun Flow<List<com.example.musicplayercompose.data.local.dao.SongRow>>.mapSongs() =
        map { rows -> rows.map { it.toDomain() } }
}
