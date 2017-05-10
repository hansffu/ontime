package hansffu.ontime;

import android.Manifest;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.wearable.view.WearableRecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;

import hansffu.ontime.adapter.StopViewAdapter;
import hansffu.ontime.conversion.Deg2UTM;
import hansffu.ontime.model.Stop;
import hansffu.ontime.service.StopService;

import static hansffu.ontime.TimetableActivity.STOP_ID;
import static hansffu.ontime.TimetableActivity.STOP_NAME;


public class NearbyFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ResultCallback<Status>, StopService.StopServiceCallbackHandler {

    private static final long UPDATE_INTERVAL_MS = 60000;
    private static final long FASTEST_INTERVAL_MS = 60000;
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    private final static String TAG = "NearybyFragment";
    private WearableRecyclerView mStopListView;
    private GoogleApiClient mGoogleApiClient;
    private StopViewAdapter stopAdapter;
    private StopService stopService;
    private List<Stop> stops;
    private ProgressBar mProgressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.stop_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mStopListView = (WearableRecyclerView) getView().findViewById(R.id.stop_list);
        mProgressBar = (ProgressBar) getView().findViewById(R.id.stop_list_progress_bar);

        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(LocationServices.API)
                .addApi(Wearable.API)  // used for data layer API
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        stopService = new StopService(this);

        setListContent();
    }

    private void setListContent() {
        stops = new ArrayList<>(0);

        stopAdapter = new StopViewAdapter(stops);
        stopAdapter.setListener(new StopViewAdapter.ItemSelectedListener() {
            @Override
            public void onItemSelected(int position) {
                Intent startTimetableActivity = new Intent(NearbyFragment.this.getActivity(), TimetableActivity.class);
                startTimetableActivity.putExtra(STOP_ID, stops.get(position).getId());
                startTimetableActivity.putExtra(STOP_NAME, stops.get(position).getName());
                NearbyFragment.this.startActivity(startTimetableActivity);
            }
        });

        mStopListView.setAdapter(stopAdapter);
        mProgressBar.setVisibility(View.VISIBLE);
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Connected");
        requestLocation();
    }

    private void requestLocation() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_LOW_POWER)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_INTERVAL_MS);

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_FINE_LOCATION);
            return;
        }
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, locationRequest, this)
                .setResultCallback(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestLocation();

                } else {
                    Toast.makeText(getContext(), "Ikke tilgang til plassering", Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }

        }

    }

    @Override
    public void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi
                    .removeLocationUpdates(mGoogleApiClient, this);
        }
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "connection to location client suspended");
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Location connection failed: " + connectionResult.getErrorMessage());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "location changed");
        Deg2UTM utmLocation = new Deg2UTM(location.getLatitude(), location.getLongitude());
        Log.d(TAG, "X = " + utmLocation.getEasting());
        Log.d(TAG, "Y = " + utmLocation.getNorthing());
        Log.d(TAG, "L = " + utmLocation.getLetter());
        Log.d(TAG, "Z = " + utmLocation.getZone());
        Log.d(TAG, "lat = " + location.getLatitude());
        Log.d(TAG, "lon = " + location.getLongitude());

        stopService.findStopsNear(utmLocation.getEasting(), utmLocation.getNorthing());
    }

    @Override
    public void onResult(@NonNull Status status) {
        if (status.getStatus().isSuccess()) {
            Log.d(TAG, "Successfully requested location updates");
        } else {
            Log.e(TAG,
                    "Failed in requesting location updates, "
                            + "status code: "
                            + status.getStatusCode()
                            + ", message: "
                            + status.getStatusMessage());
        }
    }

    @Override
    public void stopServiceCallback(List<Stop> stops) {
        this.stops = stops;
        mProgressBar.setVisibility(View.GONE);
        stopAdapter.updateStops(stops);
    }
}