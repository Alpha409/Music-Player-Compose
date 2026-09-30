package com.example.musicplayercompose.core

import com.example.musicplayercompose.core.utils.formatDuration
import com.example.musicplayercompose.core.utils.formatTotalDuration
import com.example.musicplayercompose.core.utils.pluralSongs
import org.junit.Assert.assertEquals
import org.junit.Test

class UtilsTest {
    @Test fun `formats minutes and seconds`() {
        assertEquals("0:00", formatDuration(0))
        assertEquals("0:59", formatDuration(59_999))
        assertEquals("3:05", formatDuration(185_000))
    }

    @Test fun `formats hours only when needed`() {
        assertEquals("59:59", formatDuration(3_599_000))
        assertEquals("1:00:00", formatDuration(3_600_000))
        assertEquals("1:02:03", formatDuration(3_723_000))
    }

    @Test fun `negative or unknown durations render as zero`() {
        assertEquals("0:00", formatDuration(-1))
    }

    @Test fun `total duration switches to hours`() {
        assertEquals("42 min", formatTotalDuration(42 * 60_000L))
        assertEquals("1 hr 5 min", formatTotalDuration(65 * 60_000L))
    }

    @Test fun `song count pluralises`() {
        assertEquals("0 songs", pluralSongs(0))
        assertEquals("1 song", pluralSongs(1))
        assertEquals("2 songs", pluralSongs(2))
    }
}
