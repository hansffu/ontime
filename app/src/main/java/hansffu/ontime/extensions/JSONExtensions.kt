package hansffu.ontime.extensions

import org.json.JSONArray
import org.json.JSONObject

fun JSONArray.toList(): List<JSONObject> = (0 until this.length()).map { getJSONObject(it) }

fun<R> JSONArray.mapToList(transform: (JSONObject) -> R): List<R> = this.toList().map(transform)