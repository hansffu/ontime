package dev.hansffu.ontime.viewmodels

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.hansffu.ontime.database.dao.Board
import dev.hansffu.ontime.database.dao.BoardDao
import dev.hansffu.ontime.database.dao.BoardDeparture
import dev.hansffu.ontime.database.dao.BoardDepartureDao
import dev.hansffu.ontime.database.dao.FavoriteDeparture
import dev.hansffu.ontime.database.dao.FavoriteDepartureDao
import dev.hansffu.ontime.graphql.StopPlaceQuery
import dev.hansffu.ontime.model.BoardDepartureOrdering
import dev.hansffu.ontime.model.BoardDistance
import dev.hansffu.ontime.model.BoardSuggestion
import dev.hansffu.ontime.model.BoardTime
import dev.hansffu.ontime.model.Coordinates
import dev.hansffu.ontime.model.LineDeparture
import dev.hansffu.ontime.model.LineDirectionRef
import dev.hansffu.ontime.model.Stop
import dev.hansffu.ontime.service.ActiveBoardService
import dev.hansffu.ontime.service.LocationResult
import dev.hansffu.ontime.service.LocationService
import dev.hansffu.ontime.service.StopService
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class BoardsViewModel @Inject constructor(
    private val boardDao: BoardDao,
    private val boardDepartureDao: BoardDepartureDao,
) : ViewModel() {
    val boards =
        boardDao.observeAll().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun createBoard(onCreated: (Long) -> Unit = {}) =
        viewModelScope.launch(Dispatchers.IO) {
            val id = boardDao.insert(Board(name = "Ny tavle"))
            withContext(Dispatchers.Main) { onCreated(id) }
        }

    fun deleteBoard(boardId: Long) =
        viewModelScope.launch(Dispatchers.IO) {
            boardDepartureDao.deleteForBoard(boardId)
            boardDao.deleteById(boardId)
        }
}

data class BoardEditorState(
    val board: Board? = null,
    val departures: List<BoardDeparture> = emptyList(),
)

@HiltViewModel
class BoardEditorViewModel @Inject constructor(
    private val boardDao: BoardDao,
    private val boardDepartureDao: BoardDepartureDao,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val boardId: Long = checkNotNull(savedStateHandle["boardId"])

    val uiState =
        combine(
            boardDao.observeById(boardId),
            boardDepartureDao.observeForBoard(boardId),
            ::BoardEditorState,
        ).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BoardEditorState(),
        )

    fun rename(name: String) = updateBoard {
        copy(name = name.trim().takeIf(String::isNotEmpty) ?: name)
    }

    fun setActivationStop(stop: Stop) = updateBoard {
        copy(
            activationStopId = stop.id,
            activationStopName = stop.name,
            activationLatitude = stop.latitude,
            activationLongitude = stop.longitude,
            maxDistanceMeters = maxDistanceMeters ?: 3_000,
            distanceEnabled = true,
        )
    }

    fun setDistanceKilometers(value: Int, onUpdated: () -> Unit = {}) =
        updateBoard(onUpdated) {
            copy(
                maxDistanceMeters = BoardDistance.toMeters(value),
                distanceEnabled = true,
            )
        }

    fun setDistanceEnabled(enabled: Boolean) = updateBoard {
        copy(
            distanceEnabled = enabled,
            maxDistanceMeters = maxDistanceMeters ?: 3_000,
        )
    }

    fun setTimeEnabled(enabled: Boolean) = updateBoard {
        copy(
            timeEnabled = enabled,
            startMinuteOfDay = startMinuteOfDay ?: 6 * 60,
            endMinuteOfDay = endMinuteOfDay ?: 9 * 60,
        )
    }

    fun setStartTime(value: LocalTime, onUpdated: () -> Unit = {}) =
        updateBoard(onUpdated) {
            copy(
                startMinuteOfDay = BoardTime.toMinuteOfDay(value),
                timeEnabled = true,
            )
        }

    fun setEndTime(value: LocalTime, onUpdated: () -> Unit = {}) =
        updateBoard(onUpdated) {
            copy(
                endMinuteOfDay = BoardTime.toMinuteOfDay(value),
                timeEnabled = true,
            )
        }

    fun removeDeparture(departure: BoardDeparture) =
        viewModelScope.launch(Dispatchers.IO) {
            boardDepartureDao.delete(
                boardId,
                departure.stopId,
                departure.lineRef,
                departure.destinationRef,
            )
        }

    fun delete(onDeleted: () -> Unit) =
        viewModelScope.launch(Dispatchers.IO) {
            boardDepartureDao.deleteForBoard(boardId)
            boardDao.deleteById(boardId)
            withContext(Dispatchers.Main) { onDeleted() }
        }

    private fun updateBoard(
        onUpdated: () -> Unit = {},
        transform: Board.() -> Board,
    ) =
        viewModelScope.launch(Dispatchers.IO) {
            boardDao.getById(boardId)?.let { boardDao.update(transform(it)) }
            withContext(Dispatchers.Main) { onUpdated() }
        }
}

