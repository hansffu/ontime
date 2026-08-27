package dev.hansffu.ontime.model

import java.time.OffsetDateTime

object DepartureTimeFilter {
    fun <T> upcoming(
        items: List<T>,
        now: OffsetDateTime,
        departureTime: (T) -> OffsetDateTime,
    ): List<T> = items.filter { !departureTime(it).isBefore(now) }
}

fun LineDeparture.withUpcomingDepartures(now: OffsetDateTime): LineDeparture? {
    val upcoming = DepartureTimeFilter.upcoming(departures, now) { it.expectedArrivalTime }
    return if (upcoming.isEmpty()) null else copy(departures = upcoming)
}
