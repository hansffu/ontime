package dev.hansffu.ontime.ui.boards

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.EdgeButtonSize
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Picker
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.rememberPickerState
import dev.hansffu.ontime.R
import dev.hansffu.ontime.model.BoardDistance

@Composable
fun BoardDistancePickerScreen(
    initialDistanceMeters: Int,
    onDistanceConfirmed: (Int) -> Unit,
) {
    val initialKilometers = BoardDistance.fromMeters(initialDistanceMeters)
    val pickerState =
        rememberPickerState(
            initialNumberOfOptions = BoardDistance.OPTION_COUNT,
            initiallySelectedIndex = initialKilometers - BoardDistance.MIN_KILOMETERS,
            shouldRepeatOptions = false,
        )
    val selectedKilometers =
        pickerState.selectedOptionIndex + BoardDistance.MIN_KILOMETERS
    val valueDescription =
        stringResource(R.string.distance_picker_value, selectedKilometers)

    Column(
        modifier =
            Modifier.fillMaxSize()
                .scrollable(
                    state = pickerState,
                    orientation = Orientation.Vertical,
                    reverseDirection = true,
                ),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.activation_radius),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Picker(
                modifier = Modifier.size(width = 90.dp, height = 112.dp),
                state = pickerState,
                contentDescription = { valueDescription },
            ) { optionIndex ->
                Text(
                    text =
                        (optionIndex + BoardDistance.MIN_KILOMETERS).toString(),
                    style = MaterialTheme.typography.displayMedium,
                )
            }
            Text(text = "km", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(4.dp))
        EdgeButton(
            onClick = { onDistanceConfirmed(selectedKilometers) },
            buttonSize = EdgeButtonSize.Small,
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.confirm),
            )
        }
    }
}
