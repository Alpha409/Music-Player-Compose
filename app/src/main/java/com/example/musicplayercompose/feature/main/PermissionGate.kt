package com.example.musicplayercompose.feature.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.musicplayercompose.core.designsystem.EmptyState
import com.example.musicplayercompose.core.utils.findActivity

private val AudioPermission: String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_AUDIO
    else Manifest.permission.READ_EXTERNAL_STORAGE

/**
 * Explains why music access is needed, requests it, and deep-links to system settings once
 * Android will no longer show the dialog. Re-checks on resume so returning from Settings works.
 */
@Composable
fun PermissionGate(onGranted: () -> Unit, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var granted by remember { mutableStateOf(hasPermission(context, AudioPermission)) }
    var requestedOnce by rememberSaveable { mutableStateOf(false) }
    var notificationsRequested by rememberSaveable { mutableStateOf(false) }

    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    val audioLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
        granted = result
        requestedOnce = true
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) granted = hasPermission(context, AudioPermission)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        if (!granted && !requestedOnce) audioLauncher.launch(AudioPermission)
    }

    LaunchedEffect(granted) {
        if (granted) {
            onGranted()
            // Needed for the playback notification on Android 13+; playback works without it.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationsRequested &&
                !hasPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            ) {
                notificationsRequested = true
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    if (granted) {
        content()
    } else {
        val activity = context.findActivity()
        val permanentlyDenied = requestedOnce && activity != null &&
            !ActivityCompat.shouldShowRequestPermissionRationale(activity, AudioPermission)
        EmptyState(
            icon = Icons.Rounded.LibraryMusic,
            title = "Let us find your music",
            message = "This player needs access to audio files on your device to build your library. " +
                "Your music never leaves your phone, and no account is needed.",
            actionLabel = if (permanentlyDenied) "Open settings" else "Allow access",
            onAction = {
                if (permanentlyDenied) {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                    )
                } else {
                    audioLauncher.launch(AudioPermission)
                }
            }
        )
    }
}

private fun hasPermission(context: android.content.Context, permission: String) =
    ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
