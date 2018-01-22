package hansffu.ontime

import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.view.Gravity
import hansffu.ontime.adapter.MainNavigationAdapter
import hansffu.ontime.service.FavoriteService
import kotlinx.android.synthetic.main.activity_navigation.*

class NavigationActivity : WearableActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        top_navigation_drawer.setAdapter(MainNavigationAdapter(this))
        drawer_layout.peekDrawer(Gravity.TOP)

        if (FavoriteService(this).favorites.isEmpty()) {
            top_navigation_drawer.setCurrentItem(1, false)
        }
    }
}
