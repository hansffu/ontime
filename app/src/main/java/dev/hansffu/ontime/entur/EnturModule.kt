package dev.hansffu.ontime.entur

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.network.okHttpClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class EnturModule {
    @Singleton
    @Provides
    fun provideEnturApolloClient(httpClient: OkHttpClient): ApolloClient {
        return ApolloClient.Builder()
            .serverUrl("https://api.entur.io/journey-planner/v3/graphql")
            .okHttpClient(httpClient)
            .build()
    }

    @Singleton
    @Provides
    fun provideHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("ET-Client-Name", "hansffu-ontime")
                    .build()
                chain.proceed(request)
            }.build()
    }
}