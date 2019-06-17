package hansffu.ontime.service

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import hansffu.ontime.model.Stop
import java.util.*

class FavoriteService(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    fun getFavorites(): List<Stop> {
        val favorites = LinkedList<Stop>()
        readableDatabase.query(FAVORITES_TABLE_NAME, arrayOf(STOP_ID, STOP_NAME), null,
                null, null, null, null)
                .use { cursor ->
                    for (i in 0 until cursor.count) {
                        cursor.moveToPosition(i)
                        favorites.add(Stop(cursor.getString(1), cursor.getLong(0)))
                    }
                }
        return favorites
    }

    fun toggleFavorite(stop: Stop): Boolean {
        val wasFavorite = isFavorite(stop)
        if (wasFavorite)
            removeFavorite(stop)
        else
            addFavorite(stop)
        return !wasFavorite
    }

    fun isFavorite(stop: Stop): Boolean {
        readableDatabase.query(FAVORITES_TABLE_NAME, arrayOf(STOP_ID),
                STOP_ID + " = ?", arrayOf(stop.id.toString()), null, null, null)
                .use { query -> if (query.count > 0) return true }
        return false
    }

    private fun addFavorite(stop: Stop) {
        val newFavorite = ContentValues()
        newFavorite.put(STOP_ID, stop.id)
        newFavorite.put(STOP_NAME, stop.name)
        writableDatabase.use { writableDatabase -> writableDatabase.insert(FAVORITES_TABLE_NAME, null, newFavorite) }
    }

    private fun removeFavorite(stop: Stop) {
        writableDatabase.use { writableDatabase ->
            writableDatabase.delete(FAVORITES_TABLE_NAME, STOP_ID + " = ?",
                    arrayOf(stop.id.toString()))
        }
    }


    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(FAVORITES_TABLE_CREATE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }

    companion object {

        private val FAVORITES_TABLE_NAME = "FAVORITES_TABLE_NAME"
        private val DATABASE_NAME = "db_favorites"
        private val DATABASE_VERSION = 1
        private val STOP_ID = "STOP_ID"
        private val STOP_NAME = "STOP_NAME"
        private val FAVORITES_TABLE_CREATE = "CREATE TABLE " + FAVORITES_TABLE_NAME + " " +
                "(" + STOP_ID + " INT PRIMARY_KEY," +
                "STOP_NAME TEXT)"
    }

}
