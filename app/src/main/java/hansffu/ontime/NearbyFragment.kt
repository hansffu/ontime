package hansffu.ontime

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.patloew.rxlocation.RxLocation
import com.tbruyelle.rxpermissions2.RxPermissions
import hansffu.ontime.adapter.StopViewAdapter
import hansffu.ontime.api.Properties
import hansffu.ontime.model.Stop
import hansffu.ontime.service.StopService
import hansffu.ontime.service.requestLocation
import hansffu.ontime.service.requestLocationPermission
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.stop_list.*

private val TAG = "NearybyFragment"

class NearbyFragment : Fragment() {
    private val stopService = StopService()
    private lateinit var stopAdapter: StopViewAdapter
    private lateinit var stopFetcher: Disposable
    private lateinit var rxLocation: RxLocation
    private lateinit var rxPermissions: RxPermissions

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.stop_list, container, false)
    }

    private fun getStopsForLocation() = requestLocation(rxLocation).flatMapSingleElement { stopService.findStopsNear(it) }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        stopAdapter = StopViewAdapter(getString(R.string.nearby_header))
        stop_list.adapter = stopAdapter


        context?.let { ctx ->
            rxLocation = RxLocation(ctx)
            rxPermissions = RxPermissions(this)
            stopFetcher = requestLocationPermission(rxPermissions)
                    .filter { it }
                    .flatMapMaybe { getStopsForLocation() }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { updateStops(it) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopFetcher.dispose()
    }

    override fun onResume() {
        super.onResume()
        if (stopFetcher.isDisposed) {
            stopFetcher = getStopsForLocation()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { stops -> updateStops(stops) }
        }
    }

    private fun addListener(stops: List<Stop>) {
        stopAdapter.setListener(object : StopViewAdapter.ItemSelectedListener {
            override fun onItemSelected(position: Int) {
                val startTimetableActivity = Intent(activity, TimetableActivity::class.java)
                startTimetableActivity.putExtra(TimetableActivity.STOP_ID, stops[position].id)
                startTimetableActivity.putExtra(TimetableActivity.STOP_NAME, stops[position].name)
                startActivity(startTimetableActivity)
            }
        })
    }

    fun toCategoryText(stop: Properties): String {
        val categories = stop.category.map {
            when (it) {
                "onstreetTram" -> "Trikk"
                "onstreetBus" -> "Buss"
                "metroStation" -> "T-bane"
                else -> ""
            }
        }.joinToString (separator = ", ", prefix = " [", postfix = "]")
        Log.d("categories", categories)
        return categories
    }

    private fun updateStops(properties: List<Properties>) {
        Log.d(TAG, "updateStops: $properties")
        stop_list_progress_bar.visibility = View.GONE
        val stops = properties.map { Stop(it.name + toCategoryText(it), it.id) }
        addListener(stops)
        if (stops.isNotEmpty()) {
            stopAdapter.updateStops(stops)
            stop_list.requestFocus()
        } else {
            context?.let { stopAdapter.setNoStopsText(it.getString(R.string.no_stops_found)) }
        }

    }
}