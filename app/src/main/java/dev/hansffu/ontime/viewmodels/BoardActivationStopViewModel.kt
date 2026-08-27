package dev.hansffu.ontime.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.hansffu.ontime.database.dao.BoardDao
import dev.hansffu.ontime.model.Stop
import dev.hansffu.ontime.service.StopService
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class BoardActivationStopViewModel @Inject constructor(
    private val boardDao: BoardDao,
    private val stopService: StopService,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val boardId: Long = checkNotNull(savedStateHandle["boardId"])
    private val mutableSelectionFailed = MutableStateFlow(false)
    val selectionFailed = mutableSelectionFailed.asStateFlow()

    fun select(stop: Stop, onSelected: () -> Unit) =
        viewModelScope.launch(Dispatchers.IO) {
            mutableSelectionFailed.value = false
            val resolvedStop = resolveCoordinates(stop)
            if (resolvedStop.latitude == null || resolvedStop.longitude == null) {
                mutableSelectionFailed.value = true
                return@launch
            }
            boardDao.getById(boardId)?.let { board ->
                boardDao.update(
                    board.copy(
                        activationStopId = resolvedStop.id,
                        activationStopName = resolvedStop.name,
                        activationLatitude = resolvedStop.latitude,
                        activationLongitude = resolvedStop.longitude,
                        maxDistanceMeters = board.maxDistanceMeters ?: 3_000,
                        distanceEnabled = true,
                    )
                )
            }
            withContext(Dispatchers.Main) { onSelected() }
        }

    private suspend fun resolveCoordinates(stop: Stop): Stop {
        if (stop.latitude != null && stop.longitude != null) return stop
        return runCatching {
            stopService.getDepartures(stop.id).stopPlace?.let {
                Stop(
                    id = it.id,
                    name = it.name,
                    latitude = it.latitude,
                    longitude = it.longitude,
                )
            }
        }.getOrNull() ?: stop
    }
}
