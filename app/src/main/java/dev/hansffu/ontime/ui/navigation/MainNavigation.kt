package dev.hansffu.ontime.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import dev.hansffu.ontime.ui.stoplist.StopsScreen
import dev.hansffu.ontime.ui.stoplist.nearby.NearbyStopsScreen
import dev.hansffu.ontime.ui.stoplist.search.SearchScreen
import dev.hansffu.ontime.ui.theme.OntimeTheme
import dev.hansffu.ontime.ui.timetable.TimetableUi
import dev.hansffu.ontime.viewmodels.TimetableViewModel

@Composable
fun MainNavigation() {
    OntimeTheme {
        AppScaffold {
            val navController = rememberSwipeDismissableNavController()
            val openTimetable = { stop: dev.hansffu.ontime.model.Stop ->
                navController.navigate(Screen.Timetable(stop).route())
            }

            SwipeDismissableNavHost(
                navController = navController,
                startDestination = Screen.Favorites.route,
            ) {
                composable(Screen.Favorites.route) {
                    StopsScreen(
                        onSearch = { navController.navigate(Screen.TextSearch(it).route()) },
                        onNearby = { navController.navigate(Screen.Nearby.route) },
                        onStopSelected = openTimetable,
                    )
                }
                composable(Screen.Nearby.route) {
                    NearbyStopsScreen(
                        onStopSelected = openTimetable,
                        onDismissPermission = navController::popBackStack,
                    )
                }
                composable(
                    route = Screen.TextSearch.route,
                    arguments = listOf(navArgument("searchString") { type = NavType.StringType }),
                ) { backStackEntry ->
                    SearchScreen(
                        searchString =
                            backStackEntry.arguments?.getString("searchString").orEmpty(),
                        onStopSelected = openTimetable,
                    )
                }
                composable(
                    route = Screen.Timetable.route,
                    arguments = listOf(
                        navArgument("stopId") { type = NavType.StringType },
                        navArgument("stopName") { type = NavType.StringType },
                    ),
                ) { backStackEntry ->
                    val timetableViewModel = hiltViewModel<TimetableViewModel>(backStackEntry)
                    TimetableUi(timetableViewModel = timetableViewModel)
                }
            }
        }
    }
}
