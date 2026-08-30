package dev.hansffu.ontime.service

import dev.hansffu.ontime.database.dao.BoardDeparture
import dev.hansffu.ontime.entur.EnturModule
import dev.hansffu.ontime.graphql.StopPlaceQuery
import java.util.Collections
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/** Opt-in network smoke test: ONTIME_LIVE_ENTUR_TEST=1 ./gradlew :app:testDebugUnitTest --rerun-tasks */
class BoardDeparturesLiveTest {
    @Test fun compareUnfilteredAndFilteredRequestsAgainstEntur() {
        assumeTrue("Live Entur test is opt-in", System.getenv("ONTIME_LIVE_ENTUR_TEST") == "1")
        val requests: MutableList<JsonObject> = Collections.synchronizedList(mutableListOf())
        val responseBytes: MutableList<Long> = Collections.synchronizedList(mutableListOf())
        val module = EnturModule()
        val http = module.provideHttpClient().newBuilder().addInterceptor { chain ->
            val body = Buffer().apply { chain.request().body!!.writeTo(this) }.readUtf8()
            requests += Json.parseToJsonElement(body).jsonObject
            chain.proceed(chain.request()).also { response ->
                responseBytes += response.peekBody(1_000_000).contentLength()
            }
        }.build()
        val client = module.provideEnturApolloClient(http)
        try {
            runBlocking {
                withTimeout(60_000) {
                    val stopIds = listOf("NSR:StopPlace:6032", "NSR:StopPlace:58194")
                    val rows = listOf(
                        row(stopIds[0], "23", "Simensbråten"),
                        row(stopIds[0], "23", "Lysaker"),
                        row(stopIds[1], "4", "Vestli"),
                    )
                    val baseline = stopIds.map { client.query(StopPlaceQuery(it)).execute().dataAssertNoErrors }
                    val baselineBytes = responseBytes.sum()
                    assertEquals(2, requests.size)

                    val service = BoardDeparturesService(client) { 0L }
                    service.getDepartures(rows)
                    assertEquals(4, requests.size) // One generated query per stop, including catalogues.
                    val optimized = service.getDepartures(rows)
                    assertEquals(6, requests.size) // One filtered query per stop.
                    val filteredVariables = requests.takeLast(2).map { it.getValue("variables").jsonObject }
                    filteredVariables.forEach {
                        assertFalse(it.getValue("includeLineCatalog").jsonPrimitive.content.toBoolean())
                        assertTrue(it.getValue("lineIds").jsonArray.isNotEmpty())
                    }
                    val expectedCodes = mapOf(stopIds[0] to "23", stopIds[1] to "4")
                    optimized.forEach { (id, stop) ->
                        checkNotNull(stop)
                        assertNull(stop.quays)
                        assertTrue(stop.estimatedCalls.all { it.serviceJourney.line.publicCode == expectedCodes.getValue(id) })
                    }
                    println("Legacy: requests=2, decoded JSON bytes=$baselineBytes, calls=" +
                        baseline.sumOf { it.stopPlace!!.estimatedCalls.size })
                    println("Cold refresh: requests=2, decoded JSON bytes=${responseBytes.subList(2, 4).sum()}")
                    println("Filtered refresh: requests=2, decoded JSON bytes=${responseBytes.takeLast(2).sum()}, calls=" +
                        optimized.values.sumOf { it!!.estimatedCalls.size })
                    println("Filtered variables: $filteredVariables")
                }
            }
        } finally {
            client.close()
            http.dispatcher.executorService.shutdown()
            http.connectionPool.evictAll()
        }
    }

    private fun row(stop: String, code: String, destination: String) =
        BoardDeparture(1, stop, stop, null, null, code, destination)
}
