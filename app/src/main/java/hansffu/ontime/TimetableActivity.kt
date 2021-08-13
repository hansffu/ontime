package hansffu.ontime

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.widget.CurvingLayoutCallback
import androidx.wear.widget.WearableLinearLayoutManager
import hansffu.ontime.adapter.TimetableAdapter
import hansffu.ontime.model.LineDeparture
import hansffu.ontime.model.LineDirectionRef
import hansffu.ontime.model.Stop
import hansffu.ontime.service.FavoriteService
import hansffu.ontime.service.StopService
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_timetable.*
import io.reactivex.disposables.CompositeDisposable


class TimetableActivity : Activity() {

    private lateinit var timetableAdapter: TimetableAdapter
    private lateinit var stopId: String
    private lateinit var stopName: String
    private lateinit var favoriteService: FavoriteService
    private val stopService = StopService()
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timetable)

        stopId = intent.getStringExtra(STOP_ID)!!
        stopName = intent.getStringExtra(STOP_NAME)!!
        favoriteService = FavoriteService(this)

        timetableAdapter = TimetableAdapter(this, stopName)
        departure_list.adapter = timetableAdapter


        val defaultLayoutCallback = CurvingLayoutCallback(this)
        departure_list.apply {
            adapter = timetableAdapter
            layoutManager = WearableLinearLayoutManager(this@TimetableActivity, object : WearableLinearLayoutManager.LayoutCallback() {
                override fun onLayoutFinished(child: View, parent: RecyclerView) {
                    if (child.id != R.id.timetable_header) defaultLayoutCallback.onLayoutFinished(child, parent)
                }
            })
        }

        bottom_action_drawer.setOnMenuItemClickListener { onMenuItemClick(it) }

        val toggleFavoriteMenuItem = bottom_action_drawer.menu.findItem(R.id.toggle_favorite)
        val favorite = favoriteService.isFavorite(Stop(stopName, stopId))
        toggleFavorite(favorite, toggleFavoriteMenuItem)
        if (!favorite) {
            bottom_action_drawer.controller.peekDrawer()
        }

    }


    public override fun onResume() {
        super.onResume()
        setListContent()
    }

    private fun setListContent() {

        progress_bar.visibility = View.VISIBLE
        updateTimetibles(timetableAdapter)
        departure_list.requestFocus()

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
                            .min()
                }
                .toList()
    }

    private fun updateTimetibles(adapter: TimetableAdapter) {
        disposables.add(stopService.getDepartures(stopId)
                .subscribeOn(Schedulers.io())
                .map { it.stopPlace }
                .map(::toLineDepartures)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { estimatedCalls ->
                            adapter.estimatedCall = estimatedCalls
                            progress_bar.visibility = View.GONE
                            departure_list.requestFocus()
                        },
                        { error ->
                            Log.e(TAG, "Error getting timetables", error)
                            Toast.makeText(this@TimetableActivity, "Fant ikke holdeplass", Toast.LENGTH_LONG).show()
                        }))
    }

    private fun toggleFavorite(isFavorite: Boolean, menuItem: MenuItem) {
        menuItem.setIcon(if (isFavorite) R.drawable.ic_favorite_white_48dp else R.drawable.ic_favorite_border_white_48dp)
        menuItem.setTitle(if (isFavorite) R.string.add_favorite else R.string.remove_favorite)
    }

    @SuppressLint("CheckResult")
    private fun onMenuItemClick(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.toggle_favorite) {
            Observable.just(Stop(stopName, stopId))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .map { stop -> favoriteService.toggleFavorite(stop) }
                    .subscribe { toggleFavorite(it, menuItem) }
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
