package dev.hansffu.ontime.ui.stoplist.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.Text
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.fillMaxRectangle

@Composable
fun SearchScreen(searchString: String) {
    val searchViewModel: SearchViewModel = hiltViewModel()
    ScreenScaffold {
        Column(
            modifier = Modifier
                .fillMaxRectangle()
                .align(Alignment.Center)
        ) {
            Text(
                text = searchString,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}
