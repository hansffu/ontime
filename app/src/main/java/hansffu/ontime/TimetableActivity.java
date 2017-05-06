package hansffu.ontime;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.wearable.view.WearableRecyclerView;
import android.support.wearable.view.drawer.WearableActionDrawer;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import hansffu.ontime.adapter.TimetableAdapter;
import hansffu.ontime.model.Departure;
import hansffu.ontime.model.LineDirectionRef;
import hansffu.ontime.model.Stop;
import hansffu.ontime.service.FavoriteService;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class TimetableActivity extends Activity implements WearableActionDrawer.OnMenuItemClickListener {

    public static final String TAG = "Stop Selector";
    public static final String STOP_ID = "stopId";
    public static final String STOP_NAME = "stopName";


    private WearableRecyclerView mStopListView;
    private ProgressBar mProgressBar;

    private TimetableAdapter adapter;

    private long stopId;
    private String stopName;
    private WearableActionDrawer mActionMenu;
    private FavoriteService favoriteService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);
        Intent intent = getIntent();
        stopId = intent.getLongExtra(STOP_ID, 0);
        stopName = intent.getStringExtra(STOP_NAME);
        favoriteService = new FavoriteService(this);

        mStopListView = (WearableRecyclerView) findViewById(R.id.departure_list);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mActionMenu = (WearableActionDrawer) findViewById(R.id.bottom_action_drawer);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mStopListView.setLayoutManager(layoutManager);

        mStopListView.setHasFixedSize(true);

        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(mStopListView.getContext(),
                layoutManager.getOrientation());
        mStopListView.addItemDecoration(mDividerItemDecoration);

        adapter = new TimetableAdapter(stopName, new ArrayList<>());
        mStopListView.setAdapter(adapter);

        mActionMenu.setOnMenuItemClickListener(this);

        MenuItem toggleFavoriteMenuItem = mActionMenu.getMenu().findItem(R.id.toggle_favorite);
        boolean favorite = favoriteService.isFavorite(new Stop(stopName, stopId));
        toggleFavorite(favorite, toggleFavoriteMenuItem);

    }


    @Override
    public void onResume() {
        super.onResume();
        setListContent();
    }

    private void setListContent() {

        adapter.setDepartures(new ArrayList<>());
        mProgressBar.setVisibility(View.VISIBLE);
        updateTimetibles(adapter);

    }

    private void updateTimetibles(final TimetableAdapter adapter) {
        String url = "http://reisapi.ruter.no/StopVisit/GetDepartures/" + stopId;

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest request = new JsonArrayRequest(url, response -> {

            ArrayListValuedHashMap<LineDirectionRef, Departure> departures = new ArrayListValuedHashMap<>();

            for (int i = 0; i < response.length(); i++) {
                try {
                    Departure departure = mapJsonResponseToDeparture(response.getJSONObject(i));
                    departures.put(departure.getLineDirectionRef(), departure);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            adapter.setDepartures(multimapToLSortedistOfListsOfDepartures(departures));
            mProgressBar.setVisibility(View.GONE);
        },
                error -> {
                    Log.e(TAG, "Error getting timetables", error);
                    Toast.makeText(TimetableActivity.this, "Fant ikke holdeplass", Toast.LENGTH_LONG).show();
                });

        request.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(request);
        requestQueue.start();
    }


    private List<List<Departure>> multimapToLSortedistOfListsOfDepartures(ArrayListValuedHashMap<LineDirectionRef, Departure> multimap) {
        List<List<Departure>> list = new ArrayList<>(multimap.keySet().size());
        for (LineDirectionRef lineDirectionRef : multimap.keySet()) {
            list.add(multimap.get(lineDirectionRef));
        }
        Collections.sort(list, (o1, o2) -> o1.get(0).getTime().compareTo(o2.get(0).getTime()));
        return list;
    }

    private Departure mapJsonResponseToDeparture(JSONObject departureJSON) throws JSONException {
        String lineRef, direction, lineNumber, name, rawTime;

        lineRef = departureJSON.getJSONObject("MonitoredVehicleJourney").getString("LineRef");
        direction = departureJSON.getJSONObject("MonitoredVehicleJourney").getString("DirectionRef");
        lineNumber = departureJSON.getJSONObject("MonitoredVehicleJourney").getString("PublishedLineName");
        name = departureJSON.getJSONObject("MonitoredVehicleJourney").getString("DestinationName");
        rawTime = departureJSON.getJSONObject("MonitoredVehicleJourney").
                getJSONObject("MonitoredCall").getString("ExpectedDepartureTime");

        //2017-02-07T00:18:03.7026463+01:00
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date departureTime;
        try {
            departureTime = sdf.parse(rawTime);
        } catch (ParseException e) {
            Log.e(TAG, "parse error: " + rawTime, e);
            departureTime = new Date();
        }

        return new Departure(lineRef, direction, lineNumber, name, departureTime);
    }

    private void toggleFavorite(boolean isFavorite, MenuItem menuItem) {
        menuItem.setIcon(isFavorite ? R.drawable.ic_favorite_white_48dp : R.drawable.ic_favorite_border_white_48dp);
        menuItem.setTitle(isFavorite ? R.string.add_favorite : R.string.remove_favorite);
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.toggle_favorite:
                Observable.just(new Stop(stopName, stopId))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .map(favoriteService::toggleFavorite)
                        .subscribe(isFavorite -> toggleFavorite(isFavorite, menuItem));
                return true;
            default:
                return false;
        }
    }

}
