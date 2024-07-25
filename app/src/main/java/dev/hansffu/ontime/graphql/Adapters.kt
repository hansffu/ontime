package dev.hansffu.ontime.graphql

import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.JsonReader
import com.apollographql.apollo3.api.json.JsonWriter
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

object DateTimeAdapter : Adapter<OffsetDateTime> {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXXX")

    override fun fromJson(
        reader: JsonReader,
        customScalarAdapters: CustomScalarAdapters,
    ): OffsetDateTime =
        OffsetDateTime.parse(reader.nextString(), formatter)

    override fun toJson(
        writer: JsonWriter,
        customScalarAdapters: CustomScalarAdapters,
        value: OffsetDateTime,
    ) {
        writer.value(value.format(formatter))
    }
}