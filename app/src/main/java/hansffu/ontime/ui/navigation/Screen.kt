package hansffu.ontime.ui.navigation

import hansffu.ontime.model.Stop

sealed class Screen(val route: String) {
    object StopListPager : Screen("stopListPager")
    object Timetable : Screen("timetable") {
        fun link(stop: Stop): String = route + "/${stop.id}?stopName=${stop.name}"
    }
}

