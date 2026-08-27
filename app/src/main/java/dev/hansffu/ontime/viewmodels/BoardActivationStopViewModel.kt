package dev.hansffu.ontime.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.hansffu.ontime.database.dao.BoardDao
import dev.hansffu.ontime.model.Stop
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class BoardActivationStopViewModel @Inject constructor(
    private val boardDao: BoardDao,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val boardId: Long = checkNotNull(savedStateHandle["boardId"])

    fun select(stop: Stop, onSelected: () -> Unit) =
        viewModelScope.launch(Dispatchers.IO) {
            boardDao.getById(boardId)?.let { board ->
                boardDao.update(
                    board.copy(
                        activationStopId = stop.id,
                        activationStopName = stop.name,
                        activationLatitude = stop.latitude,
                        activationLongitude = stop.longitude,
                        maxDistanceMeters = board.maxDistanceMeters ?: 3_000,
                    )
                )
            }
            withContext(Dispatchers.Main) { onSelected() }
        }
}
