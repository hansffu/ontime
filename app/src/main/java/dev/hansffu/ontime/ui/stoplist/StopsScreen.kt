package dev.hansffu.ontime.ui.stoplist

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.ButtonGroup
import androidx.wear.compose.material3.FilledTonalIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.tooling.preview.devices.WearDevices
import dev.hansffu.ontime.R
import dev.hansffu.ontime.model.Stop
import dev.hansffu.ontime.ui.components.SearchButton
import dev.hansffu.ontime.ui.components.messageItem
import dev.hansffu.ontime.ui.components.retryItem
import dev.hansffu.ontime.ui.components.stopListSection
import dev.hansffu.ontime.ui.stoplist.nearby.NearbyStopState
import dev.hansffu.ontime.ui.stoplist.nearby.NearbyViewModel
import dev.hansffu.ontime.ui.theme.OntimeTheme
import dev.hansffu.ontime.viewmodels.FavoritesViewModel

@Composable
fun StopsScreen(
    onSearch: (String) -> Unit,
    onNearby: () -> Unit,
    onStopSelected: (Stop) -> Unit,
    favoritesViewModel: FavoritesViewModel = hiltViewModel(),
    nearbyViewModel: NearbyViewModel = hiltViewModel(),
) {
    val favorites = favoritesViewModel.favoriteStops.collectAsStateWithLifecycle().value
    val nearbyStopState = nearbyViewModel.nearbyStopState.collectAsStateWithLifecycle().value
    val columnState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    val favoritesHeader = stringResource(R.string.favorites_header)
    val nearbyHeader = stringResource(R.string.nearby_header)
    val favoritesTips = stringResource(R.string.favorites_tips)
    val loadingNearby = stringResource(R.string.loading_nearby)
    val nearbyError = stringResource(R.string.nearby_error)
    val retry = stringResource(R.string.retry)
    val noStopsFound = stringResource(R.string.no_stops_found)

    ScreenScaffold(scrollState = columnState) { contentPadding ->
        TransformingLazyColumn(
            state = columnState,
            contentPadding = contentPadding,
        ) {
            item(key = "search-actions") {
                SearchButtons(
                    onSearch = onSearch,
                    onNearby = onNearby,
                    transformation = SurfaceTransformation(transformationSpec),
                    modifier =
                        Modifier.fillMaxWidth()
                            .minimumVerticalContentPadding(
                                ButtonDefaults.minimumVerticalListContentPadding
                            )
                            .transformedHeight(this, transformationSpec),
                )
            }

            if (favorites.isEmpty()) {
                messageItem("empty-favorites", favoritesTips, transformationSpec)
            } else {
                stopListSection(
                    headerKey = "favorites",
                    header = favoritesHeader,
                    stops = favorites,
                    transformationSpec = transformationSpec,
                    onStopClick = onStopSelected,
                )
            }

            when (nearbyStopState) {
                NearbyStopState.Loading ->
                    messageItem("nearby-loading", loadingNearby, transformationSpec)

                NearbyStopState.NoPermission -> Unit

                NearbyStopState.Error ->
                    retryItem(
                        "nearby-error",
                        nearbyError,
                        retry,
                        transformationSpec,
                        nearbyViewModel::refresh,
                    )

                is NearbyStopState.Content -> {
                    if (nearbyStopState.refreshFailed) {
                        retryItem(
                            "nearby-refresh-error",
                            nearbyError,
                            retry,
                            transformationSpec,
                            nearbyViewModel::refresh,
                        )
                    }
                    if (nearbyStopState.stops.isEmpty()) {
                        messageItem("nearby-empty", noStopsFound, transformationSpec)
                    } else {
                        stopListSection(
                            headerKey = "nearby",
                            header = nearbyHeader,
                            stops = nearbyStopState.stops.take(3),
                            transformationSpec = transformationSpec,
                            onStopClick = onStopSelected,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchButtons(
    onSearch: (String) -> Unit,
    onNearby: () -> Unit,
    modifier: Modifier = Modifier,
    transformation: SurfaceTransformation? = null,
) {
    val searchInteraction = remember { MutableInteractionSource() }
    val nearbyInteraction = remember { MutableInteractionSource() }

    ButtonGroup(modifier = modifier, transformation = transformation) {
        SearchButton(
            onSubmit = onSearch,
            inputLabel = stringResource(R.string.search_for_stops),
            contentDescription = stringResource(R.string.search_short),
            interactionSource = searchInteraction,
            modifier = Modifier.weight(1f).animateWidth(searchInteraction),
        )
        FilledTonalIconButton(
            onClick = onNearby,
            interactionSource = nearbyInteraction,
            modifier = Modifier.weight(1f).animateWidth(nearbyInteraction),
        ) {
            Icon(
                imageVector = Icons.Default.NearMe,
                contentDescription = stringResource(R.string.nearby_short),
            )
        }
    }
}

@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun SearchButtonsPreview() {
    OntimeTheme {
        val columnState = rememberTransformingLazyColumnState()
        val transformationSpec = rememberTransformationSpec()
        ScreenScaffold(scrollState = columnState) { contentPadding ->
            TransformingLazyColumn(state = columnState, contentPadding = contentPadding) {
                item {
                    SearchButtons(
                        onSearch = {},
                        onNearby = {},
                        transformation = SurfaceTransformation(transformationSpec),
                        modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                    )
                }
            }
        }
    }
}
