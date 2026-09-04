package com.example.musicplayercompose.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.musicplayercompose.R
import com.example.musicplayercompose.domain.models.Mp3FilesDataClass
import com.example.musicplayercompose.ui.theme.AccentPink
import com.example.musicplayercompose.ui.theme.AccentPurple
import com.example.musicplayercompose.ui.theme.DarkBackground
import com.example.musicplayercompose.ui.theme.DarkSurfaceVariant
import com.example.musicplayercompose.ui.theme.PlayerTrack
import com.example.musicplayercompose.ui.theme.TextMuted
import com.example.musicplayercompose.ui.theme.TextPrimary
import com.example.musicplayercompose.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun FullPlayerSheet(
    song: Mp3FilesDataClass?,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    isShuffle: Boolean,
    isRepeat: Boolean,
    onDismiss: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleFavorite: (Mp3FilesDataClass) -> Unit
) {
    if (song == null) return

    var isUserDraggingSlider by remember { mutableStateOf(false) }
    var sliderDragPosition by remember { mutableFloatStateOf(0f) }

    val currentSliderValue = if (isUserDraggingSlider) {
        sliderDragPosition
    } else {
        currentPosition.toFloat().coerceIn(0f, duration.toFloat().coerceAtLeast(1f))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DarkBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            painter = painterResource(id = R.drawable.playback),
                            contentDescription = "Minimize",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = "NOW PLAYING",
                        style = MaterialTheme.typography.labelLarge.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = TextSecondary
                    )

                    IconButton(onClick = { onToggleFavorite(song) }) {
                        Icon(
                            painter = painterResource(
                                id = if (song.isFav) R.drawable.heartfilled else R.drawable.heart_empty
                            ),
                            contentDescription = "Favorite",
                            tint = if (song.isFav) AccentPink else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Album Art Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .aspectRatio(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.playingnow),
                        contentDescription = song.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Track Information
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = song.displayTitle,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = song.displayArtist,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Slider & Timers
                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = currentSliderValue,
                        onValueChange = { newValue ->
                            isUserDraggingSlider = true
                            sliderDragPosition = newValue
                        },
                        onValueChangeFinished = {
                            isUserDraggingSlider = false
                            onSeekTo(sliderDragPosition.toLong())
                        },
                        valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = AccentPurple,
                            activeTrackColor = AccentPurple,
                            inactiveTrackColor = PlayerTrack
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(currentSliderValue.toLong()),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                        Text(
                            text = formatTime(duration),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Playback Control Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle
                    IconButton(onClick = onToggleShuffle) {
                        Icon(
                            painter = painterResource(id = R.drawable.searchrectangle),
                            contentDescription = "Shuffle",
                            tint = if (isShuffle) AccentPurple else TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Previous
                    IconButton(
                        onClick = onPrevious,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.playback),
                            contentDescription = "Previous",
                            tint = TextPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Play/Pause Big Button
                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(AccentPurple)
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (isPlaying) R.drawable.ic_pause else R.drawable.play
                            ),
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Next
                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.playforward),
                            contentDescription = "Next",
                            tint = TextPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Repeat
                    IconButton(onClick = onToggleRepeat) {
                        Icon(
                            painter = painterResource(id = R.drawable.echo),
                            contentDescription = "Repeat",
                            tint = if (isRepeat) AccentPurple else TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    if (millis <= 0) return "00:00"
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
