package hansffu.ontime

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
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
    private val timetableModel: TimetableViewModel by viewModels()
    private var stopViewAdapter = StopViewAdapter()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        stopViewAdapter.setListener(this)
        binding.stopList.apply {
            adapter = stopViewAdapter
            isEdgeItemsCenteringEnabled = false
            layoutManager = LinearLayoutManager(this@NavigationActivity)
        }
        binding.topNavigationDrawer.apply {
            setAdapter(MainNavigationAdapter(this@NavigationActivity))
        }
        favoriteModel.getLocationHolder().observe(this) {
            when (it) {
                is LocationHolder.NoPermission -> requestPermissions(
                    arrayOf(
                        ACCESS_FINE_LOCATION,
                        ACCESS_COARSE_LOCATION
                    ), 123
                )
                is LocationHolder.LocationFound -> favoriteModel.load()
                else -> println(it)
            }
        }

        if (favoriteService.getFavorites().isEmpty()) {
            onMenuItemSelected(1)
        } else {
            onMenuItemSelected(0)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        favoriteModel.refreshPermissions()
    }

    override fun onResume() {
        super.onResume()
        favoriteModel.getStops().observe(this) {
            stopViewAdapter.updateStops(it)
        }

        binding.topNavigationDrawer.apply {
            addOnItemSelectedListener { onMenuItemSelected(it) }
            controller.peekDrawer()
        }
    }

    private fun onMenuItemSelected(i: Int) {
        favoriteModel.load()
        favoriteModel.setCurrentList(
            when (i) {
                1 -> StopListType.NEARBY
                else -> StopListType.FAVORITES
            }
        )
    }

    override fun onItemSelected(position: Int) {
        timetableModel.setCurrentStop(stopViewAdapter.stops[position])
        val startTimetableActivity = Intent(this, TimetableActivity::class.java)
        startTimetableActivity.putExtra(
            TimetableActivity.STOP_ID,
            stopViewAdapter.stops[position].id
        )
        startTimetableActivity.putExtra(
            TimetableActivity.STOP_NAME,
            stopViewAdapter.stops[position].name
        )
        startActivity(startTimetableActivity)
    }
}

