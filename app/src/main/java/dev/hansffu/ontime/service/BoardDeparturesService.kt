package dev.hansffu.ontime.service

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import dev.hansffu.ontime.database.dao.BoardDeparture
import dev.hansffu.ontime.graphql.StopPlaceQuery
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

@Singleton
class BoardDeparturesService internal constructor(
    private val client: ApolloClient,
    private val elapsedMillis: () -> Long,
) {
    @Inject constructor(client: ApolloClient) : this(client, { android.os.SystemClock.elapsedRealtime() })

    private data class LineCatalog(val byPublicCode: Map<String, List<String>>, val loadedAt: Long)
    private val catalogs = ConcurrentHashMap<String, LineCatalog>()

    suspend fun getDepartures(departures: List<BoardDeparture>): Map<String, StopPlaceQuery.StopPlace?> {
        if (departures.isEmpty()) return emptyMap()
        val now = elapsedMillis()
        val queries = departures.groupBy { it.stopId }.toSortedMap().map { (stopId, rows) ->
            val codes = rows.map { it.lineRef }.distinct()
            val catalog = catalogs[stopId]?.takeIf { now - it.loadedAt < CATALOG_TTL_MILLIS }
            val canFilter = catalog != null && codes.all { it in catalog.byPublicCode }
            val lineIds = if (canFilter) {
                codes.flatMap { checkNotNull(catalog).byPublicCode.getValue(it) }.distinct().sorted()
            } else null
            StopPlaceQuery(
                id = stopId,
                lineIds = Optional.Present(lineIds),
                includeLineCatalog = Optional.Present(!canFilter),
            )
        }
        // The first fetch learns IDs alongside departures; subsequent fetches only request selected lines.
        // Unknown codes never become an empty whitelist (which means "all lines" to Entur).
        val result = coroutineScope {
            queries.map { query ->
                async { query.id to client.query(query).execute().dataAssertNoErrors.stopPlace }
            }.awaitAll().toMap()
        }
        result.forEach { (stopId, stop) ->
            stop?.quays?.let { quays ->
                val lines = quays.filterNotNull().flatMap { it.lines }.distinctBy { it.id }
                catalogs[stopId] = LineCatalog(
                    lines.filter { it.publicCode != null }.groupBy { checkNotNull(it.publicCode) }
                        .mapValues { (_, matches) -> matches.map { it.id } },
                    elapsedMillis(),
                )
            }
        }
        return result
    }

    companion object {
        internal const val CATALOG_TTL_MILLIS = 6 * 60 * 60 * 1_000L
    }
}
