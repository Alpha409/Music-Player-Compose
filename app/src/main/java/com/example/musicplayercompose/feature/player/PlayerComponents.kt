package com.example.musicplayercompose.feature.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.Forward30
import androidx.compose.material.icons.rounded.Forward5
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.Replay30
import androidx.compose.material.icons.rounded.Replay5
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import com.example.musicplayercompose.core.designsystem.Artwork
import com.example.musicplayercompose.core.utils.formatDuration
import com.example.musicplayercompose.feature.main.LocalSongActions
import com.example.musicplayercompose.feature.main.MainViewModel

@Composable
fun MiniPlayer(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.playbackState.collectAsState()
    val position by viewModel.positionMs.collectAsState()
    if (!state.hasMedia) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 3.dp,
        shadowElevation = 4.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .clickable(onClickLabel = "Open player") { viewModel.setPlayerExpanded(true) }
                    .padding(start = 10.dp, end = 4.dp, top = 8.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Artwork(state.artworkUri, Modifier.size(44.dp), RoundedCornerShape(12.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(state.title.orEmpty(), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        state.artist.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = viewModel::togglePlayPause) {
                    Icon(
                        if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (state.isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(30.dp)
                    )
                }
                IconButton(onClick = viewModel::next) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = "Next track")
                }
            }
            val progress = if (state.durationMs > 0) (position.toFloat() / state.durationMs).coerceIn(0f, 1f) else 0f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .semantics { contentDescription = "Track progress" },
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerSheet(viewModel: MainViewModel) {
    val state by viewModel.playbackState.collectAsState()
    val song by viewModel.currentSong.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val actions = LocalSongActions.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val skipSec = settings?.skipDurationSec ?: 10

    ModalBottomSheet(
        onDismissRequest = { viewModel.setPlayerExpanded(false) },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.setPlayerExpanded(false) }) {
                    Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Close player")
                }
                Text(
                    "NOW PLAYING",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.5.sp
                )
                IconButton(
                    onClick = { song?.let(actions.openMenu) },
                    enabled = song != null
                ) { Icon(Icons.Rounded.MoreVert, contentDescription = "More options") }
            }

            Spacer(Modifier.height(8.dp))
            Artwork(
                state.artworkUri,
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                RoundedCornerShape(28.dp),
                contentDescription = "Album art for ${state.title.orEmpty()}"
            )
            Spacer(Modifier.height(24.dp))

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(state.title.orEmpty(), style = MaterialTheme.typography.headlineSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(
                        state.artist.orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                val fav = song?.isFavorite == true
                IconButton(
                    onClick = { state.currentSongId?.let(viewModel::toggleFavorite) },
                    modifier = Modifier.semantics { stateDescription = if (fav) "Favorited" else "Not favorited" }
                ) {
                    Icon(
                        if (fav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = if (fav) "Remove from favorites" else "Add to favorites",
                        tint = if (fav) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            SeekBar(viewModel)
            Spacer(Modifier.height(8.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = viewModel::toggleShuffle) {
                    Icon(
                        Icons.Rounded.Shuffle,
                        contentDescription = if (state.shuffle) "Shuffle on" else "Shuffle off",
                        tint = if (state.shuffle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = viewModel::previous, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous track", modifier = Modifier.size(36.dp))
                }
                FilledIconButton(
                    onClick = viewModel::togglePlayPause,
                    modifier = Modifier.size(72.dp),
                    colors = IconButtonDefaults.filledIconButtonColors()
                ) {
                    Icon(
                        if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (state.isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(40.dp)
                    )
                }
                IconButton(onClick = viewModel::next, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = "Next track", modifier = Modifier.size(36.dp))
                }
                IconButton(onClick = viewModel::cycleRepeat) {
                    val (icon, description) = when (state.repeatMode) {
                        Player.REPEAT_MODE_ALL -> Icons.Rounded.Repeat to "Repeat all"
                        Player.REPEAT_MODE_ONE -> Icons.Rounded.RepeatOne to "Repeat one"
                        else -> Icons.Rounded.Repeat to "Repeat off"
                    }
                    Icon(
                        icon,
                        contentDescription = description,
                        tint = if (state.repeatMode != Player.REPEAT_MODE_OFF) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.seekBy(-skipSec * 1000L) }) {
                    Icon(skipBackIcon(skipSec), contentDescription = "Back $skipSec seconds")
                }
                IconButton(onClick = { viewModel.setQueueVisible(true) }) {
                    Icon(Icons.Rounded.QueueMusic, contentDescription = "Open queue")
                }
                IconButton(onClick = { viewModel.seekBy(skipSec * 1000L) }) {
                    Icon(skipForwardIcon(skipSec), contentDescription = "Forward $skipSec seconds")
                }
            }
        }
    }
}

private fun skipBackIcon(sec: Int) = when (sec) {
    5 -> Icons.Rounded.Replay5
    30 -> Icons.Rounded.Replay30
    else -> Icons.Rounded.Replay10
}

private fun skipForwardIcon(sec: Int) = when (sec) {
    5 -> Icons.Rounded.Forward5
    30 -> Icons.Rounded.Forward30
    else -> Icons.Rounded.Forward10
}

@Composable
private fun SeekBar(viewModel: MainViewModel) {
    val state by viewModel.playbackState.collectAsState()
    val position by viewModel.positionMs.collectAsState()
    var dragValue by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    val duration = state.durationMs.coerceAtLeast(1L)
    val shown = if (dragging) dragValue else position.toFloat().coerceIn(0f, duration.toFloat())

    Column {
        Slider(
            value = shown,
            onValueChange = {
                dragging = true
                dragValue = it
            },
            onValueChangeFinished = {
                viewModel.seekTo(dragValue.toLong())
                dragging = false
            },
            valueRange = 0f..duration.toFloat(),
            enabled = state.durationMs > 0,
            modifier = Modifier.semantics {
                contentDescription = "Seek bar"
                stateDescription = "${formatDuration(shown.toLong())} of ${formatDuration(state.durationMs)}"
            }
        )
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatDuration(shown.toLong()), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(formatDuration(state.durationMs), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
