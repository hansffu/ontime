package hansffu.ontime

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

        setContent {
            MainNavigation(favoriteModel, timetableViewModel, locationViewModel)
        }
    }
}

