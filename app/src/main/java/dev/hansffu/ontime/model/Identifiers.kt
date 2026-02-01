package dev.hansffu.ontime.model

import dev.hansffu.ontime.graphql.StopPlaceQuery

data class LineDirectionRef internal constructor(val lineRef: String, val destinationRef: String)

data class LineDeparture(
    val lineDirectionRef: LineDirectionRef,
    val departures: List<StopPlaceQuery.EstimatedCall>,
    val color: String
)

data class Stop(val name: String, val id: String)
