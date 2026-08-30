package dev.hansffu.ontime.ui.ambient

import android.graphics.Typeface
import android.text.TextPaint
import android.text.TextUtils
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.sp
import androidx.core.graphics.withTranslation
import androidx.wear.compose.foundation.AmbientMode
import dev.hansffu.ontime.R
import dev.hansffu.ontime.model.BoardTimetableState
import dev.hansffu.ontime.model.isStaleForAmbient
import dev.hansffu.ontime.model.nearestStopTimetable
import dev.hansffu.ontime.ui.components.timetable.toClockTimeString
import dev.hansffu.ontime.ui.components.timetable.toDepartureTimeString
import java.time.OffsetDateTime

/** A static B1 table. Native text painting also lets low-bit displays disable anti-aliasing. */
@Composable
fun AmbientBoardScreen(
    state: BoardTimetableState,
    now: OffsetDateTime,
    elapsedMillis: Long,
    ambient: AmbientMode.Ambient,
    modifier: Modifier = Modifier,
) {
    val content = state as? BoardTimetableState.Content
    val stop = remember(content?.rows, now) { content?.rows?.nearestStopTimetable(now) }
    val stale = content?.isStaleForAmbient(elapsedMillis) == true
    val clock = now.toClockTimeString()
    val rows = stop?.departures.orEmpty().take(4)
    val times = rows.map { if (stale) "—" else it.expected.toDepartureTimeString(now) }
    val message = when {
        state == BoardTimetableState.Loading -> stringResource(R.string.loading_departures)
        state is BoardTimetableState.Error -> stringResource(R.string.departures_error)
        content?.rows?.isEmpty() == true -> stringResource(R.string.no_board_departures)
        stop == null -> stringResource(R.string.ambient_location_unknown)
        rows.isEmpty() -> stringResource(R.string.no_departures)
        else -> null
    }
    val footer = if (stale) stringResource(R.string.ambient_stale_departures) else null
    val description = listOfNotNull(
        clock,
        stop?.stopName,
        message,
        rows.mapIndexed { index, row -> "${row.line}, ${row.destination}, ${times[index]}" }
            .joinToString("; ").takeIf { it.isNotEmpty() },
        footer,
    ).joinToString(". ")
    val paint = remember { TextPaint().apply { typeface = Typeface.create("sans-serif", Typeface.NORMAL) } }
    Canvas(
        modifier.fillMaxSize()
            .semantics { contentDescription = description }
            // A wake-up touch must never activate an invisible board/editor control underneath.
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) awaitPointerEvent().changes.forEach { it.consume() }
                }
            }
    ) {
        drawRect(Color.Black)
        val canvas = drawContext.canvas.nativeCanvas
        val diameter = minOf(size.width, size.height)
        val xOrigin = (size.width - diameter) / 2
        val yOrigin = (size.height - diameter) / 2
        val phase = ((now.toEpochSecond() / 60) % 4).toInt()
        val shiftX = if (ambient.isBurnInProtectionRequired) if (phase < 2) -2f else 2f else 0f
        val shiftY = if (ambient.isBurnInProtectionRequired) if (phase % 2 == 0) -2f else 2f else 0f
        canvas.withTranslation(xOrigin + shiftX, yOrigin + shiftY) {
            paint.isAntiAlias = !ambient.isLowBitAmbientSupported
            paint.isSubpixelText = !ambient.isLowBitAmbientSupported
            val primary = if (ambient.isLowBitAmbientSupported) android.graphics.Color.WHITE else 0xffcccccc.toInt()
            val secondary = if (ambient.isLowBitAmbientSupported) primary else 0xffaaaaaa.toInt()
            fun text(value: String, x: Float, baseline: Float, width: Float, centered: Boolean = false) {
                val fitted = TextUtils.ellipsize(value, paint, width.coerceAtLeast(0f), TextUtils.TruncateAt.END).toString()
                canvas.drawText(fitted, if (centered) x - paint.measureText(fitted) / 2 else x, baseline, paint)
            }
            paint.color = primary
            paint.textSize = 19.sp.toPx()
            text(clock, diameter / 2, diameter * .18f, diameter * .64f, centered = true)
            paint.color = secondary
            paint.textSize = 14.sp.toPx()
            stop?.let { text(it.stopName, diameter / 2, diameter * .29f, diameter * .74f, centered = true) }
            if (message != null) {
                paint.textSize = 13.sp.toPx()
                text(message, diameter / 2, diameter * .53f, diameter * .80f, centered = true)
            } else {
                val left = diameter * .12f
                val right = diameter * .88f
                paint.textSize = 14.sp.toPx()
                val timeWidth = times.maxOfOrNull(paint::measureText) ?: 0f
                val lineWidth = (rows.maxOfOrNull { paint.measureText(it.line) } ?: 0f)
                    .coerceAtMost(diameter * .20f)
                val gap = 6.sp.toPx()
                val destinationX = left + lineWidth + gap
                val destinationWidth = right - timeWidth - gap - destinationX
                rows.forEachIndexed { index, row ->
                    val baseline = diameter * (.42f + .11f * index)
                    paint.color = primary
                    paint.textSize = 14.sp.toPx()
                    text(row.line, left, baseline, lineWidth)
                    text(times[index], right - paint.measureText(times[index]), baseline, timeWidth)
                    paint.textSize = 13.sp.toPx()
                    text(row.destination, destinationX, baseline, destinationWidth)
                }
            }
            footer?.let {
                paint.color = secondary
                paint.textSize = 11.sp.toPx()
                text(it, diameter / 2, diameter * .86f, diameter * .60f, centered = true)
            }
        }
    }
}
