package dev.hansffu.ontime.model

import kotlin.math.roundToInt

object BoardDistance {
    const val MIN_KILOMETERS = 1
    const val MAX_KILOMETERS = 15
    const val OPTION_COUNT = MAX_KILOMETERS - MIN_KILOMETERS + 1

    fun fromMeters(meters: Int): Int =
        (meters / 1_000.0).roundToInt().coerceIn(MIN_KILOMETERS, MAX_KILOMETERS)

    fun toMeters(kilometers: Int): Int =
        kilometers.coerceIn(MIN_KILOMETERS, MAX_KILOMETERS) * 1_000
}
