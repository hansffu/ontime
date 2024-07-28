package dev.hansffu.ontime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.hansffu.ontime.ui.navigation.MainNavigation
import dev.hansffu.ontime.viewmodels.LocationViewModel
import dev.hansffu.ontime.viewmodels.TimetableViewModel

@AndroidEntryPoint
class NavigationActivity : ComponentActivity() {
    private val timetableViewModel: TimetableViewModel by viewModels()
    private val locationViewModel: LocationViewModel by viewModels()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MainNavigation(
                timetableViewModel = timetableViewModel,
                locationViewModel = locationViewModel
            )
        }
    }
}

