package dev.hansffu.ontime.model

import dev.hansffu.ontime.database.dao.BoardDeparture
import dev.hansffu.ontime.graphql.StopPlaceQuery
import java.time.OffsetDateTime
import org.junit.Assert.*
import org.junit.Test

class AmbientBoardTest {
    private val now = OffsetDateTime.parse("2026-08-30T17:42:00+02:00")

    @Test fun nearestStopWinsEvenWhenAFartherStopDepartsSooner() {
        val result = listOf(
            row("far", 800.0, "30", "Bygdøy", 1),
            row("near", 20.0, "23", "Lysaker", 5),
        ).nearestStopTimetable(now)!!
        assertEquals("near", result.stopId)
        assertEquals(listOf("23"), result.departures.map { it.line })
    }

    @Test fun flattensEveryDepartureAndSortsAcrossLinesWithoutGroupingOrDeduplicating() {
        val result = listOf(
            row("near", 20.0, "18", "Rikshospitalet", 7, 12),
            row("near", 20.0, "23", "Lysaker", 11, 5, 6, 6),
        ).nearestStopTimetable(now)!!
        assertEquals(listOf(5L, 6L, 6L, 7L, 11L, 12L),
            result.departures.map { java.time.Duration.between(now, it.expected).toMinutes() })
        assertEquals(listOf("23", "23", "23", "18", "23", "18"), result.departures.map { it.line })
    }

    @Test fun selectsBeforeDroppingPastDeparturesAndDoesNotSwitchToFartherStop() {
        val result = listOf(
            row("near", 10.0, "23", "Lysaker", -1),
            row("far", 500.0, "18", "Rikshospitalet", 3),
        ).nearestStopTimetable(now)!!
        assertEquals("near", result.stopId)
        assertTrue(result.departures.isEmpty())
    }

    @Test fun nearestStopWithoutAnyCallsRemainsSelected() {
        val result = listOf(
            row("near", 10.0, "23", "Lysaker"),
            row("far", 500.0, "18", "Rikshospitalet", 3),
        ).nearestStopTimetable(now)!!
        assertEquals("near", result.stopId)
        assertTrue(result.departures.isEmpty())
    }

    @Test fun pastDeparturesDisappearButDepartureAtCurrentTimeIsIncluded() {
        val result = listOf(row("near", 10.0, "23", "Lysaker", -1, 0, 5)).nearestStopTimetable(now)!!
        assertEquals(listOf(now, now.plusMinutes(5)), result.departures.map { it.expected })
        assertTrue(listOf(row("near", 10.0, "23", "Lysaker", 0, 5))
            .nearestStopTimetable(now.plusMinutes(6))!!.departures.isEmpty())
    }

    @Test fun stopIdentityIsNotItsDisplayName() {
        val result = listOf(
            row("near", 10.0, "23", "Lysaker", 5),
            row("far", 50.0, "18", "Rikshospitalet", 1),
        ).nearestStopTimetable(now)!!
        assertEquals(1, result.departures.size)
        assertEquals("near", result.stopId)
    }

    @Test fun oneStopWorksWithoutLocationButMultipleUnknownStopsDoNotGuess() {
        assertNotNull(listOf(row("only", null, "23", "Lysaker", 5)).nearestStopTimetable(now))
        assertNull(listOf(
            row("a", null, "23", "Lysaker", 1),
            row("b", null, "18", "Rikshospitalet", 3),
        ).nearestStopTimetable(now))
        assertNull(emptyList<BoardDepartureRow>().nearestStopTimetable(now))
    }

    @Test fun unknownOrInvalidDistanceNeverBeatsKnownDistance() {
        val result = listOf(
            row("unknown", null, "23", "Lysaker", 1),
            row("nan", Double.NaN, "23", "Lysaker", 1),
            row("invalid", -1.0, "23", "Lysaker", 1),
            row("known", 100.0, "18", "Rikshospitalet", 5),
        ).nearestStopTimetable(now)!!
        assertEquals("known", result.stopId)
    }

    @Test fun equalDistanceDoesNotSwitchStopsAsDepartureTimesChange() {
        val rows = listOf(row("b", 10.0, "18", "Rikshospitalet", 1), row("a", 10.0, "23", "Lysaker", 5))
        assertEquals("a", rows.nearestStopTimetable(now)!!.stopId)
        assertEquals("a", rows.reversed().nearestStopTimetable(now.plusMinutes(2))!!.stopId)
    }

    @Test fun oldUnknownAndFailedPredictionsAreStale() {
        val content = BoardTimetableState.Content("Board", emptyList(), updatedAtElapsedMillis = 1_000L)
        assertFalse(content.isStaleForAmbient(180_999L))
        assertTrue(content.isStaleForAmbient(181_000L))
        assertTrue(content.copy(refreshFailed = true).isStaleForAmbient(2_000L))
        assertTrue(content.copy(updatedAtElapsedMillis = null).isStaleForAmbient(2_000L))
    }

    private fun row(stopId: String, distance: Double?, line: String, destination: String, vararg minutes: Int): BoardDepartureRow =
        BoardDepartureRow(
            BoardDeparture(1, stopId, "Shared stop name", null, null, line, destination),
            LineDeparture(
                LineDirectionRef(line, destination),
                minutes.map { minute ->
                    val time = now.plusMinutes(minute.toLong())
                    StopPlaceQuery.EstimatedCall(time, time, StopPlaceQuery.DestinationDisplay(destination),
                        StopPlaceQuery.ServiceJourney(StopPlaceQuery.Line(line, null)))
                },
                "000000",
            ),
            distance,
            false,
        )
}
