package hansffu.ontime.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import androidx.recyclerview.widget.RecyclerView
import hansffu.ontime.R
import hansffu.ontime.databinding.StopListHeaderBinding
import hansffu.ontime.databinding.StopListItemBinding
import hansffu.ontime.model.Stop
import hansffu.ontime.model.StopListModel
import hansffu.ontime.model.StopListType

class StopViewAdapter :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var noStopsText: String? = null
    private var itemSelectedListener: ItemSelectedListener? = null
    var stops: List<Stop> = emptyList()
        private set
    private var headerText: StopListType? = null

    fun updateStops(stops: List<Stop>) {
        this.stops = stops
        this.headerText = StopListType.FAVORITES
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (ItemType.values()[viewType]) {
            ItemType.TYPE_HEADER -> HeaderViewHolder.create(parent)
            ItemType.TYPE_ITEM -> StopViewHolder.create(parent)
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            holder.setHeaderText(headerText)
            return
        }

        val lookupStoplistIndex = position - 1
        if (stops.isNotEmpty() && holder is StopViewHolder) {
            holder.stopName = stops[lookupStoplistIndex].name
            holder.bind(lookupStoplistIndex, itemSelectedListener)
        } else if (noStopsText != null && holder is StopViewHolder) {
            holder.stopName = noStopsText as String
        }
    }

    override fun getItemViewType(position: Int): Int {
        return (if (position == 0) {
            ItemType.TYPE_HEADER
        } else ItemType.TYPE_ITEM).ordinal
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

}

enum class ItemType {
    TYPE_HEADER,
    TYPE_ITEM
}

private fun inflate(parent: ViewGroup, layout: Int) =
    LayoutInflater.from(parent.context).inflate(layout, parent, false)

private class StopViewHolder(item: View) : RecyclerView.ViewHolder(item) {
    companion object {
        fun create(parent: ViewGroup) = StopViewHolder(inflate(parent, R.layout.stop_list_item))
    }

    private val binding = StopListItemBinding.bind(item)
    var stopName: String
        set(value) {
            binding.shortStopName.text = value
        }
        get() = binding.shortStopName.text.toString()

    fun bind(position: Int, listener: StopViewAdapter.ItemSelectedListener?) {
        itemView.setOnClickListener {
            listener?.onItemSelected(position)
        }
    }
}

private class HeaderViewHolder(item: View) : RecyclerView.ViewHolder(item) {

    companion object {
        fun create(parent: ViewGroup) = HeaderViewHolder(inflate(parent, R.layout.stop_list_header))
    }

    private val binding: StopListHeaderBinding = StopListHeaderBinding.bind(item)

    fun setHeaderText(stopType: StopListType?) {
        binding.stopListHeader.setText(
            when (stopType) {
                StopListType.NEARBY -> R.string.nearby_header
                StopListType.FAVORITES -> R.string.favorites_header
                null -> R.string.empty_string
            }
        )
    }
}