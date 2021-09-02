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
    private val disposables = CompositeDisposable()

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

        binding.bottomActionDrawer.setOnMenuItemClickListener { onMenuItemClick(it) }

        setUpObservers()

        val toggleFavoriteMenuItem = binding.bottomActionDrawer.menu.findItem(R.id.toggle_favorite)
        val favorite = favoriteService.isFavorite(Stop(stopName, stopId))
        toggleFavorite(favorite, toggleFavoriteMenuItem)
        if (!favorite) {
            binding.bottomActionDrawer.controller.peekDrawer()
        }

    }

    private fun setUpObservers() {
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


    private fun toggleFavorite(isFavorite: Boolean, menuItem: MenuItem) {
        menuItem.setIcon(if (isFavorite) R.drawable.ic_favorite_white_48dp else R.drawable.ic_favorite_border_white_48dp)
        menuItem.setTitle(if (isFavorite) R.string.add_favorite else R.string.remove_favorite)
    }

    @SuppressLint("CheckResult")
    private fun onMenuItemClick(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.toggle_favorite) {
            val stop = favoriteService.toggleFavorite(Stop(stopName, stopId))
            toggleFavorite(stop, menuItem)
            return true
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    companion object {

        const val TAG = "Stop Selector"
        const val STOP_ID = "stopId"
        const val STOP_NAME = "stopName"
    }
}