data class BoardAssignmentOption(val board: Board, val selected: Boolean)

@HiltViewModel
class BoardAssignmentViewModel @Inject constructor(
    private val boardDao: BoardDao,
    private val boardDepartureDao: BoardDepartureDao,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val stopId: String = checkNotNull(savedStateHandle["stopId"])
    private val stopName: String = checkNotNull(savedStateHandle["stopName"])
    private val lineRef: String = checkNotNull(savedStateHandle["lineRef"])
    private val destinationRef: String = checkNotNull(savedStateHandle["destinationRef"])
    private val stopLatitude: Double? = savedStateHandle.get<String>("latitude")?.toDoubleOrNull()
    private val stopLongitude: Double? = savedStateHandle.get<String>("longitude")?.toDoubleOrNull()

    val options =
        combine(
            boardDao.observeAll(),
            boardDepartureDao.observeBoardIdsForDeparture(stopId, lineRef, destinationRef),
        ) { boards, selectedIds ->
            boards.map { BoardAssignmentOption(it, it.id in selectedIds) }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun setSelected(boardId: Long, selected: Boolean) =
        viewModelScope.launch(Dispatchers.IO) {
            if (selected) {
                boardDepartureDao.insert(departure(boardId))
            } else {
                boardDepartureDao.delete(boardId, stopId, lineRef, destinationRef)
            }
        }

    fun createBoard(onCreated: (Long) -> Unit) =
        viewModelScope.launch(Dispatchers.IO) {
            val boardId = boardDao.insert(Board(name = "Ny tavle"))
            boardDepartureDao.insert(departure(boardId))
            withContext(Dispatchers.Main) { onCreated(boardId) }
        }

    private fun departure(boardId: Long) =
        BoardDeparture(
            boardId = boardId,
            stopId = stopId,
            stopName = stopName,
            stopLatitude = stopLatitude,
            stopLongitude = stopLongitude,
            lineRef = lineRef,
            destinationRef = destinationRef,
        )
}

data class BoardDepartureRow(
    val stored: BoardDeparture,
    val departure: LineDeparture,
    val distanceMeters: Double?,
    val isFavorite: Boolean,
)

sealed interface BoardTimetableState {
    data object Loading : BoardTimetableState
    data class Error(val boardName: String) : BoardTimetableState
    data class Content(
        val boardName: String,
        val rows: List<BoardDepartureRow>,
        val refreshing: Boolean = false,
        val refreshFailed: Boolean = false,
    ) : BoardTimetableState
}

@HiltViewModel
class BoardTimetableViewModel @Inject constructor(
    private val boardDao: BoardDao,
    private val boardDepartureDao: BoardDepartureDao,
    private val favoriteDepartureDao: FavoriteDepartureDao,
    private val stopService: StopService,
    private val locationService: LocationService,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val boardId: Long = checkNotNull(savedStateHandle["boardId"])
    private val mutableUiState = MutableStateFlow<BoardTimetableState>(BoardTimetableState.Loading)
    val uiState = mutableUiState
    val board =
        boardDao.observeById(boardId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )
    private var loadJob: Job? = null
    private var latestLocation: Coordinates? = null

    init {
        viewModelScope.launch {
            combine(
                boardDao.observeById(boardId),
                boardDepartureDao.observeForBoard(boardId),
                favoriteDepartureDao.observeAll(),
            ) { board, departures, favorites -> Triple(board, departures, favorites) }
                .collect { (board, departures, favorites) ->
                    if (board != null) load(board, departures, favorites)
                }
        }
        viewModelScope.launch {
            board.map { it?.active == true }
                .distinctUntilChanged()
                .collectLatest { active ->
                    if (active) {
                        var refreshCount = 0
                        while (isActive) {
                            delay(DEPARTURE_REFRESH_INTERVAL_MILLIS)
                            refreshCount += 1
                            refresh(
                                updateLocation =
                                    refreshCount % LOCATION_REFRESH_EVERY_N_DEPARTURE_REFRESHES == 0
                            )
                        }
                    }
                }
        }
    }

    fun refresh() = refresh(updateLocation = true)

    private fun refresh(updateLocation: Boolean) {
        viewModelScope.launch {
            val board = boardDao.getById(boardId) ?: return@launch
            val stored = boardDepartureDao.getForBoard(boardId)
            val favorites = favoriteDepartureDao.getAll()
            load(
                board,
                stored,
                favorites,
                keepContent = true,
                updateLocation = updateLocation,
            )
        }
    }

    fun activate() =
        viewModelScope.launch(Dispatchers.IO) {
            val board = boardDao.getById(boardId) ?: return@launch
            boardDao.activate(boardId)
            ActiveBoardService.start(context, board.copy(active = true))
        }

    fun deactivate() =
        viewModelScope.launch(Dispatchers.IO) {
            boardDao.deactivate(boardId)
        }

    fun toggleFavorite(row: BoardDepartureRow) =
        viewModelScope.launch(Dispatchers.IO) {
            val stored = row.stored
            val existing =
                favoriteDepartureDao.getById(
                    stored.lineRef,
                    stored.destinationRef,
                    stored.stopId,
                )
            if (existing != null) favoriteDepartureDao.delete(existing)
            else {
                favoriteDepartureDao.insertAll(
                    FavoriteDeparture(stored.lineRef, stored.destinationRef, stored.stopId)
                )
            }
        }

    private fun load(
        board: Board,
        stored: List<BoardDeparture>,
        favorites: List<FavoriteDeparture>,
        keepContent: Boolean = false,
        updateLocation: Boolean = true,
    ) {
        loadJob?.cancel()
        loadJob =
            viewModelScope.launch {
                val previousContent = mutableUiState.value as? BoardTimetableState.Content
                mutableUiState.value =
                    if (keepContent && previousContent != null) {
                        previousContent.copy(refreshing = true, refreshFailed = false)
                    } else {
                        BoardTimetableState.Loading
                    }
                try {
                    if (updateLocation || latestLocation == null) {
                        latestLocation =
                            (locationService.getLatestLocation() as? LocationResult.Success)?.location
                                ?.let { Coordinates(it.latitude, it.longitude) }
                    }
                    val location = latestLocation
                    val callsByStop =
                        coroutineScope {
                            stored.distinctBy { it.stopId }.map { item ->
                                async { item.stopId to stopService.getDepartures(item.stopId).stopPlace }
                            }.awaitAll().toMap()
                        }
                    val unsortedRows =
                        stored.mapNotNull { item ->
                            val stopPlace = callsByStop[item.stopId] ?: return@mapNotNull null
                            val line =
                                DepartureMappers.toLineDepartures(stopPlace).firstOrNull {
                                    it.lineDirectionRef.lineRef == item.lineRef &&
                                        it.lineDirectionRef.destinationRef == item.destinationRef
                                } ?: return@mapNotNull null
                            val stopCoordinates =
                                if (item.stopLatitude != null && item.stopLongitude != null) {
                                    Coordinates(item.stopLatitude, item.stopLongitude)
                                } else null
                            BoardDepartureRow(
                                stored = item,
                                departure = line,
                                distanceMeters =
                                    if (location != null && stopCoordinates != null) {
                                        BoardSuggestion.distanceMeters(location, stopCoordinates)
                                    } else null,
                                isFavorite =
                                    favorites.any {
                                        it.stopId == item.stopId &&
                                            it.lineRef == item.lineRef &&
                                            it.destinationRef == item.destinationRef
                                    },
                            )
                        }
                    val rows =
                        BoardDepartureOrdering.sort(
                            unsortedRows,
                            BoardDepartureRow::distanceMeters,
                        ) { row ->
                            row.departure.departures.minOfOrNull { it.expectedArrivalTime }
                        }
                    mutableUiState.value = BoardTimetableState.Content(board.name, rows)
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (_: Exception) {
                    mutableUiState.value =
                        previousContent?.copy(refreshing = false, refreshFailed = true)
                            ?: BoardTimetableState.Error(board.name)
                }
            }
    }

    companion object {
        private const val DEPARTURE_REFRESH_INTERVAL_MILLIS = 60_000L
        private const val LOCATION_REFRESH_EVERY_N_DEPARTURE_REFRESHES = 2
    }
}
