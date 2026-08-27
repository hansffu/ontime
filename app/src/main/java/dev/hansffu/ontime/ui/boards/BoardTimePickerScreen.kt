package dev.hansffu.ontime.ui.boards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.google.android.horologist.composables.TimePicker
import dev.hansffu.ontime.model.BoardTime
import java.time.LocalTime

@Composable
fun BoardTimePickerScreen(
    title: String,
    initialMinuteOfDay: Int,
    onTimeConfirmed: (LocalTime) -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        TimePicker(
            onTimeConfirm = onTimeConfirmed,
            time = BoardTime.fromMinuteOfDay(initialMinuteOfDay),
            showSeconds = false,
        )
        Box(
            modifier =
                Modifier.align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}
