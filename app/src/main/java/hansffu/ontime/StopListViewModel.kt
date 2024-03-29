package hansffu.ontime

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.lifecycle.*
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import hansffu.ontime.database.AppDatabase
import hansffu.ontime.model.Stop
import hansffu.ontime.service.StopService
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

class StopListViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDb(application)
    private val stopService = StopService()

    @SuppressLint("MissingPermission")
    private val fusedLocation = LocationServices.getFusedLocationProviderClient(application)


    private val hasLocationPermission: MutableLiveData<Boolean> = MutableLiveData(
        hasLocationPermission()
    )

    private fun hasLocationPermission(): Boolean =
        checkSelfPermission(getApplication(), ACCESS_FINE_LOCATION) == PERMISSION_GRANTED ||
                checkSelfPermission(getApplication(), ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED

    fun refreshPermissions() {
        hasLocationPermission.value = hasLocationPermission()
    }

    val favoriteStops: LiveData<List<Stop>> =
        db.favoritesDao().getAll().map { stops ->
            stops.map { Stop(it.name, it.id) }
        }

    @SuppressLint("MissingPermission")
    val location: LiveData<LocationHolder> =
        hasLocationPermission.switchMap { hasPermission ->
            liveData {
                if (!hasPermission) {
                    emit(LocationHolder.NoPermission)
                } else {
                    emit(LocationHolder.Loading(null))
                    requestLocation().consumeEach {
                        emit(LocationHolder.LocationFound(it))
                    }
                }
            }
        }

    val nearbyStops: LiveData<List<Stop>> = location.switchMap {
        liveData {
            emit(
                if (it is LocationHolder.LocationFound) stopService.findStopsNear(it.location)
                else emptyList()
            )
        }
    }

    fun getLocationHolder(): LiveData<LocationHolder> = location


    @RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"])
    private suspend fun requestLocation(): Channel<Location> {
        val locations = Channel<Location>()
        fusedLocation.requestLocationUpdates(
            LocationRequest.create(),
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    viewModelScope.launch { locations.send(locationResult.lastLocation) }
                }
            },
            Looper.getMainLooper()
        )
        return locations
    }


}

sealed interface LocationHolder {
    class LocationFound(val location: Location) : LocationHolder
    class Loading(val previousLocation: Location?) : LocationHolder
    object NoPermission : LocationHolder
}

fun requestMockLocation(): Location =
    Location("flp").apply {
        longitude = 10.796757
        latitude = 59.932715
    }
