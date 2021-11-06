package hansffu.ontime.service

import android.location.Location
import com.apollographql.apollo.coroutines.await
import hansffu.ontime.NearbyStopsQuery
import hansffu.ontime.StopPlaceQuery
import hansffu.ontime.graphql.enturApolloClient
import hansffu.ontime.model.Stop
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

    suspend fun findStopsNear(location: Location): List<Stop> {
        val response = enturApolloClient.query(
            NearbyStopsQuery(
                latitude = location.latitude,
                longitude = location.longitude
            )
        ).await()
        return response.data
            ?.nearest?.edges?.mapNotNull { it?.node?.place?.asStopPlace }
            ?.map { Stop(it.name, it.id) }
            ?: emptyList()
    }

    suspend fun getDepartures(id: String): StopPlaceQuery.Data {
        val response = withContext(Dispatchers.IO) {
            enturApolloClient.query(
                StopPlaceQuery(
                    id = id
                )
            ).await()
        }
        return response.data ?: throw IOException()
    }
}
