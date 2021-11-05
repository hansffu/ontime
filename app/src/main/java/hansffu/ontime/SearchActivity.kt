package hansffu.ontime

import android.os.Bundle
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import hansffu.ontime.ui.theme.OntimeTheme

class SearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OntimeTheme {
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    SearchButton()
                }
            }
        }
    }
}

@Composable
fun SearchButton() {
    Button(onClick = { /*TODO*/ }) {
        Icon(
            Icons.Rounded.Search,
            contentDescription = "Search",
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    OntimeTheme {
        SearchButton()
    }
}