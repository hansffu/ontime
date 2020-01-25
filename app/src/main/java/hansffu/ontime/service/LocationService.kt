package hansffu.ontime.service

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.location.Location
import com.patloew.rxlocation.RxLocation
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Maybe
import io.reactivex.Observable

fun requestLocationPermission(rxPermissions: RxPermissions): Observable<Boolean> =
    rxPermissions.request(ACCESS_FINE_LOCATION)

@SuppressLint("MissingPermission")
fun requestLocation(rxLocation: RxLocation): Maybe<Location> {
    return rxLocation
        .location()
        .lastLocation()
}

// fun requestLocation(rxLocation: RxLocation): Maybe<Location> =
//    Location("flp").apply {
//        longitude = 10.796757
//        latitude = 59.932715
//    }.let { Maybe.just(it) }
