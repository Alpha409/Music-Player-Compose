package com.example.musicplayercompose.feature.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.musicplayercompose.core.designsystem.Artwork
import com.example.musicplayercompose.core.designsystem.EmptyState
import com.example.musicplayercompose.core.navigation.Screen
import com.example.musicplayercompose.core.utils.pluralSongs
import com.example.musicplayercompose.domain.model.Playlist
import com.example.musicplayercompose.feature.library.PlayShuffleRow
import com.example.musicplayercompose.feature.main.LocalSongActions
import com.example.musicplayercompose.feature.main.MainViewModel
import com.example.musicplayercompose.feature.main.NameDialog
import kotlin.math.roundToInt

@Composable
fun PlaylistsScreen(
    navigate: (String) -> Unit,
    contentPadding: PaddingValues,
    viewModel: PlaylistsViewModel = hiltViewModel()
) {
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    var showCreate by remember { mutableStateOf(false) }
    var renaming by remember { mutableStateOf<Playlist?>(null) }
    var deleting by remember { mutableStateOf<Playlist?>(null) }
    val bottom = contentPadding.calculateBottomPadding()

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Text("Playlists", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(16.dp))
            if (playlists.isEmpty()) {
                EmptyState(
                    Icons.Rounded.QueueMusic, "No playlists yet",
                    "Create a playlist to group songs however you like.",
                    actionLabel = "Create playlist", onAction = { showCreate = true }
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = bottom + 88.dp)) {
                    items(playlists, key = { it.id }) { playlist ->
                        var menu by remember { mutableStateOf(false) }
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable(onClickLabel = "Open ${playlist.name}") { navigate(Screen.PlaylistDetail.create(playlist.id)) }
                                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier.size(56.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Rounded.QueueMusic, null, tint = MaterialTheme.colorScheme.onPrimaryContainer) }
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(playlist.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(pluralSongs(playlist.songCount), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Box {
                                IconButton(onClick = { menu = true }) { Icon(Icons.Rounded.MoreVert, contentDescription = "Options for ${playlist.name}") }
                                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                                    DropdownMenuItem(text = { Text("Rename") }, onClick = { menu = false; renaming = playlist })
                                    DropdownMenuItem(text = { Text("Delete") }, onClick = { menu = false; deleting = playlist })
                                }
                            }
                        }
                    }
                }
            }
        }
        if (playlists.isNotEmpty()) {
            FloatingActionButton(
                onClick = { showCreate = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = bottom + 16.dp)
            ) { Icon(Icons.Rounded.Add, contentDescription = "Create playlist") }
        }
    }

    if (showCreate) {
        NameDialog("New playlist", "Create", { showCreate = false }, { viewModel.create(it); showCreate = false })
    }
    renaming?.let { p ->
        NameDialog("Rename playlist", "Save", { renaming = null }, { viewModel.rename(p.id, it); renaming = null }, initial = p.name)
    }
    deleting?.let { p -> DeletePlaylistDialog(p.name, { deleting = null }) { viewModel.delete(p.id); deleting = null } }
}

@Composable
private fun DeletePlaylistDialog(name: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete playlist?") },
        text = { Text("\"$name\" will be deleted. Your songs stay on the device.") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private val RowHeight = 64.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    mainViewModel: MainViewModel,
    onBack: () -> Unit,
    contentPadding: PaddingValues,
    viewModel: PlaylistDetailViewModel = hiltViewModel()
) {
    val playlist by viewModel.playlist.collectAsStateWithLifecycle()
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val playback by mainViewModel.playbackState.collectAsStateWithLifecycle()
    val actions = LocalSongActions.current
    var menu by remember { mutableStateOf(false) }
    var renaming by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    val rowPx = with(LocalDensity.current) { RowHeight.toPx() }
    var draggingIndex by remember { mutableIntStateOf(-1) }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(playlist?.name.orEmpty(), maxLines = 1) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") } },
            actions = {
                IconButton(onClick = { menu = true }) { Icon(Icons.Rounded.MoreVert, contentDescription = "Playlist options") }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(text = { Text("Add all to queue") }, enabled = songs.isNotEmpty(), onClick = { menu = false; mainViewModel.addToQueue(songs) })
                    DropdownMenuItem(text = { Text("Rename") }, onClick = { menu = false; renaming = true })
                    DropdownMenuItem(text = { Text("Delete playlist") }, onClick = { menu = false; deleting = true })
                }
            }
        )
        if (songs.isEmpty()) {
            EmptyState(
                Icons.Rounded.QueueMusic, "This playlist is empty",
                "Open a song's menu and choose “Add to playlist”."
            )
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding() + 16.dp)) {
                item(key = "controls") {
                    Column {
                        Text(
                            pluralSongs(songs.size),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        PlayShuffleRow(
                            onPlay = { mainViewModel.playSongs(songs, 0, shuffle = false) },
                            onShuffle = { mainViewModel.playSongs(songs, shuffle = true) }
                        )
                    }
                }
                itemsIndexed(songs, key = { _, song -> "pls-${song.id}" }) { index, song ->
                    val isDragging = index == draggingIndex
                    val isCurrent = playback.currentSongId == song.id
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(RowHeight)
                            .zIndex(if (isDragging) 1f else 0f)
                            .graphicsLayer { translationY = if (isDragging) dragOffset else 0f }
                            .background(if (isDragging) MaterialTheme.colorScheme.surfaceContainerHighest else Color.Transparent)
                            .clickable(onClickLabel = "Play ${song.displayTitle}") { mainViewModel.playSongs(songs, index) }
                            .padding(start = 16.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Artwork(song.artworkUri, Modifier.size(48.dp), RoundedCornerShape(12.dp))
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                song.displayTitle, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(song.displayArtist, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        IconButton(onClick = { actions.openMenu(song) }) { Icon(Icons.Rounded.MoreVert, contentDescription = "More options for ${song.displayTitle}") }
                        IconButton(onClick = { viewModel.remove(song.id) }) { Icon(Icons.Rounded.Close, contentDescription = "Remove ${song.displayTitle} from playlist") }
                        Icon(
                            Icons.Rounded.DragHandle,
                            contentDescription = "Drag to reorder ${song.displayTitle}",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(48.dp)
                                .padding(12.dp)
                                .pointerInput(index, songs.size) {
                                    detectDragGestures(
                                        onDragStart = { draggingIndex = index; dragOffset = 0f },
                                        onDrag = { change, amount -> change.consume(); dragOffset += amount.y },
                                        onDragEnd = {
                                            val target = (index + (dragOffset / rowPx).roundToInt()).coerceIn(0, songs.lastIndex)
                                            if (target != index) viewModel.move(index, target)
                                            draggingIndex = -1
                                            dragOffset = 0f
                                        },
                                        onDragCancel = { draggingIndex = -1; dragOffset = 0f }
                                    )
                                }
                        )
                    }
                }
            }
        }
    }

    if (renaming) NameDialog("Rename playlist", "Save", { renaming = false }, { viewModel.rename(it); renaming = false }, initial = playlist?.name.orEmpty())
    if (deleting) DeletePlaylistDialog(playlist?.name.orEmpty(), { deleting = false }) {
        deleting = false
        viewModel.delete()
        onBack()
    }
}
