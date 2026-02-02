package dev.hansffu.ontime.model

import dev.hansffu.ontime.graphql.StopPlaceQuery

data class LineDirectionRef internal constructor(val lineRef: String, val destinationRef: String)

data class LineDeparture(
    val lineDirectionRef: LineDirectionRef,
    val departures: List<StopPlaceQuery.EstimatedCall>,
    val color: String
) : Comparable<LineDeparture> {
    override fun compareTo(other: LineDeparture) =
        compareBy<LineDeparture> { line -> line.departures.minOf { it.expectedArrivalTime } }
            .compare(this, other)
}

data class Stop(val name: String, val id: String)
