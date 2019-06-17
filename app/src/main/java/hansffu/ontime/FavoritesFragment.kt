package hansffu.ontime

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import hansffu.ontime.TimetableActivity.Companion.STOP_ID
import hansffu.ontime.TimetableActivity.Companion.STOP_NAME
import hansffu.ontime.adapter.StopViewAdapter
import hansffu.ontime.service.FavoriteService
import kotlinx.android.synthetic.main.stop_list.*


class FavoritesFragment : Fragment() {

    private val favoriteService: FavoriteService by lazy { FavoriteService(this.context!!) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.stop_list, container, false)
    }

    override fun onResume() {
        super.onResume()
        setListContent()
    }

    private fun setListContent() {
        val stops = favoriteService.getFavorites().toMutableList()

        val adapter = StopViewAdapter(context!!.getString(R.string.favorites_header), stops)
        adapter.setListener(object : StopViewAdapter.ItemSelectedListener {
            override fun onItemSelected(position: Int) {
                val startTimetableActivity = Intent(activity, TimetableActivity::class.java)
                startTimetableActivity.putExtra(STOP_ID, stops[position].id)
                startTimetableActivity.putExtra(STOP_NAME, stops[position].name)
                startActivity(startTimetableActivity)
            }
        })

        if (stops.isEmpty()) {
            adapter.setNoStopsText(context!!.getString(R.string.favorites_tips))
        }
        stop_list.adapter = adapter
    }


}