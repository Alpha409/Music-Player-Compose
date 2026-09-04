package com.example.musicplayercompose.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicplayercompose.R
import com.example.musicplayercompose.ui.theme.AccentPurple
import com.example.musicplayercompose.ui.theme.DarkSurfaceVariant
import com.example.musicplayercompose.ui.theme.TextMuted
import com.example.musicplayercompose.ui.theme.TextPrimary
import com.example.musicplayercompose.ui.theme.TextSecondary

@Composable
fun SearchBarComponent(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholderText: String = "Search tracks, artists, albums...",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = {
            Text(
                text = placeholderText,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = TextMuted
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.searchicon),
                contentDescription = "Search",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        painter = painterResource(id = R.drawable.playback),
                        contentDescription = "Clear",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = DarkSurfaceVariant,
            unfocusedContainerColor = DarkSurfaceVariant,
            focusedBorderColor = AccentPurple,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = AccentPurple
        ),
        singleLine = true
    )
}
