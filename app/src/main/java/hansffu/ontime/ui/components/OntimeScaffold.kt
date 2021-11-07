package hansffu.ontime.ui.components

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.*

@Composable
fun OntimeScaffold(
    scalingLazyListState: ScalingLazyListState? = null,
    content: @Composable () -> Unit
) {
    Scaffold(
        content = content,
        positionIndicator = { scalingLazyListState?.let { PositionIndicator(it) } },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
    )
}