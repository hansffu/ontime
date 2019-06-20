package hansffu.ontime.adapter

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearLayoutManager.HORIZONTAL
import android.support.v7.widget.LinearLayoutManager.VERTICAL
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import hansffu.ontime.R
import hansffu.ontime.model.Departure
import kotlinx.android.synthetic.main.timetable_list_header.view.*
import kotlinx.android.synthetic.main.timetable_list_item.view.*
import java.text.SimpleDateFormat
import java.util.*


class TimetableAdapter(private val context: Context, private var stopName: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var departures: List<List<Departure>> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            StopNameHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.timetable_list_header, parent, false))
        } else TimeHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.timetable_list_item, parent, false))

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, index: Int) {
        if (holder is StopNameHolder) {
            holder.update(stopName)
        }

        val lookupStopListIndex = index - 1
        if (holder is TimeHolder) {
            holder.update(departures[lookupStopListIndex])
        }
    }

    override fun getItemViewType(position: Int): Int {
//        return TYPE_ITEM
        return if (position == 0) {
            TYPE_HEADER
        } else TYPE_ITEM
    }

    override fun getItemCount(): Int {
        return departures.size + 1
    }




    companion object {

        private const val TYPE_HEADER = 1
        private const val TYPE_ITEM = 2
    }

}

private class StopNameHolder internal constructor(private val item: View) : RecyclerView.ViewHolder(item) {

    internal fun update(stopName: String) {
        item.short_stop_name.text = stopName
    }
}

private class TimeHolder internal constructor(private val item: View) : RecyclerView.ViewHolder(item) {


    private fun toTimeString(departure: Departure): String {
        val timeMins = (departure.time.time - Date().time) / 60000
        return when {
            timeMins <= 0 -> "Nå"
            timeMins >= 20 -> SimpleDateFormat("HH:mm").format(departure.time)
            else -> "$timeMins\u00A0min"
        }
    }

    internal fun update(lineDepartures: List<Departure>) {
        item.line_number.text = lineDepartures[0].lineNumber
        item.destination.text = lineDepartures[0].destination
        val times = lineDepartures.map { toTimeString(it) }.joinToString(separator = "  ") { it }
        item.departs_in_item.text = times

    }
}
