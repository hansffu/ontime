package dev.hansffu.ontime.graphql

import com.apollographql.apollo3.ApolloClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class GraphqlModule {
    @Singleton
    @Provides
    fun provideEnturApolloClient(): ApolloClient {
        return enturApolloClient
    }
}