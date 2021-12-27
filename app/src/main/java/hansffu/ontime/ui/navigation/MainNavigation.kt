package hansffu.ontime.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import hansffu.ontime.StopListViewModel
import hansffu.ontime.TimetableViewModel
import hansffu.ontime.ui.stoplist.StopListPager
import hansffu.ontime.ui.theme.OntimeTheme
import hansffu.ontime.ui.timetable.Timetable

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun MainNavigation(stopListViewModel: StopListViewModel, timetableViewModel: TimetableViewModel) {
    OntimeTheme {
        val navController = rememberSwipeDismissableNavController()
        SwipeDismissableNavHost(navController = navController, Screen.StopListPager.route) {
            composable(route = Screen.StopListPager.route) {
                StopListPager(stopListViewModel = stopListViewModel,
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
                println(it.arguments?.getString("stopId"))
                Timetable(
                    timetableViewModel = timetableViewModel,
                    stopId = it.arguments!!.getString("stopId")!!,
                    stopName = it.arguments!!.getString("stopName")!!
                )
            }
        }

    }
}

