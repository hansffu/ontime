package dev.hansffu.ontime.ui.stoplist.nearby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.hansffu.ontime.model.Stop
import dev.hansffu.ontime.service.LocationResult
import dev.hansffu.ontime.service.LocationService
import dev.hansffu.ontime.service.StopService
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface NearbyStopState {
    data object Loading : NearbyStopState
    data object NoPermission : NearbyStopState
    data object Error : NearbyStopState

    data class Content(
        val stops: List<Stop>,
        val refreshing: Boolean = false,
        val refreshFailed: Boolean = false,
    ) : NearbyStopState
}

@HiltViewModel
class NearbyViewModel @Inject constructor(
    private val locationService: LocationService,
    private val stopService: StopService,
) : ViewModel() {
    private val mutableNearbyStopState =
        MutableStateFlow<NearbyStopState>(NearbyStopState.Loading)
    val nearbyStopState: StateFlow<NearbyStopState> = mutableNearbyStopState.asStateFlow()

    private var refreshJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        val previousContent = mutableNearbyStopState.value as? NearbyStopState.Content
        refreshJob?.cancel()
        refreshJob =
            viewModelScope.launch {
                mutableNearbyStopState.value =
                    previousContent?.copy(refreshing = true, refreshFailed = false)
                        ?: NearbyStopState.Loading

                try {
                    mutableNearbyStopState.value =
                        when (val locationState = locationService.getLatestLocation()) {
                            LocationResult.NoPermission -> NearbyStopState.NoPermission
                            LocationResult.Unavailable -> NearbyStopState.Error
                            is LocationResult.Success ->
                                NearbyStopState.Content(
                                    stops = stopService.findStopsNear(locationState.location)
                                )
                        }
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (_: Exception) {
                    mutableNearbyStopState.value =
                        previousContent?.copy(refreshing = false, refreshFailed = true)
                            ?: NearbyStopState.Error
                }
            }
    }

    val locationPermissions: List<String> = locationService.locationPermissions
}
