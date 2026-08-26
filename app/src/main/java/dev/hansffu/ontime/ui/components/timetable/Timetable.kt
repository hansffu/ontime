package dev.hansffu.ontime.ui.components.timetable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnScope
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import dev.hansffu.ontime.model.LineDirectionRef
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
    retry: () -> Unit,
) {
    listHeaderItem("stop-header", uiState.stopName, transformationSpec)

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

            if (uiState.favoriteDepartures.isNotEmpty()) {
                listHeaderItem(
                    "favorite-departures-header",
                    strings.favoriteDeparturesHeader,
                    transformationSpec,
                )
                items(
                    items = uiState.favoriteDepartures,
                    key = { departure -> "favorite-${departure.lineDirectionRef.lineRef}-${departure.lineDirectionRef.destinationRef}" },
                ) { lineDeparture ->
                    LineDepartureCard(
                        lineDirectionRef = lineDeparture.lineDirectionRef,
                        departureTimes =
                            lineDeparture.departures.mapNotNull { it.expectedArrivalTime },
                        isFavorite = true,
                        toggleFavorite = toggleFavoriteDeparture,
                        color = lineDeparture.color,
                        now = now,
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

            if (uiState.otherDepartures.isNotEmpty()) {
                if (uiState.favoriteDepartures.isNotEmpty()) {
                    listHeaderItem(
                        "other-departures-header",
                        strings.otherDeparturesHeader,
                        transformationSpec,
                    )
                }
                items(
                    items = uiState.otherDepartures,
                    key = { departure -> "other-${departure.lineDirectionRef.lineRef}-${departure.lineDirectionRef.destinationRef}" },
                ) { lineDeparture ->
                    LineDepartureCard(
                        lineDirectionRef = lineDeparture.lineDirectionRef,
                        departureTimes =
                            lineDeparture.departures.mapNotNull { it.expectedArrivalTime },
                        isFavorite = false,
                        toggleFavorite = toggleFavoriteDeparture,
                        color = lineDeparture.color,
                        now = now,
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

            if (uiState.favoriteDepartures.isEmpty() && uiState.otherDepartures.isEmpty()) {
                messageItem("departures-empty", strings.empty, transformationSpec)
            }

        }
    }
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
