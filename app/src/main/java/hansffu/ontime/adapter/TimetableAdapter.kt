package hansffu.ontime.adapter

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearLayoutManager.HORIZONTAL
import android.support.v7.widget.LinearLayoutManager.VERTICAL
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import hansffu.ontime.R
import hansffu.ontime.model.Departure
import kotlinx.android.synthetic.main.timetable_list_item.view.*
import kotlinx.android.synthetic.main.timetable_list_header.view.*
import java.text.SimpleDateFormat
import java.util.*


class TimetableAdapter(private val context: Context, private var stopName: String, private var departures: List<List<Departure>>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun setDepartures(departures: List<List<Departure>>) {
        this.departures = departures
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            StopNameHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.timetable_list_header, parent, false))
        } else TimeHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.timetable_list_item, parent, false), context)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, index: Int) {
        if (holder is StopNameHolder) {//is header
            holder.update(stopName)
        }

        val lookupStopListIndex = index - 1
        if (holder is TimeHolder) {
            holder.update(departures[lookupStopListIndex])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TYPE_HEADER
        } else TYPE_ITEM
    }

    override fun getItemCount(): Int {
        return departures.size + 1
    }




    companion object {

        private val TYPE_HEADER = 111
        private val TYPE_ITEM = 333
    }

}

private class StopNameHolder internal constructor(private val item: View) : RecyclerView.ViewHolder(item) {

    internal fun update(stopName: String) {
        item.short_stop_name.text = stopName
    }
}

private class TimeHolder internal constructor(private val item: View, private val context: Context) : RecyclerView.ViewHolder(item), View.OnClickListener {

    private val stopTimesAdapter: StopTimesAdapter = StopTimesAdapter(this)
    private val stopTimesLayoutManager: LinearLayoutManager
    internal var isExpanded = false

    init {
        item.departs_in_list.adapter = stopTimesAdapter
        stopTimesLayoutManager = LinearLayoutManager(context, HORIZONTAL, false)
        item.departs_in_list.layoutManager = stopTimesLayoutManager
        itemView.setOnClickListener(this)
    }

    internal fun update(lineDepartures: List<Departure>) {
        item.line_number.text = lineDepartures[0].lineNumber
        item.destination.text = lineDepartures[0].destination
        val times = ArrayList<String>(lineDepartures.size)
        for (departure in lineDepartures) {
            val timeMins = (departure.time.time - Date().time) / 60000
            if (timeMins <= 0) {
                times.add("Nå")
            } else if (timeMins >= 20) {
                times.add(SimpleDateFormat("HH:mm").format(departure.time))
            } else {
                times.add(timeMins.toString() + "\u00A0min")
            }
        }
        stopTimesAdapter.update(times)

    }


    override fun onClick(v: View) {
        toggleExpanded()
    }

    private fun toggleExpanded() {
        isExpanded = !isExpanded
        stopTimesLayoutManager.orientation = if (isExpanded) VERTICAL else HORIZONTAL
        stopTimesAdapter.isExpanded = isExpanded
        setBrighterColor(isExpanded)
        item.destination.maxLines = if (isExpanded) 3 else 1
    }

    internal fun setBrighterColor(brighterColor: Boolean) {
        itemView.setBackgroundColor(context.getColor(if (brighterColor) R.color.light_background else R.color.dark_background))
    }

}
