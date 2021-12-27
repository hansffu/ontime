package hansffu.ontime.ui.navigation

import androidx.compose.runtime.Composable
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
                        timetableViewModel.setCurrentStop(it)
                        navController.navigate(Screen.Timetable.route)
                    }
                )
            }
            composable(route = Screen.Timetable.route) {
                Timetable(
                    timetableViewModel = timetableViewModel,
                )
            }
        }

    }
}

