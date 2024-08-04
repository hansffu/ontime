package dev.hansffu.ontime.service

import android.util.Log
import dev.hansffu.ontime.model.Stop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

private const val TAG = "SearchService"

class SearchService @Inject constructor(private val httpClient: OkHttpClient) {
    private val json = Json {
        ignoreUnknownKeys = true
    }
    private val baseUrl = HttpUrl.Builder().scheme("https").host("api.entur.io")
        .addPathSegments("geocoder/v1/autocomplete")
        .addQueryParameter("layers", "venue")
        .build()

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun search(searchString: String): List<Stop> {
        val url = baseUrl.newBuilder()
            .addQueryParameter("text", searchString)
            .build()
        val request = Request.Builder().get().url(url).build()
        val response = withContext(Dispatchers.IO) {
            val resp = httpClient.newCall(request).execute()
            resp.body?.byteStream()?.let {
                json.decodeFromStream<AutocompleteResponse>(it)
            }
        }
        Log.i(TAG, response.toString())
        return response?.features?.map { Stop(name = it.properties.name, id = it.properties.id) }
            ?: emptyList()
    }
}

@Serializable
data class AutocompleteResponse(
    val features: List<Feature>
)

@Serializable
data class Feature(
    val properties: Properties
)

@Serializable
data class Properties(
    val id: String,
    val name: String,
)