package hansffu.ontime.graphql

import com.apollographql.apollo.ApolloClient
import hansffu.ontime.type.CustomType
import okhttp3.OkHttpClient

val enturApolloClient: ApolloClient = ApolloClient.builder()
    .serverUrl("https://api.entur.io/journey-planner/v2/graphql")
    .addCustomTypeAdapter(CustomType.DATETIME, DateTimeAdapter)
    .okHttpClient(OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("ET-Client-Name", "hansffu-ontime")
                .build()
            chain.proceed(request)
        }.build()
    )
    .build()
