package com.example.musicplayercompose.domain.repository

import com.example.musicplayercompose.domain.model.Album
import com.example.musicplayercompose.domain.model.AppSettings
import com.example.musicplayercompose.domain.model.Artist
import com.example.musicplayercompose.domain.model.Genre
import com.example.musicplayercompose.domain.model.Playlist
import com.example.musicplayercompose.domain.model.SavedPlaybackState
import com.example.musicplayercompose.domain.model.SearchResults
import com.example.musicplayercompose.domain.model.Song
import com.example.musicplayercompose.domain.model.SortOrder
import com.example.musicplayercompose.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    fun observeSongs(sort: SortOrder): Flow<List<Song>>
    fun observeSong(id: Long): Flow<Song?>
    fun observeSongCount(): Flow<Int>
    fun observeFavorites(): Flow<List<Song>>
    fun observeRecentlyPlayed(limit: Int): Flow<List<Song>>
    fun observeRecentlyAdded(limit: Int): Flow<List<Song>>
    fun observeMostPlayed(limit: Int): Flow<List<Song>>
    fun observeAlbums(): Flow<List<Album>>
    fun observeArtists(): Flow<List<Artist>>
    fun observeGenres(): Flow<List<Genre>>
    fun observeAlbum(id: Long): Flow<Album?>
    fun observeAlbumSongs(albumId: Long): Flow<List<Song>>
    fun observeArtistAlbums(artist: String): Flow<List<Album>>
    fun observeArtistSongs(artist: String): Flow<List<Song>>
    fun observeGenreSongs(genre: String): Flow<List<Song>>
    suspend fun search(query: String): SearchResults
    suspend fun getSongsByIds(ids: List<Long>): List<Song>
    suspend fun toggleFavorite(songId: Long)
    suspend fun recordPlay(songId: Long)

    /** Synchronises Room with MediaStore. Returns false when the scan itself failed. */
    suspend fun syncLibrary(excludedFolders: Set<String>): Boolean
    fun observeMediaStoreChanges(): Flow<Unit>
    suspend fun libraryFolders(): List<String>
}

interface PlaylistRepository {
    fun observePlaylists(): Flow<List<Playlist>>
    fun observePlaylist(id: Long): Flow<Playlist?>
    fun observePlaylistSongs(id: Long): Flow<List<Song>>
    suspend fun createPlaylist(name: String, description: String? = null): Long
    suspend fun renamePlaylist(id: Long, name: String)
    suspend fun deletePlaylist(id: Long)
    suspend fun addSongs(playlistId: Long, songIds: List<Long>): Int
    suspend fun removeSong(playlistId: Long, songId: Long)
    suspend fun reorder(playlistId: Long, orderedSongIds: List<Long>)
}

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setDynamicColor(enabled: Boolean)
    suspend fun setResumePlayback(enabled: Boolean)
    suspend fun setAutoPlayOnResume(enabled: Boolean)
    suspend fun setRememberShuffleRepeat(enabled: Boolean)
    suspend fun setSkipDuration(seconds: Int)
    suspend fun setSortOrder(order: SortOrder)
    suspend fun setExcludedFolders(folders: Set<String>)
    suspend fun setPauseOnHeadsetDisconnect(enabled: Boolean)
    suspend fun setHandleAudioFocus(enabled: Boolean)
}

interface PlaybackStateRepository {
    suspend fun save(state: SavedPlaybackState)
    suspend fun load(): SavedPlaybackState?
}
