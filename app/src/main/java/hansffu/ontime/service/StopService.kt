package hansffu.ontime.service

import android.location.Location
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.rx2.Rx2Apollo
import hansffu.ontime.StopPlaceQuery
import hansffu.ontime.api.Feature
import hansffu.ontime.api.StopsApi
import hansffu.ontime.model.Stop
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

private val TAG = "StopService"


class StopService {
    private val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.entur.io")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

    private val stopsApi = retrofit.create(StopsApi::class.java)

    private val apolloClient = ApolloClient.builder()
            .serverUrl("https://api.entur.io/journey-planner/v2/graphql")
            .build()


    fun findStopsNear(location: Location): Single<List<Stop>> = stopsApi
            .getNearbyStops(location.latitude, location.longitude, 2, 20, "venue")
            .subscribeOn(Schedulers.io())
            .map { it.features.map(::toStop) }

    private fun toStop(feature: Feature) = Stop(feature.properties.name, feature.properties.id)

    fun getDepartures(id: String): Observable<StopPlaceQuery.Data> {
        val watcher = apolloClient.query(
                StopPlaceQuery.builder()
                        .id(id)
                        .build())
        return Rx2Apollo.from(watcher)
                .subscribeOn(Schedulers.io())
                .map { it.data() }
                .filterNotNull()
                .filter(Objects::nonNull)
    }

}

fun <T> Observable<T?>.filterNotNull(): Observable<T> = filter { it != null }.map { it!! }
