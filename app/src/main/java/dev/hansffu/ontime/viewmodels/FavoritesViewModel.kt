package dev.hansffu.ontime.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.hansffu.ontime.database.dao.FavoriteStopDao
import dev.hansffu.ontime.model.Stop
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class FavoritesViewModel @Inject constructor(favoriteStopDao: FavoriteStopDao) : ViewModel() {
    val favoriteStops =
        favoriteStopDao
            .getAll()
            .map { stops -> stops.map { Stop(it.name, it.id) } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )
}
