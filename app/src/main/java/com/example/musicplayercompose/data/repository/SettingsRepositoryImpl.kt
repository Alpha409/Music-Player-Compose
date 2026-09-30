package com.example.musicplayercompose.data.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.musicplayercompose.domain.model.AppSettings
import com.example.musicplayercompose.domain.model.SavedPlaybackState
import com.example.musicplayercompose.domain.model.SortOrder
import com.example.musicplayercompose.domain.model.ThemeMode
import com.example.musicplayercompose.domain.repository.PlaybackStateRepository
import com.example.musicplayercompose.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsStore by preferencesDataStore("settings")
private val Context.playbackStore by preferencesDataStore("playback_state")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private val store get() = context.settingsStore

    override val settings: Flow<AppSettings> = store.data.map { p ->
        val defaults = AppSettings()
        AppSettings(
            themeMode = p[THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: defaults.themeMode,
            dynamicColor = p[DYNAMIC] ?: defaults.dynamicColor,
            resumePlayback = p[RESUME] ?: defaults.resumePlayback,
            autoPlayOnResume = p[AUTO_PLAY] ?: defaults.autoPlayOnResume,
            rememberShuffleRepeat = p[REMEMBER_MODES] ?: defaults.rememberShuffleRepeat,
            skipDurationSec = p[SKIP] ?: defaults.skipDurationSec,
            sortOrder = p[SORT]?.let { runCatching { SortOrder.valueOf(it) }.getOrNull() } ?: defaults.sortOrder,
            excludedFolders = p[EXCLUDED] ?: defaults.excludedFolders,
            pauseOnHeadsetDisconnect = p[NOISY] ?: defaults.pauseOnHeadsetDisconnect,
            handleAudioFocus = p[FOCUS] ?: defaults.handleAudioFocus
        )
    }

    private suspend fun <T> set(key: Preferences.Key<T>, value: T) {
        store.edit { it[key] = value }
    }

    override suspend fun setThemeMode(mode: ThemeMode) = set(THEME, mode.name)
    override suspend fun setDynamicColor(enabled: Boolean) = set(DYNAMIC, enabled)
    override suspend fun setResumePlayback(enabled: Boolean) = set(RESUME, enabled)
    override suspend fun setAutoPlayOnResume(enabled: Boolean) = set(AUTO_PLAY, enabled)
    override suspend fun setRememberShuffleRepeat(enabled: Boolean) = set(REMEMBER_MODES, enabled)
    override suspend fun setSkipDuration(seconds: Int) = set(SKIP, seconds)
    override suspend fun setSortOrder(order: SortOrder) = set(SORT, order.name)
    override suspend fun setExcludedFolders(folders: Set<String>) = set(EXCLUDED, folders)
    override suspend fun setPauseOnHeadsetDisconnect(enabled: Boolean) = set(NOISY, enabled)
    override suspend fun setHandleAudioFocus(enabled: Boolean) = set(FOCUS, enabled)

    private companion object {
        val THEME = stringPreferencesKey("theme_mode")
        val DYNAMIC = booleanPreferencesKey("dynamic_color")
        val RESUME = booleanPreferencesKey("resume_playback")
        val AUTO_PLAY = booleanPreferencesKey("auto_play_on_resume")
        val REMEMBER_MODES = booleanPreferencesKey("remember_shuffle_repeat")
        val SKIP = intPreferencesKey("skip_duration_sec")
        val SORT = stringPreferencesKey("sort_order")
        val EXCLUDED = stringSetPreferencesKey("excluded_folders")
        val NOISY = booleanPreferencesKey("pause_on_headset_disconnect")
        val FOCUS = booleanPreferencesKey("handle_audio_focus")
    }
}

@Singleton
class PlaybackStateRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PlaybackStateRepository {

    override suspend fun save(state: SavedPlaybackState) {
        context.playbackStore.edit {
            it[QUEUE] = state.queueIds.joinToString(",")
            it[INDEX] = state.index
            it[POSITION] = state.positionMs
            it[SHUFFLE] = state.shuffle
            it[REPEAT] = state.repeatMode
        }
    }

    override suspend fun load(): SavedPlaybackState? {
        val p = context.playbackStore.data.first()
        val ids = p[QUEUE]?.split(',')?.mapNotNull { it.toLongOrNull() }.orEmpty()
        if (ids.isEmpty()) return null
        return SavedPlaybackState(
            queueIds = ids,
            index = (p[INDEX] ?: 0).coerceIn(0, ids.lastIndex),
            positionMs = p[POSITION] ?: 0L,
            shuffle = p[SHUFFLE] ?: false,
            repeatMode = p[REPEAT] ?: 0
        )
    }

    private companion object {
        val QUEUE = stringPreferencesKey("queue_ids")
        val INDEX = intPreferencesKey("index")
        val POSITION = longPreferencesKey("position_ms")
        val SHUFFLE = booleanPreferencesKey("shuffle")
        val REPEAT = intPreferencesKey("repeat_mode")
    }
}
