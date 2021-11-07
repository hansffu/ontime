package hansffu.ontime.ui.components

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.*

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun OntimeScaffold(
    scalingLazyListState: ScalingLazyListState? = null,
    content: @Composable () -> Unit
) {
    Scaffold(
        content = content,
        timeText = { TimeText() },
        positionIndicator = { scalingLazyListState?.let { PositionIndicator(it) } },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
    )
}