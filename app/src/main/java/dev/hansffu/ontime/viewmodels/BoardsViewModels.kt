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
import dev.hansffu.ontime.model.BoardDistance
import dev.hansffu.ontime.model.BoardDepartureRow
import dev.hansffu.ontime.model.BoardTime
import dev.hansffu.ontime.model.Stop
import dev.hansffu.ontime.service.ActiveBoardService
import dev.hansffu.ontime.service.BoardTimetableRepository
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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

@HiltViewModel
class BoardTimetableViewModel @Inject constructor(
    private val boardDao: BoardDao,
    private val favoriteDepartureDao: FavoriteDepartureDao,
    private val timetableRepository: BoardTimetableRepository,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val boardId: Long = checkNotNull(savedStateHandle["boardId"])
    val uiState =
        timetableRepository.observe(boardId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = timetableRepository.cachedState(boardId),
        )
    val board =
        boardDao.observeById(boardId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

    init {
        viewModelScope.launch {
            timetableRepository.openBoard(boardId)
            timetableRepository.watchConfiguration(boardId)
        }
    }

    fun refresh() = viewModelScope.launch {
        timetableRepository.refresh(boardId)
    }

    fun refreshOnResume() = viewModelScope.launch {
        timetableRepository.refreshOnResume(boardId)
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

}
