package com.example.musicplayercompose.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.musicplayercompose.core.utils.formatDuration
import com.example.musicplayercompose.core.utils.pluralSongs
import com.example.musicplayercompose.domain.model.Album
import com.example.musicplayercompose.domain.model.Artist
import com.example.musicplayercompose.domain.model.Playlist
import com.example.musicplayercompose.domain.model.Song

/** Artwork with a music-note fallback that shows through when there is no art or it fails to load. */
@Composable
fun Artwork(
    model: String?,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    fallbackIcon: ImageVector = Icons.Rounded.MusicNote,
    contentDescription: String? = null
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = fallbackIcon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxSize(0.45f)
        )
        if (model != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(model).crossfade(true).build(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() }
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(20.dp))
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Composable
fun SongListItem(
    song: Song,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onMore: () -> Unit,
    modifier: Modifier = Modifier,
    showDuration: Boolean = true
) {
    val accent = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClickLabel = "Play ${song.displayTitle}", role = Role.Button, onClick = onClick)
            .padding(start = 16.dp, top = 6.dp, bottom = 6.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Artwork(song.artworkUri, Modifier.size(52.dp), RoundedCornerShape(12.dp))
            if (isCurrent) {
                Box(
                    Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.GraphicEq,
                        contentDescription = if (isPlaying) "Playing" else "Paused",
                        tint = androidx.compose.ui.graphics.Color.White
                    )
                }
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(song.displayTitle, style = MaterialTheme.typography.titleMedium, color = accent, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                "${song.displayArtist} · ${song.displayAlbum}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (showDuration) {
            Text(
                formatDuration(song.durationMs),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
        IconButton(onClick = onMore) {
            Icon(Icons.Rounded.MoreVert, contentDescription = "More options for ${song.displayTitle}")
        }
    }
}

/** Square card for horizontal carousels (recently played, favourites, …). */
@Composable
fun SongCard(song: Song, onClick: () -> Unit, onLongClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .width(148.dp)
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClickLabel = "Play ${song.displayTitle}",
                onClick = onClick,
                onLongClickLabel = "More options",
                onLongClick = onLongClick
            ),
    ) {
        Artwork(song.artworkUri, Modifier.size(148.dp), RoundedCornerShape(16.dp))
        Spacer(Modifier.height(8.dp))
        Text(song.displayTitle, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            song.displayArtist,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AlbumCard(album: Album, onClick: () -> Unit, modifier: Modifier = Modifier, width: Dp = 148.dp) {
    Column(
        modifier = modifier
            .width(width)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClickLabel = "Open album ${album.displayTitle}", onClick = onClick)
    ) {
        Artwork(album.artworkUri, Modifier.fillMaxWidth().height(width), RoundedCornerShape(16.dp))
        Spacer(Modifier.height(8.dp))
        Text(album.displayTitle, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            "${album.displayArtist} · ${pluralSongs(album.songCount)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ArtistCircle(artist: Artist, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .width(112.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClickLabel = "Open artist ${artist.displayName}", onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Artwork(artist.artworkUri, Modifier.size(104.dp), CircleShape, Icons.Rounded.Person)
        Spacer(Modifier.height(8.dp))
        Text(
            artist.displayName,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ArtistRow(artist: Artist, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClickLabel = "Open artist ${artist.displayName}", onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Artwork(artist.artworkUri, Modifier.size(52.dp), CircleShape, Icons.Rounded.Person)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(artist.displayName, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                "${if (artist.albumCount == 1) "1 album" else "${artist.albumCount} albums"} · ${pluralSongs(artist.songCount)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PlaylistCard(playlist: Playlist, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .width(148.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClickLabel = "Open playlist ${playlist.name}", onClick = onClick)
    ) {
        Box(
            Modifier
                .size(148.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.QueueMusic, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(56.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(playlist.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            pluralSongs(playlist.songCount),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** A titled, horizontally scrolling row; renders nothing for an empty list. */
fun <T> LazyListScope.carousel(
    title: String,
    items: List<T>,
    key: (T) -> Any,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    itemContent: @Composable (index: Int, item: T) -> Unit
) {
    if (items.isEmpty()) return
    item(key = "header-$title", contentType = "header") { SectionHeader(title, actionLabel = actionLabel, onAction = onAction) }
    item(key = "row-$title", contentType = "carousel") {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(items.size, key = { key(items[it]) }) { index -> itemContent(index, items[index]) }
        }
    }
}
