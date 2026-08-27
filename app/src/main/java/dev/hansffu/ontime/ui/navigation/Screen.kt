package dev.hansffu.ontime.ui.navigation

import android.net.Uri
import dev.hansffu.ontime.database.dao.BoardDeparture
import dev.hansffu.ontime.model.LineDirectionRef
import dev.hansffu.ontime.model.Stop

sealed interface Screen {
    data object Favorites : Screen {
        const val route = "favorites"
    }

    data object Nearby : Screen {
        const val route = "nearby"
    }

    data object Boards : Screen {
        const val route = "boards"
    }

    data class BoardEditor(val boardId: Long) : Screen {
        fun route() = "boards/" + boardId + "/edit"
        companion object {
            const val route = "boards/{boardId}/edit"
        }
    }

    data class BoardTimetable(val boardId: Long) : Screen {
        fun route() = "boards/" + boardId
        companion object {
            const val route = "boards/{boardId}"
        }
    }

    data class BoardActivationStop(val boardId: Long) : Screen {
        fun route() = "boards/" + boardId + "/activation-stop"
        companion object {
            const val route = "boards/{boardId}/activation-stop"
        }
    }

    data class BoardAssignment(
        val stopId: String,
        val stopName: String,
        val latitude: Double?,
        val longitude: Double?,
        val lineRef: String,
        val destinationRef: String,
    ) : Screen {
        constructor(stop: Stop, line: LineDirectionRef) : this(
            stopId = stop.id,
            stopName = stop.name,
            latitude = stop.latitude,
            longitude = stop.longitude,
            lineRef = line.lineRef,
            destinationRef = line.destinationRef,
        )

        constructor(departure: BoardDeparture) : this(
            stopId = departure.stopId,
            stopName = departure.stopName,
            latitude = departure.stopLatitude,
            longitude = departure.stopLongitude,
            lineRef = departure.lineRef,
            destinationRef = departure.destinationRef,
        )

        fun route() =
            listOf(
                "board-assignment",
                Uri.encode(stopId),
                Uri.encode(stopName),
                latitude?.toString() ?: "none",
                longitude?.toString() ?: "none",
                Uri.encode(lineRef),
                Uri.encode(destinationRef),
            ).joinToString("/")

        companion object {
            const val route =
                "board-assignment/{stopId}/{stopName}/{latitude}/{longitude}/{lineRef}/{destinationRef}"
        }
    }

    data class TextSearch(val searchString: String) : Screen {
        fun route() = "search/" + Uri.encode(searchString)
        companion object {
            const val route = "search/{searchString}"
        }
    }

    data class Timetable(val stopName: String, val stopId: String) : Screen {
        constructor(stop: Stop) : this(stopId = stop.id, stopName = stop.name)
        fun route() = "timetable/" + Uri.encode(stopId) + "/" + Uri.encode(stopName)
        companion object {
            const val route = "timetable/{stopId}/{stopName}"
        }
    }
}
