package hansffu.ontime

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.wear.widget.WearableLinearLayoutManager
import androidx.wear.widget.WearableRecyclerView
import hansffu.ontime.adapter.TimetableAdapter
import hansffu.ontime.databinding.ActivityTimetableBinding
import hansffu.ontime.model.Stop
import hansffu.ontime.utils.ListLayout
import hansffu.ontime.utils.RotatingInputListener

class TimetableActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimetableBinding
    private lateinit var timetableAdapter: TimetableAdapter
    private lateinit var stopId: String
    private lateinit var stopName: String
    private val timetableModel: TimetableViewModel by viewModels()
    private val timeModel: TimeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimetableBinding.inflate(layoutInflater)
        setContentView(binding.root)

        stopId = intent.getStringExtra(STOP_ID)!!
        stopName = intent.getStringExtra(STOP_NAME)!!
        timetableAdapter = TimetableAdapter(stopName)

        timeModel.shortTime.observe(this){
            binding.clock.text = it ?: ""
        }

        binding.departureList.apply {
            adapter = timetableAdapter
            isEdgeItemsCenteringEnabled = false
            layoutManager = WearableLinearLayoutManager(context, ListLayout())
            setOnGenericMotionListener(RotatingInputListener(context))
        }

        binding.bottomActionDrawer.setOnMenuItemClickListener {
            onMenuItemClick(it)
            false
        }

        timetableModel.setCurrentStop(Stop(stopName, stopId))

        startBackgroundTasks()
    }

    private fun startBackgroundTasks() {

        binding.departureList.setOnScrollChangeListener { view, _, _, _, _ ->
            if (view is WearableRecyclerView) {
                binding.clock.y = -view.computeVerticalScrollOffset().toFloat()
            }
        }
        timetableModel.isFavorite.observe(this) {
            toggleFavorite(it)
            if (!it) {
                binding.bottomActionDrawer.controller.peekDrawer()
            }
        }

        timetableModel.getLineDepartures().observe(this) {
            if (it != null) {
                timetableAdapter.estimatedCall = it
                binding.progressBar.visibility = View.INVISIBLE
                binding.departureList.requestFocus()
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        binding.progressBar.visibility = View.VISIBLE
        timetableModel.loadDepartures(Stop(stopName, stopId))
    }


    private fun toggleFavorite(isFavorite: Boolean) {
        val menuItem = binding.bottomActionDrawer.menu.findItem(R.id.toggle_favorite)
        menuItem.setIcon(if (isFavorite) R.drawable.ic_favorite_white_48dp else R.drawable.ic_favorite_border_white_48dp)
        menuItem.setTitle(if (isFavorite) R.string.add_favorite else R.string.remove_favorite)
    }

    private fun onMenuItemClick(menuItem: MenuItem) {
        if (menuItem.itemId == R.id.toggle_favorite) {
            timetableModel.toggleFavorite(Stop(stopName, stopId))
        }
    }

    companion object {

        const val TAG = "Stop Selector"
        const val STOP_ID = "stopId"
        const val STOP_NAME = "stopName"
    }
}
