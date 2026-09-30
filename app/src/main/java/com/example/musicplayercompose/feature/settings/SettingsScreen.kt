package com.example.musicplayercompose.feature.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.musicplayercompose.BuildConfig
import com.example.musicplayercompose.domain.model.SortOrder
import com.example.musicplayercompose.domain.model.ThemeMode
import com.example.musicplayercompose.feature.main.MainViewModel

@Composable
fun SettingsScreen(
    mainViewModel: MainViewModel,
    contentPadding: PaddingValues,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val s by viewModel.settings.collectAsStateWithLifecycle()
    val syncing by mainViewModel.isSyncing.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var dialog by remember { mutableStateOf<Dialog?>(null) }
    var folders by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(dialog) {
        if (dialog == Dialog.Folders) folders = viewModel.candidateFolders()
    }

    Column(
        Modifier
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = contentPadding.calculateBottomPadding() + 16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(16.dp))

        Section("Appearance")
        ValueRow("Theme", s.themeMode.label) { dialog = Dialog.Theme }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SwitchRow("Dynamic colors", "Use colors from your wallpaper", s.dynamicColor, viewModel::setDynamicColor)
        }

        Section("Playback")
        SwitchRow("Resume playback", "Restore your last queue and position on launch", s.resumePlayback, viewModel::setResume)
        SwitchRow("Auto-play on resume", "Start playing automatically when the last session is restored", s.autoPlayOnResume, viewModel::setAutoPlay, enabled = s.resumePlayback)
        SwitchRow("Remember shuffle & repeat", "Keep your shuffle and repeat modes between sessions", s.rememberShuffleRepeat, viewModel::setRememberModes)
        ValueRow("Skip duration", "${s.skipDurationSec} seconds") { dialog = Dialog.Skip }

        Section("Audio")
        SwitchRow("Pause when headset disconnects", "Stops playback when headphones or Bluetooth disconnect", s.pauseOnHeadsetDisconnect, viewModel::setNoisy)
        SwitchRow("Handle audio focus", "Pause or lower volume for calls and other apps", s.handleAudioFocus, viewModel::setFocus)

        Section("Library")
        ValueRow("Default sort order", s.sortOrder.label) { dialog = Dialog.Sort }
        ValueRow(
            "Excluded folders",
            if (s.excludedFolders.isEmpty()) "None" else "${s.excludedFolders.size} excluded"
        ) { dialog = Dialog.Folders }
        ValueRow("Rescan library", if (syncing) "Scanning…" else "Look for new, changed and removed music") {
            if (!syncing) mainViewModel.rescan()
        }

        Section("About")
        ValueRow("Version", BuildConfig.VERSION_NAME) {}
        ValueRow("Privacy", "Your music and listening data stay on this device. No account, no tracking.") {}
    }

    when (dialog) {
        Dialog.Theme -> ChoiceDialog("Theme", ThemeMode.entries, s.themeMode, { it.label }, { viewModel.setTheme(it); dialog = null }) { dialog = null }
        Dialog.Skip -> ChoiceDialog("Skip duration", listOf(5, 10, 30), s.skipDurationSec, { "$it seconds" }, { viewModel.setSkip(it); dialog = null }) { dialog = null }
        Dialog.Sort -> ChoiceDialog("Default sort order", SortOrder.entries, s.sortOrder, { it.label }, { viewModel.setSort(it); dialog = null }) { dialog = null }
        Dialog.Folders -> FoldersDialog(folders, s.excludedFolders, { viewModel.setExcluded(it); dialog = null }) { dialog = null }
        null -> Unit
    }
}

private enum class Dialog { Theme, Skip, Sort, Folders }

@Composable
private fun Section(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 4.dp)
    )
}

@Composable
private fun ValueRow(title: String, value: String, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit, enabled: Boolean = true) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, role = Role.Switch) { onChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                title, style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = null, enabled = enabled)
    }
}

@Composable
private fun <T> ChoiceDialog(
    title: String,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable(role = Role.RadioButton) { onSelect(option) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = option == selected, onClick = null, modifier = Modifier.padding(end = 12.dp))
                        Text(label(option))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun FoldersDialog(
    folders: List<String>,
    excluded: Set<String>,
    onSave: (Set<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var selection by remember(excluded) { mutableStateOf(excluded) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Excluded folders") },
        text = {
            if (folders.isEmpty()) {
                Text("No folders to show yet. Scan your library first.")
            } else {
                Column {
                    Text(
                        "Songs in checked folders are hidden from your library.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    HorizontalDivider()
                    LazyColumn {
                        items(folders, key = { it }) { folder ->
                            val checked = folder in selection
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable(role = Role.Checkbox) { selection = if (checked) selection - folder else selection + folder }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(checked = checked, onCheckedChange = null, modifier = Modifier.padding(end = 12.dp))
                                Text(
                                    folder.removePrefix("/storage/emulated/0/"),
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onSave(selection) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
