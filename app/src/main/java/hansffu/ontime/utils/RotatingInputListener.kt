package hansffu.ontime.utils

import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.view.InputDeviceCompat
import androidx.core.view.MotionEventCompat
import androidx.core.view.ViewConfigurationCompat
import androidx.wear.widget.WearableRecyclerView
import kotlin.math.roundToInt

class RotatingInputListener(private val context: Context) : View.OnGenericMotionListener {
    override fun onGenericMotion(view: View, ev: MotionEvent): Boolean =
        if (view is WearableRecyclerView &&
            ev.action == MotionEvent.ACTION_SCROLL &&
            ev.isFromSource(InputDeviceCompat.SOURCE_ROTARY_ENCODER)
        ) {
            val delta = -ev.getAxisValue(MotionEventCompat.AXIS_SCROLL) *
                    ViewConfigurationCompat.getScaledVerticalScrollFactor(
                        ViewConfiguration.get(context), context
                    )
            view.smoothScrollBy(0, delta.roundToInt())
            true
        } else {
            false
        }

}
