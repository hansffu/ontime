package dev.hansffu.ontime.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.hansffu.ontime.database.dao.FavoriteStopDao
import dev.hansffu.ontime.model.Stop
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(favoriteStopDao: FavoriteStopDao) : ViewModel() {

    val favoriteStops: LiveData<List<Stop>> =
        favoriteStopDao.getAllL().map { stops ->
            stops.map { Stop(it.name, it.id) }
        }


}
