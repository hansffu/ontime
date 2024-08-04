package dev.hansffu.ontime.ui.navigation

import dev.hansffu.ontime.model.Stop
import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Favorites : Screen

    @Serializable
    data object Nearby : Screen

    @Serializable
    data object TextSearch: Screen

    @Serializable
    data class Timetable(val stopName: String, val stopId: String) : Screen {
        constructor(stop: Stop) : this(
            stopId = stop.id,
            stopName = stop.name,
        )
    }
}

