package dev.hansffu.ontime.ui.timetable

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.AmbientMode
import androidx.wear.compose.foundation.LocalAmbientModeManager
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import dev.hansffu.ontime.R
import dev.hansffu.ontime.model.LineDirectionRef
import dev.hansffu.ontime.model.Stop
import dev.hansffu.ontime.ui.components.RefreshOnResume
import dev.hansffu.ontime.ui.components.timetable.Timetable
import dev.hansffu.ontime.ui.components.timetable.TimetableStrings
import dev.hansffu.ontime.viewmodels.TimetableUiState
import dev.hansffu.ontime.viewmodels.TimetableViewModel
import java.time.OffsetDateTime
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableUi(
    onManageBoards: (Stop, LineDirectionRef) -> Unit,
    timetableViewModel: TimetableViewModel = hiltViewModel(),
) {
    val uiState by timetableViewModel.uiState.collectAsStateWithLifecycle()
    var now by remember { mutableStateOf(OffsetDateTime.now()) }
    RefreshOnResume {
        now = OffsetDateTime.now()
        timetableViewModel.loadDepartures()
    }
    val columnState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    val strings =
        TimetableStrings(
            loading = stringResource(R.string.loading_departures),
            error = stringResource(R.string.departures_error),
            empty = stringResource(R.string.no_departures),
            refreshFailed = stringResource(R.string.refresh_failed),
            retry = stringResource(R.string.retry),
            favoriteDeparturesHeader = stringResource(R.string.favorite_departures_header),
            otherDeparturesHeader = stringResource(R.string.other_departures_header),
        )
    val addStopFavoriteDescription = stringResource(R.string.add_stop_favorite)
    val removeStopFavoriteDescription = stringResource(R.string.remove_stop_favorite)
    val isAmbient = LocalAmbientModeManager.current?.currentAmbientMode is AmbientMode.Ambient
    LaunchedEffect(isAmbient) {
        if (isAmbient) return@LaunchedEffect
        now = OffsetDateTime.now()
        while (true) {
            delay(30_000)
            now = OffsetDateTime.now()
        }
    }

    val pullToRefreshState = rememberPullToRefreshState()
    val edgeButtonOverscrollEffect = rememberOverscrollEffect()
    val successState = uiState as? TimetableUiState.Success
    Box(
        modifier =
            Modifier.fillMaxSize().pullToRefresh(
                isRefreshing = uiState.refreshing,
                state = pullToRefreshState,
                threshold = 40.dp,
                onRefresh = timetableViewModel::loadDepartures,
            ),
    ) {
        ScreenScaffold(
            scrollState = columnState,
            edgeButton = {
                if (successState != null) {
                    EdgeButton(
                        onClick = timetableViewModel::toggleFavoriteStop,
                        modifier =
                            Modifier.scrollable(
                                state = columnState,
                                orientation = Orientation.Vertical,
                                reverseDirection = true,
                                overscrollEffect = edgeButtonOverscrollEffect,
                            ),
                    ) {
                        Icon(
                            imageVector =
                                if (successState.isFavorite) Icons.Filled.Favorite
                                else Icons.Outlined.FavoriteBorder,
                            contentDescription =
                                if (successState.isFavorite) removeStopFavoriteDescription
                                else addStopFavoriteDescription,
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                        )
                    }
                }
            },
        ) { contentPadding ->
            TransformingLazyColumn(state = columnState, contentPadding = contentPadding) {
                Timetable(
                    uiState = uiState,
                    strings = strings,
                    now = now,
                    transformationSpec = transformationSpec,
                    toggleFavoriteDeparture = timetableViewModel::toggleFavoriteDeparture,
                    manageBoards = { onManageBoards(uiState.stop, it) },
                    retry = timetableViewModel::loadDepartures,
                )
            }
        }
        PullToRefreshDefaults.Indicator(
            modifier = Modifier.align(Alignment.TopCenter),
            isRefreshing = uiState.refreshing,
            state = pullToRefreshState,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}
