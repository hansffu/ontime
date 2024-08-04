package dev.hansffu.ontime.ui.components

import android.app.RemoteInput
import android.content.Intent
import android.view.inputmethod.EditorInfo
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.input.RemoteInputIntentHelper
import androidx.wear.input.wearableExtender
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.material.Chip

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun TextInput(
    label: String,
    value: String?,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    onChange: (value: String) -> Unit,
) {
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            RemoteInput.getResultsFromIntent(it.data).getString(label)
        }
    val remoteInputs = remember {
        listOf(
            RemoteInput.Builder(label)
                .setLabel(label)
                .wearableExtender {
                    setEmojisAllowed(false)
                    setInputActionType(EditorInfo.IME_ACTION_SEARCH)
                }.build()
        )
    }
    Chip(
        label = if (value.isNullOrEmpty()) label else value,
        icon = icon?.let<ImageVector, @Composable() (BoxScope.() -> Unit)> { { Icon(it, label) } },
        colors = ChipDefaults.secondaryChipColors(),
        modifier = modifier,
        onClick = {
            val intent: Intent = RemoteInputIntentHelper.createActionRemoteInputIntent();

            RemoteInputIntentHelper.putRemoteInputsExtra(intent, remoteInputs)

            launcher.launch(intent)
        }
    )

}