package dev.hansffu.ontime

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dagger.hilt.android.AndroidEntryPoint
import dev.hansffu.ontime.ui.navigation.MainNavigation
import dev.hansffu.ontime.ui.ambient.AmbientBoardHost

@AndroidEntryPoint
class NavigationActivity : ComponentActivity() {
    private var boardToOpen by mutableStateOf<Long?>(null)

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        boardToOpen = intent.boardId()

        setContent {
            AmbientBoardHost {
                MainNavigation(
                    boardToOpen = boardToOpen,
                    onBoardOpened = { boardToOpen = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        boardToOpen = intent.boardId()
    }

    private fun Intent.boardId(): Long? =
        getLongExtra(EXTRA_BOARD_ID, NO_BOARD_ID).takeUnless { it == NO_BOARD_ID }

    companion object {
        const val EXTRA_BOARD_ID = "board-id"
        private const val NO_BOARD_ID = -1L
    }
}
