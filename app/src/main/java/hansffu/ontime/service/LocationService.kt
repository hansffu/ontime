package hansffu.ontime.service

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import arrow.core.left
import arrow.core.right
import arrow.fx.IO
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsOptions
import hansffu.ontime.R

private const val TAG = "LocationService"

class LocationService(private val fragment: Fragment) {

    private val locationProvider = LocationServices.getFusedLocationProviderClient(fragment.requireContext())

    @SuppressLint("MissingPermission")
    fun getLocation(): IO<Location> = IO.async { callback ->
        fragment.runWithPermissions(Manifest.permission.ACCESS_FINE_LOCATION, options = QuickPermissionsOptions(
                handleRationale = false,
                permissionsDeniedMethod = {
                    Toast.makeText(fragment.context, R.string.no_location_permission, Toast.LENGTH_LONG).show()
                    callback(IllegalStateException("failed to get location").left())
                }
        )) {
            locationProvider.requestLocationUpdates(LocationRequest(), object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    Log.i(TAG, "location: ${result.lastLocation}")
                    callback(result.lastLocation.right())
                }
            }, null)
        }
    }
}
