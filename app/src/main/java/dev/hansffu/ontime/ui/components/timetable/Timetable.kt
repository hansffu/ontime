package dev.hansffu.ontime.ui.components.timetable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnScope
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import dev.hansffu.ontime.model.LineDeparture
import dev.hansffu.ontime.model.LineDirectionRef
import dev.hansffu.ontime.model.withUpcomingDepartures
import dev.hansffu.ontime.ui.components.listHeaderItem
import dev.hansffu.ontime.ui.components.messageItem
import dev.hansffu.ontime.ui.components.retryItem
import dev.hansffu.ontime.viewmodels.TimetableUiState
import java.time.OffsetDateTime

fun TransformingLazyColumnScope.Timetable(
    uiState: TimetableUiState,
    strings: TimetableStrings,
    now: OffsetDateTime,
    transformationSpec: TransformationSpec,
    toggleFavoriteDeparture: (LineDirectionRef) -> Unit,
    manageBoards: (LineDirectionRef) -> Unit,
    retry: () -> Unit,
) {
    listHeaderItem("stop-header", uiState.stop.name, transformationSpec)

    when (uiState) {
        is TimetableUiState.Loading ->
            messageItem("departures-loading", strings.loading, transformationSpec)
        is TimetableUiState.Error ->
            retryItem(
                "departures-error",
                strings.error,
                strings.retry,
                transformationSpec,
                retry,
            )
        is TimetableUiState.Success -> {
            val favoriteDepartures = uiState.favoriteDepartures.upcoming(now)
            val otherDepartures = uiState.otherDepartures.upcoming(now)
            if (uiState.refreshing) {
                messageItem("departures-refreshing", strings.loading, transformationSpec)
            } else if (uiState.refreshFailed) {
                retryItem(
                    "departures-refresh-error",
                    strings.refreshFailed,
                    strings.retry,
                    transformationSpec,
                    retry,
                )
            }
            if (favoriteDepartures.isNotEmpty()) {
                listHeaderItem(
                    "favorite-departures-header",
                    strings.favoriteDeparturesHeader,
                    transformationSpec,
                )
                items(
                    items = favoriteDepartures,
                    key = {
                        "favorite-" + it.lineDirectionRef.lineRef + "-" +
                            it.lineDirectionRef.destinationRef
                    },
                ) { line ->
                    DepartureItem(
                        line,
                        true,
                        now,
                        transformationSpec,
                        toggleFavoriteDeparture,
                        manageBoards,
                    )
                }
            }
            if (otherDepartures.isNotEmpty()) {
                if (favoriteDepartures.isNotEmpty()) {
                    listHeaderItem(
                        "other-departures-header",
                        strings.otherDeparturesHeader,
                        transformationSpec,
                    )
                }
                items(
                    items = otherDepartures,
                    key = {
                        "other-" + it.lineDirectionRef.lineRef + "-" +
                            it.lineDirectionRef.destinationRef
                    },
                ) { line ->
                    DepartureItem(
                        line,
                        false,
                        now,
                        transformationSpec,
                        toggleFavoriteDeparture,
                        manageBoards,
                    )
                }
            }
            if (favoriteDepartures.isEmpty() && otherDepartures.isEmpty()) {
                messageItem("departures-empty", strings.empty, transformationSpec)
            }
        }
    }
}

private fun List<LineDeparture>.upcoming(now: OffsetDateTime): List<LineDeparture> =
    mapNotNull { it.withUpcomingDepartures(now) }
        .sortedBy { it.departures.first().expectedArrivalTime }

@Composable
private fun TransformingLazyColumnItemScope.DepartureItem(
    line: LineDeparture,
    favorite: Boolean,
    now: OffsetDateTime,
    transformationSpec: TransformationSpec,
    toggleFavorite: (LineDirectionRef) -> Unit,
    manageBoards: (LineDirectionRef) -> Unit,
) {
    LineDepartureCard(
        lineDirectionRef = line.lineDirectionRef,
        departureTimes = line.departures.map { it.expectedArrivalTime },
        isFavorite = favorite,
        toggleFavorite = toggleFavorite,
        color = line.color,
        now = now,
        manageBoards = { manageBoards(line.lineDirectionRef) },
        transformation = SurfaceTransformation(transformationSpec),
        modifier =
            Modifier.fillMaxWidth()
                .minimumVerticalContentPadding(
                    ButtonDefaults.minimumVerticalListContentPadding
                )
                .transformedHeight(this, transformationSpec),
    )
}

data class TimetableStrings(
    val loading: String,
    val error: String,
    val empty: String,
    val refreshFailed: String,
    val retry: String,
    val favoriteDeparturesHeader: String,
    val otherDeparturesHeader: String,
)
