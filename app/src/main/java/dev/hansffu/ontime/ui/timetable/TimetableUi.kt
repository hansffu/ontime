package dev.hansffu.ontime.ui.timetable

import android.util.Log
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import dev.hansffu.ontime.database.dao.FavoriteDeparture
import dev.hansffu.ontime.model.LineDeparture
import dev.hansffu.ontime.ui.components.timetable.Timetable
import dev.hansffu.ontime.ui.components.timetable.TimetableData
import dev.hansffu.ontime.viewmodels.TimetableUiState
import dev.hansffu.ontime.viewmodels.TimetableViewModel


@OptIn(ExperimentalHorologistApi::class)
@Composable
fun TimetableUi(
    timetableViewModel: TimetableViewModel = hiltViewModel(),
    stopId: String,
    stopName: String,
) {

    val state by timetableViewModel.uiState.collectAsState()

    val columnState = rememberResponsiveColumnState()
    ScreenScaffold(
        scrollState = columnState
    ) {
        Timetable(
            uiState = state,
            scalingLazyColumnState = columnState,
            toggleFavorite = {
                timetableViewModel.toggleFavoriteStop(
                    id = stopId,
                    name = stopName
                )
            },
            toggleFavoriteDeparture = {
                timetableViewModel.toggleFavoriteDeparture(it, stopId)
            }
        )


    }
}

@Composable
fun Loading() {
    Text("Loading")
}
