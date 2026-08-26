package dev.hansffu.ontime.viewmodels

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
import dev.hansffu.ontime.service.StopService
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val stopService: StopService,
    private val favoriteStopDao: FavoriteStopDao,
    private val favoriteDepartureDao: FavoriteDepartureDao,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val stopId: String = checkNotNull(savedStateHandle["stopId"])
    private val stopName: String = checkNotNull(savedStateHandle["stopName"])

    private sealed interface DeparturesLoadState {
        data object Loading : DeparturesLoadState
        data object Error : DeparturesLoadState

        data class Content(
            val departures: List<LineDeparture>,
            val refreshing: Boolean = false,
            val refreshFailed: Boolean = false,
        ) : DeparturesLoadState
    }

    private val departuresLoadState =
        MutableStateFlow<DeparturesLoadState>(DeparturesLoadState.Loading)
    private var loadJob: Job? = null

    private val isFavorite =
        favoriteStopDao.getAll().map { favorites -> favorites.any { it.id == stopId } }
    private val favoriteDepartures = favoriteDepartureDao.getByStopId(stopId)

    val uiState =
        combine(departuresLoadState, isFavorite, favoriteDepartures) {
            loadState,
            isFavorite,
            favorites,
            ->
            when (loadState) {
                DeparturesLoadState.Loading -> TimetableUiState.Loading(stopId, stopName)
                DeparturesLoadState.Error -> TimetableUiState.Error(stopId, stopName)
                is DeparturesLoadState.Content -> {
                    val partitioned =
                        loadState.departures.partition { departure ->
                            favorites.any {
                                it.lineRef == departure.lineDirectionRef.lineRef &&
                                    it.destinationRef ==
                                        departure.lineDirectionRef.destinationRef
                            }
                        }
                    TimetableUiState.Success(
                        stopId = stopId,
                        stopName = stopName,
                        refreshing = loadState.refreshing,
                        refreshFailed = loadState.refreshFailed,
                        favoriteDepartures = partitioned.first,
                        otherDepartures = partitioned.second,
                        isFavorite = isFavorite,
                    )
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TimetableUiState.Loading(stopId, stopName),
        )

    init {
        loadDepartures()
    }

    fun loadDepartures() {
        val previousContent = departuresLoadState.value as? DeparturesLoadState.Content
        loadJob?.cancel()
        loadJob =
            viewModelScope.launch {
                departuresLoadState.value =
                    previousContent?.copy(refreshing = true, refreshFailed = false)
                        ?: DeparturesLoadState.Loading
                try {
                    val loaded =
                        stopService
                            .getDepartures(stopId)
                            .stopPlace
                            ?.let(DepartureMappers::toLineDepartures)
                            .orEmpty()
                    departuresLoadState.value = DeparturesLoadState.Content(loaded)
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (_: Exception) {
                    departuresLoadState.value =
                        previousContent?.copy(refreshing = false, refreshFailed = true)
                            ?: DeparturesLoadState.Error
                }
            }
    }

    fun toggleFavoriteStop() = viewModelScope.launch(Dispatchers.IO) {
        val existing = favoriteStopDao.getById(stopId)
        if (existing != null) {
            favoriteStopDao.delete(existing)
        } else {
            favoriteStopDao.insertAll(FavoriteStop(stopId, stopName))
        }
    }

    fun toggleFavoriteDeparture(lineDirectionRef: LineDirectionRef) =
        viewModelScope.launch(Dispatchers.IO) {
            with(lineDirectionRef) {
                val existing = favoriteDepartureDao.getById(lineRef, destinationRef, stopId)
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

    data class Loading(
        override val stopId: String,
        override val stopName: String,
    ) : TimetableUiState {
        override val refreshing: Boolean = true
    }

    data class Error(
        override val stopId: String,
        override val stopName: String,
    ) : TimetableUiState {
        override val refreshing: Boolean = false
    }

    data class Success(
        override val stopId: String,
        override val stopName: String,
        override val refreshing: Boolean,
        val refreshFailed: Boolean,
        val isFavorite: Boolean,
        val favoriteDepartures: List<LineDeparture>,
        val otherDepartures: List<LineDeparture>,
    ) : TimetableUiState
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
                            ?: "000000",
                    )
                }
            }
            .sortedBy { it.departures.minOfOrNull { call -> call.expectedArrivalTime } }
            .toList()
    }

    private fun groupLines(estimatedCall: StopPlaceQuery.EstimatedCall): LineDirectionRef? {
        val publicCode = estimatedCall.serviceJourney.line.publicCode
        val destination = estimatedCall.destinationDisplay?.frontText
        return if (publicCode != null && destination != null) {
            LineDirectionRef(publicCode, destination)
        } else {
            null
        }
    }
}
