package hansffu.ontime.service

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.support.v4.app.ActivityCompat
import android.widget.Toast
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks

internal const val PERMISSIONS_REQUEST_FINE_LOCATION = 1

fun hasLocationPermission(activity: Activity) =
    ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

fun requestLocationPermission(activity: Activity) =
    ActivityCompat.requestPermissions(activity,
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
        PERMISSIONS_REQUEST_FINE_LOCATION)

fun handlePermissionResult(activity: Activity, grantResults: IntArray): Task<Location> {
    if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
        Toast.makeText(activity.applicationContext, "Ikke tilgang til plassering", Toast.LENGTH_SHORT).show()
    }
    return requestWatchLocation(activity)
}

fun requestWatchLocation(activity: Activity): Task<Location> {
    return if (hasLocationPermission(activity)) {
        val locationProvider = LocationServices.getFusedLocationProviderClient(activity)
        locationProvider.lastLocation
    } else {
        Tasks.forCanceled<Location>()
    }
}
