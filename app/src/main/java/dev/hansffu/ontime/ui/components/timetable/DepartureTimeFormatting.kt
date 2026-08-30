package dev.hansffu.ontime.ui.components.timetable

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import dev.hansffu.ontime.R
import java.time.Duration
import java.time.OffsetDateTime
import java.util.Date

@Composable
fun OffsetDateTime.toDepartureTimeString(now: OffsetDateTime): String {
    val minutes = Duration.between(now, this).toMinutes()
    return when {
        minutes <= 0 -> stringResource(R.string.now)
        minutes < 20 -> stringResource(R.string.minutes_format, minutes)
        else -> toClockTimeString()
    }
}

@Composable
fun OffsetDateTime.toClockTimeString(): String =
    DateFormat.getTimeFormat(LocalContext.current).format(Date.from(toInstant()))
