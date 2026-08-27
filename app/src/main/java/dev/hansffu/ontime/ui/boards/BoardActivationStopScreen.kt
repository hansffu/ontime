package dev.hansffu.ontime.ui.boards

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import dev.hansffu.ontime.R
import dev.hansffu.ontime.model.Stop
import dev.hansffu.ontime.ui.components.RefreshOnResume
import dev.hansffu.ontime.ui.components.SearchButton
import dev.hansffu.ontime.ui.components.listHeaderItem
import dev.hansffu.ontime.ui.components.messageItem
import dev.hansffu.ontime.ui.components.retryItem
import dev.hansffu.ontime.ui.components.stopListSection
import dev.hansffu.ontime.ui.stoplist.search.SearchUiState
import dev.hansffu.ontime.ui.stoplist.search.SearchViewModel

@Composable
fun BoardActivationStopScreen(
    onStopSelected: (Stop) -> Unit,
    searchViewModel: SearchViewModel = hiltViewModel(),
) {
    val state = searchViewModel.uiState.collectAsStateWithLifecycle().value
    val columnState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    val chooseStopLabel = stringResource(R.string.choose_activation_stop)
    val loadingLabel = stringResource(R.string.loading_search)
    val errorLabel = stringResource(R.string.search_error)
    val retryLabel = stringResource(R.string.retry)
    val resultsLabel = stringResource(R.string.search_results)
    val emptyLabel = (state as? SearchUiState.Content)?.let {
        stringResource(R.string.no_search_results, it.query)
    }.orEmpty()
    var hasSearched by remember { mutableStateOf(false) }
    RefreshOnResume {
        val query =
            when (val current = state) {
                SearchUiState.Idle -> null
                is SearchUiState.Loading -> current.query
                is SearchUiState.Content -> current.query
                is SearchUiState.Error -> current.query
            }
        if (query != null) searchViewModel.search(query)
    }

    ScreenScaffold(scrollState = columnState) { contentPadding ->
        TransformingLazyColumn(state = columnState, contentPadding = contentPadding) {
            listHeaderItem(
                "activation-search-header",
                chooseStopLabel,
                transformationSpec,
            )
            item("activation-search-button") {
                SearchButton(
                    onSubmit = {
                        hasSearched = true
                        searchViewModel.search(it)
                    },
                    inputLabel = stringResource(R.string.search_for_stops),
                    contentDescription = stringResource(R.string.search_short),
                    modifier =
                        Modifier.fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                )
            }
            when (state) {
                SearchUiState.Idle -> Unit
                is SearchUiState.Loading ->
                    messageItem(
                        "activation-search-loading",
                        loadingLabel,
                        transformationSpec,
                    )
                is SearchUiState.Error ->
                    retryItem(
                        "activation-search-error",
                        errorLabel,
                        retryLabel,
                        transformationSpec,
                    ) { searchViewModel.search(state.query) }
                is SearchUiState.Content ->
                    if (state.stops.isEmpty() && hasSearched) {
                        messageItem(
                            "activation-search-empty",
                            emptyLabel,
                            transformationSpec,
                        )
                    } else {
                        stopListSection(
                            headerKey = "activation-results",
                            header = resultsLabel,
                            stops = state.stops,
                            transformationSpec = transformationSpec,
                            onStopClick = onStopSelected,
                        )
                    }
            }
        }
    }
}
