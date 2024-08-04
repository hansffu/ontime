package dev.hansffu.ontime.ui.stoplist.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.fillMaxRectangle
import dev.hansffu.ontime.R
import dev.hansffu.ontime.ui.components.TextInput

@Composable
fun SearchScreen() {
    val searchViewModel: SearchViewModel = hiltViewModel()
    val searchQuery by searchViewModel.searchQuery.collectAsState()
    ScreenScaffold {
        Column(
            modifier = Modifier
                .fillMaxRectangle()
                .align(Alignment.Center)
        ) {
            TextInput(
                label = stringResource(id = R.string.search_button),
                icon = Icons.Default.Search,
                value = searchQuery,
                onChange = { searchViewModel.updateSearchQuery(it) },
                modifier = Modifier.fillMaxWidth(0.8f).align(Alignment.CenterHorizontally)
            )
        }
    }
}
