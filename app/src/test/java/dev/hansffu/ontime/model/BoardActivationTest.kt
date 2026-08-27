package dev.hansffu.ontime.model

import dev.hansffu.ontime.database.dao.Board
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardActivationTest {
    @Test
    fun boardWithoutConditionsIsInactive() {
        assertTrue(BoardActivation.evaluate(listOf(Board(name = "Draft")), null, noon).isEmpty())
    }

    @Test
    fun timeOnlyBoardActivatesInsideWindow() {
        val board =
            Board(
                name = "Morning",
                startMinuteOfDay = 6 * 60,
                endMinuteOfDay = 9 * 60,
                timeEnabled = true,
            )

        assertEquals(
            listOf(board),
            BoardActivation.evaluate(listOf(board), null, LocalTime.of(8, 59)).map { it.board },
        )
        assertTrue(
            BoardActivation.evaluate(listOf(board), null, LocalTime.of(9, 0)).isEmpty()
        )
    }

    @Test
    fun overnightWindowWrapsAcrossMidnight() {
        assertTrue(BoardActivation.isWithinWindow(23 * 60, 22 * 60, 2 * 60))
        assertTrue(BoardActivation.isWithinWindow(60, 22 * 60, 2 * 60))
        assertTrue(!BoardActivation.isWithinWindow(12 * 60, 22 * 60, 2 * 60))
    }

    @Test
    fun distanceConditionRequiresLocationAndHonorsRadius() {
        val board =
            Board(
                name = "Nearby",
                activationLatitude = 59.91,
                activationLongitude = 10.75,
                maxDistanceMeters = 1_000,
                distanceEnabled = true,
            )

        assertTrue(BoardActivation.evaluate(listOf(board), null, noon).isEmpty())
        assertEquals(
            1,
            BoardActivation.evaluate(
                listOf(board),
                Coordinates(59.915, 10.75),
                noon,
            ).size,
        )
        assertTrue(
            BoardActivation.evaluate(
                listOf(board),
                Coordinates(59.93, 10.75),
                noon,
            ).isEmpty()
        )
    }

    @Test
    fun distanceAndTimeMustBothMatch() {
        val board =
            Board(
                name = "Combined",
                activationLatitude = 59.91,
                activationLongitude = 10.75,
                maxDistanceMeters = 3_000,
                startMinuteOfDay = 6 * 60,
                endMinuteOfDay = 9 * 60,
                distanceEnabled = true,
                timeEnabled = true,
            )
        val nearby = Coordinates(59.915, 10.75)

        assertEquals(1, BoardActivation.evaluate(listOf(board), nearby, LocalTime.of(8, 0)).size)
        assertTrue(BoardActivation.evaluate(listOf(board), nearby, LocalTime.of(10, 0)).isEmpty())
    }

    @Test
    fun activeBoardsAreOrderedByDistanceThenName() {
        val near =
            Board(
                id = 1,
                name = "Zulu",
                activationLatitude = 59.911,
                activationLongitude = 10.75,
                maxDistanceMeters = 10_000,
                distanceEnabled = true,
            )
        val far =
            Board(
                id = 2,
                name = "Alpha",
                activationLatitude = 59.93,
                activationLongitude = 10.75,
                maxDistanceMeters = 10_000,
                distanceEnabled = true,
            )
        val timeOnly =
            Board(
                id = 3,
                name = "Always",
                startMinuteOfDay = 0,
                endMinuteOfDay = 0,
                timeEnabled = true,
            )

        assertEquals(
            listOf(near, far, timeOnly),
            BoardActivation.evaluate(
                listOf(timeOnly, far, near),
                Coordinates(59.91, 10.75),
                noon,
            ).map { it.board },
        )
    }

    @Test
    fun distanceCalculationIsStableEnoughForRadiusChecks() {
        val oneDegreeNorth =
            BoardActivation.distanceMeters(
                Coordinates(59.0, 10.0),
                Coordinates(60.0, 10.0),
            )

        assertEquals(111_195.0, oneDegreeNorth, 100.0)
    }

    @Test
    fun disabledRequirementsKeepValuesButDoNotActivateBoard() {
        val board =
            Board(
                name = "Paused",
                activationLatitude = 59.91,
                activationLongitude = 10.75,
                maxDistanceMeters = 10_000,
                startMinuteOfDay = 0,
                endMinuteOfDay = 0,
                distanceEnabled = false,
                timeEnabled = false,
            )

        assertTrue(
            BoardActivation.evaluate(
                listOf(board),
                Coordinates(59.91, 10.75),
                noon,
            ).isEmpty()
        )
    }

    @Test
    fun disabledDistanceIsIgnoredWhileTimeRequirementIsEnabled() {
        val board =
            Board(
                name = "Time only",
                activationLatitude = 59.91,
                activationLongitude = 10.75,
                maxDistanceMeters = 1,
                startMinuteOfDay = 6 * 60,
                endMinuteOfDay = 18 * 60,
                distanceEnabled = false,
                timeEnabled = true,
            )

        assertEquals(
            1,
            BoardActivation.evaluate(
                listOf(board),
                Coordinates(60.0, 11.0),
                noon,
            ).size,
        )
    }

    @Test
    fun disabledTimeIsIgnoredWhileDistanceRequirementIsEnabled() {
        val board =
            Board(
                name = "Distance only",
                activationLatitude = 59.91,
                activationLongitude = 10.75,
                maxDistanceMeters = 1_000,
                startMinuteOfDay = 6 * 60,
                endMinuteOfDay = 9 * 60,
                distanceEnabled = true,
                timeEnabled = false,
            )

        assertEquals(
            1,
            BoardActivation.evaluate(
                listOf(board),
                Coordinates(59.91, 10.75),
                noon,
            ).size,
        )
    }

    private val noon = LocalTime.NOON
}
