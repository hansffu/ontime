package hansffu.ontime.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import hansffu.ontime.R
import hansffu.ontime.model.Stop
import kotlinx.android.synthetic.main.stop_list_header.view.*
import kotlinx.android.synthetic.main.stop_list_item.view.*

class StopViewAdapter(private var headerText: String, private val stops: MutableList<Stop>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var noStopsText: String? = null
    private var itemSelectedListener: ItemSelectedListener? = null

    fun updateStops(stops: List<Stop>) {
        this.stops.clear()
        this.stops.addAll(stops)
        notifyDataSetChanged()
    }

    fun setNoStopsText(noStopsText: String) {
        stops.clear()
        this.noStopsText = noStopsText
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            HeaderViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.stop_list_header, parent, false))
        } else StopViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.stop_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            holder.headerText = headerText
            return
        }

        val lookupStoplistIndex = position - 1
        if (!stops.isEmpty() && holder is StopViewHolder) {
            holder.stopName = stops[lookupStoplistIndex].name
            holder.bind(lookupStoplistIndex, itemSelectedListener)
        } else if (noStopsText != null && holder is StopViewHolder) {
            holder.stopName = noStopsText as String
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TYPE_HEADER
        } else TYPE_ITEM
    }


    fun setListener(itemSelectedListener: ItemSelectedListener) {
        this.itemSelectedListener = itemSelectedListener
    }

    override fun getItemCount(): Int {
        return if (stops.isEmpty())
            2
        else
            stops.size + 1
    }

    interface ItemSelectedListener {
        fun onItemSelected(position: Int)
    }


    companion object {
        private const val TYPE_HEADER = 1
        private const val TYPE_ITEM = 2
    }
}

private class StopViewHolder internal constructor(item: View) : RecyclerView.ViewHolder(item) {

    internal var stopName: String
    set(value) {
        itemView.short_stop_name.text = value
    }
    get() = itemView.short_stop_name.text.toString()

    internal fun bind(position: Int, listener: StopViewAdapter.ItemSelectedListener?) {
        itemView.setOnClickListener {
            listener?.onItemSelected(position)
        }
    }

}

private class HeaderViewHolder internal constructor(item: View) : RecyclerView.ViewHolder(item) {

    internal var headerText: String
        set(value) {
            itemView.stop_list_header.text = value
        }
        get() = itemView.stop_list_header.text.toString()
}