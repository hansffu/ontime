package dev.hansffu.ontime.ui.stoplist

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.ButtonGroup
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.FilledTonalIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.tooling.preview.devices.WearDevices
import dev.hansffu.ontime.R
import dev.hansffu.ontime.model.Stop
import dev.hansffu.ontime.ui.components.RefreshOnResume
import dev.hansffu.ontime.ui.components.SearchButton
import dev.hansffu.ontime.ui.components.listHeaderItem
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
    onBoardSelected: (Long) -> Unit,
    onManageBoards: () -> Unit,
    favoritesViewModel: FavoritesViewModel = hiltViewModel(),
    nearbyViewModel: NearbyViewModel = hiltViewModel(),
) {
    val favorites = favoritesViewModel.favoriteStops.collectAsStateWithLifecycle().value
    val suggestedBoards = favoritesViewModel.suggestedBoards.collectAsStateWithLifecycle().value
    val nearbyStopState = nearbyViewModel.nearbyStopState.collectAsStateWithLifecycle().value
    RefreshOnResume {
        favoritesViewModel.refresh()
        nearbyViewModel.refresh()
    }
    val columnState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    val suggestedBoardsLabel = stringResource(R.string.suggested_boards)
    val favoritesTipsLabel = stringResource(R.string.favorites_tips)
    val favoritesHeaderLabel = stringResource(R.string.favorites_header)
    val loadingNearbyLabel = stringResource(R.string.loading_nearby)
    val nearbyErrorLabel = stringResource(R.string.nearby_error)
    val retryLabel = stringResource(R.string.retry)
    val noStopsLabel = stringResource(R.string.no_stops_found)
    val nearbyHeaderLabel = stringResource(R.string.nearby_header)

    ScreenScaffold(
        scrollState = columnState,
        edgeButton = {
            EdgeButton(onClick = onManageBoards) {
                Icon(Icons.AutoMirrored.Filled.List, stringResource(R.string.manage_boards))
            }
        },
    ) { contentPadding ->
        TransformingLazyColumn(state = columnState, contentPadding = contentPadding) {
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

            if (suggestedBoards.isNotEmpty()) {
                listHeaderItem(
                    "suggested-boards-header",
                    suggestedBoardsLabel,
                    transformationSpec,
                )
                items(suggestedBoards, key = { it.board.id }) { suggested ->
                    Button(
                        modifier =
                            Modifier.fillMaxWidth()
                                .minimumVerticalContentPadding(
                                    ButtonDefaults.minimumVerticalListContentPadding
                                )
                                .transformedHeight(this, transformationSpec),
                        onClick = { onBoardSelected(suggested.board.id) },
                        transformation = SurfaceTransformation(transformationSpec),
                        label = { Text(suggested.board.name) },
                        secondaryLabel =
                            suggested.distanceMeters?.let { meters ->
                                {
                                    Text(
                                        stringResource(
                                            R.string.distance_away,
                                            meters / 1_000.0,
                                        )
                                    )
                                }
                            },
                    )
                }
            }

            if (favorites.isEmpty()) {
                messageItem(
                    "empty-favorites",
                    favoritesTipsLabel,
                    transformationSpec,
                )
            } else {
                stopListSection(
                    headerKey = "favorites",
                    header = favoritesHeaderLabel,
                    stops = favorites,
                    transformationSpec = transformationSpec,
                    onStopClick = onStopSelected,
                )
            }

            when (nearbyStopState) {
                NearbyStopState.Loading ->
                    messageItem(
                        "nearby-loading",
                        loadingNearbyLabel,
                        transformationSpec,
                    )
                NearbyStopState.NoPermission -> Unit
                NearbyStopState.Error ->
                    retryItem(
                        "nearby-error",
                        nearbyErrorLabel,
                        retryLabel,
                        transformationSpec,
                        nearbyViewModel::refresh,
                    )
                is NearbyStopState.Content -> {
                    if (nearbyStopState.refreshFailed) {
                        retryItem(
                            "nearby-refresh-error",
                            nearbyErrorLabel,
                            retryLabel,
                            transformationSpec,
                            nearbyViewModel::refresh,
                        )
                    }
                    if (nearbyStopState.stops.isEmpty()) {
                        messageItem(
                            "nearby-empty",
                            noStopsLabel,
                            transformationSpec,
                        )
                    } else {
                        stopListSection(
                            headerKey = "nearby",
                            header = nearbyHeaderLabel,
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
                        modifier =
                            Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                    )
                }
            }
        }
    }
}
