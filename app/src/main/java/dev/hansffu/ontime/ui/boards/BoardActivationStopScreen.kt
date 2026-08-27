package dev.hansffu.ontime.ui.boards

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import dev.hansffu.ontime.R
import dev.hansffu.ontime.model.Stop
import dev.hansffu.ontime.ui.components.RefreshOnResume
import dev.hansffu.ontime.ui.components.listHeaderItem
import dev.hansffu.ontime.ui.components.messageItem
import dev.hansffu.ontime.ui.components.retryItem
import dev.hansffu.ontime.ui.components.stopListSection
import dev.hansffu.ontime.ui.stoplist.SearchButtons
import dev.hansffu.ontime.ui.stoplist.nearby.LocationPermissionPrompt
import dev.hansffu.ontime.ui.stoplist.nearby.NearbyStopState
import dev.hansffu.ontime.ui.stoplist.nearby.NearbyViewModel
import dev.hansffu.ontime.ui.stoplist.search.SearchUiState
import dev.hansffu.ontime.ui.stoplist.search.SearchViewModel
import dev.hansffu.ontime.viewmodels.FavoritesViewModel

@Composable
fun BoardActivationStopScreen(
    onStopSelected: (Stop) -> Unit,
    selectionFailed: Boolean,
    searchViewModel: SearchViewModel = hiltViewModel(),
    favoritesViewModel: FavoritesViewModel = hiltViewModel(),
    nearbyViewModel: NearbyViewModel = hiltViewModel(),
) {
    val searchState = searchViewModel.uiState.collectAsStateWithLifecycle().value
    val favorites = favoritesViewModel.favoriteStops.collectAsStateWithLifecycle().value
    val nearbyState = nearbyViewModel.nearbyStopState.collectAsStateWithLifecycle().value
    var mode by rememberSaveable { mutableStateOf(ActivationStopMode.Overview) }
    val columnState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()

    val chooseStopLabel = stringResource(R.string.choose_activation_stop)
    val favoritesLabel = stringResource(R.string.favorites_header)
    val favoritesTipsLabel = stringResource(R.string.favorites_tips)
    val nearbyLabel = stringResource(R.string.nearby_header)
    val loadingNearbyLabel = stringResource(R.string.loading_nearby)
    val nearbyErrorLabel = stringResource(R.string.nearby_error)
    val noNearbyLabel = stringResource(R.string.no_stops_found)
    val loadingSearchLabel = stringResource(R.string.loading_search)
    val searchErrorLabel = stringResource(R.string.search_error)
    val selectionErrorLabel = stringResource(R.string.activation_stop_error)
    val retryLabel = stringResource(R.string.retry)
    val searchResultsLabel = stringResource(R.string.search_results)
    val emptySearchLabel =
        (searchState as? SearchUiState.Content)?.let {
            stringResource(R.string.no_search_results, it.query)
        }.orEmpty()

    RefreshOnResume {
        favoritesViewModel.refresh()
        nearbyViewModel.refresh()
        if (mode == ActivationStopMode.Search) {
            val query =
                when (val current = searchState) {
                    SearchUiState.Idle -> null
                    is SearchUiState.Loading -> current.query
                    is SearchUiState.Content -> current.query
                    is SearchUiState.Error -> current.query
                }
            if (query != null) searchViewModel.search(query)
        }
    }

    ScreenScaffold(scrollState = columnState) { contentPadding ->
        TransformingLazyColumn(state = columnState, contentPadding = contentPadding) {
            listHeaderItem("activation-stop-header", chooseStopLabel, transformationSpec)
            item("activation-stop-actions") {
                SearchButtons(
                    onSearch = {
                        mode = ActivationStopMode.Search
                        searchViewModel.search(it)
                    },
                    onNearby = {
                        mode = ActivationStopMode.Nearby
                        nearbyViewModel.refresh()
                    },
                    transformation = SurfaceTransformation(transformationSpec),
                    modifier =
                        Modifier.fillMaxWidth()
                            .minimumVerticalContentPadding(
                                ButtonDefaults.minimumVerticalListContentPadding
                            )
                            .transformedHeight(this, transformationSpec),
                )
            }
            if (selectionFailed) {
                messageItem(
                    "activation-selection-error",
                    selectionErrorLabel,
                    transformationSpec,
                )
            }

            when (mode) {
                ActivationStopMode.Overview -> {
                    if (favorites.isEmpty()) {
                        messageItem(
                            "activation-empty-favorites",
                            favoritesTipsLabel,
                            transformationSpec,
                        )
                    } else {
                        stopListSection(
                            headerKey = "activation-favorites",
                            header = favoritesLabel,
                            stops = favorites,
                            transformationSpec = transformationSpec,
                            onStopClick = onStopSelected,
                        )
                    }

                    when (nearbyState) {
                        NearbyStopState.Loading ->
                            messageItem(
                                "activation-nearby-loading",
                                loadingNearbyLabel,
                                transformationSpec,
                            )
                        NearbyStopState.NoPermission -> Unit
                        NearbyStopState.Error ->
                            retryItem(
                                "activation-nearby-error",
                                nearbyErrorLabel,
                                retryLabel,
                                transformationSpec,
                                nearbyViewModel::refresh,
                            )
                        is NearbyStopState.Content -> {
                            if (nearbyState.stops.isEmpty()) {
                                messageItem(
                                    "activation-nearby-empty",
                                    noNearbyLabel,
                                    transformationSpec,
                                )
                            } else {
                                stopListSection(
                                    headerKey = "activation-nearby",
                                    header = nearbyLabel,
                                    stops = nearbyState.stops.take(3),
                                    transformationSpec = transformationSpec,
                                    onStopClick = onStopSelected,
                                )
                            }
                        }
                    }
                }

                ActivationStopMode.Search ->
                    when (searchState) {
                        SearchUiState.Idle,
                        is SearchUiState.Loading,
                        -> messageItem(
                            "activation-search-loading",
                            loadingSearchLabel,
                            transformationSpec,
                        )
                        is SearchUiState.Error ->
                            retryItem(
                                "activation-search-error",
                                searchErrorLabel,
                                retryLabel,
                                transformationSpec,
                            ) { searchViewModel.search(searchState.query) }
                        is SearchUiState.Content ->
                            if (searchState.stops.isEmpty()) {
                                messageItem(
                                    "activation-search-empty",
                                    emptySearchLabel,
                                    transformationSpec,
                                )
                            } else {
                                stopListSection(
                                    headerKey = "activation-search-results",
                                    header = searchResultsLabel,
                                    stops = searchState.stops,
                                    transformationSpec = transformationSpec,
                                    onStopClick = onStopSelected,
                                )
                            }
                    }

                ActivationStopMode.Nearby ->
                    when (nearbyState) {
                        NearbyStopState.Loading -> {
                            listHeaderItem(
                                "activation-nearby-header",
                                nearbyLabel,
                                transformationSpec,
                            )
                            messageItem(
                                "activation-nearby-loading",
                                loadingNearbyLabel,
                                transformationSpec,
                            )
                        }
                        NearbyStopState.NoPermission -> Unit
                        NearbyStopState.Error -> {
                            listHeaderItem(
                                "activation-nearby-header",
                                nearbyLabel,
                                transformationSpec,
                            )
                            retryItem(
                                "activation-nearby-error",
                                nearbyErrorLabel,
                                retryLabel,
                                transformationSpec,
                                nearbyViewModel::refresh,
                            )
                        }
                        is NearbyStopState.Content ->
                            if (nearbyState.stops.isEmpty()) {
                                listHeaderItem(
                                    "activation-nearby-header",
                                    nearbyLabel,
                                    transformationSpec,
                                )
                                messageItem(
                                    "activation-nearby-empty",
                                    noNearbyLabel,
                                    transformationSpec,
                                )
                            } else {
                                stopListSection(
                                    headerKey = "activation-nearby-all",
                                    header = nearbyLabel,
                                    stops = nearbyState.stops,
                                    transformationSpec = transformationSpec,
                                    onStopClick = onStopSelected,
                                )
                            }
                    }
            }
        }
    }

    if (mode == ActivationStopMode.Nearby && nearbyState == NearbyStopState.NoPermission) {
        LocationPermissionPrompt(
            permissions = nearbyViewModel.locationPermissions,
            onPermissionAvailable = nearbyViewModel::refresh,
            onDismiss = { mode = ActivationStopMode.Overview },
        )
    }
}

private enum class ActivationStopMode {
    Overview,
    Search,
    Nearby,
}
