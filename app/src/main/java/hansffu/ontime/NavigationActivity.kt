package hansffu.ontime

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import hansffu.ontime.model.StopListType
import hansffu.ontime.ui.navigation.Screen
import hansffu.ontime.ui.stoplist.StopListUi
import hansffu.ontime.ui.theme.OntimeTheme
import hansffu.ontime.ui.timetable.Timetable

class NavigationActivity : ComponentActivity() {

    private val favoriteModel: StopListViewModel by viewModels()
    private val timetableViewModel: TimetableViewModel by viewModels()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        favoriteModel.getLocationHolder().observe(this) {
            if (it is LocationHolder.NoPermission) requestPermissions(
                arrayOf(
                    ACCESS_FINE_LOCATION,
                    ACCESS_COARSE_LOCATION
                ), 123
            )
        }

        setContent {
            OntimeApp(favoriteModel, timetableViewModel)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        favoriteModel.refreshPermissions()
    }
}

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun OntimeApp(favoriteModel: StopListViewModel, timetableViewModel: TimetableViewModel) {
    OntimeTheme {
        val navController = rememberSwipeDismissableNavController()
        SwipeDismissableNavHost(navController = navController, Screen.FavoriteStops.route) {
            StopListType.values().forEach { stopListType ->
                composable(
                    when (stopListType) {
                        StopListType.FAVORITES -> Screen.FavoriteStops
                        StopListType.NEARBY -> Screen.NearbyStops
                    }.route
                ) {
                    StopListUi(
                        favoriteModel = favoriteModel,
                        stopListType = stopListType,
                        navigateTo = { navController.navigate(it.route) },
                        onStopSelected = {
                            timetableViewModel.setCurrentStop(it)
                            navController.navigate(Screen.Timetable.route)
                        }
                    )
                }
            }
            composable(route = Screen.Timetable.route) {
                Timetable(
                    timetableViewModel = timetableViewModel,
                )
            }
        }

    }
}

