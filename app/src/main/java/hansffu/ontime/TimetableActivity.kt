package hansffu.ontime

import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.*
import hansffu.ontime.model.Stop
import hansffu.ontime.ui.theme.OntimeTheme
import hansffu.ontime.ui.timetable.Timetable
import hansffu.ontime.utils.rememberScrollingScalingLazyListState

class TimetableActivity : ComponentActivity() {

    private lateinit var stopId: String
    private lateinit var stopName: String
    private val timetableModel: TimetableViewModel by viewModels()

    @OptIn(ExperimentalWearMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val scalingLazyListState = rememberScrollingScalingLazyListState()
            OntimeTheme {
                Scaffold(
                    timeText = { TimeText() },
                    modifier = Modifier.fillMaxSize() then Modifier.background(MaterialTheme.colors.background),
                    positionIndicator = { PositionIndicator(scalingLazyListState) }
                ) {
                    Timetable(stop = Stop(stopName, stopId),
                        timetableViewModel = timetableModel,
                        scalingLazyListState = scalingLazyListState
                    )
                }
            }
        }

        stopId = intent.getStringExtra(STOP_ID)!!
        stopName = intent.getStringExtra(STOP_NAME)!!

        timetableModel.setCurrentStop(Stop(stopName, stopId))

    }

    public override fun onResume() {
        super.onResume()
        timetableModel.loadDepartures(Stop(stopName, stopId))
    }

    companion object {

        const val TAG = "Stop Selector"
        const val STOP_ID = "stopId"
        const val STOP_NAME = "stopName"
    }
}
