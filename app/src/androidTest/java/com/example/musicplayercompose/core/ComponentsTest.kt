package com.example.musicplayercompose.core

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import com.example.musicplayercompose.core.designsystem.EmptyState
import com.example.musicplayercompose.core.designsystem.MusicPlayerTheme
import com.example.musicplayercompose.core.designsystem.SongListItem
import com.example.musicplayercompose.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ComponentsTest {
    @get:Rule val compose = createComposeRule()

    private val song = Song(
        id = 1, title = "Bohemian Rhapsody", artist = "Queen", album = "Opera", albumId = 1, albumArtist = null, genre = null,
        durationMs = 354_000, uri = "content://x/1", artworkUri = null, trackNumber = null, discNumber = null, year = null,
        dateAdded = 0, dateModified = 0, size = 0, folder = ""
    )

    @Test fun songRowShowsMetadataAndReportsClicks() {
        var clicks = 0
        var menus = 0
        compose.setContent {
            MusicPlayerTheme {
                SongListItem(song, isCurrent = false, isPlaying = false, onClick = { clicks++ }, onMore = { menus++ })
            }
        }
        compose.onNodeWithText("Bohemian Rhapsody").assertIsDisplayed()
        compose.onNodeWithText("Queen · Opera").assertIsDisplayed()
        compose.onNodeWithText("5:54").assertIsDisplayed()
        compose.onNodeWithText("Bohemian Rhapsody").performClick()
        compose.onNodeWithContentDescription("More options for Bohemian Rhapsody").performClick()
        assertEquals(1, clicks)
        assertEquals(1, menus)
    }

    @Test fun emptyStateActionIsClickable() {
        var tapped = false
        compose.setContent {
            MusicPlayerTheme {
                EmptyState(Icons.Rounded.MusicNote, "No music found", "Add some", actionLabel = "Scan storage", onAction = { tapped = true })
            }
        }
        compose.onNodeWithText("No music found").assertIsDisplayed()
        compose.onNodeWithText("Scan storage").performClick()
        assertEquals(true, tapped)
    }
}
