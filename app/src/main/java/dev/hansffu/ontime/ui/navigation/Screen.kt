package dev.hansffu.ontime.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Favorites : Screen
    @Serializable
    data object Nearby : Screen
    @Serializable
    data class Timetable(val stopName: String, val stopId: String) : Screen
}

