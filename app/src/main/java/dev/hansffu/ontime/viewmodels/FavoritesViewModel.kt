package dev.hansffu.ontime.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.hansffu.ontime.database.dao.BoardDao
import dev.hansffu.ontime.database.dao.FavoriteStopDao
import dev.hansffu.ontime.model.SuggestedBoard
import dev.hansffu.ontime.model.BoardSuggestion
import dev.hansffu.ontime.model.Coordinates
import dev.hansffu.ontime.model.Stop
import dev.hansffu.ontime.service.LocationResult
import dev.hansffu.ontime.service.LocationService
import dev.hansffu.ontime.service.StopService
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    favoriteStopDao: FavoriteStopDao,
    boardDao: BoardDao,
    private val locationService: LocationService,
    private val stopService: StopService,
) : ViewModel() {
    private val location = MutableStateFlow<Coordinates?>(null)
    private val time = MutableStateFlow(LocalTime.now())
    private var locationJob: Job? = null
    private val storedFavoriteStops =
        favoriteStopDao.getAll()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    private val favoriteDetails = MutableStateFlow<Map<String, Stop>>(emptyMap())

    val favoriteStops =
        combine(storedFavoriteStops, favoriteDetails, location) {
            storedStops,
            details,
            coordinates,
            ->
            storedStops.map { stored ->
                val stop = details[stored.id] ?: Stop(stored.name, stored.id)
                stop.copy(
                    distanceMeters =
                        coordinates?.let { from ->
                            stop.coordinates?.let { to ->
                                BoardSuggestion.distanceMeters(from, to)
                            }
                        }
                )
            }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

    val suggestedBoards =
        combine(boardDao.observeAll(), location, time, BoardSuggestion::evaluate)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList<SuggestedBoard>(),
            )

    init {
        refresh()
        viewModelScope.launch {
            storedFavoriteStops.collectLatest { storedStops ->
                val ids = storedStops.map { it.id }
                favoriteDetails.value =
                    try {
                        stopService.getStops(ids).associateBy(Stop::id)
                    } catch (cancellation: CancellationException) {
                        throw cancellation
                    } catch (_: Exception) {
                        favoriteDetails.value.filterKeys { it in ids }
                    }
            }
        }
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
