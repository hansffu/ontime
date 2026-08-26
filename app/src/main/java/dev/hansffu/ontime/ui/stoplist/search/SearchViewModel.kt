package dev.hansffu.ontime.ui.stoplist.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.hansffu.ontime.model.Stop
import dev.hansffu.ontime.service.SearchService
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data class Loading(val query: String) : SearchUiState
    data class Content(val query: String, val stops: List<Stop>) : SearchUiState
    data class Error(val query: String) : SearchUiState
}

@HiltViewModel
class SearchViewModel @Inject constructor(private val searchService: SearchService) : ViewModel() {
    private val mutableUiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = mutableUiState.asStateFlow()

    private var searchJob: Job? = null

    fun search(searchString: String) {
        val query = searchString.trim()
        searchJob?.cancel()
        searchJob =
            viewModelScope.launch {
                if (query.isEmpty()) {
                    mutableUiState.value = SearchUiState.Content(query, emptyList())
                    return@launch
                }

                mutableUiState.value = SearchUiState.Loading(query)
                try {
                    mutableUiState.value = SearchUiState.Content(query, searchService.search(query))
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (_: Exception) {
                    mutableUiState.value = SearchUiState.Error(query)
                }
            }
    }
}
