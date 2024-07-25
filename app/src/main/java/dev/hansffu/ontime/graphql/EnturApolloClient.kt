package dev.hansffu.ontime.graphql

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.okHttpClient
import dev.hansffu.ontime.graphql.type.DateTime
import okhttp3.OkHttpClient

val enturApolloClient: ApolloClient = ApolloClient.Builder()
    .serverUrl("https://api.entur.io/journey-planner/v2/graphql")
    .addCustomScalarAdapter(DateTime.type, DateTimeAdapter)
    .okHttpClient(
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("ET-Client-Name", "hansffu-ontime")
                    .build()
                chain.proceed(request)
            }.build()
    )
    .build()
