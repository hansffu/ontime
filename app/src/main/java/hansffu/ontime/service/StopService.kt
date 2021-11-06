package hansffu.ontime.service

import android.location.Location
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import hansffu.ontime.StopPlaceQuery
import hansffu.ontime.api.Feature
import hansffu.ontime.api.Properties
import hansffu.ontime.api.StopsApi
import hansffu.ontime.model.Stop
import hansffu.ontime.model.TransportationType
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

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

    private fun toCategoryText(stop: Properties): List<TransportationType> =
        stop.category.mapNotNull {
            when (it) {
                "onstreetTram" -> TransportationType.TRAM
                "onstreetBus" -> TransportationType.BUS
                "metroStation" -> TransportationType.METRO
                else -> null
            }
        }


    suspend fun findStopsNear(location: Location): List<Stop> {
        println(location.latitude)
        println(location.longitude)
        return stopsApi
            .getNearbyStops(location.latitude, location.longitude, 2, 20, "venue", "parent")
            .features.map(Feature::properties)
            .map {
                println(it)
                it
            }
            .map { Stop(it.name, it.id, toCategoryText(it)) }
    }

    suspend fun getDepartures(id: String): StopPlaceQuery.Data {
        val response = withContext(Dispatchers.IO) {
            apolloClient.query(
                StopPlaceQuery.builder()
                    .id(id)
                    .build()
            ).await()
        }
        return response.data ?: throw IOException()
    }
}
