package hansffu.ontime.service

import android.content.Context
import android.location.Location
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import hansffu.ontime.conversion.Deg2UTM
import hansffu.ontime.extensions.mapToList
import hansffu.ontime.model.Stop
import io.reactivex.Observable
import org.json.JSONArray
import org.json.JSONObject

private val TAG = "StopService"

fun findStopsNear(context: Context, location: Location): Observable<List<Stop>> {
    val deg2UTM = Deg2UTM(location.latitude, location.longitude)
    val url = String.format("https://reisapi.ruter.no/Place/GetClosestStops?coordinates=(x=%d,y=%d)", deg2UTM.easting.toInt(), deg2UTM.northing.toInt())
//        val url = String.format("https://reisapi.ruter.no/Place/GetClosestStops?coordinates=(x=%d,y=%d)", location.latitude, location.longitude)
    Log.d(TAG, "Closest stops url: " + url)

    return makeRequest(context, url).map { it.mapToList(::mapJsonObjectToStop) }
}

private fun makeRequest(context: Context, url: String): Observable<JSONArray> {
    return Observable.create<JSONArray> {
        val request = JsonArrayRequest(url, it::onNext, it::onError)

        val requestQueue = Volley.newRequestQueue(context)
        request.retryPolicy = DefaultRetryPolicy(20 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        requestQueue.add(request)
    }
}

private fun mapJsonObjectToStop(jsonObject: JSONObject): Stop {
    val name = jsonObject.getString("Name")
    val id = jsonObject.getLong("ID")
    return Stop(name, id)
}
