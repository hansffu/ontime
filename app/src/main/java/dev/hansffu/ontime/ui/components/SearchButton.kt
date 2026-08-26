package dev.hansffu.ontime.ui.components

import android.app.RemoteInput
import android.content.Intent
import android.view.inputmethod.EditorInfo
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.wear.compose.material3.FilledTonalIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.input.RemoteInputIntentHelper
import androidx.wear.input.wearableExtender

private const val RESULT_KEY = "search_result"

@Composable
fun SearchButton(
    onSubmit: (value: String) -> Unit,
    inputLabel: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource? = null,
) {
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            RemoteInput.getResultsFromIntent(it.data)
                .getCharSequence(RESULT_KEY)
                ?.toString()
                ?.trim()
                ?.takeIf(String::isNotEmpty)
                ?.let(onSubmit)
        }
    val remoteInputs = remember(inputLabel) {
        listOf(
            RemoteInput.Builder(RESULT_KEY)
                .setLabel(inputLabel)
                .wearableExtender {
                    setEmojisAllowed(false)
                    setInputActionType(EditorInfo.IME_ACTION_SEARCH)
                }
                .build()
        )
    }

    FilledTonalIconButton(
        modifier = modifier,
        interactionSource = interactionSource,
        onClick = {
            val intent: Intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
            RemoteInputIntentHelper.putRemoteInputsExtra(intent, remoteInputs)
            launcher.launch(intent)
        },
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = contentDescription,
        )
    }
}
