package hansffu.ontime.service

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import hansffu.ontime.database.AppDatabase
import hansffu.ontime.database.dao.FavoriteStop
import hansffu.ontime.model.Stop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoriteService(context: Context) {
    private val db = AppDatabase.getDb(context)

    fun getFavorites(): LiveData<List<Stop>> =
        Transformations.map(db.favoritesDao().getAll()) { stops ->
            stops.map { Stop(it.name, it.id) }
        }


    suspend fun toggleFavorite(stop: Stop) {
        withContext(Dispatchers.IO) {
            val existing = db.favoritesDao().getById(stop.id)
            if (existing != null) {
                db.favoritesDao().delete(existing)
            } else {
                db.favoritesDao().insertAll(FavoriteStop(stop.id, stop.name))
            }
        }
    }


}
