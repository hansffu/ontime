package dev.hansffu.ontime

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.os.Build
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val fusedLocation = LocationServices.getFusedLocationProviderClient(application)

    private val mutableLocationState = mutableStateOf<LocationState>(LocationState.Uninitialized)
    val locationState: State<LocationState> = mutableLocationState

    @SuppressLint("MissingPermission")
    fun refreshLocation() {
        mutableLocationState.value = LocationState.Loading
        if (Build.HARDWARE.equals("ranchu")) {
            // Mock location for emulator
            viewModelScope.launch {
                requestMockLocation()
            }
        }
        val locationTask =
            fusedLocation.getCurrentLocation(CurrentLocationRequest.Builder().build(), null)
        locationTask.addOnSuccessListener { location: Location? ->
            location?.let {
                mutableLocationState.value = LocationState.LocationFound(it)
            }
        }
    }

    private suspend fun requestMockLocation() {
        delay(1000)

        mutableLocationState.value = LocationState.LocationFound(
            Location("flp").apply {
                longitude = 10.796757
                latitude = 59.932715
                bearing = Random.nextFloat() * 360
            }
        )
    }
}


sealed interface LocationState {
    data object Uninitialized : LocationState
    data object Loading : LocationState
    data class LocationFound(val location: Location) : LocationState
}