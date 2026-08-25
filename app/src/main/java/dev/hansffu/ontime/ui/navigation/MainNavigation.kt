package dev.hansffu.ontime.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation.SwipeDismissableNavHost
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
            SwipeDismissableNavHost(
                navController = navController,
                startDestination = Screen.Favorites.route,
            ) {
                composable(Screen.Favorites.route) {
                    StopsScreen(navController = navController)
                }
                composable(Screen.Nearby.route) {
                    NearbyStopsScreen(navController = navController)
                }
                composable(
                    route = Screen.TextSearch.route,
                    arguments = listOf(navArgument("searchString") { type = NavType.StringType }),
                ) { backStackEntry ->
                    SearchScreen(
                        searchString = backStackEntry.arguments?.getString("searchString").orEmpty(),
                        navController = navController,
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
                    TimetableUi(
                        stopId = backStackEntry.arguments?.getString("stopId").orEmpty(),
                        stopName = backStackEntry.arguments?.getString("stopName").orEmpty(),
                        timetableViewModel = timetableViewModel,
                    )
                }
            }
        }
    }
}
