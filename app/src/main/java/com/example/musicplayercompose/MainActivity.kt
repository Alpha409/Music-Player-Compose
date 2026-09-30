package com.example.musicplayercompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.musicplayercompose.core.designsystem.MusicPlayerTheme
import com.example.musicplayercompose.feature.main.MainScreen
import com.example.musicplayercompose.feature.main.MainViewModel
import com.example.musicplayercompose.feature.main.PermissionGate
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settings.collectAsState()
            MusicPlayerTheme(
                themeMode = settings?.themeMode ?: com.example.musicplayercompose.domain.model.ThemeMode.SYSTEM,
                dynamicColor = settings?.dynamicColor ?: true
            ) {
                PermissionGate(onGranted = viewModel::startLibrarySync) {
                    MainScreen(viewModel)
                }
            }
        }
    }
}
