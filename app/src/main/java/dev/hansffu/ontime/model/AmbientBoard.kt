package dev.hansffu.ontime.model

import java.time.OffsetDateTime

data class AmbientDeparture(
    val line: String,
    val destination: String,
    val expected: OffsetDateTime,
)

data class AmbientStopTimetable(
    val stopId: String,
    val stopName: String,
    val departures: List<AmbientDeparture>,
)

/** Select the stop before filtering times: an empty nearest stop must not become a farther stop. */
fun List<BoardDepartureRow>.nearestStopTimetable(now: OffsetDateTime): AmbientStopTimetable? {
    val nearest =
        filter { it.distanceMeters?.let { distance -> distance.isFinite() && distance >= 0 } == true }
            .minWithOrNull(compareBy<BoardDepartureRow> { it.distanceMeters }.thenBy { it.stored.stopId })
            // A single-stop board needs no location fix; multiple unknown stops have no nearest.
            ?: firstOrNull()?.takeIf { first -> all { it.stored.stopId == first.stored.stopId } }
            ?: return null
    val departures =
        filter { it.stored.stopId == nearest.stored.stopId }
            .flatMap { row ->
                DepartureTimeFilter.upcoming(row.departure.departures, now) { it.expectedArrivalTime }
                    .map { AmbientDeparture(row.stored.lineRef, row.stored.destinationRef, it.expectedArrivalTime) }
            }
            .sortedWith(compareBy<AmbientDeparture> { it.expected }.thenBy { it.line }.thenBy { it.destination })
    return AmbientStopTimetable(nearest.stored.stopId, nearest.stored.stopName, departures)
}

/** Use monotonic time: changing the wall clock must not make cached predictions look fresh. */
fun BoardTimetableState.Content.isStaleForAmbient(elapsedMillis: Long): Boolean =
    refreshFailed || updatedAtElapsedMillis?.let { elapsedMillis - it >= 180_000L } != false
