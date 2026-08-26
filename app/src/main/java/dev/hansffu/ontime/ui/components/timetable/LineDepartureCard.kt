package dev.hansffu.ontime.ui.components.timetable

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.RevealState
import androidx.wear.compose.material3.RevealValue
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.SwipeToReveal
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.rememberRevealState
import dev.hansffu.ontime.R
import dev.hansffu.ontime.model.LineDirectionRef
import dev.hansffu.ontime.ui.theme.OntimeTheme
import dev.hansffu.ontime.ui.theme.primaryTintedSurfaceColor
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date
import kotlinx.coroutines.launch

@Composable
fun LineDepartureCard(
    lineDirectionRef: LineDirectionRef,
    departureTimes: List<OffsetDateTime>,
    isFavorite: Boolean,
    toggleFavorite: (LineDirectionRef) -> Unit,
    color: String,
    now: OffsetDateTime,
    modifier: Modifier = Modifier,
    transformation: SurfaceTransformation? = null,
    revealState: RevealState = rememberRevealState(RevealValue.Covered),
) {
    val coroutineScope = rememberCoroutineScope()
    val actionDescription =
        stringResource(
            if (isFavorite) R.string.remove_departure_favorite
            else R.string.add_departure_favorite
        )
    val actionLabel =
        stringResource(
            if (isFavorite) R.string.remove_favorite_short
            else R.string.add_favorite_short
        )
    val transitColor = rememberTransitColor(color)
    val containerColor = primaryTintedSurfaceColor()
    val accentColor =
        if (transitColor.luminance() < 0.2f) lerp(transitColor, Color.White, 0.38f)
        else transitColor

    SwipeToReveal(
        modifier = modifier,
        revealState = revealState,
        primaryAction = {
            PrimaryActionButton(
                onClick = {
                    toggleFavorite(lineDirectionRef)
                    coroutineScope.launch { revealState.animateTo(RevealValue.Covered) }
                },
                icon = {
                    Icon(
                        imageVector =
                            if (isFavorite) Icons.Filled.Favorite
                            else Icons.Outlined.FavoriteBorder,
                        contentDescription = actionDescription,
                    )
                },
                text = { Text(actionLabel) },
            )
        },
        onSwipePrimaryAction = {
            coroutineScope.launch { revealState.animateTo(RevealValue.Covered) }
        },
    ) {
        Card(
            colors =
                CardDefaults.cardColors(
                    containerColor = containerColor,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            transformation = transformation,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = lineDirectionRef.destinationRef,
                        style = MaterialTheme.typography.titleSmall,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                    Text(
                        text = lineDirectionRef.lineRef,
                        style = MaterialTheme.typography.titleSmall,
                        color = accentColor,
                    )
                    Icon(
                        imageVector =
                            if (isFavorite) Icons.Filled.Favorite
                            else Icons.Outlined.FavoriteBorder,
                        contentDescription = actionDescription,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                DepartureTimes(departureTimes = departureTimes, now = now)
            }
        }
    }
}

@Composable
private fun DepartureTimes(
    departureTimes: List<OffsetDateTime>,
    now: OffsetDateTime,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        departureTimes.take(3).forEach { departure ->
            Text(
                text = departure.toTimeString(now),
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun OffsetDateTime.toTimeString(now: OffsetDateTime): String {
    val minutes = Duration.between(now, this).toMinutes()
    return when {
        minutes <= 0 -> stringResource(R.string.now)
        minutes < 20 -> stringResource(R.string.minutes_format, minutes)
        else -> {
            val formatter = DateFormat.getTimeFormat(LocalContext.current)
            formatter.format(Date.from(toInstant()))
        }
    }
}

@Composable
private fun rememberTransitColor(color: String): Color {
    val fallback = MaterialTheme.colorScheme.primary
    return androidx.compose.runtime.remember(color, fallback) {
        runCatching { Color("#${color.removePrefix("#")}".toColorInt()) }.getOrDefault(fallback)
    }
}

@Preview(
    showBackground = true,
    device = "spec:shape=Round,width=480,height=480,unit=px,dpi=340",
    backgroundColor = 0xFF000000,
)
@Composable
private fun LineDepartureCardPreview() {
    OntimeTheme {
        LineDepartureCard(
            lineDirectionRef = LineDirectionRef("23", "Lysaker"),
            departureTimes =
                listOf(
                    OffsetDateTime.now(),
                    OffsetDateTime.now().plus(8, ChronoUnit.MINUTES),
                    OffsetDateTime.now().plus(24, ChronoUnit.MINUTES),
                    OffsetDateTime.now().plus(44, ChronoUnit.MINUTES),
                ),
            isFavorite = true,
            toggleFavorite = {},
            color = "E60000",
            now = OffsetDateTime.now(ZoneId.systemDefault()),
        )
    }
}
