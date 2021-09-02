package hansffu.ontime

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.*
import hansffu.ontime.model.*
import hansffu.ontime.model.StopListType.*
import hansffu.ontime.service.FavoriteService
import hansffu.ontime.service.StopService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TimetableViewModel(application: Application) : AndroidViewModel(application) {
    private val stopService = StopService()
    private val currentStop: MutableLiveData<Stop> by lazy { MutableLiveData(null) }
    private val departures: MutableLiveData<StopPlaceQuery.Data> by lazy { MutableLiveData(null) }

    fun getCurrentStop(): LiveData<Stop> {
        return currentStop
    }

    fun getLineDepartures(): LiveData<List<LineDeparture>> =
        Transformations.map(departures) { data ->
            data?.stopPlace?.let {
                DepartureMappers.toLineDepartures(it)
            }
        }


    fun loadDepartures(stop: Stop) {
        viewModelScope.launch {
            departures.value = stopService.getDepartures(stop.id)
        }
    }

    val stopName: LiveData<String>
        get() = Transformations.map(currentStop) { it.name }

    fun setCurrentStop(stop: Stop) {
        Log.e("hello", "new stop: ${stop.name} replaces ${currentStop.value?.name}")
        currentStop.postValue(stop)
    }

}

object DepartureMappers {
    fun toLineDepartures(stopPlace: StopPlaceQuery.StopPlace): List<LineDeparture> {
        val quays = stopPlace.quays ?: emptyList()
        return quays.flatMap { it.estimatedCalls() }
            .asSequence()
            .filterNotNull()
            .groupBy(::groupLines)
            .map { (ref, departures) -> ref?.let { LineDeparture(it, departures) } }
            .filterNotNull()
            .sortedBy { lineDeparture ->
                lineDeparture.departures
                    .mapNotNull { call -> call.expectedArrivalTime }
                    .minOrNull()
            }
            .toList()
    }

    private fun groupLines(estimatedCall: StopPlaceQuery.EstimatedCall): LineDirectionRef? {
        val publicCode = estimatedCall.serviceJourney?.line?.publicCode
        val dest = estimatedCall.destinationDisplay()?.frontText()
        return if (publicCode != null && dest != null) {
            LineDirectionRef(publicCode, dest)
        } else {
            null
        }
    }

}