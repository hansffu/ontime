package hansffu.ontime.service

import android.content.Context
import android.util.Log

import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import hansffu.ontime.extensions.mapToList
import hansffu.ontime.extensions.toList

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.util.ArrayList

import hansffu.ontime.model.Stop

class StopService(private val callbackHandler: StopServiceCallbackHandler) : Response.Listener<JSONArray>, Response.ErrorListener {


    fun findStopsNear(east: Double?, north: Double?) {
        val url = String.format("https://reisapi.ruter.no/Place/GetClosestStops?coordinates=(x=%d,y=%d)", east!!.toInt(), north!!.toInt())
        Log.d(TAG, "Closest stops url: " + url)

        val requestQueue = Volley.newRequestQueue(callbackHandler.getContext())
        val request = JsonArrayRequest(url, this, this)

        request.retryPolicy = DefaultRetryPolicy(20 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        requestQueue.add(request)
    }

    override fun onResponse(response: JSONArray) {
        val stops = response.mapToList { mapJsonObjectToStop(it) }
        callbackHandler.stopServiceCallback(stops)
    }

    @Throws(JSONException::class)
    private fun mapJsonObjectToStop(jsonObject: JSONObject): Stop {
        val name = jsonObject.getString("Name")
        val id = jsonObject.getLong("ID")
        return Stop(name, id)
    }

    override fun onErrorResponse(error: VolleyError) {
        Log.e(TAG, "Find stops error", error)
        callbackHandler.stopServiceCallback(ArrayList(0))
    }

    interface StopServiceCallbackHandler {
        fun getContext(): Context?
        fun stopServiceCallback(stops: List<Stop>)
    }

    companion object {

        private val TAG = "StopService"
    }

}

