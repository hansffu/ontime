package hansffu.ontime

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import arrow.core.Either
import arrow.fx.IO
import arrow.fx.typeclasses.Disposable
import hansffu.ontime.adapter.StopViewAdapter
import hansffu.ontime.api.Properties
import hansffu.ontime.model.Stop
import hansffu.ontime.service.LocationService
import hansffu.ontime.service.StopService
import kotlinx.android.synthetic.main.stop_list.*

private val TAG = "NearybyFragment"

class NearbyFragment : Fragment() {
    private val stopService = StopService()
    private lateinit var stopAdapter: StopViewAdapter
    var disposable: Disposable? = null

    private val updateStops: IO<Unit> by lazy {
        LocationService(this).getLocation()
                .flatMap { stopService.findStopsNear(it) }
                .map { updateStops(it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.stop_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        stopAdapter = StopViewAdapter(getString(R.string.nearby_header))
        stop_list.adapter = stopAdapter
        stop_list.layoutManager = LinearLayoutManager(context)

        disposable = updateStops.unsafeRunAsyncCancellable {
            if (it is Either.Left) {
                Log.e(TAG, "Error when updating stops", it.a)
                Toast.makeText(context, R.string.nearby_error, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposable?.invoke()
    }

    override fun onResume() {
        super.onResume()
        disposable = updateStops.unsafeRunAsyncCancellable {
            if (it is Either.Left) {
                Log.e(TAG, "Error when updating stops", it.a)
                Toast.makeText(context, R.string.nearby_error, Toast.LENGTH_LONG).show()
            }
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
        }.joinToString(separator = ", ", prefix = " [", postfix = "]")
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