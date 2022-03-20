package hansffu.ontime.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import hansffu.ontime.utils.rememberScrollingScalingLazyListState

@Composable
fun OntimeList(
    headerText: String,
    scalingLazyListState: ScalingLazyListState = rememberScrollingScalingLazyListState(),
    content: ScalingLazyListScope.() -> Unit
){

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        state = scalingLazyListState,
        contentPadding = PaddingValues(
            top = 28.dp,
            start = 10.dp,
            end = 10.dp,
            bottom = 40.dp
        ),
    ) {
        item { ListHeader { Text(headerText, textAlign = TextAlign.Center) } }
        content()
    }
}