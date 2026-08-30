package dev.hansffu.ontime.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.hansffu.ontime.database.dao.BoardDao
import dev.hansffu.ontime.model.BoardTimetableState
import dev.hansffu.ontime.service.BoardTimetableRepository
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class AmbientBoardState(val boardId: Long, val timetable: BoardTimetableState)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AmbientBoardViewModel @Inject constructor(
    boardDao: BoardDao,
    repository: BoardTimetableRepository,
) : ViewModel() {
    // Observe the existing service-owned cache; ambient mode never starts a second polling loop.
    val state = boardDao.observeActive()
        .map { it?.id }
        .distinctUntilChanged()
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else repository.observe(id).map { AmbientBoardState(id, it) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
