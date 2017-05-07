package hansffu.ontime.service;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import hansffu.ontime.model.Stop;

/**
 * Created by hansffu on 04.02.17.
 */

public class StopService implements Response.Listener<JSONArray>, Response.ErrorListener {

    private static final String TAG = "StopService";

    private StopServiceCallbackHandler callbackHanlder;

    public StopService(StopServiceCallbackHandler callbackHanlder) {
        this.callbackHanlder = callbackHanlder;
    }


    public void findStopsNear(Double east, Double north) {
        String url = String.format("http://reisapi.ruter.no/Place/GetClosestStops?coordinates=(x=%d,y=%d)", east.intValue(), north.intValue());
        Log.d(TAG, "Closest stops url: " + url);

        RequestQueue requestQueue = Volley.newRequestQueue(callbackHanlder.getContext());
        JsonArrayRequest request = new JsonArrayRequest(url, this, this);

        request.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(request);
    }

    @Override
    public void onResponse(JSONArray response) {
        List<Stop> stops = new ArrayList<>(response.length());

        for (int i = 0; i < response.length(); i++) {
            try {
                stops.add(mapJsonObjectToStop(response.getJSONObject(i)));
            } catch (JSONException e) {
                Log.e(TAG, "Could not unmarshal element at index " + i, e);
            }
        }
        if (callbackHanlder != null) {
            callbackHanlder.stopServiceCallback(stops);
        }
    }

    private Stop mapJsonObjectToStop(JSONObject jsonObject) throws JSONException {
        String name = jsonObject.getString("Name");
        long id = jsonObject.getLong("ID");
        return new Stop(name, id);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.e(TAG, "Find stops error", error);
        if (callbackHanlder != null) {
            Toast.makeText(callbackHanlder.getContext(), "Kunne ikke hente stopp", Toast.LENGTH_SHORT).show();
            callbackHanlder.stopServiceCallback(new ArrayList<Stop>(0));
        }
    }

    public interface StopServiceCallbackHandler {
        Context getContext();

        void stopServiceCallback(List<Stop> stops);
    }

}
