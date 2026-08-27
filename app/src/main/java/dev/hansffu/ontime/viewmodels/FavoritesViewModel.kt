package dev.hansffu.ontime.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.hansffu.ontime.database.dao.BoardDao
import dev.hansffu.ontime.database.dao.FavoriteStopDao
import dev.hansffu.ontime.model.ActiveBoard
import dev.hansffu.ontime.model.BoardActivation
import dev.hansffu.ontime.model.Coordinates
import dev.hansffu.ontime.model.Stop
import dev.hansffu.ontime.service.LocationResult
import dev.hansffu.ontime.service.LocationService
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    favoriteStopDao: FavoriteStopDao,
    boardDao: BoardDao,
    private val locationService: LocationService,
) : ViewModel() {
    private val location = MutableStateFlow<Coordinates?>(null)
    private val time = MutableStateFlow(LocalTime.now())
    private var locationJob: Job? = null

    val favoriteStops =
        favoriteStopDao.getAll()
            .map { stops -> stops.map { Stop(it.name, it.id) } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

    val activeBoards =
        combine(boardDao.observeAll(), location, time, BoardActivation::evaluate)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList<ActiveBoard>(),
            )

    init {
        refresh()
        viewModelScope.launch {
            while (isActive) {
                delay(60_000)
                refresh()
            }
        }
    }

    fun refresh() {
        time.value = LocalTime.now()
        locationJob?.cancel()
        locationJob =
            viewModelScope.launch {
                location.value =
                    (locationService.getLatestLocation() as? LocationResult.Success)?.location
                        ?.let { Coordinates(it.latitude, it.longitude) }
            }
    }
}
