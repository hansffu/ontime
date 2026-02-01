package dev.hansffu.ontime.ui.components.timetable

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.sharp.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.edgeSwipeToDismiss
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.RevealState
import androidx.wear.compose.material.RevealValue
import androidx.wear.compose.material.SwipeToRevealCard
import androidx.wear.compose.material.SwipeToRevealPrimaryAction
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberRevealState
import dev.hansffu.ontime.model.LineDirectionRef
import dev.hansffu.ontime.ui.theme.OntimeTheme
import dev.hansffu.ontime.viewmodels.TimetableViewModel
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun LineDepartureCard(
    stopId: String,
    lineDirectionRef: LineDirectionRef,
    departureTimes: List<OffsetDateTime>,
    isFavorite: Boolean,
    toggleFavorite: (LineDirectionRef) -> Unit,
    revealState: RevealState = rememberRevealState(RevealValue.Covered),
) {
    val coroutineScope = rememberCoroutineScope()
    SwipeToRevealCard(
        revealState = revealState,
        primaryAction = {
            SwipeToRevealPrimaryAction(
                revealState = revealState,
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
                label = { Text("Favoritt") },
            )
        },
        onFullSwipe = { coroutineScope.launch { revealState.animateTo(RevealValue.RightRevealing) } })
    {
        Card(onClick = { coroutineScope.launch { revealState.animateTo(RevealValue.RightRevealing) } }) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        Text(
                            text = lineDirectionRef.destinationRef,
                            style = MaterialTheme.typography.title3,
                            color = MaterialTheme.colors.onSurface,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Box(contentAlignment = Alignment.TopEnd) {
                        Text(
                            text = lineDirectionRef.lineRef,
                            style = MaterialTheme.typography.title3,
                            color = MaterialTheme.colors.onSurface,
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    departureTimes.forEach {
                        Text(
                            text = it.toTimeString(),
                            overflow = TextOverflow.Ellipsis,
                            softWrap = false,
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.onSurfaceVariant
                        )
                    }
                }
            }
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


@OptIn(ExperimentalWearMaterialApi::class)
@Preview(
    showBackground = true,
    device = "spec:shape=Square,width=300,height=300,unit=px,dpi=240",
    backgroundColor = 0x000000
)
@Composable
fun DefaultPreview() {
    OntimeTheme {
        LineDepartureCard(
            stopId = "",
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
            toggleFavorite = {}
        )
    }
}

