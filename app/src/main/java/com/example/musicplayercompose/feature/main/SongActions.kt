package com.example.musicplayercompose.feature.main

import android.app.RecoverableSecurityException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.musicplayercompose.core.designsystem.Artwork
import com.example.musicplayercompose.core.navigation.Screen
import com.example.musicplayercompose.core.utils.pluralSongs
import com.example.musicplayercompose.domain.model.Song

/** Entry points any screen can trigger without knowing how menus, pickers or deletion work. */
class SongActions(
    val openMenu: (Song) -> Unit,
    val addToPlaylist: (List<Song>) -> Unit
)

val LocalSongActions = staticCompositionLocalOf<SongActions> { error("SongActions not provided") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongActionsHost(
    viewModel: MainViewModel,
    navigate: (String) -> Unit,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    var menuSong by remember { mutableStateOf<Song?>(null) }
    var pickerSongs by remember { mutableStateOf<List<Song>?>(null) }
    var deleteSong by remember { mutableStateOf<Song?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    val playlists by viewModel.playlists.collectAsState()

    val deleteLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            viewModel.showMessage("Song deleted")
            viewModel.rescan()
        }
    }

    fun requestDelete(song: Song) {
        val uri = Uri.parse(song.uri)
        val resolver = context.contentResolver
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                val pending = MediaStore.createDeleteRequest(resolver, listOf(uri))
                deleteLauncher.launch(IntentSenderRequest.Builder(pending.intentSender).build())
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> try {
                resolver.delete(uri, null, null)
                viewModel.showMessage("Song deleted")
                viewModel.rescan()
            } catch (e: SecurityException) {
                val sender = recoverableDeleteIntent(e)
                if (sender != null) deleteLauncher.launch(IntentSenderRequest.Builder(sender).build())
                else viewModel.showMessage("Couldn't delete this song")
            }
            else -> try {
                // Pre-Q has no user-consent flow and the app holds no write permission.
                resolver.delete(uri, null, null)
                viewModel.showMessage("Song deleted")
                viewModel.rescan()
            } catch (e: SecurityException) {
                viewModel.showMessage("This Android version doesn't allow deleting this song from here")
            }
        }
    }

    val actions = remember {
        SongActions(
            openMenu = { menuSong = it },
            addToPlaylist = { pickerSongs = it }
        )
    }

    CompositionLocalProvider(LocalSongActions provides actions) { content() }

    menuSong?.let { song ->
        ModalBottomSheet(
            onDismissRequest = { menuSong = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(Modifier.navigationBarsPadding().padding(bottom = 12.dp)) {
                Row(Modifier.padding(horizontal = 24.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Artwork(song.artworkUri, Modifier.size(56.dp), RoundedCornerShape(14.dp))
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(song.displayTitle, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                        Text(song.displayArtist, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                }
                MenuRow(Icons.Rounded.SkipNext, "Play next") { viewModel.playNext(song); menuSong = null }
                MenuRow(Icons.Rounded.QueueMusic, "Add to queue") { viewModel.addToQueue(song); menuSong = null }
                MenuRow(Icons.Rounded.PlaylistAdd, "Add to playlist") { pickerSongs = listOf(song); menuSong = null }
                MenuRow(
                    if (song.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    if (song.isFavorite) "Remove from favorites" else "Add to favorites"
                ) { viewModel.toggleFavorite(song.id); menuSong = null }
                MenuRow(Icons.Rounded.Album, "Go to album") {
                    menuSong = null
                    viewModel.setPlayerExpanded(false)
                    navigate(Screen.AlbumDetail.create(song.albumId))
                }
                MenuRow(Icons.Rounded.Person, "Go to artist") {
                    menuSong = null
                    viewModel.setPlayerExpanded(false)
                    navigate(Screen.ArtistDetail.create(song.artist.orEmpty()))
                }
                MenuRow(Icons.Rounded.Share, "Share") {
                    menuSong = null
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "audio/*"
                        putExtra(Intent.EXTRA_STREAM, Uri.parse(song.uri))
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(send, "Share ${song.displayTitle}"))
                }
                MenuRow(Icons.Rounded.Delete, "Delete from device", tint = MaterialTheme.colorScheme.error) {
                    deleteSong = song
                    menuSong = null
                }
            }
        }
    }

    pickerSongs?.let { songs ->
        ModalBottomSheet(
            onDismissRequest = { pickerSongs = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(Modifier.navigationBarsPadding().padding(bottom = 12.dp)) {
                Text(
                    "Add ${if (songs.size == 1) "\"${songs.first().displayTitle}\"" else pluralSongs(songs.size)} to…",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                MenuRow(Icons.Rounded.Add, "New playlist", tint = MaterialTheme.colorScheme.primary) { showCreate = true }
                LazyColumn {
                    items(playlists, key = { it.id }) { playlist ->
                        MenuRow(Icons.Rounded.QueueMusic, "${playlist.name}  ·  ${pluralSongs(playlist.songCount)}") {
                            viewModel.addToPlaylist(playlist, songs)
                            pickerSongs = null
                        }
                    }
                }
            }
        }
    }

    if (showCreate) {
        NameDialog(
            title = "New playlist",
            confirmLabel = "Create",
            onDismiss = { showCreate = false },
            onConfirm = { name ->
                viewModel.createPlaylistAndAdd(name, pickerSongs.orEmpty())
                showCreate = false
                pickerSongs = null
            }
        )
    }

    deleteSong?.let { song ->
        AlertDialog(
            onDismissRequest = { deleteSong = null },
            title = { Text("Delete song?") },
            text = { Text("\"${song.displayTitle}\" will be permanently deleted from this device.") },
            confirmButton = {
                TextButton(onClick = { deleteSong = null; requestDelete(song) }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { deleteSong = null }) { Text("Cancel") } }
        )
    }
}

/** Android 10 signals "ask the user" with a RecoverableSecurityException carrying the consent dialog. */
@androidx.annotation.RequiresApi(Build.VERSION_CODES.Q)
private fun recoverableDeleteIntent(e: SecurityException): android.content.IntentSender? =
    (e as? RecoverableSecurityException)?.userAction?.actionIntent?.intentSender

@Composable
private fun MenuRow(
    icon: ImageVector,
    label: String,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint)
        Spacer(Modifier.width(20.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = tint)
    }
}

/** Single-field dialog used for creating and renaming playlists. */
@Composable
fun NameDialog(
    title: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    initial: String = ""
) {
    var name by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(60) },
                label = { Text("Name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done)
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name.trim()) }, enabled = name.isNotBlank()) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
