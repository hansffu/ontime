package dev.hansffu.ontime.ui.stoplist.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.hansffu.ontime.model.Stop
import dev.hansffu.ontime.service.SearchService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

const val TAG = "SearchViewModel"

@HiltViewModel
class SearchViewModel @Inject constructor(private val searchService: SearchService) : ViewModel() {
    val stops = MutableStateFlow<List<Stop>>(emptyList())

    fun search(searchString: String) {
        viewModelScope.launch {
            Log.i(TAG, "searching for $searchString")
            val found = searchService.search(searchString)
            stops.emit(found)
        }
    }
}