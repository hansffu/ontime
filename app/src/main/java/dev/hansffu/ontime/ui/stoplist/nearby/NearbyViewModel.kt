package dev.hansffu.ontime.ui.stoplist.nearby

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.hansffu.ontime.service.LocationResult
import dev.hansffu.ontime.service.LocationService
import dev.hansffu.ontime.service.StopService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NearbyViewModel @Inject constructor(
    private val locationService: LocationService,
    private val stopService: StopService,
) :
    ViewModel() {
    private val mutableNearbyStopState: MutableStateFlow<NearbyStopState> =
        MutableStateFlow(NearbyStopState.Uninitialized)
    val nearbyStopState: StateFlow<NearbyStopState> = mutableNearbyStopState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val sf = getStopStateFlow()
            sf.collect {
                Log.i("NearbyViewModel", "Collecting $it")
                mutableNearbyStopState.value = it
            }
            mutableNearbyStopState.emitAll(getStopStateFlow())
        }
    }

    val locationPermissions: List<String> = locationService.locationPermissions
    private suspend fun getStopStateFlow(): Flow<NearbyStopState> = flow {
        emit(NearbyStopState.Loading)
        when (val locationState = locationService.getLatestLocation()) {
            is LocationResult.NoPermission -> emit(NearbyStopState.NoPermission)
            is LocationResult.Success -> {
                val stops = stopService.findStopsNear(locationState.location)
                emit(NearbyStopState.StopsFound(stops) { refresh() })
            }
        }

    }
}