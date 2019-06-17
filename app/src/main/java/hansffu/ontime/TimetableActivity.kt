package hansffu.ontime

import android.app.Activity
import android.os.Bundle
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


    private val adapter: TimetableAdapter by lazy { TimetableAdapter(this, stopName, ArrayList()) }
    private val stopId: Long by lazy { intent.getLongExtra(STOP_ID, 0) }
    private val stopName: String by lazy { intent.getStringExtra(STOP_NAME) }
    private val favoriteService: FavoriteService by lazy { FavoriteService(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timetable)

//        val layoutManager = LinearLayoutManager(this)
//        departure_list.layoutManager = layoutManager

        departure_list.setHasFixedSize(true)

//        val mDividerItemDecoration = DividerItemDecoration(departure_list.context,
//                layoutManager.orientation)
//        departure_list.addItemDecoration(mDividerItemDecoration)

        departure_list.adapter = adapter

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

        adapter.setDepartures(ArrayList())
        progress_bar.visibility = View.VISIBLE
        updateTimetibles(adapter)

    }

    private fun updateTimetibles(adapter: TimetableAdapter) {
        val url = "https://reisapi.ruter.no/StopVisit/GetDepartures/" + stopId

        val requestQueue = Volley.newRequestQueue(this)
        val request = JsonArrayRequest(url, Listener { response ->
            val departures = ArrayListValuedHashMap<LineDirectionRef, Departure>()
            response.mapToList { mapJsonResponseToDeparture(TAG, it) }
                    .forEach { departures.put(it.lineDirectionRef, it) }

            adapter.setDepartures(multimapToLSortedistOfListsOfDepartures(departures))
            progress_bar.visibility = View.GONE
        },
                Response.ErrorListener { error ->
                    Log.e(TAG, "Error getting timetables", error)
                    Toast.makeText(this@TimetableActivity, "Fant ikke holdeplass", Toast.LENGTH_LONG).show()
                })

        request.retryPolicy = DefaultRetryPolicy(20 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        requestQueue.add(request)
    }


    private fun multimapToLSortedistOfListsOfDepartures(multimap: ArrayListValuedHashMap<LineDirectionRef, Departure>): List<List<Departure>> {
        val list = ArrayList<List<Departure>>(multimap.keySet().size)
        for (lineDirectionRef in multimap.keySet()) {
            list.add(multimap.get(lineDirectionRef))
        }
        Collections.sort(list) { o1, o2 -> o1[0].time.compareTo(o2[0].time) }
        return list
    }


    private fun toggleFavorite(isFavorite: Boolean, menuItem: MenuItem) {
        menuItem.setIcon(if (isFavorite) R.drawable.ic_favorite_white_48dp else R.drawable.ic_favorite_border_white_48dp)
        menuItem.setTitle(if (isFavorite) R.string.add_favorite else R.string.remove_favorite)
    }

    private fun onMenuItemClick(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.toggle_favorite) {
            Observable.just(Stop(stopName, stopId))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .map { stop -> favoriteService.toggleFavorite(stop) }
                    .subscribe({ toggleFavorite(it, menuItem) })
            return true
        }
        return false
    }

    companion object {

        val TAG = "Stop Selector"
        @JvmField
        val STOP_ID = "stopId"
        @JvmField
        val STOP_NAME = "stopName"
    }

}
