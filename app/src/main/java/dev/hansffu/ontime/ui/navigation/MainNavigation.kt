package dev.hansffu.ontime.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.toRoute
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.nav.SwipeDismissableNavHost
import com.google.android.horologist.compose.nav.composable
import dev.hansffu.ontime.ui.stoplist.StopsScreen
import dev.hansffu.ontime.ui.stoplist.nearby.NearbyStopsScreen
import dev.hansffu.ontime.ui.stoplist.search.SearchScreen
import dev.hansffu.ontime.ui.theme.OntimeTheme
import dev.hansffu.ontime.ui.timetable.TimetableUi

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun MainNavigation() {
    OntimeTheme {
        AppScaffold {
            val navController = rememberSwipeDismissableNavController()
            SwipeDismissableNavHost(
                navController = navController,
                startDestination = Screen.Favorites
            ) {
                composable<Screen.Favorites> {
                    StopsScreen(navController = navController)
                }
                composable<Screen.Nearby> {
                    NearbyStopsScreen(navController = navController)
                }
                composable<Screen.TextSearch> {
                    val route = it.toRoute<Screen.TextSearch>()
                    SearchScreen(searchString = route.searchString)
                }
                composable<Screen.Timetable> {
                    val route = it.toRoute<Screen.Timetable>()
                    TimetableUi(
                        stopId = route.stopId,
                        stopName = route.stopName
                    )
                }
            }
        }
    }

}

