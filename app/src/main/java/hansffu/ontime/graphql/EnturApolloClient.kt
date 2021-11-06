package hansffu.ontime.graphql

import com.apollographql.apollo.ApolloClient
import hansffu.ontime.type.CustomType

val enturApolloClient: ApolloClient = ApolloClient.builder()
    .serverUrl("https://api.entur.io/journey-planner/v2/graphql")
    .addCustomTypeAdapter(CustomType.DATETIME, DateTimeAdapter)
    .build()
