package dev.hansffu.ontime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import dev.hansffu.ontime.ui.navigation.MainNavigation

@AndroidEntryPoint
class NavigationActivity : ComponentActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent { MainNavigation() }
    }
}

