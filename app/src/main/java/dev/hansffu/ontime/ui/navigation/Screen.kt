package dev.hansffu.ontime.ui.navigation

import dev.hansffu.ontime.model.Stop

sealed class Screen(val route: String) {
    data object Favorites : Screen("favorites")
    data object Timetable : Screen("timetable") {
        fun link(stop: Stop): String = route + "/${stop.id}?stopName=${stop.name}"
    }
}

