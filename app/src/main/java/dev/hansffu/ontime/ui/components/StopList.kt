package dev.hansffu.ontime.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnScope
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ListHeaderDefaults
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import dev.hansffu.ontime.model.Stop

fun TransformingLazyColumnScope.stopListSection(
    headerKey: String,
    header: String,
    stops: List<Stop>,
    transformationSpec: TransformationSpec,
    onStopClick: (Stop) -> Unit,
) {
    if (stops.isEmpty()) return

    listHeaderItem(
        key = "header-$headerKey",
        header = header,
        transformationSpec = transformationSpec,
    )
    items(items = stops, key = { stop -> headerKey to stop.id }) { stop ->
        Button(
            modifier =
                Modifier.fillMaxWidth()
                    .minimumVerticalContentPadding(
                        ButtonDefaults.minimumVerticalListContentPadding
                    )
                    .transformedHeight(this, transformationSpec)
                    .animateItem(),
            onClick = { onStopClick(stop) },
            colors = ButtonDefaults.filledTonalButtonColors(),
            transformation = SurfaceTransformation(transformationSpec),
            label = { Text(stop.name) },
        )
    }
}

fun TransformingLazyColumnScope.listHeaderItem(
    key: String,
    header: String,
    transformationSpec: TransformationSpec,
) {
    item(key = key) {
        ListHeader(
            modifier =
                Modifier.fillMaxWidth()
                    .minimumVerticalContentPadding(
                        top = ListHeaderDefaults.minimumTopListContentPadding,
                        bottom = ListHeaderDefaults.minimumBottomListContentPadding,
                    )
                    .transformedHeight(this, transformationSpec),
            transformation = SurfaceTransformation(transformationSpec),
        ) {
            Text(header)
        }
    }
}

fun TransformingLazyColumnScope.messageItem(
    key: String,
    message: String,
    transformationSpec: TransformationSpec,
) {
    item(key = key) {
        Text(
            text = message,
            textAlign = TextAlign.Center,
            modifier =
                Modifier.fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .transformedHeight(this, transformationSpec)
                    .graphicsLayer {
                        with(SurfaceTransformation(transformationSpec)) {
                            applyContentTransformation()
                            applyContainerTransformation()
                        }
                    },
        )
    }
}

fun TransformingLazyColumnScope.retryItem(
    key: String,
    message: String,
    retryLabel: String,
    transformationSpec: TransformationSpec,
    onRetry: () -> Unit,
) {
    item(key = key) {
        Button(
            modifier =
                Modifier.fillMaxWidth()
                    .minimumVerticalContentPadding(
                        ButtonDefaults.minimumVerticalListContentPadding
                    )
                    .transformedHeight(this, transformationSpec),
            onClick = onRetry,
            colors = ButtonDefaults.filledTonalButtonColors(),
            secondaryLabel = { Text(message) },
            transformation = SurfaceTransformation(transformationSpec),
            label = { Text(retryLabel) },
        )
    }
}
