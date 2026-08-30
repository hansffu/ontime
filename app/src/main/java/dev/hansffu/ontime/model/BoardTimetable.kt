package dev.hansffu.ontime.model

import dev.hansffu.ontime.database.dao.BoardDeparture

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
