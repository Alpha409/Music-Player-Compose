package com.example.musicplayercompose.feature.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import com.example.musicplayercompose.core.designsystem.SongListItem
import com.example.musicplayercompose.domain.model.Song
import com.example.musicplayercompose.playback.PlaybackState

/** Adds a full song list. Tapping a row queues the whole list starting at that row. */
fun LazyListScope.songItems(
    songs: List<Song>,
    playback: PlaybackState,
    onPlay: (index: Int) -> Unit,
    onMore: (Song) -> Unit,
    keyPrefix: String = "song"
) {
    items(songs.size, key = { "$keyPrefix-${songs[it].id}" }, contentType = { "song" }) { index ->
        val song = songs[index]
        SongListItem(
            song = song,
            isCurrent = playback.currentSongId == song.id,
            isPlaying = playback.isPlaying,
            onClick = { onPlay(index) },
            onMore = { onMore(song) }
        )
    }
}

@Composable
fun PlayShuffleRow(onPlay: () -> Unit, onShuffle: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(onClick = onPlay, enabled = enabled, contentPadding = ButtonDefaults.ButtonWithIconContentPadding) {
            Icon(Icons.Rounded.PlayArrow, null)
            Text("Play", modifier = Modifier.padding(start = 6.dp))
        }
        FilledTonalButton(onClick = onShuffle, enabled = enabled, contentPadding = ButtonDefaults.ButtonWithIconContentPadding) {
            Icon(Icons.Rounded.Shuffle, null)
            Text("Shuffle", modifier = Modifier.padding(start = 6.dp))
        }
    }
}
