package dev.hansffu.ontime.ui.stoplist.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import dev.hansffu.ontime.R
import dev.hansffu.ontime.model.Stop
import dev.hansffu.ontime.ui.components.RefreshOnResume
import dev.hansffu.ontime.ui.components.listHeaderItem
import dev.hansffu.ontime.ui.components.messageItem
import dev.hansffu.ontime.ui.components.retryItem
import dev.hansffu.ontime.ui.components.stopListSection

@Composable
fun SearchScreen(
    searchString: String,
    onStopSelected: (Stop) -> Unit,
    searchViewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState = searchViewModel.uiState.collectAsStateWithLifecycle().value
    RefreshOnResume { searchViewModel.search(searchString) }
    val columnState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    val header = stringResource(R.string.search_results)
    val loading = stringResource(R.string.loading_search)
    val error = stringResource(R.string.search_error)
    val retry = stringResource(R.string.retry)
    val empty =
        if (uiState is SearchUiState.Content) {
            stringResource(R.string.no_search_results, uiState.query)
        } else {
            ""
        }

    LaunchedEffect(searchString) {
        searchViewModel.search(searchString)
    }

    ScreenScaffold(scrollState = columnState) { contentPadding ->
        TransformingLazyColumn(state = columnState, contentPadding = contentPadding) {
            when (uiState) {
                SearchUiState.Idle,
                is SearchUiState.Loading,
                -> {
                    listHeaderItem("search-header", header, transformationSpec)
                    messageItem("search-loading", loading, transformationSpec)
                }

                is SearchUiState.Error -> {
                    listHeaderItem("search-header", header, transformationSpec)
                    retryItem(
                        "search-error",
                        error,
                        retry,
                        transformationSpec,
                        onRetry = { searchViewModel.search(uiState.query) },
                    )
                }

                is SearchUiState.Content -> {
                    if (uiState.stops.isEmpty()) {
                        listHeaderItem("search-header", header, transformationSpec)
                        messageItem("search-empty", empty, transformationSpec)
                    } else {
                        stopListSection(
                            headerKey = "search-results",
                            header = header,
                            stops = uiState.stops,
                            transformationSpec = transformationSpec,
                            onStopClick = onStopSelected,
                        )
                    }
                }
            }
        }
    }
}
