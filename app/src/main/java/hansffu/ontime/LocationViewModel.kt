package hansffu.ontime

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.os.Build
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val fusedLocation = LocationServices.getFusedLocationProviderClient(application)

    private val mutableLocationState = mutableStateOf<LocationState>(LocationState.Uninitialized)
    val locationState: State<LocationState> = mutableLocationState

    @SuppressLint("MissingPermission")
    fun refreshLocation() {
        mutableLocationState.value = LocationState.Loading
        val locationTask =
            fusedLocation.getCurrentLocation(CurrentLocationRequest.Builder().build(), null)
        if (Build.HARDWARE.equals("ranchu")) {
            // Mock location for emulator
            mutableLocationState.value = LocationState.LocationFound(requestMockLocation())
        }
        locationTask.addOnSuccessListener { location ->
            mutableLocationState.value =
                LocationState.LocationFound(location ?: requestMockLocation())
        }
    }

}

fun requestMockLocation(): Location =
    Location("flp").apply {
        longitude = 10.796757
        latitude = 59.932715
    }

sealed interface LocationState {
    data object Uninitialized : LocationState
    data object Loading : LocationState
    data class LocationFound(val location: Location) : LocationState
}