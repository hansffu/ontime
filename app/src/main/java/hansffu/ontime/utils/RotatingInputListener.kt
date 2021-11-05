package hansffu.ontime.utils

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.ScrollView
import androidx.compose.foundation.gestures.ScrollableState
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
import androidx.wear.widget.WearableRecyclerView
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class RotatingInputListener(private val context: Context) : View.OnGenericMotionListener {
    override fun onGenericMotion(view: View, ev: MotionEvent): Boolean =
        if (view is WearableRecyclerView &&
            ev.action == MotionEvent.ACTION_SCROLL &&
            ev.isFromSource(InputDeviceCompat.SOURCE_ROTARY_ENCODER)
        ) {
            view.smoothScrollBy(0, ev.getDelta(context).roundToInt())
            true
        } else {
            false
        }

}

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

            if (isScrollEvent) scope.launch { listState.scrollBy(event.getDelta(context)) }
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