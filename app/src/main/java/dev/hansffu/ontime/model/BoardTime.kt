package dev.hansffu.ontime.model

import java.time.LocalTime

object BoardTime {
    fun toMinuteOfDay(time: LocalTime): Int = time.hour * 60 + time.minute

    fun fromMinuteOfDay(minuteOfDay: Int): LocalTime {
        require(minuteOfDay in 0 until 24 * 60)
        return LocalTime.of(minuteOfDay / 60, minuteOfDay % 60)
    }
}
