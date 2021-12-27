package hansffu.ontime.service

import android.location.Location
import android.util.Log
import com.apollographql.apollo3.api.ApolloResponse
import hansffu.ontime.graphql.NearbyStopsQuery
import hansffu.ontime.graphql.StopPlaceQuery
import hansffu.ontime.graphql.enturApolloClient
import hansffu.ontime.model.Stop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

private const val TAG = "StopService"

class StopService {
    suspend fun findStopsNear(location: Location): List<Stop> {
        Log.d(TAG, "requesting stops")
        val response = enturApolloClient.query(
            NearbyStopsQuery(
                latitude = location.latitude,
                longitude = location.longitude
            )
        ).execute()
        return response.data
            ?.nearest?.edges?.mapNotNull { it?.node?.place?.onStopPlace }
            ?.map { Stop(it.name, it.id) }
            ?: emptyList()
    }

    suspend fun getDepartures(id: String): StopPlaceQuery.Data {
        Log.d(TAG, "requesting departures for $id")
        val response: ApolloResponse<StopPlaceQuery.Data> = withContext(Dispatchers.IO) {
            enturApolloClient.query(
                StopPlaceQuery(
                    id = id
                )
            ).execute()
        }
        return response.dataAssertNoErrors
    }
}
