package dev.hansffu.ontime.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import dev.hansffu.ontime.database.AppDatabase
import dev.hansffu.ontime.database.dao.FavoriteStop
import dev.hansffu.ontime.graphql.StopPlaceQuery
import dev.hansffu.ontime.model.LineDeparture
import dev.hansffu.ontime.model.LineDirectionRef
import dev.hansffu.ontime.model.Stop
import dev.hansffu.ontime.service.StopService
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
            .groupBy(DepartureMappers::groupLines)
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