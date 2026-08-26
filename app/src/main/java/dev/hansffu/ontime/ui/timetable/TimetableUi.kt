package dev.hansffu.ontime.ui.timetable

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Box
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
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import dev.hansffu.ontime.R
import dev.hansffu.ontime.ui.components.timetable.Timetable
import dev.hansffu.ontime.ui.components.timetable.TimetableStrings
import dev.hansffu.ontime.viewmodels.TimetableViewModel
import java.time.OffsetDateTime
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableUi(
    timetableViewModel: TimetableViewModel = hiltViewModel(),
) {
    val uiState by timetableViewModel.uiState.collectAsStateWithLifecycle()
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
            favoriteStop = stringResource(R.string.favorite_stop),
            addStopFavorite = stringResource(R.string.add_stop_favorite),
            removeStopFavorite = stringResource(R.string.remove_stop_favorite),
        )
    var now by remember { mutableStateOf(OffsetDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            now = OffsetDateTime.now()
        }
    }

    val pullToRefreshState = rememberPullToRefreshState()
    Box(
        modifier =
            Modifier.pullToRefresh(
                isRefreshing = uiState.refreshing,
                state = pullToRefreshState,
                threshold = 40.dp,
                onRefresh = timetableViewModel::loadDepartures,
            ),
    ) {
        ScreenScaffold(scrollState = columnState) { contentPadding ->
            TransformingLazyColumn(state = columnState, contentPadding = contentPadding) {
                Timetable(
                    uiState = uiState,
                    strings = strings,
                    now = now,
                    transformationSpec = transformationSpec,
                    toggleFavoriteStop = timetableViewModel::toggleFavoriteStop,
                    toggleFavoriteDeparture = timetableViewModel::toggleFavoriteDeparture,
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
