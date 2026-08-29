package dev.hansffu.ontime.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.wear.ongoing.OngoingActivity
import dagger.hilt.android.AndroidEntryPoint
import dev.hansffu.ontime.NavigationActivity
import dev.hansffu.ontime.R
import dev.hansffu.ontime.database.dao.Board
import dev.hansffu.ontime.database.dao.BoardDao
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ActiveBoardService : Service() {
    @Inject lateinit var boardDao: BoardDao

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var activeBoardJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val boardId = intent?.getLongExtra(EXTRA_BOARD_ID, INVALID_BOARD_ID) ?: INVALID_BOARD_ID
        val boardName = intent?.getStringExtra(EXTRA_BOARD_NAME)
        if (boardId != INVALID_BOARD_ID && boardName != null) {
            showOngoingActivity(boardId, boardName)
        } else {
            showOngoingActivity(INVALID_BOARD_ID, getString(R.string.active_board))
        }

        if (activeBoardJob == null) {
            activeBoardJob =
                serviceScope.launch {
                    boardDao.observeActive().collectLatest { board ->
                        if (board == null) {
                            stopForeground(STOP_FOREGROUND_REMOVE)
                            stopSelf()
                        } else {
                            showOngoingActivity(board.id, board.name)
                        }
                    }
                }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        activeBoardJob = null
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun showOngoingActivity(boardId: Long, boardName: String) {
        val openBoardIntent =
            Intent(this, NavigationActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                if (boardId != INVALID_BOARD_ID) {
                    putExtra(NavigationActivity.EXTRA_BOARD_ID, boardId)
                }
            }
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                boardId.hashCode(),
                openBoardIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        val notificationBuilder =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(boardName)
                .setContentText(getString(R.string.active_board_status))
                .setSmallIcon(R.drawable.ic_board_activity)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setOngoing(true)

        OngoingActivity.Builder(this, NOTIFICATION_ID, notificationBuilder)
            .setTitle(boardName)
            .setContentDescription(boardName)
            .setStaticIcon(R.drawable.ic_board_activity)
            .setTouchIntent(pendingIntent)
            .build()
            .apply(this)

        startForeground(
            NOTIFICATION_ID,
            notificationBuilder.build(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
        )
    }

    private fun createNotificationChannel() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                getString(R.string.active_board_notification_channel),
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = getString(R.string.active_board_notification_channel_description)
            }
        )
    }

    companion object {
        private const val CHANNEL_ID = "active-board"
        private const val NOTIFICATION_ID = 10_001
        private const val EXTRA_BOARD_ID = "active-board-id"
        private const val EXTRA_BOARD_NAME = "active-board-name"
        private const val INVALID_BOARD_ID = -1L

        fun start(context: Context, board: Board) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, ActiveBoardService::class.java).apply {
                    putExtra(EXTRA_BOARD_ID, board.id)
                    putExtra(EXTRA_BOARD_NAME, board.name)
                },
            )
        }
    }
}
