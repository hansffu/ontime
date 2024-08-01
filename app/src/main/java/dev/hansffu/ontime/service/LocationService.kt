package dev.hansffu.ontime.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class LocationService @Inject constructor(
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    @ApplicationContext private val context: Context,
) {
    val locationPermissions =
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

    suspend fun getLatestLocation(): LocationResult = when {
        !checkLocationPermission() -> LocationResult.NoPermission
        isEmulator -> LocationResult.Success(getMockLocation())
        else -> {
            val location =
                fusedLocationProviderClient.getCurrentLocation(
                    CurrentLocationRequest.Builder()
                        .setMaxUpdateAgeMillis(30.seconds.inWholeMilliseconds)
                        .build(), null
                ).await()
            LocationResult.Success(location)
        }
    }

    private fun checkLocationPermission(): Boolean = locationPermissions.any {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}

private val isEmulator: Boolean
    get() = Build.HARDWARE.equals("ranchu")

private suspend fun getMockLocation(): Location {
    delay(1000)
    return Location("flp").apply {
        longitude = 10.796757
        latitude = 59.932715
        bearing = Random.nextFloat() * 360
    }
}

sealed interface LocationResult {
    data class Success(val location: Location) : LocationResult
    data object NoPermission : LocationResult
}