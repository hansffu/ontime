package dev.hansffu.ontime.graphql

import com.apollographql.apollo3.ApolloClient

val enturApolloClient: ApolloClient
    get() = ApolloClient.Builder()
        .serverUrl("https://api.entur.io/journey-planner/v3/graphql")
        .addHttpHeader("ET-Client-Name", "hansffu-ontime")
        .build()
