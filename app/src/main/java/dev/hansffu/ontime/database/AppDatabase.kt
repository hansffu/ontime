package dev.hansffu.ontime.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.hansffu.ontime.database.dao.FavoriteStop
import dev.hansffu.ontime.database.dao.FavoritesDao

@Database(entities = [FavoriteStop::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDb(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {

                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, "app_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = db
                db
            }

        }
    }

    abstract fun favoritesDao(): FavoritesDao
}