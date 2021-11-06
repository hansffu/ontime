package hansffu.ontime.graphql

import com.apollographql.apollo.api.CustomTypeAdapter
import com.apollographql.apollo.api.CustomTypeValue
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

object DateTimeAdapter : CustomTypeAdapter<OffsetDateTime> {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXXX")

    override fun decode(value: CustomTypeValue<*>): OffsetDateTime =
        OffsetDateTime.parse(value.value.toString(), formatter)

    override fun encode(value: OffsetDateTime): CustomTypeValue<*> {
        return CustomTypeValue.GraphQLString(value.format(formatter))
    }
}