package hansffu.ontime.service

import android.annotation.SuppressLint
import android.util.Log
import hansffu.ontime.model.Departure
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
@Throws(JSONException::class)
fun mapJsonResponseToDeparture(TAG: String, departureJSON: JSONObject): Departure {
    val mvj = departureJSON.getJSONObject("MonitoredVehicleJourney")
    val lineRef: String = mvj.getString("LineRef")
    val direction: String = mvj.getString("DirectionRef")
    val lineNumber: String = mvj.getString("PublishedLineName")
    val destName: String = mvj.getString("DestinationName")
    val destRef: String = mvj.getString("DestinationRef")
    val rawTime: String = mvj.getJSONObject("MonitoredCall").getString("ExpectedDepartureTime")

    //2017-02-07T00:18:03.7026463+01:00
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    val departureTime: Date = try {
        sdf.parse(rawTime)
    } catch (e: ParseException) {
        Log.e(TAG, "parse error: $rawTime", e)
        Date()
    }

    return Departure(lineRef, direction, lineNumber, destName, destRef, departureTime)
}
