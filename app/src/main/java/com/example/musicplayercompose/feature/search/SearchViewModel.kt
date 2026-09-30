package com.example.musicplayercompose.feature.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayercompose.domain.model.SearchResults
import com.example.musicplayercompose.domain.usecase.SearchLibraryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    search: SearchLibraryUseCase
) : ViewModel() {

    val query: StateFlow<String> = savedState.getStateFlow(KEY_QUERY, "")

    // Queries hit Room (LIKE with indexes on the grouped columns), so typing stays responsive on big libraries.
    val results: StateFlow<SearchResults> = query
        .debounce(200)
        .mapLatest { search(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchResults())

    fun onQueryChange(value: String) {
        savedState[KEY_QUERY] = value
    }

    private companion object {
        const val KEY_QUERY = "query"
    }
}
