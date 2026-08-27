package dev.hansffu.ontime.model

import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class BoardTimeTest {
    @Test
    fun pickerTimeConvertsToMinuteOfDay() {
        assertEquals(0, BoardTime.toMinuteOfDay(LocalTime.MIDNIGHT))
        assertEquals(6 * 60 + 45, BoardTime.toMinuteOfDay(LocalTime.of(6, 45)))
        assertEquals(23 * 60 + 59, BoardTime.toMinuteOfDay(LocalTime.of(23, 59)))
    }

    @Test
    fun storedMinuteOfDayConvertsToPickerTime() {
        assertEquals(LocalTime.MIDNIGHT, BoardTime.fromMinuteOfDay(0))
        assertEquals(LocalTime.of(15, 30), BoardTime.fromMinuteOfDay(15 * 60 + 30))
        assertEquals(LocalTime.of(23, 59), BoardTime.fromMinuteOfDay(23 * 60 + 59))
    }
}
