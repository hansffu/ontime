package dev.hansffu.ontime.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnScope
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ListHeaderDefaults
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import dev.hansffu.ontime.R
import dev.hansffu.ontime.model.Stop
import dev.hansffu.ontime.model.StopTransportMode
import dev.hansffu.ontime.ui.icons.DistanceIcon
import java.text.NumberFormat

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
            label = {
                Text(
                    text = stop.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            secondaryLabel = { StopDetails(stop) },
        )
    }
}

@Composable
private fun StopDetails(stop: Stop) {
    val transportIcons =
        stop.transportModes
            .mapNotNull(StopTransportMode::icon)
            .distinctBy(TransportIcon::drawable)
            .take(4)
    val distanceFormatter =
        remember {
            NumberFormat.getNumberInstance().apply {
                minimumFractionDigits = 0
                maximumFractionDigits = 1
            }
        }
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            transportIcons.forEach { transportIcon ->
                Icon(
                    painter = painterResource(transportIcon.drawable),
                    contentDescription = stringResource(transportIcon.description),
                    tint = Color.Unspecified,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        stop.distanceMeters?.let { meters ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = DistanceIcon,
                    contentDescription = stringResource(R.string.distance),
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text =
                        stringResource(
                            R.string.stop_distance_value,
                            distanceFormatter.format(meters / 1_000.0),
                        ),
                    maxLines = 1,
                )
            }
        }
    }
}

private data class TransportIcon(
    @param:DrawableRes val drawable: Int,
    @param:StringRes val description: Int,
)

private fun StopTransportMode.icon(): TransportIcon? =
    when (this) {
        StopTransportMode.AIR ->
            TransportIcon(R.drawable.ic_entur_plane, R.string.transport_mode_air)
        StopTransportMode.BUS,
        StopTransportMode.COACH,
        StopTransportMode.TROLLEYBUS,
        -> TransportIcon(R.drawable.ic_entur_bus, R.string.transport_mode_bus)
        StopTransportMode.METRO ->
            TransportIcon(R.drawable.ic_entur_metro, R.string.transport_mode_metro)
        StopTransportMode.MONORAIL,
        StopTransportMode.RAIL ->
            TransportIcon(R.drawable.ic_entur_train, R.string.transport_mode_rail)
        StopTransportMode.TAXI ->
            TransportIcon(R.drawable.ic_entur_taxi, R.string.transport_mode_taxi)
        StopTransportMode.TRAM ->
            TransportIcon(R.drawable.ic_entur_tram, R.string.transport_mode_tram)
        StopTransportMode.WATER ->
            TransportIcon(R.drawable.ic_entur_ferry, R.string.transport_mode_water)
        StopTransportMode.CABLEWAY,
        StopTransportMode.LIFT,
        -> TransportIcon(R.drawable.ic_entur_cableway, R.string.transport_mode_other)
        StopTransportMode.FUNICULAR ->
            TransportIcon(R.drawable.ic_entur_funicular, R.string.transport_mode_other)
        StopTransportMode.UNKNOWN -> null
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
