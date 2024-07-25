package hansffu.ontime.ui.navigation

import hansffu.ontime.model.Stop

sealed class Screen(val route: String) {
    data object StopListPager : Screen("stopListPager")
    data object Favorites : Screen("favorites")
    data object Timetable : Screen("timetable") {
        fun link(stop: Stop): String = route + "/${stop.id}?stopName=${stop.name}"
    }
}

