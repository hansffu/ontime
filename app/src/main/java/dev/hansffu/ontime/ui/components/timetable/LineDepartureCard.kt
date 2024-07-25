package dev.hansffu.ontime.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import dev.hansffu.ontime.model.LineDirectionRef
import dev.hansffu.ontime.ui.theme.OntimeTheme
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun LineDepartureCard(

    lineDirectionRef: LineDirectionRef,
    departureTimes: List<OffsetDateTime>,
) {
    Card(
        onClick = {},
        content = {
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
    )
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
            LineDirectionRef(
                "23",
                "Lysaker and very long text",
            ),
            listOf(
                OffsetDateTime.now(),
                OffsetDateTime.from(Instant.now().plus(2, ChronoUnit.MINUTES)),
                OffsetDateTime.from(Instant.now().plus(12, ChronoUnit.MINUTES)),
                OffsetDateTime.from(Instant.now().plus(22, ChronoUnit.MINUTES)),
            )
        )
    }
}

