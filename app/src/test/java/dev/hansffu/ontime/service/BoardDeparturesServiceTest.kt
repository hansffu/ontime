package dev.hansffu.ontime.service

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.network.okHttpClient
import dev.hansffu.ontime.database.dao.BoardDeparture
import dev.hansffu.ontime.graphql.StopPlaceQuery
import java.util.Collections
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.*
import org.junit.Test

class BoardDeparturesServiceTest {
    @Test fun emptyBoardDoesNotMakeARequest() = runTest {
        Fixture().use { fixture ->
            assertTrue(fixture.service.getDepartures(emptyList()).isEmpty())
            assertTrue(fixture.requests.isEmpty())
        }
    }

    @Test fun usesOneGeneratedQueryPerDistinctStopWithIndependentLineFilters() = runTest {
        Fixture().use { fixture ->
            val rows = listOf(row("A", "23"), row("A", "23", "Other direction"), row("B", "4"))
            val cold = fixture.service.getDepartures(rows)
            assertEquals(setOf("A", "B"), cold.keys)
            assertEquals(2, fixture.requests.size)
            assertEquals(JsonNull, fixture.variables("A")["lineIds"])
            assertEquals(JsonPrimitive(true), fixture.variables("A")["includeLineCatalog"])
            val warm = fixture.service.getDepartures(rows)
            assertEquals(4, fixture.requests.size)
            assertEquals(listOf("ONE:Line:23"), fixture.variables("A").ids("lineIds"))
            assertEquals(listOf("TWO:Line:4"), fixture.variables("B").ids("lineIds"))
            assertEquals(JsonPrimitive(false), fixture.variables("A")["includeLineCatalog"])
            assertEquals(JsonPrimitive(false), fixture.variables("B")["includeLineCatalog"])
            assertEquals(listOf("23"), warm.getValue("A")!!.estimatedCalls.map { it.serviceJourney.line.publicCode })
            assertEquals(listOf("4"), warm.getValue("B")!!.estimatedCalls.map { it.serviceJourney.line.publicCode })
            assertTrue(fixture.requests.all { it.getValue("operationName").jsonPrimitive.content == "StopPlace" })
        }
    }

    @Test fun stopQueriesRunInParallel() = runTest {
        Fixture().use { fixture ->
            fixture.barrier = CyclicBarrier(2)
            val result = fixture.service.getDepartures(listOf(row("A", "23"), row("B", "4")))
            assertEquals(setOf("A", "B"), result.keys)
            assertEquals(2, fixture.requests.size)
        }
    }

    @Test fun allIdsForSamePublicCodeAreIncludedAndDeduplicatedAcrossQuays() = runTest {
        Fixture().use { fixture ->
            fixture.catalogs["A"] = listOf("ONE:Line:23" to "23", "OTHER:Line:23" to "23")
            fixture.service.getDepartures(listOf(row("A", "23")))
            fixture.service.getDepartures(listOf(row("A", "23")))
            assertEquals(listOf("ONE:Line:23", "OTHER:Line:23"), fixture.variables().ids("lineIds"))
        }
    }

    @Test fun addingALineReusesFullCatalogIncludingLinesWithNoCurrentDepartures() = runTest {
        Fixture().use { fixture ->
            fixture.noDepartures = true
            fixture.service.getDepartures(listOf(row("A", "23")))
            fixture.service.getDepartures(listOf(row("A", "23"), row("A", "24")))
            assertEquals(listOf("ONE:Line:23", "ONE:Line:24"), fixture.variables().ids("lineIds"))
            assertEquals(JsonPrimitive(false), fixture.variables()["includeLineCatalog"])
            assertEquals(2, fixture.requests.size)
        }
    }

    @Test fun unknownLineDoesNotSilentlyDropOtherRequestedLines() = runTest {
        Fixture().use { fixture ->
            fixture.service.getDepartures(listOf(row("A", "23")))
            fixture.service.getDepartures(listOf(row("A", "23"), row("A", "New line")))
            assertEquals(JsonNull, fixture.variables()["lineIds"])
            assertEquals(JsonPrimitive(true), fixture.variables()["includeLineCatalog"])
        }
    }

    @Test fun expiredCatalogIsRefreshedInTheSameRequest() = runTest {
        Fixture().use { fixture ->
            val rows = listOf(row("A", "23"))
            fixture.service.getDepartures(rows)
            fixture.now = BoardDeparturesService.CATALOG_TTL_MILLIS
            fixture.catalogs["A"] = listOf("NEW:Line:23" to "23")
            fixture.service.getDepartures(rows)
            assertEquals(JsonPrimitive(true), fixture.variables()["includeLineCatalog"])
            fixture.service.getDepartures(rows)
            assertEquals(listOf("NEW:Line:23"), fixture.variables().ids("lineIds"))
            assertEquals(3, fixture.requests.size)
        }
    }

    @Test fun resultsAreMappedByStopAndNullStopsAreAllowed() = runTest {
        Fixture().use { fixture ->
            fixture.nullStop = "B"
            val result = fixture.service.getDepartures(listOf(row("A", "23"), row("B", "4")))
            assertEquals("A", result["A"]?.id)
            assertNull(result["B"])
        }
    }

