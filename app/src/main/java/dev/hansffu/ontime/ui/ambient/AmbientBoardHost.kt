package dev.hansffu.ontime.ui.ambient

import android.os.SystemClock
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.AmbientMode
import androidx.wear.compose.foundation.AmbientTickEffect
import androidx.wear.compose.foundation.LocalAmbientModeManager
import androidx.wear.compose.foundation.rememberAmbientModeManager
import dev.hansffu.ontime.viewmodels.AmbientBoardViewModel
import java.time.OffsetDateTime

@Composable
fun AmbientBoardHost(
    viewModel: AmbientBoardViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // With no active session, leave the app's normal system-managed dimming behavior alone.
    val manager = if (state != null) rememberAmbientModeManager() else null
    val ambient = manager?.currentAmbientMode as? AmbientMode.Ambient
    var now by remember(ambient) { mutableStateOf(OffsetDateTime.now()) }
    var elapsed by remember(ambient) { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    manager?.AmbientTickEffect {
        now = OffsetDateTime.now()
        elapsed = SystemClock.elapsedRealtime()
    }
    CompositionLocalProvider(LocalAmbientModeManager provides manager) {
        Box(Modifier.fillMaxSize()) {
            // Keep navigation and remembered editor/scroll state alive, but neither draw nor
            // expose the hidden controls to accessibility while the ambient surface is visible.
            Box(
                Modifier.fillMaxSize()
                    .graphicsLayer { alpha = if (ambient == null) 1f else 0f }
                    .then(if (ambient != null) Modifier.clearAndSetSemantics {} else Modifier)
            ) {
                content()
            }
            if (ambient != null) {
                state?.let {
                    AmbientBoardScreen(it.timetable, now, elapsed, ambient)
                }
            }
        }
    }
}
