package dev.hansffu.ontime.viewmodels

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.hansffu.ontime.database.dao.FavoriteDeparture
import dev.hansffu.ontime.database.dao.FavoriteDepartureDao
import dev.hansffu.ontime.database.dao.FavoriteStop
import dev.hansffu.ontime.database.dao.FavoriteStopDao
import dev.hansffu.ontime.graphql.StopPlaceQuery
import dev.hansffu.ontime.model.LineDeparture
import dev.hansffu.ontime.model.LineDirectionRef
import dev.hansffu.ontime.model.Stop
import dev.hansffu.ontime.service.StopService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "TimetableViewModel"

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val stopService: StopService,
    private val favoriteStopDao: FavoriteStopDao,
    private val favoriteDepartureDao: FavoriteDepartureDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val stopId: String = checkNotNull(savedStateHandle["stopId"])
    private val stopName: String = checkNotNull(savedStateHandle["stopName"])

    val departureChannel = Channel<List<LineDeparture>>()
    private val allDepartures: Flow<List<LineDeparture>> = departureChannel.consumeAsFlow()

    private val refreshing: MutableStateFlow<Boolean> = MutableStateFlow(false)
    fun loadDepartures() {
        viewModelScope.launch {
            refreshing.tryEmit(true)
            val loaded = stopService.getDepartures(stopId).stopPlace
                ?.let { DepartureMappers.toLineDepartures(it) }
                ?: emptyList()
            departureChannel.send(loaded)
            refreshing.tryEmit(false)
        }
    }

    private val favoriteStops: Flow<List<Stop>> =
        favoriteStopDao.getAll().map { stops ->
            stops.map { Stop(it.name, it.id) }
        }
    val isFavorite = favoriteStops.map { favoriteStops -> favoriteStops.any { it.id == stopId } }

    val favoriteDepartureDtos = favoriteDepartureDao.getByStopId(stopId)
    val groupedDepartures = combine(allDepartures, favoriteDepartureDtos) { all, favorites ->
        all.partition { dep ->
            favorites.any { it.lineRef == dep.lineDirectionRef.lineRef && it.destinationRef == dep.lineDirectionRef.destinationRef }
        }
    }

    val uiState =
        combine(groupedDepartures, isFavorite, refreshing) { dep, isFavorite, refreshing ->
            TimetableUiState.Success(
                stopId = stopId,
                stopName = stopName,
                refreshing = refreshing,
                favoriteDepartures = dep.first,
                otherDepartures = dep.second,
                isFavorite = isFavorite
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            TimetableUiState.Loading(stopId, stopName)
        )


    fun toggleFavoriteStop(id: String, name: String) = viewModelScope.launch(Dispatchers.IO) {
        val existing = favoriteStopDao.getById(id)
        if (existing != null) {
            favoriteStopDao.delete(existing)
        } else {
            favoriteStopDao.insertAll(FavoriteStop(id, name))
        }
    }


    fun toggleFavoriteDeparture(lineDirectionRef: LineDirectionRef, stopId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            with(lineDirectionRef) {
                val existing = favoriteDepartureDao.getById(lineRef, destinationRef, stopId)
                Log.i(TAG, "existing: $existing")
                if (existing != null) {
                    favoriteDepartureDao.delete(existing)
                } else {
                    favoriteDepartureDao.insertAll(
                        FavoriteDeparture(lineRef, destinationRef, stopId)
                    )
                }
            }
        }
}

sealed interface TimetableUiState {
    val stopId: String
    val stopName: String

    val refreshing: Boolean

    data class Loading(override val stopId: String, override val stopName: String) :
        TimetableUiState {
        override val refreshing: Boolean = true
    }

    data class Success(
        override val stopId: String,
        override val stopName: String,
        override val refreshing: Boolean,
        val isFavorite: Boolean,
        val favoriteDepartures: List<LineDeparture>,
        val otherDepartures: List<LineDeparture>

    ) :
        TimetableUiState
}

object DepartureMappers {
    fun toLineDepartures(stopPlace: StopPlaceQuery.StopPlace): List<LineDeparture> {
        return stopPlace.estimatedCalls
            .groupBy(DepartureMappers::groupLines)
            .mapNotNull { (ref, departures) ->
                ref?.let {
                    LineDeparture(
                        it,
                        departures,
                        departures.firstOrNull()?.serviceJourney?.line?.presentation?.colour
                            ?: "000000"
                    )
                }
            }
            .sortedBy { it.departures.minOfOrNull { call -> call.expectedArrivalTime } }
            .toList()
    }

    private fun groupLines(estimatedCall: StopPlaceQuery.EstimatedCall): LineDirectionRef? {
        val publicCode = estimatedCall.serviceJourney.line.publicCode
        val dest = estimatedCall.destinationDisplay?.frontText
        return if (publicCode != null && dest != null) {
            LineDirectionRef(publicCode, dest)
        } else {
            null
        }
    }

}