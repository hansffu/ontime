package dev.hansffu.ontime.service

import dev.hansffu.ontime.model.Stop
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

class SearchService @Inject constructor(
    private val httpClient: OkHttpClient,
    private val stopService: StopService,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val baseUrl =
        HttpUrl.Builder().scheme("https").host("api.entur.io")
            .addPathSegments("geocoder/v1/autocomplete")
            .addQueryParameter("layers", "venue")
            .build()

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun search(searchString: String): List<Stop> {
        val url = baseUrl.newBuilder().addQueryParameter("text", searchString).build()
        val request = Request.Builder().get().url(url).build()
        val searchResults = withContext(Dispatchers.IO) {
            httpClient.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "Search request failed" }
                response.body.byteStream().use { stream ->
                    json.decodeFromStream<AutocompleteResponse>(stream).features.map { feature ->
                        Stop(
                            name = feature.properties.name,
                            id = feature.properties.id,
                            longitude = feature.geometry?.coordinates?.getOrNull(0),
                            latitude = feature.geometry?.coordinates?.getOrNull(1),
                        )
                    }
                }
            }
        }
        val detailsById =
            try {
                stopService.getStops(searchResults.map(Stop::id)).associateBy(Stop::id)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                emptyMap()
            }
        return searchResults.map { result ->
            detailsById[result.id]?.copy(
                name = result.name,
                latitude = result.latitude,
                longitude = result.longitude,
            ) ?: result
        }
    }
}

@Serializable
data class AutocompleteResponse(val features: List<Feature>)

@Serializable
data class Feature(
    val properties: Properties,
    val geometry: Geometry? = null,
)

@Serializable
data class Properties(val id: String, val name: String)

@Serializable
data class Geometry(val coordinates: List<Double>)
