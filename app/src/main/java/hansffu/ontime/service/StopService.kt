package hansffu.ontime.service

import android.location.Location
import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import hansffu.ontime.StopPlaceQuery
import hansffu.ontime.api.Feature
import hansffu.ontime.api.Properties
import hansffu.ontime.api.StopsApi
import hansffu.ontime.model.Stop
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
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

    private fun toCategoryText(stop: Properties): String {
        val categories = stop.category.map {
            when (it) {
                "onstreetTram" -> "Trikk"
                "onstreetBus" -> "Buss"
                "metroStation" -> "T-bane"
                else -> ""
            }
        }.joinToString(separator = ", ", prefix = " [", postfix = "]")
        Log.d("categories", categories)
        return categories
    }

    suspend fun findStopsNear(location: Location): List<Stop> = stopsApi
        .getNearbyStops(location.latitude, location.longitude, 2, 20, "venue")
        .features.map(Feature::properties)
        .map { Stop(it.name + toCategoryText(it), it.id) }

    suspend fun getDepartures(id: String): StopPlaceQuery.Data {
        val response = withContext(Dispatchers.IO) {
            apolloClient.query(
                StopPlaceQuery.builder()
                    .id(id)
                    .build()
            ).await()
        }
        return response.data ?: throw IOException()
        // return Rx2Apollo.from(watcher)
        //         .subscribeOn(Schedulers.io())
        //         .map { it.data() }
        //         .filterNotNull()
        //         .filter(Objects::nonNull)
    }
}

fun <T> Observable<T?>.filterNotNull(): Observable<T> = filter { it != null }.map { it!! }
