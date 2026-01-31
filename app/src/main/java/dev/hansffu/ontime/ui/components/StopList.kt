@file:OptIn(ExperimentalHorologistApi::class)

package dev.hansffu.ontime.ui.components

import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.material.Chip
import com.google.android.horologist.compose.material.ResponsiveListHeader
import dev.hansffu.ontime.model.Stop
import dev.hansffu.ontime.ui.navigation.Screen

fun ScalingLazyListScope.stopListSection(headerResource: Int, stops: List<Stop>, navController: NavController) {
    item { ResponsiveListHeader { Text(stringResource(headerResource)) } }
    items(stops) {
        Chip(
            label = it.name,
            onClick = { navController.navigate(Screen.Timetable(it)) })
    }
}