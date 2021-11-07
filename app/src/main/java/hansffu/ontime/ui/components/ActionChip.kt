package hansffu.ontime.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.Text

@Composable
fun ActionChip(label: String, onClick: () -> Unit) {

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        CompactChip(
            label = {
                Text(
                    text = label,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(0.75f),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:shape=Square,width=300,height=300,unit=px,dpi=240",
    backgroundColor = 0x000000
)
@Composable
fun ActionChipPreview() {
    Column {
        Spacer(modifier = Modifier.size(30.dp))
        ActionChip(label = "I nærheten", onClick = { })
    }
}