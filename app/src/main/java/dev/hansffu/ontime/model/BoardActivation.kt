package dev.hansffu.ontime.model

import dev.hansffu.ontime.database.dao.Board
import java.time.LocalTime
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Coordinates(val latitude: Double, val longitude: Double)

data class ActiveBoard(
    val board: Board,
    val distanceMeters: Double?,
)

object BoardActivation {
    fun evaluate(
        boards: List<Board>,
        location: Coordinates?,
        time: LocalTime,
    ): List<ActiveBoard> =
        boards.mapNotNull { board ->
            val hasDistanceCondition =
                board.maxDistanceMeters != null &&
                    board.activationLatitude != null &&
                    board.activationLongitude != null
            val hasTimeCondition =
                board.startMinuteOfDay != null && board.endMinuteOfDay != null
            if (!hasDistanceCondition && !hasTimeCondition) return@mapNotNull null

            val distance =
                if (hasDistanceCondition && location != null) {
                    distanceMeters(
                        location,
                        Coordinates(board.activationLatitude!!, board.activationLongitude!!),
                    )
                } else {
                    null
                }
            val distanceMatches =
                !hasDistanceCondition ||
                    (distance != null && distance <= board.maxDistanceMeters!!)
            val timeMatches =
                !hasTimeCondition ||
                    isWithinWindow(
                        time.toSecondOfDay() / 60,
                        board.startMinuteOfDay!!,
                        board.endMinuteOfDay!!,
                    )

            if (distanceMatches && timeMatches) ActiveBoard(board, distance) else null
        }.sortedWith(
            compareBy<ActiveBoard> { it.distanceMeters ?: Double.POSITIVE_INFINITY }
                .thenBy(String.CASE_INSENSITIVE_ORDER) { it.board.name }
                .thenBy { it.board.id }
        )

    fun isWithinWindow(
        minuteOfDay: Int,
        startMinuteOfDay: Int,
        endMinuteOfDay: Int,
    ): Boolean =
        when {
            startMinuteOfDay == endMinuteOfDay -> true
            startMinuteOfDay < endMinuteOfDay ->
                minuteOfDay in startMinuteOfDay until endMinuteOfDay
            else ->
                minuteOfDay >= startMinuteOfDay || minuteOfDay < endMinuteOfDay
        }

    fun distanceMeters(from: Coordinates, to: Coordinates): Double {
        val earthRadiusMeters = 6_371_000.0
        val latitudeDelta = Math.toRadians(to.latitude - from.latitude)
        val longitudeDelta = Math.toRadians(to.longitude - from.longitude)
        val fromLatitude = Math.toRadians(from.latitude)
        val toLatitude = Math.toRadians(to.latitude)
        val haversine =
            sin(latitudeDelta / 2) * sin(latitudeDelta / 2) +
                cos(fromLatitude) * cos(toLatitude) *
                sin(longitudeDelta / 2) * sin(longitudeDelta / 2)
        return earthRadiusMeters * 2 * atan2(sqrt(haversine), sqrt(1 - haversine))
    }
}
