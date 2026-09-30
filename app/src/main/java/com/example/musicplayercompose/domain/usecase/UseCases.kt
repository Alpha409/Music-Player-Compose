package com.example.musicplayercompose.domain.usecase

import com.example.musicplayercompose.domain.model.SearchResults
import com.example.musicplayercompose.domain.repository.MusicRepository
import com.example.musicplayercompose.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** Trims and collapses whitespace; LIKE wildcards are escaped by the repository. */
fun sanitizeSearchQuery(raw: String): String = raw.trim().replace(Regex("\\s+"), " ")

class SearchLibraryUseCase @Inject constructor(private val music: MusicRepository) {
    suspend operator fun invoke(rawQuery: String): SearchResults {
        val query = sanitizeSearchQuery(rawQuery)
        return if (query.isEmpty()) SearchResults() else music.search(query)
    }
}

class SyncLibraryUseCase @Inject constructor(
    private val music: MusicRepository,
    private val settings: SettingsRepository
) {
    suspend operator fun invoke(): Boolean = music.syncLibrary(settings.settings.first().excludedFolders)
}

class ToggleFavoriteUseCase @Inject constructor(private val music: MusicRepository) {
    suspend operator fun invoke(songId: Long) = music.toggleFavorite(songId)
}

class RecordPlayUseCase @Inject constructor(private val music: MusicRepository) {
    suspend operator fun invoke(songId: Long) = music.recordPlay(songId)
}
