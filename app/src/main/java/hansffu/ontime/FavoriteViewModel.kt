package hansffu.ontime

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hansffu.ontime.model.Stop
import hansffu.ontime.model.StopListModel
import hansffu.ontime.model.StopListType
import hansffu.ontime.model.StopListType.*
import hansffu.ontime.service.FavoriteService
import hansffu.ontime.service.StopService
import kotlinx.coroutines.launch

class FavoriteViewModel(application: Application) : AndroidViewModel(application) {
    private val favoriteService: FavoriteService = FavoriteService(application.applicationContext)
    private val stopService = StopService()
    private val favoriteStops: MutableLiveData<StopListModel?> = MutableLiveData(null)

    fun getStops(): MutableLiveData<StopListModel?> = favoriteStops

    fun load(type: StopListType) {
        // favoriteStops.value = favoriteService.getFavorites()
        viewModelScope.launch {
            favoriteStops.value = when (type) {
                FAVORITES -> StopListModel(type, favoriteService.getFavorites())
                NEARBY -> StopListModel(type, stopService.findStopsNear(requestLocation()))
            }
        }
    }
}

fun requestLocation(): Location =
    Location("flp").apply {
        longitude = 10.796757
        latitude = 59.932715
    }
