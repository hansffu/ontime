package hansffu.ontime

import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import hansffu.ontime.adapter.StopViewAdapter
import hansffu.ontime.conversion.Deg2UTM
import hansffu.ontime.model.Stop
import hansffu.ontime.service.*
import kotlinx.android.synthetic.main.stop_list.*

class NearbyFragment : Fragment(), ResultCallback<Status>, StopService.StopServiceCallbackHandler {
    private val stopService: StopService by lazy { StopService(this) }
    private val stops: MutableList<Stop> = ArrayList(0)
    private val stopAdapter: StopViewAdapter by lazy { StopViewAdapter(getString(R.string.nearby_header), stops) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.stop_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setListContent()
//        activity?.let { requestLocation(it) }

        val location = Location("flp")
        location.longitude = 10.796757
        location.latitude = 59.932715
        onLocationChanged(location)
    }

    private fun setListContent() {
        stopAdapter.setListener(object : StopViewAdapter.ItemSelectedListener {
            override fun onItemSelected(position: Int) {
                val startTimetableActivity = Intent(activity, TimetableActivity::class.java)
                startTimetableActivity.putExtra(TimetableActivity.STOP_ID, stops[position].id)
                startTimetableActivity.putExtra(TimetableActivity.STOP_NAME, stops[position].name)
                startActivity(startTimetableActivity)
            }
        })

        stop_list.adapter = stopAdapter
        stop_list_progress_bar.visibility = View.VISIBLE
    }

    private fun requestLocation(activity: Activity) {
        when (checkLocationPermission(activity)) {
            false -> requestLocationPermission(activity)
            true -> requestWatchLocation(activity).addOnSuccessListener(this::onLocationChanged)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_FINE_LOCATION -> activity?.let { handlePermissionResult(it, grantResults) }
        }

    }


    private fun onLocationChanged(location: Location) {
        val utmLocation = Deg2UTM(location.latitude, location.longitude)
        stopService.findStopsNear(utmLocation.easting, utmLocation.northing)
    }

    override fun onResult(status: Status) {
        if (status.status.isSuccess) {
            Log.d(TAG, "Successfully requested location updates")
        } else {
            Log.e(TAG,
                    "Failed in requesting location updates, "
                            + "status code: "
                            + status.statusCode
                            + ", message: "
                            + status.statusMessage)
        }
    }

    override fun stopServiceCallback(stops: List<Stop>) {
        this.stops.clear()
        this.stops.addAll(stops)
        stop_list_progress_bar.visibility = View.GONE
        if (!stops.isEmpty()) {
            stopAdapter.updateStops(stops)
        } else {
            context?.let { stopAdapter.setNoStopsText(it.getString(R.string.no_stops_found)) }
        }

    }

    companion object {

        private val PERMISSIONS_REQUEST_FINE_LOCATION = 1
        private val TAG = "NearybyFragment"
    }
}