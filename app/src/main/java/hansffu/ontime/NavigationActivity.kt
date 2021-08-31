package hansffu.ontime

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import hansffu.ontime.adapter.MainNavigationAdapter
import hansffu.ontime.adapter.StopViewAdapter
import hansffu.ontime.databinding.ActivityNavigationBinding
import hansffu.ontime.model.StopListType
import hansffu.ontime.service.FavoriteService

class NavigationActivity : AppCompatActivity(), StopViewAdapter.ItemSelectedListener {

    private lateinit var binding: ActivityNavigationBinding
    private val favoriteService: FavoriteService by lazy { FavoriteService(applicationContext) }
    private val favoriteModel: FavoriteViewModel by viewModels()
    private var stopViewAdapter = StopViewAdapter()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        stopViewAdapter.setListener(this)
        binding.stopList.apply {
            adapter = stopViewAdapter
            isEdgeItemsCenteringEnabled = true
            layoutManager = LinearLayoutManager(this@NavigationActivity)
        }
        binding.topNavigationDrawer.apply {
            setAdapter(MainNavigationAdapter(this@NavigationActivity))
        }

    }

    override fun onResume() {
        super.onResume()
        favoriteModel.getStops().observe(this) {
            if (it != null) {
                stopViewAdapter.updateStops(it)
            }
        }

        binding.topNavigationDrawer.apply {
            addOnItemSelectedListener { onMenuItemSelected(it) }
            controller.peekDrawer()
        }

        if (favoriteService.getFavorites().isEmpty()) {
            onMenuItemSelected(1)
        } else {
            onMenuItemSelected(0)
        }
    }

    private fun onMenuItemSelected(i: Int) {
        favoriteModel.load(if (i == 0) StopListType.FAVORITES else StopListType.NEARBY)
    }

    override fun onItemSelected(position: Int) {
        favoriteModel.getStops().value?.let { model ->
            val startTimetableActivity = Intent(this, TimetableActivity::class.java)
            startTimetableActivity.putExtra(TimetableActivity.STOP_ID, model.stops[position].id)
            startTimetableActivity.putExtra(TimetableActivity.STOP_NAME, model.stops[position].name)
            startActivity(startTimetableActivity)
        }
    }
}

