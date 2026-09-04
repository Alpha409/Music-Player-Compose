package com.example.musicplayercompose.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicplayercompose.R
import com.example.musicplayercompose.domain.models.Mp3FilesDataClass
import com.example.musicplayercompose.ui.theme.AccentPink
import com.example.musicplayercompose.ui.theme.AccentPurple
import com.example.musicplayercompose.ui.theme.DarkSurface
import com.example.musicplayercompose.ui.theme.DarkSurfaceVariant
import com.example.musicplayercompose.ui.theme.TextMuted
import com.example.musicplayercompose.ui.theme.TextPrimary
import com.example.musicplayercompose.ui.theme.TextSecondary

@Composable
fun SongListItem(
    song: Mp3FilesDataClass,
    isCurrentPlaying: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBackground by animateColorAsState(
        targetValue = if (isCurrentPlaying) DarkSurfaceVariant else DarkSurface,
        label = "song_card_bg"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentPlaying) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art thumbnail
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.DarkGray)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.allsongsplaceholder),
                    contentDescription = song.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (isCurrentPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (isPlaying) R.drawable.ic_pause else R.drawable.play
                            ),
                            contentDescription = if (isPlaying) "Playing" else "Paused",
                            tint = AccentPurple,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title, artist, duration
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = song.displayTitle,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isCurrentPlaying) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 15.sp
                    ),
                    color = if (isCurrentPlaying) AccentPurple else TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.size(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = song.displayArtist,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Text(
                        text = " • ${song.formattedDuration}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = TextMuted
                    )
                }
            }

            // Favorite Button
            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(
                        id = if (song.isFav) R.drawable.heartfilled else R.drawable.heart_empty
                    ),
                    contentDescription = if (song.isFav) "Remove from favorites" else "Add to favorites",
                    tint = if (song.isFav) AccentPink else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
