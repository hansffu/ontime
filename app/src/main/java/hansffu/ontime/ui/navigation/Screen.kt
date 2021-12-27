package hansffu.ontime.ui.navigation

sealed class Screen(val route: String) {
    object StopListPager : Screen("stopListPager")
    object Timetable : Screen("timetable")
}

