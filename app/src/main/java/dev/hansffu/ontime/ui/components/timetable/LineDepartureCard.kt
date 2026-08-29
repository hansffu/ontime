package dev.hansffu.ontime.ui.components.timetable

import android.text.format.DateFormat
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
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
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date
import kotlinx.coroutines.launch

@Composable
fun LineDepartureCard(
    lineDirectionRef: LineDirectionRef,
    departureTimes: List<DepartureTime>,
    isFavorite: Boolean,
    toggleFavorite: (LineDirectionRef) -> Unit,
    color: String,
    now: OffsetDateTime,
    modifier: Modifier = Modifier,
    transformation: SurfaceTransformation? = null,
    revealState: RevealState = rememberRevealState(RevealValue.Covered),
    stopName: String? = null,
    manageBoards: (() -> Unit)? = null,
) {
    var expanded by
        rememberSaveable(
            lineDirectionRef.lineRef,
            lineDirectionRef.destinationRef,
            stopName,
        ) {
            mutableStateOf(false)
        }
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
    val expansionStateDescription =
        stringResource(
            if (expanded) R.string.departure_card_expanded
            else R.string.departure_card_collapsed
        )
    val accentColor =
        if (transitColor.luminance() < 0.2f) lerp(transitColor, Color.White, 0.38f)
        else transitColor
    val toggleAndClose: () -> Unit = {
        toggleFavorite(lineDirectionRef)
        coroutineScope.launch { revealState.animateTo(RevealValue.Covered) }
    }

    SwipeToReveal(
        modifier = modifier,
        revealState = revealState,
        primaryAction = {
            PrimaryActionButton(
                onClick = toggleAndClose,
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
        secondaryAction =
            manageBoards?.let { openBoards ->
                {
                    SecondaryActionButton(
                        onClick = {
                            openBoards()
                            coroutineScope.launch {
                                revealState.animateTo(RevealValue.Covered)
                            }
                        },
                        icon = {
                            Icon(
                                Icons.Default.Add,
                                stringResource(R.string.add_to_board),
                            )
                        },
                    )
                }
            },
        onSwipePrimaryAction = toggleAndClose,
    ) {
        Card(
            onClick = { expanded = !expanded },
            modifier = Modifier.semantics { stateDescription = expansionStateDescription },
            colors = CardDefaults.cardColors(),
            transformation = transformation,
        ) {
            Column(
                modifier = Modifier.animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                if (stopName != null) {
                    Text(
                        text = stopName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                }
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
                }
                if (expanded) {
                    ExpandedDepartureTimes(departureTimes = departureTimes, now = now)
                } else {
                    CollapsedDepartureTimes(departureTimes = departureTimes, now = now)
                }
            }
        }
    }
}

@Composable
private fun CollapsedDepartureTimes(
    departureTimes: List<DepartureTime>,
    now: OffsetDateTime,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        departureTimes.take(3).forEach { departure ->
            Text(
                text = departure.expected.toTimeString(now),
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ExpandedDepartureTimes(
    departureTimes: List<DepartureTime>,
    now: OffsetDateTime,
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        departureTimes.forEach { departure ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = departure.expected.toTimeString(now),
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (departure.isDelayed) {
                    Text(
                        text = departure.aimed.toClockTimeString(),
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textDecoration = TextDecoration.LineThrough,
                    )
                }
            }
        }
    }
}

data class DepartureTime(
    val aimed: OffsetDateTime,
    val expected: OffsetDateTime,
) {
    val isDelayed: Boolean
        get() = expected.isAfter(aimed)
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
private fun OffsetDateTime.toClockTimeString(): String {
    val formatter = DateFormat.getTimeFormat(LocalContext.current)
    return formatter.format(Date.from(toInstant()))
}

@Composable
private fun rememberTransitColor(color: String): Color {
    val fallback = MaterialTheme.colorScheme.primary
    return androidx.compose.runtime.remember(color, fallback) {
        runCatching { Color(("#" + color.removePrefix("#")).toColorInt()) }.getOrDefault(fallback)
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
        val now = OffsetDateTime.now(ZoneId.systemDefault())
        LineDepartureCard(
            lineDirectionRef = LineDirectionRef("23", "Lysaker"),
            departureTimes =
                listOf(
                    DepartureTime(now, now),
                    DepartureTime(
                        aimed = now.plus(4, ChronoUnit.MINUTES),
                        expected = now.plus(8, ChronoUnit.MINUTES),
                    ),
                    DepartureTime(
                        now.plus(24, ChronoUnit.MINUTES),
                        now.plus(24, ChronoUnit.MINUTES),
                    ),
                    DepartureTime(
                        now.plus(44, ChronoUnit.MINUTES),
                        now.plus(44, ChronoUnit.MINUTES),
                    ),
                ),
            isFavorite = true,
            toggleFavorite = {},
            color = "E60000",
            now = now,
            stopName = "Example stop",
            manageBoards = {},
        )
    }
}
