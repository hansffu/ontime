package hansffu.ontime.api

import hansffu.ontime.model.Stop

data class Geocoding(val features: List<Feature>)

data class Feature(val properties: Properties)

data class Properties(
        val id: String,
        val name: String,
        val distance: Double,
        val category: List<String>
)
