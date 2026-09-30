package com.example.musicplayercompose.domain

import com.example.musicplayercompose.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Test

class ModelsTest {
    private fun song(title: String = "T", artist: String? = "A", album: String? = "B") = Song(
        id = 1, title = title, artist = artist, album = album, albumId = 1, albumArtist = null, genre = null,
        durationMs = 1000, uri = "content://x/1", artworkUri = null, trackNumber = null, discNumber = null,
        year = null, dateAdded = 0, dateModified = 0, size = 0, folder = ""
    )

    @Test fun `MediaStore unknown placeholders become friendly labels`() {
        assertEquals("Unknown artist", song(artist = "<unknown>").displayArtist)
        assertEquals("Unknown artist", song(artist = "  ").displayArtist)
        assertEquals("Unknown artist", song(artist = null).displayArtist)
        assertEquals("Unknown album", song(album = "<unknown>").displayAlbum)
    }

    @Test fun `real metadata is kept and blank titles get a fallback`() {
        assertEquals("Queen", song(artist = "Queen").displayArtist)
        assertEquals("Untitled track", song(title = "").displayTitle)
    }
}
