@file:OptIn(ExperimentalHorologistApi::class)

package dev.hansffu.ontime.ui.stoplist.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberColumnState
import dev.hansffu.ontime.R
import dev.hansffu.ontime.ui.components.stopListSection

@Composable
fun SearchScreen(
    searchString: String,
    columnState: ScalingLazyColumnState = rememberColumnState(),
    navController: NavController,
) {
    val searchViewModel: SearchViewModel = hiltViewModel()
    val stops by searchViewModel.stops.collectAsState()
    LaunchedEffect(key1 = searchString) {
        searchViewModel.search(searchString)
    }
    ScreenScaffold {
        ScalingLazyColumn(columnState = columnState) {
            stopListSection(R.string.search_results, stops, navController)
        }
    }
}
