package hansffu.ontime

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.recyclerview.widget.LinearLayoutManager
import hansffu.ontime.adapter.*
import hansffu.ontime.databinding.ActivityNavigationBinding
import hansffu.ontime.model.StopListType

class NavigationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNavigationBinding
    private val favoriteModel: FavoriteViewModel by viewModels()
    private var stopViewAdapter = StopViewAdapter()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.stopList.apply {
            adapter = stopViewAdapter
            isEdgeItemsCenteringEnabled = false
            layoutManager = LinearLayoutManager(this@NavigationActivity)
        }
        binding.topNavigationDrawer.apply {
            setAdapter(MainNavigationAdapter(this@NavigationActivity))
        }
        favoriteModel.getLocationHolder().observe(this) {
            if (it is LocationHolder.NoPermission) requestPermissions(
                arrayOf(
                    ACCESS_FINE_LOCATION,
                    ACCESS_COARSE_LOCATION
                ), 123
            )
        }
        onMenuItemSelected(0)
        startObservers()
    }

    private fun startObservers() {
        favoriteModel.currentList
            .switchMap(this::createListItems)
            .observe(this) { items ->
                println(items.map { item ->
                    when (item) {
                        is StopItem -> item.stop.name
                        is ButtonItem -> "button"
                        is HeaderItem -> "header"
                    }
                }
                )
                stopViewAdapter.items = items
            }

    }

    private fun createListItems(type: StopListType): LiveData<List<StopViewItem>> =
        when (type) {
            StopListType.FAVORITES -> createFavoriteItems()
            StopListType.NEARBY -> createNearbyItems()
        }

    private fun createFavoriteItems(): LiveData<List<StopViewItem>> =
        favoriteModel.favoriteStops.map { stops ->
            listOf(
                listOf(HeaderItem(resources.getString(R.string.favorites_header))),
                stops.map { StopItem(it, ::onItemSelected) },
                listOf(ButtonItem(resources.getString(R.string.find_more)) { favoriteModel.setCurrentList(StopListType.NEARBY) })
            ).flatten()
        }


    private fun createNearbyItems(): LiveData<List<StopViewItem>> =
        favoriteModel.nearbyStops.map { stops ->
            listOf(HeaderItem(resources.getString(R.string.nearby_header))) + stops.map {
                StopItem(it, ::onItemSelected)
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

        binding.topNavigationDrawer.apply {
            addOnItemSelectedListener { onMenuItemSelected(it) }
            controller.peekDrawer()
        }
    }

    private fun onMenuItemSelected(i: Int) {
        favoriteModel.setCurrentList(
            when (i) {
                1 -> StopListType.NEARBY
                else -> StopListType.FAVORITES
            }
        )
    }

    private fun onItemSelected(item: StopViewItem) {
        if (item !is StopItem) return
        val startTimetableActivity = Intent(this, TimetableActivity::class.java)
        startTimetableActivity.putExtra(TimetableActivity.STOP_ID, item.stop.id)
        startTimetableActivity.putExtra(TimetableActivity.STOP_NAME, item.stop.name)
        startActivity(startTimetableActivity)
    }
}


