package hansffu.ontime

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.wear.widget.WearableLinearLayoutManager
import hansffu.ontime.adapter.MainNavigationAdapter
import hansffu.ontime.adapter.StopViewAdapter
import hansffu.ontime.databinding.ActivityNavigationBinding
import hansffu.ontime.model.StopListType
import hansffu.ontime.service.FavoriteService

class NavigationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNavigationBinding
    private val favoriteService: FavoriteService by lazy { FavoriteService(applicationContext) }
    private val favoriteModel: FavoriteViewModel by viewModels()
    private var stopViewAdapter = StopViewAdapter()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.stopList.apply {
            adapter = stopViewAdapter
            isEdgeItemsCenteringEnabled = true
            layoutManager = WearableLinearLayoutManager(
                this@NavigationActivity,
                CustomScrollingLayoutCallback()
            )
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
            addOnItemSelectedListener { onItemSelected(it) }
            controller.peekDrawer()
        }

        if (favoriteService.getFavorites().isEmpty()) {
            onItemSelected(1)
        } else {
            onItemSelected(0)
        }
    }

    private fun onItemSelected(i: Int) {
        favoriteModel.load(if (i == 0) StopListType.FAVORITES else StopListType.NEARBY)
    }
}

