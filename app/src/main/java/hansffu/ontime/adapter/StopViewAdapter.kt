package hansffu.ontime.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hansffu.ontime.R
import hansffu.ontime.databinding.StopListButtonBinding
import hansffu.ontime.databinding.StopListHeaderBinding
import hansffu.ontime.databinding.StopListStopBinding
import hansffu.ontime.model.Stop

private typealias Listener = (StopViewItem) -> Unit

class StopViewAdapter() : RecyclerView.Adapter<StopViewHolder>() {
    var items: List<StopViewItem> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopViewHolder =
        ItemType.values()[viewType].create(parent)

    override fun onBindViewHolder(holder: StopViewHolder, position: Int) =
        holder.update(items[position])

    override fun getItemViewType(position: Int): Int = items[position].type.ordinal
    override fun getItemCount(): Int = items.size
}


private fun inflate(parent: ViewGroup, layout: Int) =
    LayoutInflater.from(parent.context).inflate(layout, parent, false)


enum class ItemType(val create: (ViewGroup) -> StopViewHolder) {
    HEADER(::createHeader),
    STOP(::createStop),
    BUTTON(::createButton)
}

sealed interface StopViewItem {
    val type: ItemType
}

abstract class StopViewHolder(protected val view: View) : RecyclerView.ViewHolder(view) {
    abstract fun update(item: StopViewItem)
}


data class HeaderItem(val headerText: String) : StopViewItem {
    override val type: ItemType = ItemType.HEADER

}

private fun createHeader(parent: ViewGroup): StopViewHolder =
    object : StopViewHolder(inflate(parent, R.layout.stop_list_header)) {
        private val binding: StopListHeaderBinding = StopListHeaderBinding.bind(view)
        override fun update(item: StopViewItem) {
            if (item is HeaderItem) binding.stopListHeader.text = item.headerText
        }
    }

data class StopItem(val stop: Stop, val listener: Listener) : StopViewItem {
    override val type: ItemType = ItemType.STOP
}

private fun createStop(parent: ViewGroup) =
    object : StopViewHolder(inflate(parent, R.layout.stop_list_stop)) {
        private val binding = StopListStopBinding.bind(view)
        override fun update(item: StopViewItem) {
            if (item is StopItem) {
                binding.shortStopName.text = item.stop.name
                itemView.setOnClickListener { item.listener(item) }
            }
        }
    }

data class ButtonItem(val text: String, val action: () -> Unit) : StopViewItem {
    override val type: ItemType = ItemType.BUTTON
}

fun createButton(parent: ViewGroup): StopViewHolder =
    object : StopViewHolder(inflate(parent, R.layout.stop_list_button)) {
        private val binding = StopListButtonBinding.bind(view)
        override fun update(item: StopViewItem) {
            if (item is ButtonItem) {
                binding.stopListButtonText.text = item.text
                itemView.setOnClickListener { item.action() }
            }
        }
    }

enum class ButtonAction {
    NEARBY
}
