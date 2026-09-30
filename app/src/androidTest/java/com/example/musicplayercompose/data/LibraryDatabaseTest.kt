package com.example.musicplayercompose.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.musicplayercompose.data.local.dao.PlaylistDao
import com.example.musicplayercompose.data.local.dao.SongDao
import com.example.musicplayercompose.data.local.dao.StatsDao
import com.example.musicplayercompose.data.local.database.AppDatabase
import com.example.musicplayercompose.data.local.entity.PlaylistEntity
import com.example.musicplayercompose.data.local.entity.SongEntity
import com.example.musicplayercompose.data.repository.likePattern
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibraryDatabaseTest {
    private lateinit var db: AppDatabase
    private lateinit var songs: SongDao
    private lateinit var stats: StatsDao
    private lateinit var playlists: PlaylistDao

    @Before fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), AppDatabase::class.java)
            .allowMainThreadQueries().build()
        songs = db.songDao()
        stats = db.statsDao()
        playlists = db.playlistDao()
    }

    @After fun tearDown() = db.close()

    private fun song(id: Long, title: String = "Song $id", artist: String = "Artist", album: String = "Album", albumId: Long = 1, modified: Long = 0, added: Long = id) =
        SongEntity(id, title, artist, album, albumId, null, null, 200_000, "content://media/$id", null, id.toInt(), null, null, added, modified, 1, "/music")

    @Test fun favoritesPersistAndToggle() = runBlocking {
        songs.upsertAll(listOf(song(1), song(2)))
        stats.toggleFavorite(1)
        assertEquals(listOf(1L), songs.observeFavorites().first().map { it.song.id })
        stats.toggleFavorite(1)
        assertTrue(songs.observeFavorites().first().isEmpty())
    }

    @Test fun rescanKeepsFavoritesAndPlayCounts() = runBlocking {
        songs.upsertAll(listOf(song(1)))
        stats.toggleFavorite(1)
        stats.recordPlay(1, now = 100)
        // A rescan rewrites the song row (REPLACE); user data must survive.
        songs.upsertAll(listOf(song(1, title = "Renamed", modified = 5)))
        val row = songs.observeById(1).first()!!
        assertEquals("Renamed", row.song.title)
        assertEquals(true, row.isFavorite)
        assertEquals(1, row.playCount)
    }

    @Test fun historyOrdersRecentlyAndMostPlayed() = runBlocking {
        songs.upsertAll(listOf(song(1), song(2), song(3)))
        stats.recordPlay(1, 100); stats.recordPlay(2, 200); stats.recordPlay(2, 300)
        assertEquals(listOf(2L, 1L), songs.observeRecentlyPlayed(10).first().map { it.song.id })
        assertEquals(listOf(2L, 1L), songs.observeMostPlayed(10).first().map { it.song.id })
        assertEquals(2, songs.observeMostPlayed(10).first().first().playCount)
    }

    @Test fun recentlyAddedIsNewestFirst() = runBlocking {
        songs.upsertAll(listOf(song(1, added = 10), song(2, added = 30), song(3, added = 20)))
        assertEquals(listOf(2L, 3L, 1L), songs.observeRecentlyAdded(10).first().map { it.song.id })
    }

    @Test fun searchMatchesFieldsAndTreatsWildcardsLiterally() = runBlocking {
        songs.upsertAll(listOf(song(1, title = "100% Pure", artist = "Zed"), song(2, title = "Other", artist = "Queen")))
        assertEquals(listOf(1L), songs.search(likePattern("100%")).map { it.song.id })
        assertEquals(listOf(2L), songs.search(likePattern("queen")).map { it.song.id })
        assertTrue(songs.search(likePattern("%")).map { it.song.id }.contains(1L)) // literal % only matches song 1
        assertFalse(songs.search(likePattern("%")).map { it.song.id }.contains(2L))
    }

    @Test fun albumsAndArtistsAreGrouped() = runBlocking {
        songs.upsertAll(listOf(song(1, albumId = 10, album = "A"), song(2, albumId = 10, album = "A"), song(3, albumId = 11, album = "B")))
        val albums = songs.observeAlbums().first()
        assertEquals(2, albums.size)
        assertEquals(2, albums.first { it.id == 10L }.songCount)
        assertEquals(1, songs.observeArtists().first().size)
        assertEquals(2, songs.observeArtists().first().first().albumCount)
    }

    @Test fun playlistAddDedupesAndKeepsOrder() = runBlocking {
        songs.upsertAll(listOf(song(1), song(2), song(3)))
        val id = playlists.insert(PlaylistEntity(name = "Mix", createdAt = 0, updatedAt = 0))
        assertEquals(2, playlists.addSongs(id, listOf(3, 1), now = 1))
        assertEquals(1, playlists.addSongs(id, listOf(1, 2), now = 2)) // 1 already present
        assertEquals(listOf(3L, 1L, 2L), playlists.observeSongs(id).first().map { it.song.id })
    }

    @Test fun playlistRemoveAndReorderKeepPositionsDense() = runBlocking {
        songs.upsertAll(listOf(song(1), song(2), song(3)))
        val id = playlists.insert(PlaylistEntity(name = "Mix", createdAt = 0, updatedAt = 0))
        playlists.addSongs(id, listOf(1, 2, 3), now = 1)
        playlists.removeSong(id, 2, now = 2)
        assertEquals(listOf(1L, 3L), playlists.observeSongs(id).first().map { it.song.id })
        playlists.replaceOrder(id, listOf(3, 1), now = 3)
        assertEquals(listOf(3L, 1L), playlists.observeSongs(id).first().map { it.song.id })
        // A later append lands after the last item instead of colliding.
        playlists.addSongs(id, listOf(2), now = 4)
        assertEquals(listOf(3L, 1L, 2L), playlists.observeSongs(id).first().map { it.song.id })
    }

    @Test fun deletingPlaylistCascadesAndMissingSongsAreHidden() = runBlocking {
        songs.upsertAll(listOf(song(1), song(2)))
        val id = playlists.insert(PlaylistEntity(name = "Mix", createdAt = 0, updatedAt = 0))
        playlists.addSongs(id, listOf(1, 2), now = 1)
        songs.deleteByIds(listOf(2))
        assertEquals(1, playlists.observePlaylist(id).first()!!.songCount)
        songs.deleteOrphanPlaylistEntries()
        assertEquals(listOf(1L), playlists.songIds(id))
        playlists.delete(id)
        assertTrue(playlists.songIds(id).isEmpty())
    }
}
