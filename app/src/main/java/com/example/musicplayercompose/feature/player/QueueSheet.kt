package com.example.musicplayercompose.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.musicplayercompose.core.designsystem.Artwork
import com.example.musicplayercompose.core.designsystem.EmptyState
import com.example.musicplayercompose.feature.main.MainViewModel
import kotlin.math.roundToInt

private val RowHeight = 64.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueSheet(viewModel: MainViewModel) {
    val state by viewModel.playbackState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()
    val rowPx = with(LocalDensity.current) { RowHeight.toPx() }
    var draggingIndex by remember { mutableIntStateOf(-1) }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        if (state.currentIndex > 0) listState.scrollToItem((state.currentIndex - 1).coerceAtLeast(0))
    }

    ModalBottomSheet(onDismissRequest = { viewModel.setQueueVisible(false) }, sheetState = sheetState) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Queue", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Text(
                "${state.queue.size} songs",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = viewModel::clearQueue, enabled = state.queue.isNotEmpty()) { Text("Clear") }
        }
        if (state.queue.isEmpty()) {
            EmptyState(
                Icons.Rounded.QueueMusic,
                "Queue is empty",
                "Add songs from any list with “Add to queue”.",
                Modifier.height(280.dp)
            )
        } else {
            LazyColumn(state = listState, modifier = Modifier.navigationBarsPadding()) {
                itemsIndexed(state.queue, key = { index, item -> "$index-${item.songId}" }) { index, item ->
                    val isCurrent = index == state.currentIndex
                    val isDragging = index == draggingIndex
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(RowHeight)
                            .zIndex(if (isDragging) 1f else 0f)
                            .graphicsLayer { translationY = if (isDragging) dragOffset else 0f }
                            .background(
                                when {
                                    isDragging -> MaterialTheme.colorScheme.surfaceContainerHighest
                                    isCurrent -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                    else -> androidx.compose.ui.graphics.Color.Transparent
                                }
                            )
                            .clickable(onClickLabel = "Play ${item.title}") { viewModel.skipToQueueItem(index) }
                            .padding(start = 24.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Artwork(item.artworkUri, Modifier.size(44.dp), RoundedCornerShape(10.dp))
                        Spacer(Modifier.width(12.dp))
                        Box(Modifier.weight(1f)) {
                            androidx.compose.foundation.layout.Column {
                                Text(
                                    item.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(
                                    item.artist,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.removeFromQueue(index) }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Remove ${item.title} from queue")
                        }
                        Icon(
                            Icons.Rounded.DragHandle,
                            contentDescription = "Drag to reorder ${item.title}",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(48.dp)
                                .padding(12.dp)
                                .pointerInput(index, state.queue.size) {
                                    detectDragGestures(
                                        onDragStart = {
                                            draggingIndex = index
                                            dragOffset = 0f
                                        },
                                        onDrag = { change, amount ->
                                            change.consume()
                                            dragOffset += amount.y
                                        },
                                        onDragEnd = {
                                            val target = (index + (dragOffset / rowPx).roundToInt())
                                                .coerceIn(0, state.queue.lastIndex)
                                            if (target != index) viewModel.moveQueueItem(index, target)
                                            draggingIndex = -1
                                            dragOffset = 0f
                                        },
                                        onDragCancel = {
                                            draggingIndex = -1
                                            dragOffset = 0f
                                        }
                                    )
                                }
                        )
                    }
                }
            }
        }
    }
}


