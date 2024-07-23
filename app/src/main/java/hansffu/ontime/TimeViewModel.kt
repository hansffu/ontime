package hansffu.ontime

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.text.format.DateFormat
import androidx.core.os.ConfigurationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class TimeViewModel(application: Application) : AndroidViewModel(application) {
    val timeChannel: Channel<Date> = Channel()

    private val currentTime: LiveData<Date> = liveData {
        emit(Calendar.getInstance().time)
        registerReceiver()
        timeChannel.consumeEach { emit(it) }
    }

    val shortTime: LiveData<String> = currentTime.map {
        val pattern = DateFormat.getBestDateTimePattern(
            ConfigurationCompat.getLocales(application.resources.configuration)[0],
            "Hm"
        )
        DateFormat.format(pattern, it).toString()
    }

    private fun registerReceiver() {
        getApplication<Application>().applicationContext.registerReceiver(
            timeBroadcastReceiver,
            timeBroadcastReceiverFilter
        )
    }

    private val timeBroadcastReceiverFilter = IntentFilter().apply {
        addAction(Intent.ACTION_TIME_TICK)
        addAction(Intent.ACTION_TIME_CHANGED)
        addAction(Intent.ACTION_TIMEZONE_CHANGED)
    }

    private val timeBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            viewModelScope.launch {
                when (intent.action) {
                    Intent.ACTION_TIMEZONE_CHANGED,
                    Intent.ACTION_TIME_TICK,
                    Intent.ACTION_TIME_CHANGED -> {
                        timeChannel.send(Calendar.getInstance().time)
                    }
                }
            }
        }
    }
}