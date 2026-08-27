package dev.hansffu.ontime.ui.components

import android.app.RemoteInput
import android.content.Intent
import android.view.inputmethod.EditorInfo
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.input.RemoteInputIntentHelper
import androidx.wear.input.wearableExtender

private const val REMOTE_INPUT_RESULT = "board_input_result"

@Composable
fun RemoteInputButton(
    label: String,
    inputLabel: String,
    onSubmit: (String) -> Unit,
    modifier: Modifier = Modifier,
    value: String? = null,
    transformation: SurfaceTransformation? = null,
) {
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            it.data?.let(RemoteInput::getResultsFromIntent)
                ?.getCharSequence(REMOTE_INPUT_RESULT)
                ?.toString()
                ?.trim()
                ?.takeIf(String::isNotEmpty)
                ?.let(onSubmit)
        }
    val remoteInputs =
        remember(inputLabel) {
            listOf(
                RemoteInput.Builder(REMOTE_INPUT_RESULT)
                    .setLabel(inputLabel)
                    .wearableExtender {
                        setEmojisAllowed(false)
                        setInputActionType(EditorInfo.IME_ACTION_DONE)
                    }.build()
            )
        }

    Button(
        modifier = modifier,
        onClick = {
            val intent: Intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
            RemoteInputIntentHelper.putRemoteInputsExtra(intent, remoteInputs)
            launcher.launch(intent)
        },
        colors = ButtonDefaults.filledTonalButtonColors(),
        transformation = transformation,
        label = { Text(label) },
        secondaryLabel = value?.let { current -> { Text(current) } },
    )
}
