package hansffu.ontime.service

import android.location.Location
import hansffu.ontime.api.Feature
import hansffu.ontime.api.StopsApi
import hansffu.ontime.model.Stop
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

private val TAG = "StopService"


class StopService {
    private val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.entur.io")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

    private val stopsApi = retrofit.create(StopsApi::class.java)

    fun findStopsNear(location: Location): Single<List<Stop>> = stopsApi
            .getNearbyStops(location.latitude, location.longitude, 1, 10, "venue")
            .subscribeOn(Schedulers.io())
            .map { it.features.map(::toStop) }

    private fun toStop(feature: Feature) = Stop(feature.properties.name, feature.properties.id)
}
