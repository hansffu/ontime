package dev.hansffu.ontime.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.focus.FocusRequester

val LocalRotatingInputConsumer = compositionLocalOf<FocusRequester?> { null }