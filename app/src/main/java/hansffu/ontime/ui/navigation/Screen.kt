package hansffu.ontime.ui.navigation

sealed class Screen(val route: String) {
    object FavoriteStops : Screen("favoriteStops")
    object NearbyStops : Screen("nearbyStops")
    object Timetable : Screen("timetable")
}

