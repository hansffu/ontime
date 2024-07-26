package dev.hansffu.ontime.ui.timetable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import dev.hansffu.ontime.model.LineDeparture
import dev.hansffu.ontime.ui.components.timetable.Timetable
import dev.hansffu.ontime.viewmodels.TimetableViewModel


@OptIn(ExperimentalHorologistApi::class)
@Composable
fun TimetableUi(
    timetableViewModel: TimetableViewModel,
    stopId: String,
    stopName: String,
) {
    val lineDeparturesState: List<LineDeparture>? by timetableViewModel.getDepartures(stopId)
        .observeAsState()
    val isFavorite by timetableViewModel.isFavorite(stopId).observeAsState()


    val columnState = rememberResponsiveColumnState()
    ScreenScaffold(
        scrollState = columnState
    ) {

        Timetable(
            stopName = stopName,
            lineDepartures = lineDeparturesState,
            isFavorite = isFavorite ?: false,
            scalingLazyColumnState = columnState,
            toggleFavorite = { timetableViewModel.toggleFavorite(id = stopId, name = stopName) }
        )

    }
}
