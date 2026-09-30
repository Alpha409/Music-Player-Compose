package com.example.musicplayercompose.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayercompose.domain.model.AppSettings
import com.example.musicplayercompose.domain.model.SortOrder
import com.example.musicplayercompose.domain.model.ThemeMode
import com.example.musicplayercompose.domain.repository.MusicRepository
import com.example.musicplayercompose.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: SettingsRepository,
    private val music: MusicRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = repo.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    fun setTheme(mode: ThemeMode) = launch { repo.setThemeMode(mode) }
    fun setDynamicColor(v: Boolean) = launch { repo.setDynamicColor(v) }
    fun setResume(v: Boolean) = launch { repo.setResumePlayback(v) }
    fun setAutoPlay(v: Boolean) = launch { repo.setAutoPlayOnResume(v) }
    fun setRememberModes(v: Boolean) = launch { repo.setRememberShuffleRepeat(v) }
    fun setSkip(sec: Int) = launch { repo.setSkipDuration(sec) }
    fun setSort(order: SortOrder) = launch { repo.setSortOrder(order) }
    fun setNoisy(v: Boolean) = launch { repo.setPauseOnHeadsetDisconnect(v) }
    fun setFocus(v: Boolean) = launch { repo.setHandleAudioFocus(v) }
    fun setExcluded(folders: Set<String>) = launch { repo.setExcludedFolders(folders) }

    /** Folders offered in the exclusion picker: everything in the library plus any already excluded. */
    suspend fun candidateFolders(): List<String> =
        (music.libraryFolders() + settings.value.excludedFolders).distinct().sorted()
}
