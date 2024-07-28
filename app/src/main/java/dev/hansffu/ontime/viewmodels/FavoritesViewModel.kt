package dev.hansffu.ontime.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.hansffu.ontime.database.dao.FavoritesDao
import dev.hansffu.ontime.model.Stop
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(favoritesDao: FavoritesDao) : ViewModel() {

    val favoriteStops: LiveData<List<Stop>> =
        favoritesDao.getAll().map { stops ->
            stops.map { Stop(it.name, it.id) }
        }


}
