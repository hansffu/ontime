package dev.hansffu.ontime.database

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.hansffu.ontime.database.dao.FavoritesDao
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {
    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDb(context)
    }

    @Singleton
    @Provides
    fun provideFavoritesDao(appDatabase: AppDatabase): FavoritesDao = appDatabase.favoritesDao()
}