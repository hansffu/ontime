package dev.hansffu.ontime.model

import java.time.OffsetDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class BoardDepartureOrderingTest {
    @Test
    fun departuresSortByDistanceBeforeTime() {
        val now = OffsetDateTime.parse("2026-08-26T08:00:00+02:00")
        val soonButFar = Row("far", 2_000.0, now.plusMinutes(1))
        val lateButNear = Row("near-late", 500.0, now.plusMinutes(10))
        val soonAndNear = Row("near-soon", 500.0, now.plusMinutes(3))
        val unknownDistance = Row("unknown", null, now)

        assertEquals(
            listOf(soonAndNear, lateButNear, soonButFar, unknownDistance),
            BoardDepartureOrdering.sort(
                listOf(unknownDistance, soonButFar, lateButNear, soonAndNear),
                Row::distance,
                Row::time,
            ),
        )
    }

    private data class Row(
        val name: String,
        val distance: Double?,
        val time: OffsetDateTime?,
    )
}
