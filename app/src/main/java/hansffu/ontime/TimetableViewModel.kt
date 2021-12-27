package hansffu.ontime

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import hansffu.ontime.database.AppDatabase
import hansffu.ontime.database.dao.FavoriteStop
import hansffu.ontime.graphql.StopPlaceQuery
import hansffu.ontime.model.LineDeparture
import hansffu.ontime.model.LineDirectionRef
import hansffu.ontime.model.Stop
import hansffu.ontime.service.StopService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "TimetableViewModel"

class TimetableViewModel(application: Application) : AndroidViewModel(application) {
    private val stopService = StopService()
    private val db = AppDatabase.getDb(application)

    private val favoriteStops: LiveData<List<Stop>> =
        db.favoritesDao().getAll().map { stops ->
            stops.map { Stop(it.name, it.id) }
        }

    fun getDepartures(stopId: String): LiveData<List<LineDeparture>> {
        Log.d(TAG, "updating departures for $stopId")
        return liveData {
            stopService.getDepartures(stopId).stopPlace
                ?.let { DepartureMappers.toLineDepartures(it) }
                ?.let { emit(it) }
        }
    }

    fun isFavorite(stopId: String): LiveData<Boolean> =
        favoriteStops.map { favoriteStops ->
            favoriteStops.any { it.id == stopId }
        }


    fun toggleFavorite(id: String, name: String) = viewModelScope.launch(Dispatchers.IO) {
        val existing = db.favoritesDao().getById(id)
        if (existing != null) {
            db.favoritesDao().delete(existing)
        } else {
            db.favoritesDao().insertAll(FavoriteStop(id, name))
        }
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