package dev.hansffu.ontime.model

import java.time.OffsetDateTime

object BoardDepartureOrdering {
    fun <T> sort(
        items: List<T>,
        distanceMeters: (T) -> Double?,
        firstDeparture: (T) -> OffsetDateTime?,
    ): List<T> =
        items.sortedWith(
            compareBy<T> { distanceMeters(it) ?: Double.POSITIVE_INFINITY }
                .thenBy { firstDeparture(it) ?: OffsetDateTime.MAX }
        )
}
