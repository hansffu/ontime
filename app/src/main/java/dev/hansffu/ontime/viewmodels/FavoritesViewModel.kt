package dev.hansffu.ontime.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import dev.hansffu.ontime.database.AppDatabase
import dev.hansffu.ontime.model.Stop

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDb(application)

    val favoriteStops: LiveData<List<Stop>> =
        db.favoritesDao().getAll().map { stops ->
            stops.map { Stop(it.name, it.id) }
        }


}

