package dev.hansffu.ontime.ui.components.timetable

import java.time.OffsetDateTime
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DepartureTimeTest {
    private val aimed = OffsetDateTime.parse("2026-08-29T12:00:00+02:00")

    @Test
    fun expectedAfterAimedIsDelayed() {
        assertTrue(DepartureTime(aimed, aimed.plusMinutes(1)).isDelayed)
    }

    @Test
    fun expectedAtAimedIsNotDelayed() {
        assertFalse(DepartureTime(aimed, aimed).isDelayed)
    }

    @Test
    fun expectedBeforeAimedIsNotDelayed() {
        assertFalse(DepartureTime(aimed, aimed.minusMinutes(1)).isDelayed)
    }
}
