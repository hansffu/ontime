package hansffu.ontime

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import hansffu.ontime.adapter.StopViewAdapter
import hansffu.ontime.model.Stop
import hansffu.ontime.service.findStopsNear
import hansffu.ontime.service.requestLocation
import hansffu.ontime.service.requestLocationPermission
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.stop_list.*

private val TAG = "NearybyFragment"

class NearbyFragment : Fragment() {
    private lateinit var stopAdapter: StopViewAdapter
    private lateinit var stopFetcher: Disposable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.stop_list, container, false)
    }

    fun getStopsForLocation(ctx: Context) = requestLocation(ctx).flatMapObservable { findStopsNear(ctx, it) }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        stopAdapter = StopViewAdapter(getString(R.string.nearby_header))
        stop_list.adapter = stopAdapter

        context?.let { ctx ->
            stopFetcher = requestLocationPermission(ctx)
                    .flatMapObservable { getStopsForLocation(ctx) }
                    .subscribe { updateStops(it) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopFetcher.dispose()
    }

//    override fun onResume() {
//        super.onResume()
//        context?.let { ctx ->
//            stopFetcher = getStopsForLocation(ctx) .subscribe { updateStops(it) }
//        }
//    }

    fun addListener(stops: List<Stop>){
        stopAdapter.setListener(object : StopViewAdapter.ItemSelectedListener {
            override fun onItemSelected(position: Int) {
                val startTimetableActivity = Intent(activity, TimetableActivity::class.java)
                startTimetableActivity.putExtra(TimetableActivity.STOP_ID, stops[position].id)
                startTimetableActivity.putExtra(TimetableActivity.STOP_NAME, stops[position].name)
                startActivity(startTimetableActivity)
            }
        })
    }

    private fun updateStops(stops: List<Stop>) {
        Log.d(TAG, "updateStops: $stops")
        stop_list_progress_bar.visibility = View.GONE
        addListener(stops)
        if (stops.isNotEmpty()) {
            stopAdapter.updateStops(stops)
            stop_list.requestFocus()
        } else {
            context?.let { stopAdapter.setNoStopsText(it.getString(R.string.no_stops_found)) }
        }

    }
}