package hansffu.ontime

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import hansffu.ontime.adapter.TimetableAdapter
import hansffu.ontime.databinding.ActivityTimetableBinding
import hansffu.ontime.model.Stop
import hansffu.ontime.service.FavoriteService
import hansffu.ontime.service.StopService
import io.reactivex.disposables.CompositeDisposable

class TimetableActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimetableBinding
    private lateinit var timetableAdapter: TimetableAdapter
    private lateinit var stopId: String
    private lateinit var stopName: String
    private lateinit var favoriteService: FavoriteService
    private val timetableModel: TimetableViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimetableBinding.inflate(layoutInflater)
        setContentView(binding.root)

        stopId = intent.getStringExtra(STOP_ID)!!
        stopName = intent.getStringExtra(STOP_NAME)!!
        favoriteService = FavoriteService(this)
        timetableAdapter = TimetableAdapter(stopName)

        binding.departureList.apply {
            adapter = timetableAdapter
            isEdgeItemsCenteringEnabled = false
            layoutManager = LinearLayoutManager(this@TimetableActivity)
        }

        binding.bottomActionDrawer.setOnMenuItemClickListener {
            onMenuItemClick(it)
            false
        }

        timetableModel.setCurrentStop(Stop(stopName, stopId))

        setUpObservers()
    }

    private fun setUpObservers() {
        timetableModel.isFavorite.observe(this) {
            toggleFavorite(it)
            if (!it) {
                binding.bottomActionDrawer.controller.peekDrawer()
            }
        }

        timetableModel.getLineDepartures().observe(this) {
            if (it != null) {
                timetableAdapter.estimatedCall = it
                binding.progressBar.visibility = View.INVISIBLE
                binding.departureList.requestFocus()
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        binding.progressBar.visibility = View.VISIBLE
        timetableModel.loadDepartures(Stop(stopName, stopId))
    }


    private fun toggleFavorite(isFavorite: Boolean) {
        val menuItem = binding.bottomActionDrawer.menu.findItem(R.id.toggle_favorite)
        menuItem.setIcon(if (isFavorite) R.drawable.ic_favorite_white_48dp else R.drawable.ic_favorite_border_white_48dp)
        menuItem.setTitle(if (isFavorite) R.string.add_favorite else R.string.remove_favorite)
    }

    private fun onMenuItemClick(menuItem: MenuItem) {
        if (menuItem.itemId == R.id.toggle_favorite) {
            timetableModel.toggleFavorite(Stop(stopName, stopId))
        }
    }

    companion object {

        const val TAG = "Stop Selector"
        const val STOP_ID = "stopId"
        const val STOP_NAME = "stopName"
    }
}
