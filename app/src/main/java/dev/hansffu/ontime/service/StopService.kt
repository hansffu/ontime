package dev.hansffu.ontime.service

import android.location.Location
import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import dev.hansffu.ontime.graphql.FavoriteStopsQuery
import dev.hansffu.ontime.graphql.NearbyStopsQuery
import dev.hansffu.ontime.graphql.StopPlaceQuery
import dev.hansffu.ontime.graphql.type.TransportMode
import dev.hansffu.ontime.model.Stop
import dev.hansffu.ontime.model.StopTransportMode
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "StopService"

class StopService @Inject constructor(
    private val enturApolloClient: ApolloClient,
) {
    suspend fun findStopsNear(location: Location): List<Stop> {
        Log.d(TAG, "requesting stops")
        val response =
            enturApolloClient.query(
                NearbyStopsQuery(
                    latitude = location.latitude,
                    longitude = location.longitude,
                )
            ).execute()
        return response.data
            ?.nearest?.edges?.mapNotNull { edge ->
                edge?.node?.let { node ->
                    node.place?.onStopPlace?.let { stopPlace -> node to stopPlace }
                }
            }?.map { (node, stopPlace) ->
                Stop(
                    name = stopPlace.name,
                    id = stopPlace.id,
                    latitude = stopPlace.latitude,
                    longitude = stopPlace.longitude,
                    transportModes = stopPlace.transportMode.toStopTransportModes(),
                    distanceMeters = node.distance,
                )
            }.orEmpty()
    }

    suspend fun getStops(ids: List<String>): List<Stop> {
        if (ids.isEmpty()) return emptyList()
        val response = enturApolloClient.query(FavoriteStopsQuery(ids)).execute()
        return response.data?.stopPlaces?.mapNotNull { stopPlace ->
            stopPlace?.let {
                Stop(
                    name = it.name,
                    id = it.id,
                    latitude = it.latitude,
                    longitude = it.longitude,
                    transportModes = it.transportMode.toStopTransportModes(),
                )
            }
        }.orEmpty()
    }

    suspend fun getDepartures(id: String): StopPlaceQuery.Data {
        Log.d(TAG, "requesting departures for $id")
        val response: ApolloResponse<StopPlaceQuery.Data> =
            withContext(Dispatchers.IO) {
                enturApolloClient.query(StopPlaceQuery(id = id)).execute()
            }
        return response.dataAssertNoErrors
    }
}

private fun List<TransportMode?>?.toStopTransportModes(): Set<StopTransportMode> =
    orEmpty()
        .mapNotNull { mode -> mode?.let { StopTransportMode.fromApiName(it.rawValue) } }
        .toSet()
