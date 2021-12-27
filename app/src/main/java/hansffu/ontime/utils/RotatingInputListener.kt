package hansffu.ontime.utils

import android.content.Context
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.InputDeviceCompat
import androidx.core.view.MotionEventCompat
import androidx.core.view.ViewConfigurationCompat
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.rememberScalingLazyListState
import kotlinx.coroutines.launch

@Composable
fun rememberScrollingScalingLazyListState(): ScalingLazyListState {
    val listState = rememberScalingLazyListState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    with(LocalView.current) {
        setOnGenericMotionListener { _, event ->
            val isScrollEvent = with(event) {
                action == MotionEvent.ACTION_SCROLL && isFromSource(InputDeviceCompat.SOURCE_ROTARY_ENCODER)
            }

            if (isScrollEvent) scope.launch { listState.animateScrollBy(event.getDelta(context)) }
            isScrollEvent
        }

        requestFocus()
    }

    return listState
}

private fun MotionEvent.getDelta(context: Context): Float {
    val axisValue = -getAxisValue(MotionEventCompat.AXIS_SCROLL)
    val scrollFactor = ViewConfigurationCompat.getScaledVerticalScrollFactor(
        ViewConfiguration.get(context),
        context
    )
    return axisValue * scrollFactor

}