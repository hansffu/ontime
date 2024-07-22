package hansffu.ontime

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.currentCoroutineContext

class LocationViewModel(private val application: Application) : AndroidViewModel(application) {
    private val fusedLocation = LocationServices.getFusedLocationProviderClient(application)

    private val mutableLocationState = mutableStateOf<LocationState>(LocationState.Uninitialized)
    val locationState: State<LocationState> = mutableLocationState

    @SuppressLint("MissingPermission")
    fun refreshLocation() {
        mutableLocationState.value = LocationState.Loading
        val locationTask =
            fusedLocation.getCurrentLocation(CurrentLocationRequest.Builder().build(), null)
        locationTask.addOnSuccessListener { location ->
            mutableLocationState.value = LocationState.LocationFound(location ?: requestMockLocation())
        }
    }

}

sealed interface LocationState {
    data object Uninitialized : LocationState
    data object Loading : LocationState
    data class LocationFound(val location: Location) : LocationState
}