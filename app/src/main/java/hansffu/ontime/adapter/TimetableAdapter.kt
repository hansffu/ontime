package hansffu.ontime.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hansffu.ontime.R
import hansffu.ontime.databinding.TimetableListHeaderBinding
import hansffu.ontime.databinding.TimetableListItemBinding
import hansffu.ontime.model.LineDeparture
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date

class TimetableAdapter(private val context: Context, private val stopName: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var estimatedCall: List<LineDeparture> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            StopNameHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.timetable_list_header, parent, false)
            )
        } else TimeHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.timetable_list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, index: Int) {
        if (holder is StopNameHolder) {
            holder.update(stopName)
        }

        val lookupStopListIndex = index - 1
        if (holder is TimeHolder) {
            holder.update(estimatedCall[lookupStopListIndex])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TYPE_HEADER
        } else TYPE_ITEM
    }

    override fun getItemCount(): Int {
        return estimatedCall.size + 1
    }

    companion object {

        private const val TYPE_HEADER = 1
        private const val TYPE_ITEM = 2
    }
}

private class StopNameHolder(item: View) : RecyclerView.ViewHolder(item) {
    private val binding = TimetableListHeaderBinding.bind(item)

    fun update(stopName: String) {
        binding.shortStopName.text = stopName
    }
}

private class TimeHolder(item: View) : RecyclerView.ViewHolder(item) {
    private val binding = TimetableListItemBinding.bind(item)

    private fun toTimeString(timeString: String): String {
        val time = timeString.toDate()
        val timeMins = (time.time - Date().time) / 60000
        return when {
            timeMins <= 0 -> "Nå"
            timeMins >= 20 -> SimpleDateFormat("HH:mm").format(time)
            else -> "$timeMins\u00A0min"
        }
    }

    fun update(lineDeparture: LineDeparture) {
        binding.lineNumber.text = lineDeparture.lineDirectionRef.lineRef
        binding.destination.text = lineDeparture.lineDirectionRef.destinationRef
        val times =
            lineDeparture.departures.mapNotNull { it.expectedArrivalTime() }.map(::toTimeString)
                .joinToString("  ")
        binding.departsInItem.text = times
    }
}

@SuppressLint("SimpleDateFormat")
fun String.toDate(): Date = try {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    sdf.parse(this)
} catch (e: ParseException) {
    Log.e("String to time", "parse error: $this", e)
    Date()
}
