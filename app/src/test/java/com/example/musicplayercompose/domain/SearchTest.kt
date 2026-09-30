package com.example.musicplayercompose.domain

import com.example.musicplayercompose.data.repository.likePattern
import com.example.musicplayercompose.domain.model.Album
import com.example.musicplayercompose.domain.model.Artist
import com.example.musicplayercompose.domain.model.Genre
import com.example.musicplayercompose.domain.model.SearchResults
import com.example.musicplayercompose.domain.model.Song
import com.example.musicplayercompose.domain.model.SortOrder
import com.example.musicplayercompose.domain.repository.MusicRepository
import com.example.musicplayercompose.domain.usecase.SearchLibraryUseCase
import com.example.musicplayercompose.domain.usecase.sanitizeSearchQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchTest {
    @Test fun `sanitize trims and collapses whitespace`() {
        assertEquals("the beatles", sanitizeSearchQuery("  the    beatles \n"))
    }

    @Test fun `like pattern escapes wildcards so they match literally`() {
        assertEquals("%100\\%%", likePattern("100%"))
        assertEquals("%a\\_b%", likePattern("a_b"))
        assertEquals("%a\\\\b%", likePattern("a\\b"))
    }

    @Test fun `blank query never reaches the repository`() = runBlocking {
        val repo = RecordingRepository()
        val results = SearchLibraryUseCase(repo)("   ")
        assertTrue(results.isEmpty)
        assertTrue(repo.queries.isEmpty())
    }

    @Test fun `query is sanitized before searching`() = runBlocking {
        val repo = RecordingRepository()
        SearchLibraryUseCase(repo)("  queen   ")
        assertEquals(listOf("queen"), repo.queries)
    }

    private class RecordingRepository : MusicRepository {
        val queries = mutableListOf<String>()
        override suspend fun search(query: String): SearchResults {
            queries += query
            return SearchResults()
        }

        override fun observeSongs(sort: SortOrder): Flow<List<Song>> = TODO()
        override fun observeSong(id: Long): Flow<Song?> = TODO()
        override fun observeSongCount(): Flow<Int> = TODO()
        override fun observeFavorites(): Flow<List<Song>> = TODO()
        override fun observeRecentlyPlayed(limit: Int): Flow<List<Song>> = TODO()
        override fun observeRecentlyAdded(limit: Int): Flow<List<Song>> = TODO()
        override fun observeMostPlayed(limit: Int): Flow<List<Song>> = TODO()
        override fun observeAlbums(): Flow<List<Album>> = TODO()
        override fun observeArtists(): Flow<List<Artist>> = TODO()
        override fun observeGenres(): Flow<List<Genre>> = TODO()
        override fun observeAlbum(id: Long): Flow<Album?> = TODO()
        override fun observeAlbumSongs(albumId: Long): Flow<List<Song>> = TODO()
        override fun observeArtistAlbums(artist: String): Flow<List<Album>> = TODO()
        override fun observeArtistSongs(artist: String): Flow<List<Song>> = TODO()
        override fun observeGenreSongs(genre: String): Flow<List<Song>> = TODO()
        override suspend fun getSongsByIds(ids: List<Long>): List<Song> = TODO()
        override suspend fun toggleFavorite(songId: Long) = TODO()
        override suspend fun recordPlay(songId: Long) = TODO()
        override suspend fun syncLibrary(excludedFolders: Set<String>): Boolean = TODO()
        override fun observeMediaStoreChanges(): Flow<Unit> = TODO()
        override suspend fun libraryFolders(): List<String> = TODO()
    }
}
