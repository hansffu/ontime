package dev.hansffu.ontime.viewmodels

import android.location.Location
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.hansffu.ontime.service.LocationResult
import dev.hansffu.ontime.service.LocationService
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(private val locationService: LocationService) :
    ViewModel() {

    private val mutableLocationState = mutableStateOf<LocationState>(LocationState.Uninitialized)
    val locationState: State<LocationState> = mutableLocationState

    val locationPermissions: List<String> = locationService.locationPermissions

    fun refreshLocation() {
        viewModelScope.launch {
            if (!locationService.checkLocationPermission()) {
                mutableLocationState.value = LocationState.NoPermission
            } else {
                mutableLocationState.value = LocationState.Loading
                when (val result = locationService.getLatestLocation()) {
                    is LocationResult.Success -> mutableLocationState.value =
                        LocationState.LocationFound(result.location)

                    is LocationResult.NoPermission -> mutableLocationState.value =
                        LocationState.NoPermission

                }
            }
        }
    }

}


sealed interface LocationState {
    data object Uninitialized : LocationState
    data object NoPermission : LocationState
    data object Loading : LocationState
    data class LocationFound(val location: Location) : LocationState
}
