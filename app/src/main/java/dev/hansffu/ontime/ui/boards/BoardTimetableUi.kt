package dev.hansffu.ontime.ui.boards

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import dev.hansffu.ontime.R
import dev.hansffu.ontime.database.dao.BoardDeparture
import dev.hansffu.ontime.model.BoardDepartureOrdering
import dev.hansffu.ontime.model.withUpcomingDepartures
import dev.hansffu.ontime.ui.components.RefreshOnResume
import dev.hansffu.ontime.ui.components.listHeaderItem
import dev.hansffu.ontime.ui.components.messageItem
import dev.hansffu.ontime.ui.components.retryItem
import dev.hansffu.ontime.ui.components.timetable.LineDepartureCard
import dev.hansffu.ontime.viewmodels.BoardDepartureRow
import dev.hansffu.ontime.viewmodels.BoardTimetableState
import dev.hansffu.ontime.viewmodels.BoardTimetableViewModel
import java.time.OffsetDateTime
import kotlinx.coroutines.delay

@Composable
fun BoardTimetableScreen(
    onManageBoards: (BoardDeparture) -> Unit,
    viewModel: BoardTimetableViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var now by remember { mutableStateOf(OffsetDateTime.now()) }
    RefreshOnResume {
        now = OffsetDateTime.now()
        viewModel.refresh()
    }
    val columnState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    val loadingLabel = stringResource(R.string.loading_departures)
    val errorLabel = stringResource(R.string.departures_error)
    val retryLabel = stringResource(R.string.retry)
    val refreshFailedLabel = stringResource(R.string.refresh_failed)
    val emptyLabel = stringResource(R.string.no_board_departures)
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            now = OffsetDateTime.now()
        }
    }
    val pullState = rememberPullToRefreshState()
    val refreshing =
        uiState == BoardTimetableState.Loading ||
            (uiState as? BoardTimetableState.Content)?.refreshing == true

    Box(
        modifier =
            Modifier.fillMaxSize().pullToRefresh(
                isRefreshing = refreshing,
                state = pullState,
                threshold = 40.dp,
                onRefresh = viewModel::refresh,
            ),
    ) {
        ScreenScaffold(scrollState = columnState) { contentPadding ->
            TransformingLazyColumn(state = columnState, contentPadding = contentPadding) {
                when (val state = uiState) {
                    BoardTimetableState.Loading ->
                        messageItem("board-loading", loadingLabel, transformationSpec)

                    is BoardTimetableState.Error -> {
                        listHeaderItem("board-header", state.boardName, transformationSpec)
                        retryItem(
                            "board-error",
                            errorLabel,
                            retryLabel,
                            transformationSpec,
                            viewModel::refresh,
                        )
                    }

                    is BoardTimetableState.Content -> {
                        val rows =
                            BoardDepartureOrdering.sort(
                                state.rows.mapNotNull { row ->
                                    row.departure.withUpcomingDepartures(now)?.let {
                                        row.copy(departure = it)
                                    }
                                },
                                BoardDepartureRow::distanceMeters,
                            ) { row ->
                                row.departure.departures.minOfOrNull { it.expectedArrivalTime }
                            }
                        listHeaderItem("board-header", state.boardName, transformationSpec)
                        if (state.refreshFailed) {
                            retryItem(
                                "board-refresh-error",
                                refreshFailedLabel,
                                retryLabel,
                                transformationSpec,
                                viewModel::refresh,
                            )
                        }
                        if (rows.isEmpty()) {
                            messageItem("board-empty", emptyLabel, transformationSpec)
                        }
                        items(
                            rows,
                            key = {
                                it.stored.stopId + "-" + it.stored.lineRef + "-" +
                                    it.stored.destinationRef
                            },
                        ) { row ->
                            LineDepartureCard(
                                lineDirectionRef = row.departure.lineDirectionRef,
                                departureTimes =
                                    row.departure.departures.map {
                                        it.expectedArrivalTime
                                    },
                                isFavorite = row.isFavorite,
                                toggleFavorite = { viewModel.toggleFavorite(row) },
                                color = row.departure.color,
                                now = now,
                                stopName = row.stored.stopName,
                                manageBoards = { onManageBoards(row.stored) },
                                transformation = SurfaceTransformation(transformationSpec),
                                modifier =
                                    Modifier.fillMaxWidth()
                                        .minimumVerticalContentPadding(
                                            ButtonDefaults.minimumVerticalListContentPadding
                                        )
                                        .transformedHeight(this, transformationSpec),
                            )
                        }
                    }
                }
            }
        }
        PullToRefreshDefaults.Indicator(
            modifier = Modifier.align(Alignment.TopCenter),
            isRefreshing = refreshing,
            state = pullState,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}
