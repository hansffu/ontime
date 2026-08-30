package dev.hansffu.ontime.service

import android.util.Log
import dev.hansffu.ontime.database.dao.Board
import dev.hansffu.ontime.database.dao.BoardDao
import dev.hansffu.ontime.database.dao.BoardDeparture
import dev.hansffu.ontime.database.dao.BoardDepartureDao
import dev.hansffu.ontime.database.dao.FavoriteDeparture
import dev.hansffu.ontime.database.dao.FavoriteDepartureDao
import dev.hansffu.ontime.model.BoardDepartureOrdering
import dev.hansffu.ontime.model.BoardDepartureRow
import dev.hansffu.ontime.model.BoardSuggestion
import dev.hansffu.ontime.model.Coordinates
import dev.hansffu.ontime.model.LineDeparture
import dev.hansffu.ontime.model.LineDirectionRef
import dev.hansffu.ontime.viewmodels.DepartureMappers
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class BoardTimetableInputs(
    val board: Board?,
    val departures: List<BoardDeparture>,
    val favorites: List<FavoriteDeparture>,
)

interface BoardTimetableSource {
    fun observeInputs(boardId: Long): Flow<BoardTimetableInputs>
    suspend fun getInputs(boardId: Long): BoardTimetableInputs
    suspend fun getLocation(): Coordinates?
    suspend fun getRows(inputs: BoardTimetableInputs, location: Coordinates?): List<BoardDepartureRow>
}

class DefaultBoardTimetableSource @Inject constructor(
    private val boardDao: BoardDao,
    private val boardDepartureDao: BoardDepartureDao,
    private val favoriteDepartureDao: FavoriteDepartureDao,
    private val boardDeparturesService: BoardDeparturesService,
    private val locationService: LocationService,
) : BoardTimetableSource {
    override fun observeInputs(boardId: Long): Flow<BoardTimetableInputs> =
        combine(
            boardDao.observeById(boardId),
            boardDepartureDao.observeForBoard(boardId),
            favoriteDepartureDao.observeAll(),
            ::BoardTimetableInputs,
        )

    override suspend fun getInputs(boardId: Long) = BoardTimetableInputs(
        boardDao.getById(boardId),
        boardDepartureDao.getForBoard(boardId),
        favoriteDepartureDao.getAll(),
    )

    override suspend fun getLocation(): Coordinates? {
        Log.d("BoardTimetable", "Refreshing board location")
        return (locationService.getLatestLocation() as? LocationResult.Success)?.location
            ?.let { Coordinates(it.latitude, it.longitude) }
    }

    override suspend fun getRows(
        inputs: BoardTimetableInputs,
        location: Coordinates?,
    ): List<BoardDepartureRow> {
        Log.d("BoardTimetable", "Refreshing departures for board ${inputs.board?.id}")
        val callsByStop = boardDeparturesService.getDepartures(inputs.departures)
        // Keep empty stops for ambient nearest-stop selection. Interactive lists already
        // remove empty lines with withUpcomingDepartures.
        val rows = inputs.departures.map { item ->
            val line = callsByStop[item.stopId]?.let(DepartureMappers::toLineDepartures)?.firstOrNull {
                it.lineDirectionRef.lineRef == item.lineRef &&
                    it.lineDirectionRef.destinationRef == item.destinationRef
            } ?: LineDeparture(LineDirectionRef(item.lineRef, item.destinationRef), emptyList(), "000000")
            val stopCoordinates =
                if (item.stopLatitude != null && item.stopLongitude != null) {
                    Coordinates(item.stopLatitude, item.stopLongitude)
                } else null
            BoardDepartureRow(
                stored = item,
                departure = line,
                distanceMeters = if (location != null && stopCoordinates != null) {
                    BoardSuggestion.distanceMeters(location, stopCoordinates)
                } else null,
                isFavorite = false,
            )
        }
        return BoardDepartureOrdering.sort(rows, BoardDepartureRow::distanceMeters) { row ->
            row.departure.departures.minOfOrNull { it.expectedArrivalTime }
        }
    }
}
