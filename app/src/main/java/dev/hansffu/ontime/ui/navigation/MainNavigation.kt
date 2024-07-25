package dev.hansffu.ontime.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.android.horologist.compose.layout.AppScaffold
import dev.hansffu.ontime.viewmodels.FavoritesViewModel
import dev.hansffu.ontime.viewmodels.LocationViewModel
import dev.hansffu.ontime.viewmodels.TimetableViewModel
import dev.hansffu.ontime.ui.stoplist.FavoritesScreen
import dev.hansffu.ontime.ui.theme.OntimeTheme
import dev.hansffu.ontime.ui.timetable.TimetableUi

@Composable
fun MainNavigation(
    favoritesViewModel: FavoritesViewModel,
    timetableViewModel: TimetableViewModel,
    locationViewModel: LocationViewModel,
) {
    OntimeTheme {
        AppScaffold {
            val navController = rememberSwipeDismissableNavController()
            SwipeDismissableNavHost(navController = navController, Screen.Favorites.route) {
                composable(route = Screen.Favorites.route) {
                    FavoritesScreen(
                        favoritesViewModel = favoritesViewModel,
                        locationViewModel = locationViewModel,
                        onStopSelected = {
                            navController.navigate(Screen.Timetable.link(it))
                        }
                    )
                }
                composable(
                    route = Screen.Timetable.route + "/{stopId}?stopName={stopName}",
                    arguments = listOf(
                        navArgument("stopId") { type = NavType.StringType },
                        navArgument("stopName") { type = NavType.StringType })
                ) {
                    TimetableUi(
                        timetableViewModel = timetableViewModel,
                        stopId = it.arguments!!.getString("stopId")!!,
                        stopName = it.arguments!!.getString("stopName")!!
                    )
                }
            }
        }
    }

}
