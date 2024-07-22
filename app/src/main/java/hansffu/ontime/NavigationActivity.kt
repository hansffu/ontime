package hansffu.ontime

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import hansffu.ontime.ui.navigation.MainNavigation

class NavigationActivity : ComponentActivity() {

    private val favoriteModel: StopListViewModel by viewModels()
    private val timetableViewModel: TimetableViewModel by viewModels()
    private val locationViewModel: LocationViewModel by viewModels()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        favoriteModel.getLocationHolder().observe(this) {
//            if (it is LocationHolder.NoPermission) requestPermissions(
//                arrayOf(
//                    ACCESS_FINE_LOCATION,
//                    ACCESS_COARSE_LOCATION
//                ), 123
//            )
//        }

        setContent {
            MainNavigation(favoriteModel, timetableViewModel, locationViewModel)
        }

    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        favoriteModel.refreshPermissions()
//    }
}

