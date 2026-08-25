package dev.hansffu.ontime.ui.components.timetable

import android.annotation.SuppressLint
import android.graphics.Color.parseColor
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.RevealState
import androidx.wear.compose.material3.RevealValue
import androidx.wear.compose.material3.SwipeToReveal
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.rememberRevealState
import dev.hansffu.ontime.model.LineDirectionRef
import dev.hansffu.ontime.ui.theme.OntimeTheme
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun LineDepartureCard(
    lineDirectionRef: LineDirectionRef,
    departureTimes: List<OffsetDateTime>,
    isFavorite: Boolean,
    toggleFavorite: (LineDirectionRef) -> Unit,
    color: String,
    revealState: RevealState = rememberRevealState(RevealValue.Covered),
) {
    val coroutineScope = rememberCoroutineScope()
    SwipeToReveal(
        revealState = revealState,
        primaryAction = {
            PrimaryActionButton(
                onClick = {
                    toggleFavorite(lineDirectionRef)
                    coroutineScope.launch { revealState.animateTo(RevealValue.Covered) }
                },
                icon = {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        "Favoritt"
                    )
                },
                text = { Text("Favoritt") },
            )
        },
        onSwipePrimaryAction = { toggleFavorite(lineDirectionRef) })
    {
        Card(
            onClick = { coroutineScope.launch { revealState.animateTo(RevealValue.RightRevealing) } },
            colors = CardDefaults.cardColors(
                containerColor = Color(parseColor("#$color")),
            )
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        Text(
                            text = lineDirectionRef.destinationRef,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Box(contentAlignment = Alignment.TopEnd) {
                        Text(
                            text = lineDirectionRef.lineRef,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                DepartureTimes(departureTimes)
            }
        }
    }
}

@Composable
private fun DepartureTimes(departureTimes: List<OffsetDateTime>) {

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        departureTimes.forEach {
            Text(
                text = it.toTimeString(),
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@SuppressLint("SimpleDateFormat")
private fun OffsetDateTime.toTimeString(): String {
    val timeMins = Duration.between(OffsetDateTime.now(), this).toMinutes()
    return when {
        timeMins <= 0 -> "Nå"
        timeMins >= 20 -> format(DateTimeFormatter.ofPattern("HH:mm"))
        else -> "$timeMins\u00A0min"
    }
}


@Preview(
    showBackground = true,
    device = "spec:shape=Square,width=300,height=300,unit=px,dpi=240",
    backgroundColor = 0x000000
)
@Composable
fun DefaultPreview() {
    OntimeTheme {
        LineDepartureCard(
            lineDirectionRef = LineDirectionRef(
                lineRef = "23",
                destinationRef = "Lysaker and very long text",
            ),
            departureTimes = listOf(
                OffsetDateTime.now(),
                OffsetDateTime.now().plus(2, ChronoUnit.MINUTES),
                OffsetDateTime.now().plus(12, ChronoUnit.MINUTES),
                OffsetDateTime.now().plus(22, ChronoUnit.MINUTES),
            ),
            isFavorite = false,
            toggleFavorite = {},
            color = "111111"
        )
    }
}
