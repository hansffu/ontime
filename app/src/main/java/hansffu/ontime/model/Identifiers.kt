package hansffu.ontime.model

import hansffu.ontime.StopPlaceQuery
import java.util.*

data class Departure(
        val lineRef: String,
        val direction: String,
        val lineNumber: String,
        val destination: String,
        private val destinationRef: String,
        val time: Date)


data class LineDirectionRef internal constructor(val lineRef: String, val destinationRef: String)

data class LineDeparture(val lineDirectionRef: LineDirectionRef, val departures: List<StopPlaceQuery.EstimatedCall>)

open class Stop(open val name: String, open val id: String)