    @Test fun graphQlErrorsFailTheWholeRefreshAndDoNotPopulateCatalogCache() = runTest {
        Fixture().use { fixture ->
            fixture.graphQlError = true
            val failure = runCatching { fixture.service.getDepartures(listOf(row("A", "23"))) }
            assertTrue(failure.isFailure)
            fixture.graphQlError = false
            fixture.service.getDepartures(listOf(row("A", "23")))
            assertEquals(JsonPrimitive(true), fixture.variables()["includeLineCatalog"])
        }
    }

    @Test fun malformedResponseFailsInsteadOfClearingCachedRows() = runTest {
        Fixture().use { fixture ->
            fixture.responseOverride = """{"data":{"stopPlace":{"id":"A","name":"Stop A"}}}"""
            assertTrue(runCatching { fixture.service.getDepartures(listOf(row("A", "23"))) }.isFailure)
        }
    }

    @Test fun externalValuesStayInJsonVariablesNotTheGraphQlDocument() = runTest {
        Fixture().use { fixture ->
            val unusualId = "stop\" ) { injectedField } #"
            fixture.service.getDepartures(listOf(row(unusualId, "23")))
            val request = fixture.requests.single()
            assertEquals(unusualId, fixture.variables(unusualId).getValue("id").jsonPrimitive.content)
            assertFalse(request.getValue("query").jsonPrimitive.content.contains(unusualId))
            assertFalse(request.getValue("query").jsonPrimitive.content.contains("injectedField"))
        }
    }

    @Test fun ordinaryStopQueryStillOmitsCatalogAndHasNoLineFilter() = runTest {
        Fixture().use { fixture ->
            fixture.responseOverride = """{"data":{"stopPlace":{"id":"A","name":"Stop A","estimatedCalls":[]}}}"""
            val result = fixture.client.query(StopPlaceQuery("A")).execute().dataAssertNoErrors
            assertEquals("A", result.stopPlace?.id)
            assertNull(result.stopPlace?.quays)
            assertFalse(fixture.variables().containsKey("lineIds"))
            assertFalse(fixture.variables()["includeLineCatalog"]?.jsonPrimitive?.boolean ?: false)
        }
    }

    private class Fixture : AutoCloseable {
        val requests: MutableList<JsonObject> = Collections.synchronizedList(mutableListOf())
        val catalogs = mutableMapOf(
            "A" to listOf("ONE:Line:23" to "23", "ONE:Line:24" to "24"),
            "B" to listOf("TWO:Line:4" to "4", "ONE:Line:23" to "23"),
        )
        var now = 0L
        var noDepartures = false
        var graphQlError = false
        var nullStop: String? = null
        var responseOverride: String? = null
        var barrier: CyclicBarrier? = null
        private val httpClient = OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request()
            val body = Buffer().apply { request.body!!.writeTo(this) }.readUtf8()
            val parsed = Json.parseToJsonElement(body).jsonObject
            requests += parsed
            barrier?.await(5, TimeUnit.SECONDS)
            Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(200).message("OK")
                .body((responseOverride ?: response(parsed).toString())
                    .toResponseBody("application/json".toMediaType())).build()
        }.build()
        val client = ApolloClient.Builder().serverUrl("https://example.test/graphql")
            .okHttpClient(httpClient).build()
        val service = BoardDeparturesService(client) { now }

        fun variables(stopId: String = "A") = requests.last {
            it.getValue("variables").jsonObject.getValue("id").jsonPrimitive.content == stopId
        }.getValue("variables").jsonObject

        private fun response(request: JsonObject): JsonObject = buildJsonObject {
            val variables = request.getValue("variables").jsonObject
            val id = variables.getValue("id").jsonPrimitive.content
            put("data", buildJsonObject {
                if (id == nullStop) {
                    put("stopPlace", JsonNull)
                } else {
                    val lines = catalogs[id].orEmpty()
                    val selected = variables["lineIds"]?.takeUnless { it == JsonNull }
                        ?.jsonArray?.map { it.jsonPrimitive.content }
                    put("stopPlace", buildJsonObject {
                        put("id", id)
                        put("name", "Stop $id")
                        put("estimatedCalls", JsonArray(if (noDepartures) emptyList() else {
                            lines.filter { selected == null || it.first in selected }.map { (_, code) ->
                                Json.parseToJsonElement("""{
                                  "aimedArrivalTime":"2026-08-30T12:00:00+02:00",
                                  "expectedArrivalTime":"2026-08-30T12:01:00+02:00",
                                  "destinationDisplay":{"frontText":"Destination"},
                                  "serviceJourney":{"line":{"publicCode":"$code","presentation":{"colour":"FF0000","textColour":"FFFFFF"}}}
                                }""")
                            }
                        }))
                        if (variables["includeLineCatalog"]?.jsonPrimitive?.boolean == true) {
                            val quay = buildJsonObject { put("lines", JsonArray(lines.map { (lineId, code) ->
                                buildJsonObject { put("id", lineId); put("publicCode", code) }
                            })) }
                            put("quays", JsonArray(listOf(quay, quay, JsonNull)))
                        }
                    })
                }
            })
            if (graphQlError) put("errors", Json.parseToJsonElement("""[{"message":"Temporary upstream failure","path":["stopPlace"]}]"""))
        }

        override fun close() {
            client.close()
            httpClient.dispatcher.executorService.shutdown()
            httpClient.connectionPool.evictAll()
        }
    }

    private fun JsonObject.ids(name: String) = getValue(name).jsonArray.map { it.jsonPrimitive.content }

    private fun row(stop: String, line: String, destination: String = "Destination") =
        BoardDeparture(1, stop, "Stop $stop", null, null, line, destination)
}
