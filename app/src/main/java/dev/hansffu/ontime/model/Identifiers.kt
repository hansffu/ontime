package dev.hansffu.ontime.model

import dev.hansffu.ontime.graphql.StopPlaceQuery

data class LineDirectionRef internal constructor(val lineRef: String, val destinationRef: String)

data class LineDeparture(
    val lineDirectionRef: LineDirectionRef,
    val departures: List<StopPlaceQuery.EstimatedCall>,
    val color: String,
) : Comparable<LineDeparture> {
    override fun compareTo(other: LineDeparture) =
        compareBy<LineDeparture> { line -> line.departures.minOf { it.expectedArrivalTime } }
            .compare(this, other)
}

data class Stop(
    val name: String,
    val id: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val transportModes: Set<StopTransportMode> = emptySet(),
    val distanceMeters: Double? = null,
) {
    val coordinates: Coordinates?
        get() =
            if (latitude != null && longitude != null) Coordinates(latitude, longitude)
            else null
}

enum class StopTransportMode {
    AIR,
    BUS,
    CABLEWAY,
    COACH,
    FUNICULAR,
    LIFT,
    METRO,
    MONORAIL,
    RAIL,
    TAXI,
    TRAM,
    TROLLEYBUS,
    WATER,
    UNKNOWN;

    companion object {
        fun fromApiName(name: String): StopTransportMode =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: UNKNOWN
    }
}
