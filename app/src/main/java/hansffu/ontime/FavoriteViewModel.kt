package hansffu.ontime

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager.*
import android.location.Location
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.lifecycle.*
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import hansffu.ontime.model.Stop
import hansffu.ontime.model.StopListModel
import hansffu.ontime.model.StopListType
import hansffu.ontime.model.StopListType.*
import hansffu.ontime.service.FavoriteService
import hansffu.ontime.service.StopService
import kotlinx.coroutines.*
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FavoriteViewModel(application: Application) : AndroidViewModel(application) {
    private val favoriteService: FavoriteService = FavoriteService(application.applicationContext)
    private val stopService = StopService()

    @SuppressLint("MissingPermission")
    private val fusedLocation = LocationServices.getFusedLocationProviderClient(application)


    private val currentList: MutableLiveData<StopListType> = MutableLiveData(FAVORITES)

    private val hasLocationPermission: MutableLiveData<Boolean> = MutableLiveData(
        hasLocationPermission()
    )

    private fun hasLocationPermission(): Boolean =
        checkSelfPermission(getApplication(), ACCESS_FINE_LOCATION) == PERMISSION_GRANTED ||
                checkSelfPermission(getApplication(), ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED

    fun refreshPermissions() {
        hasLocationPermission.value = hasLocationPermission()
    }


    private val favoriteStops: LiveData<List<Stop>> = liveData {
        emit(favoriteService.getFavorites())
    }

    @SuppressLint("MissingPermission")
    val location: LiveData<LocationHolder> =
        Transformations.switchMap(hasLocationPermission) { hasPermission ->
            println("location permission: $hasPermission")
            liveData {
                if (!hasPermission) {
                    emit(LocationHolder.NoPermission)
                } else {
                    emit(LocationHolder.Loading(null))
                    emit(LocationHolder.LocationFound(requestLocation()))
                }
            }
        }

    private val nearbyStops: LiveData<List<Stop>> = Transformations.switchMap(location) {
        liveData {
            println("find stops near $it")
            emit(if (it is LocationHolder.LocationFound) stopService.findStopsNear(it.location) else emptyList())
        }
    }


    fun setCurrentList(type: StopListType) {
        currentList.value = type
    }

    fun getStops(): LiveData<List<Stop>> =
        Transformations.switchMap(currentList) { type: StopListType ->
            when (type) {
                FAVORITES -> favoriteStops
                NEARBY -> nearbyStops
            }
        }


    //    fun getNearbyStops(): LiveData<List<Stop>> = nearbyStops
    fun getLocationHolder(): LiveData<LocationHolder> = location

    fun load() {
    }

    @RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"])
    private suspend fun requestLocation(): Location {
        return suspendCoroutine {callback ->
            fusedLocation.requestLocationUpdates(
                LocationRequest.create(),
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        println("location callback")
                        viewModelScope.launch {
                            callback.resume(locationResult.lastLocation)
                        }
                    }
                },
                Looper.getMainLooper()
            )
        }
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
