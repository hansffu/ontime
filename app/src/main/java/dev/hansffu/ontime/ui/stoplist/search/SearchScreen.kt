package dev.hansffu.ontime.ui.stoplist.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import dev.hansffu.ontime.R
import dev.hansffu.ontime.ui.components.stopListSection

@Composable
fun SearchScreen(
    searchString: String,
    columnState: ScalingLazyListState = rememberScalingLazyListState(),
    navController: NavController,
) {
    val searchViewModel: SearchViewModel = hiltViewModel()
    val stops by searchViewModel.stops.collectAsState()
    LaunchedEffect(key1 = searchString) {
        searchViewModel.search(searchString)
    }
    ScreenScaffold(scrollState = columnState) { contentPadding ->
        ScalingLazyColumn(
            state = columnState,
            contentPadding = contentPadding,
        ) {
            stopListSection(R.string.search_results, stops, navController)
        }
    }
}
