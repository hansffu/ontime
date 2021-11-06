package hansffu.ontime.model

import hansffu.ontime.StopPlaceQuery

data class LineDirectionRef internal constructor(val lineRef: String, val destinationRef: String)

data class LineDeparture(
    val lineDirectionRef: LineDirectionRef,
    val departures: List<StopPlaceQuery.EstimatedCall>
)

data class Stop(val name: String, val id: String)