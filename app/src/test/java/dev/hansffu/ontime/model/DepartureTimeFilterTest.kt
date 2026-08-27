package dev.hansffu.ontime.model

import java.time.OffsetDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class DepartureTimeFilterTest {
    private val now = OffsetDateTime.parse("2026-08-27T08:00:00+02:00")

    @Test
    fun departedItemsAreRemoved() {
        val departures = listOf(now.minusSeconds(1), now, now.plusMinutes(4))

        assertEquals(
            listOf(now, now.plusMinutes(4)),
            DepartureTimeFilter.upcoming(departures, now) { it },
        )
    }

    @Test
    fun listBecomesEmptyAfterFinalDeparture() {
        val departures = listOf(now.minusMinutes(2), now.minusSeconds(1))

        assertEquals(
            emptyList<OffsetDateTime>(),
            DepartureTimeFilter.upcoming(departures, now) { it },
        )
    }
}
