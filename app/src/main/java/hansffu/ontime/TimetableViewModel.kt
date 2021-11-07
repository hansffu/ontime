package hansffu.ontime

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import hansffu.ontime.database.AppDatabase
import hansffu.ontime.database.dao.FavoriteStop
import hansffu.ontime.model.LineDeparture
import hansffu.ontime.model.LineDirectionRef
import hansffu.ontime.model.Stop
import hansffu.ontime.service.StopService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TimetableViewModel(application: Application) : AndroidViewModel(application) {
    private val stopService = StopService()
    private val db = AppDatabase.getDb(application)
    val currentStop: MutableLiveData<Stop> by lazy { MutableLiveData(null) }
    private val departures: MutableLiveData<StopPlaceQuery.Data> by lazy { MutableLiveData(null) }
    private val favoriteStops: LiveData<List<Stop>> =
        db.favoritesDao().getAll().map { stops ->
            stops.map { Stop(it.name, it.id) }
        }


    val isFavorite: LiveData<Boolean> = currentStop.switchMap { stop ->
        favoriteStops.map { favoriteStops ->
            favoriteStops.any { it.id == stop.id }
        }
    }

    fun toggleFavorite(stop: Stop) = viewModelScope.launch(Dispatchers.IO) {
        val existing = db.favoritesDao().getById(stop.id)
        if (existing != null) {
            db.favoritesDao().delete(existing)
        } else {
            db.favoritesDao().insertAll(
                FavoriteStop(
                    stop.id,
                    stop.name,
                )
            )
        }
    }

    fun getLineDepartures(): LiveData<List<LineDeparture>> =
        departures.map { data ->
            data?.stopPlace
                ?.let { DepartureMappers.toLineDepartures(it) }
                ?: emptyList()
        }


    fun loadDepartures(stop: Stop) {
        viewModelScope.launch {
            departures.value = stopService.getDepartures(stop.id)
        }
    }

    fun setCurrentStop(stop: Stop) {
        Log.e("hello", "new stop: ${stop.name} replaces ${currentStop.value?.name}")
        currentStop.value = stop
    }

}

object DepartureMappers {
    fun toLineDepartures(stopPlace: StopPlaceQuery.StopPlace): List<LineDeparture> {
        val quays = stopPlace.quays ?: emptyList()
        return quays.flatMap { it?.estimatedCalls ?: emptyList() }
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
        val dest = estimatedCall.destinationDisplay?.frontText
        return if (publicCode != null && dest != null) {
            LineDirectionRef(publicCode, dest)
        } else {
            null
        }
    }

}