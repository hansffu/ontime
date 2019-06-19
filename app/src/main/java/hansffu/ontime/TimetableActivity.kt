package hansffu.ontime

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.support.wear.widget.WearableLinearLayoutManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.Response.Listener
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import hansffu.ontime.adapter.TimetableAdapter
import hansffu.ontime.extensions.mapToList
import hansffu.ontime.model.Departure
import hansffu.ontime.model.LineDirectionRef
import hansffu.ontime.model.Stop
import hansffu.ontime.service.FavoriteService
import hansffu.ontime.service.mapJsonResponseToDeparture
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_timetable.*
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap
import java.util.*

class TimetableActivity : Activity() {

    private lateinit var timetableAdapter: TimetableAdapter
    private var stopId: Long = 0
    private lateinit var stopName: String
    private lateinit var favoriteService: FavoriteService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timetable)

        stopId = intent.getLongExtra(STOP_ID, 0)
        stopName = intent.getStringExtra(STOP_NAME)
        favoriteService = FavoriteService(this)

        timetableAdapter = TimetableAdapter(this, stopName)
        departure_list.adapter = timetableAdapter

        departure_list.apply {
            adapter = timetableAdapter
            layoutManager = WearableLinearLayoutManager(this@TimetableActivity)
            isEdgeItemsCenteringEnabled = true
        }

        bottom_action_drawer.setOnMenuItemClickListener { onMenuItemClick(it) }
        bottom_action_drawer.controller.peekDrawer()

        val toggleFavoriteMenuItem = bottom_action_drawer.menu.findItem(R.id.toggle_favorite)
        val favorite = favoriteService.isFavorite(Stop(stopName, stopId))
        toggleFavorite(favorite, toggleFavoriteMenuItem)

    }


    public override fun onResume() {
        super.onResume()
        setListContent()
    }

    private fun setListContent() {

        timetableAdapter.departures = ArrayList()
        progress_bar.visibility = View.VISIBLE
        updateTimetibles(timetableAdapter)
        departure_list.requestFocus()

    }

    private fun updateTimetibles(adapter: TimetableAdapter) {
        val url = "https://reisapi.ruter.no/StopVisit/GetDepartures/" + stopId

        val requestQueue = Volley.newRequestQueue(this)
        val request = JsonArrayRequest(url, Listener { response ->
            val departures = ArrayListValuedHashMap<LineDirectionRef, Departure>()
            response.mapToList { mapJsonResponseToDeparture(TAG, it) }
                    .forEach { departures.put(it.lineDirectionRef, it) }

            adapter.departures = multimapToLSortedListOfListsOfDepartures(departures)
            progress_bar.visibility = View.GONE
            departure_list.requestFocus()
        },
                Response.ErrorListener { error ->
                    Log.e(TAG, "Error getting timetables", error)
                    Toast.makeText(this@TimetableActivity, "Fant ikke holdeplass", Toast.LENGTH_LONG).show()
                })

        request.retryPolicy = DefaultRetryPolicy(20 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        requestQueue.add(request)
    }


    private fun multimapToLSortedListOfListsOfDepartures(multimap: ArrayListValuedHashMap<LineDirectionRef, Departure>) =
            multimap.keySet()
                    .map { multimap.get(it) }
                    .sortedBy { it[0].time }


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

    companion object {

        const val TAG = "Stop Selector"
        const val STOP_ID = "stopId"
        const val STOP_NAME = "stopName"
    }

}
