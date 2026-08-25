package dev.hansffu.ontime.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.Text
import dev.hansffu.ontime.model.Stop
import dev.hansffu.ontime.ui.navigation.Screen

fun ScalingLazyListScope.stopListSection(headerResource: Int, stops: List<Stop>, navController: NavController) {
    item { ListHeader { Text(stringResource(headerResource)) } }
    items(stops) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate(Screen.Timetable(it).route()) },
            label = { Text(it.name) },
        )
    }
}
