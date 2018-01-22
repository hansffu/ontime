package hansffu.ontime.model

import java.util.*

data class Departure(val lineRef: String, val direction: String, val lineNumber: String,
                     val destination: String, private val destinationRef: String, val time: Date) {
    val lineDirectionRef: LineDirectionRef = LineDirectionRef(lineRef, destinationRef)
}

data class LineDirectionRef internal constructor(val lineRef: String, val destinationRef: String)

class Stop(val name: String, val id: Long)