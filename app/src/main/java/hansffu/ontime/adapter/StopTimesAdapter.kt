package hansffu.ontime.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import hansffu.ontime.R

class StopTimesAdapter(private val listener: View.OnClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val timeList: MutableList<String> = ArrayList()
    var isExpanded = false
        set (value){
            field = value
            notifyDataSetChanged()
        }

    fun update(times: List<String>) {
        timeList.clear()
        timeList.addAll(times)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            TextViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.departs_in_list_item, parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as TextViewHolder).update(timeList[position])
    }

    override fun getItemCount(): Int = if (!isExpanded && timeList.size >= 2) 2 else timeList.size

    private inner class TextViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        private val textView: TextView = view.findViewById(R.id.departs_in_item)

        init {
            view.setOnClickListener(listener)
        }

        internal fun update(time: String) {
            textView.text = time
        }

    }
}
