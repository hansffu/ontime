package dev.hansffu.ontime.model

import org.junit.Assert.assertEquals
import org.junit.Test

class BoardDistanceTest {
    @Test
    fun storedMetersConvertToNearestAllowedWholeKilometer() {
        assertEquals(1, BoardDistance.fromMeters(500))
        assertEquals(4, BoardDistance.fromMeters(3_500))
        assertEquals(15, BoardDistance.fromMeters(20_000))
    }

    @Test
    fun selectedKilometersConvertToBoundedMeters() {
        assertEquals(1_000, BoardDistance.toMeters(0))
        assertEquals(7_000, BoardDistance.toMeters(7))
        assertEquals(15_000, BoardDistance.toMeters(16))
    }
}
