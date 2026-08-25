package dev.hansffu.ontime.ui.navigation

import android.net.Uri
import dev.hansffu.ontime.model.Stop

sealed interface Screen {
    data object Favorites : Screen {
        const val route = "favorites"
    }

    data object Nearby : Screen {
        const val route = "nearby"
    }

    data class TextSearch(val searchString: String) : Screen {
        fun route() = "search/${Uri.encode(searchString)}"

        companion object {
            const val route = "search/{searchString}"
        }
    }

    data class Timetable(val stopName: String, val stopId: String) : Screen {
        constructor(stop: Stop) : this(
            stopId = stop.id,
            stopName = stop.name,
        )

        fun route() = "timetable/${Uri.encode(stopId)}/${Uri.encode(stopName)}"

        companion object {
            const val route = "timetable/{stopId}/{stopName}"
        }
    }
}

