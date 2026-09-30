package com.example.musicplayercompose.core.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Base64
import androidx.activity.ComponentActivity
import java.util.Locale

fun formatDuration(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%d:%02d", minutes, seconds)
    }
}

fun formatTotalDuration(ms: Long): String {
    val minutes = ms / 60_000
    return if (minutes >= 60) "${minutes / 60} hr ${minutes % 60} min" else "$minutes min"
}

fun pluralSongs(count: Int) = if (count == 1) "1 song" else "$count songs"

/** Route-safe encoding for free text (artist/genre names can contain `/`, `%`, `?`). */
fun encodeRouteArg(value: String): String =
    Base64.encodeToString(value.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)

fun decodeRouteArg(value: String): String =
    String(Base64.decode(value, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING))

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun Context.requireComponentActivity(): ComponentActivity =
    findActivity() as? ComponentActivity ?: error("No ComponentActivity in context")
