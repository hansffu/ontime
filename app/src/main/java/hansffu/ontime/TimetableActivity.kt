package hansffu.ontime

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.wear.widget.WearableLinearLayoutManager
import hansffu.ontime.adapter.TimetableAdapter
import hansffu.ontime.databinding.ActivityTimetableBinding
import hansffu.ontime.model.LineDeparture
import hansffu.ontime.model.LineDirectionRef
import hansffu.ontime.model.Stop
import hansffu.ontime.service.FavoriteService
import hansffu.ontime.service.StopService
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TimetableActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimetableBinding
    private lateinit var timetableAdapter: TimetableAdapter
    private lateinit var stopId: String
    private lateinit var stopName: String
    private lateinit var favoriteService: FavoriteService
    private val stopService = StopService()
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimetableBinding.inflate(layoutInflater)
        setContentView(binding.root)

        stopId = intent.getStringExtra(STOP_ID)!!
        stopName = intent.getStringExtra(STOP_NAME)!!
        favoriteService = FavoriteService(this)

        binding.departureList.apply {
            adapter = TimetableAdapter(this@TimetableActivity, stopName)
            isEdgeItemsCenteringEnabled = true
            layoutManager = WearableLinearLayoutManager(this@TimetableActivity)
        }

        binding.bottomActionDrawer.setOnMenuItemClickListener { onMenuItemClick(it) }

        val toggleFavoriteMenuItem = binding.bottomActionDrawer.menu.findItem(R.id.toggle_favorite)
        val favorite = favoriteService.isFavorite(Stop(stopName, stopId))
        toggleFavorite(favorite, toggleFavoriteMenuItem)
        if (!favorite) {
            binding.bottomActionDrawer.controller.peekDrawer()
        }
    }

    public override fun onResume() {
        super.onResume()
        setListContent()
    }

    private fun setListContent() {

        binding.progressBar.visibility = View.VISIBLE
        // updateTimetibles(timetableAdapter)
        binding.departureList.requestFocus()
    }

    fun groupLines(estimatedCall: StopPlaceQuery.EstimatedCall): LineDirectionRef? {
        val publicCode = estimatedCall.serviceJourney?.line?.publicCode
        val dest = estimatedCall.destinationDisplay()?.frontText()
        return if (publicCode != null && dest != null) {
            LineDirectionRef(publicCode, dest)
        } else {
            null
        }
    }

    private fun toLineDepartures(stopPlace: StopPlaceQuery.StopPlace): List<LineDeparture> {
        val quays = stopPlace.quays ?: emptyList()
        return quays.flatMap { it.estimatedCalls() }
            .asSequence()
            .filterNotNull()
            .groupBy(::groupLines)
            .map { (ref, departures) -> ref?.let { LineDeparture(it, departures) } }
            .filterNotNull()
            .sortedBy { lineDeparture ->
                lineDeparture.departures
                    .mapNotNull { call -> call.expectedArrivalTime }
                    .minOrNull()
            }
            .toList()
    }

    private suspend fun updateTimetibles(adapter: TimetableAdapter) {
        val estimatedCalls = withContext(Dispatchers.IO) {
            stopService.getDepartures(stopId).stopPlace?.let {
                toLineDepartures(it)
            } ?: emptyList()
        }
        withContext(Dispatchers.Main) {
            adapter.estimatedCall = estimatedCalls
            binding.progressBar.visibility = View.GONE
            binding.departureList.requestFocus()
        }
    }

    private fun toggleFavorite(isFavorite: Boolean, menuItem: MenuItem) {
        menuItem.setIcon(if (isFavorite) R.drawable.ic_favorite_white_48dp else R.drawable.ic_favorite_border_white_48dp)
        menuItem.setTitle(if (isFavorite) R.string.add_favorite else R.string.remove_favorite)
    }

    @SuppressLint("CheckResult")
    private fun onMenuItemClick(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.toggle_favorite) {
            val stop = favoriteService.toggleFavorite(Stop(stopName, stopId))
            toggleFavorite(stop, menuItem)
            return true
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    companion object {

        const val TAG = "Stop Selector"
        const val STOP_ID = "stopId"
        const val STOP_NAME = "stopName"
    }
}
