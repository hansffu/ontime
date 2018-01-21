package hansffu.ontime

import android.Manifest
import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.wearable.Wearable
import hansffu.ontime.adapter.StopViewAdapter
import hansffu.ontime.conversion.Deg2UTM
import hansffu.ontime.model.Stop
import hansffu.ontime.service.StopService
import kotlinx.android.synthetic.main.stop_list.*

class NearbyFragment : Fragment(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<Status>, StopService.StopServiceCallbackHandler {
    private val mGoogleApiClient: GoogleApiClient by lazy {
        GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addApi(Wearable.API)  // used for data layer API
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
    }
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


    override fun onConnected(bundle: Bundle?) {
        Log.d(TAG, "Connected")
        requestLocation()
    }

    private fun requestLocation() {
        val locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_LOW_POWER)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_INTERVAL_MS)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_FINE_LOCATION)
            return
        }
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, locationRequest, this)
                .setResultCallback(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestLocation()

                } else {
                    Toast.makeText(context, "Ikke tilgang til plassering", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        mGoogleApiClient.connect()
    }

    override fun onPause() {
        super.onPause()
        if (mGoogleApiClient.isConnected) {
            LocationServices.FusedLocationApi
                    .removeLocationUpdates(mGoogleApiClient, this)
        }
        mGoogleApiClient.disconnect()
    }

    override fun onConnectionSuspended(i: Int) {
        Log.d(TAG, "connection to location client suspended")
    }


    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.e(TAG, "Location connection failed: " + connectionResult.errorMessage)
    }

    override fun onLocationChanged(location: Location) {
        Log.d(TAG, "location changed")
        val utmLocation = Deg2UTM(location.latitude, location.longitude)
        Log.d(TAG, "X = " + utmLocation.easting)
        Log.d(TAG, "Y = " + utmLocation.northing)
        Log.d(TAG, "L = " + utmLocation.letter)
        Log.d(TAG, "Z = " + utmLocation.zone)
        Log.d(TAG, "lat = " + location.latitude)
        Log.d(TAG, "lon = " + location.longitude)

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
            stopAdapter.setNoStopsText(context.getString(R.string.no_stops_found))
        }

    }

    companion object {

        private val UPDATE_INTERVAL_MS: Long = 60000
        private val FASTEST_INTERVAL_MS: Long = 60000
        private val PERMISSIONS_REQUEST_FINE_LOCATION = 1
        private val TAG = "NearybyFragment"
    }
}