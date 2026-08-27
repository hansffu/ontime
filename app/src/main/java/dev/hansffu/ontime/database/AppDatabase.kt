package dev.hansffu.ontime.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.hansffu.ontime.database.dao.Board
import dev.hansffu.ontime.database.dao.BoardDao
import dev.hansffu.ontime.database.dao.BoardDeparture
import dev.hansffu.ontime.database.dao.BoardDepartureDao
import dev.hansffu.ontime.database.dao.FavoriteDeparture
import dev.hansffu.ontime.database.dao.FavoriteDepartureDao
import dev.hansffu.ontime.database.dao.FavoriteStop
import dev.hansffu.ontime.database.dao.FavoriteStopDao

@Database(
    entities = [FavoriteStop::class, FavoriteDeparture::class, Board::class, BoardDeparture::class],
    version = 4,
)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDb(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                val db =
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "app_db",
                    ).addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                        .fallbackToDestructiveMigration(true)
                        .build()
                INSTANCE = db
                db
            }
    }

    abstract fun favoritesDao(): FavoriteStopDao
    abstract fun favoriteDeparturesDao(): FavoriteDepartureDao
    abstract fun boardsDao(): BoardDao
    abstract fun boardDeparturesDao(): BoardDepartureDao
}

private val MIGRATION_2_3 =
    object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS Board (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    activationStopId TEXT,
                    activationStopName TEXT,
                    activationLatitude REAL,
                    activationLongitude REAL,
                    maxDistanceMeters INTEGER,
                    startMinuteOfDay INTEGER,
                    endMinuteOfDay INTEGER
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS BoardDeparture (
                    boardId INTEGER NOT NULL,
                    stopId TEXT NOT NULL,
                    stopName TEXT NOT NULL,
                    stopLatitude REAL,
                    stopLongitude REAL,
                    lineRef TEXT NOT NULL,
                    destinationRef TEXT NOT NULL,
                    PRIMARY KEY(boardId, stopId, lineRef, destinationRef)
                )
                """.trimIndent()
            )
        }
    }

private val MIGRATION_3_4 =
    object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE Board ADD COLUMN distanceEnabled INTEGER NOT NULL DEFAULT 0"
            )
            db.execSQL(
                """
                UPDATE Board
                SET distanceEnabled = 1
                WHERE maxDistanceMeters IS NOT NULL
                  AND activationLatitude IS NOT NULL
                  AND activationLongitude IS NOT NULL
                """.trimIndent()
            )
            db.execSQL(
                "ALTER TABLE Board ADD COLUMN timeEnabled INTEGER NOT NULL DEFAULT 0"
            )
            db.execSQL(
                """
                UPDATE Board
                SET timeEnabled = 1
                WHERE startMinuteOfDay IS NOT NULL
                  AND endMinuteOfDay IS NOT NULL
                """.trimIndent()
            )
        }
    }
