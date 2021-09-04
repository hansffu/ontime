package hansffu.ontime.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hansffu.ontime.R
import hansffu.ontime.databinding.StopListHeaderBinding
import hansffu.ontime.databinding.StopListItemBinding
import hansffu.ontime.model.Stop
import hansffu.ontime.model.StopListType

class StopViewAdapter :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var noStopsText: String? = null
    private var itemSelectedListener: ItemSelectedListener? = null
    var stops: List<Stop> = emptyList()
        set(value){
            field = value
            notifyDataSetChanged()
        }
    var headerText: String = ""
        set(value) {
            field = value
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
            holder.update(stops[lookupStoplistIndex], itemSelectedListener)
        } else if (noStopsText != null && holder is StopViewHolder) {
            holder.update(null, null)
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
        fun onItemSelected(stop: Stop)
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

    fun update(stop: Stop?, listener: StopViewAdapter.ItemSelectedListener?) {
        binding.shortStopName.text = stop?.name ?: "No stops"
        itemView.setOnClickListener {
            if (listener != null && stop != null) listener.onItemSelected(stop)
        }
    }
}

private class HeaderViewHolder(item: View) : RecyclerView.ViewHolder(item) {

    companion object {
        fun create(parent: ViewGroup) = HeaderViewHolder(inflate(parent, R.layout.stop_list_header))
    }

    private val binding: StopListHeaderBinding = StopListHeaderBinding.bind(item)

    fun setHeaderText(text: String) {
        binding.stopListHeader.text = text
    }
}