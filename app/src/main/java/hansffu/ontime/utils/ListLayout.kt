package hansffu.ontime.utils

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.widget.WearableLinearLayoutManager
import kotlin.math.abs
import kotlin.math.min

private const val MAX_ICON_PROGRESS = 0.65f
private const val SCALE_THRESHOLD = 0.25f

class ListLayout : WearableLinearLayoutManager.LayoutCallback() {

    override fun onLayoutFinished(child: View, parent: RecyclerView) {
        child.apply {
            val centerOffset = height.toFloat() / 2.0f / parent.height.toFloat()
            val yRelativeToCenterOffset = y / parent.height + centerOffset

            val progressToCenter: Float = run {
                val absoluteOffsetFromCenter = abs(0.5f - yRelativeToCenterOffset)
                val absoluteOffsetFromThreshold =
                    if (absoluteOffsetFromCenter > SCALE_THRESHOLD) absoluteOffsetFromCenter - SCALE_THRESHOLD
                    else 0f
                min(absoluteOffsetFromThreshold, MAX_ICON_PROGRESS)
            }

            scaleX = 1 - progressToCenter
            scaleY = 1 - progressToCenter
        }
    }
}