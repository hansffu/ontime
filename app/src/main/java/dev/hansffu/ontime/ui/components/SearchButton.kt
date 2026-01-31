package dev.hansffu.ontime.ui.components

import android.app.RemoteInput
import android.content.Intent
import android.view.inputmethod.EditorInfo
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.wear.input.RemoteInputIntentHelper
import androidx.wear.input.wearableExtender
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.material.Button
import dev.hansffu.ontime.R


const val RESULT_KEY = "search_result"

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun SearchButton(
    onSubmit: (value: String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            RemoteInput.getResultsFromIntent(it.data)
                .getCharSequence(RESULT_KEY)
                ?.toString()
                ?.let(onSubmit)
        }
    val remoteInputs = remember {
        listOf(
            RemoteInput.Builder(RESULT_KEY)
                .setLabel(label)
                .wearableExtender {
                    setEmojisAllowed(false)
                    setInputActionType(EditorInfo.IME_ACTION_SEARCH)
                }.build()
        )
    }
    Button(
        imageVector = Icons.Default.Search,
        contentDescription = stringResource(id = R.string.search_for_stops),
        modifier = modifier,
        onClick = {
            val intent: Intent = RemoteInputIntentHelper.createActionRemoteInputIntent()

            RemoteInputIntentHelper.putRemoteInputsExtra(intent, remoteInputs)

            launcher.launch(intent)
        }
    )

}