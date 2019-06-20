package hansffu.ontime.service

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.location.Location
import com.patloew.rxlocation.RxLocation
import com.vanniktech.rxpermission.RealRxPermission
import io.reactivex.Maybe

fun requestLocationPermission(context: Context) = RealRxPermission.getInstance(context).request(ACCESS_FINE_LOCATION)

fun requestLocation(context: Context): Maybe<Location> = RxLocation(context)
        .location()
        .lastLocation()

//fun requestLocation(context: Context): Maybe<Location> = Maybe.just(
//        Location("flp").apply {
//            longitude = 10.796757
//            latitude = 59.932715
//        })
