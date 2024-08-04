package dev.hansffu.ontime.ui.stoplist.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

const val TAG = "SearchViewModel"

@HiltViewModel
class SearchViewModel @Inject constructor() : ViewModel() {
    private val mutableSearchQuery: MutableStateFlow<String?> = MutableStateFlow(null)
    val searchQuery: StateFlow<String?> = mutableSearchQuery

    fun updateSearchQuery(searchString: String) {
        viewModelScope.launch {
            Log.i(TAG, "searching for $searchString")
            mutableSearchQuery.emit(searchString)
        }
    }
}