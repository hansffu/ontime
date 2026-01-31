package dev.hansffu.ontime.ui.timetable

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import dev.hansffu.ontime.database.dao.FavoriteDeparture
import dev.hansffu.ontime.model.LineDeparture
import dev.hansffu.ontime.ui.components.timetable.Timetable
import dev.hansffu.ontime.ui.components.timetable.TimetableData
import dev.hansffu.ontime.viewmodels.TimetableViewModel


@OptIn(ExperimentalHorologistApi::class)
@Composable
fun TimetableUi(
    timetableViewModel: TimetableViewModel = hiltViewModel(),
    stopId: String,
    stopName: String,
) {
    val lineDeparturesState: List<LineDeparture>? by timetableViewModel.getDepartures(stopId)
        .observeAsState()
    val favoriteDepartures: List<FavoriteDeparture> by timetableViewModel.getFavoriteDepartures(
        stopId
    ).observeAsState(emptyList())
    val isFavorite by timetableViewModel.isFavorite(stopId).observeAsState()
    Log.i("TimetableUi", "favorites: ${favoriteDepartures.map { it.destinationRef }}")
    val timetableData = remember(lineDeparturesState, favoriteDepartures) {
        lineDeparturesState?.partition { lineDeparture ->
            favoriteDepartures.any {
                it.lineRef == lineDeparture.lineDirectionRef.lineRef
                        && it.destinationRef == lineDeparture.lineDirectionRef.destinationRef
            }
        }
            ?.let { TimetableData(false, it.first, it.second) }
            ?: TimetableData(true, emptyList(), emptyList())
    }


    val columnState = rememberResponsiveColumnState()
    ScreenScaffold(
        scrollState = columnState
    ) {

        Timetable(
            stopId = stopId,
            stopName = stopName,
            timetableData,
            isFavorite = isFavorite ?: false,
            scalingLazyColumnState = columnState,
            toggleFavorite = { timetableViewModel.toggleFavoriteStop(id = stopId, name = stopName) }
        )

    }
}
