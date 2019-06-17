package hansffu.ontime.adapter

import android.graphics.drawable.Drawable
import android.support.v4.app.Fragment
import android.support.wear.widget.drawer.WearableNavigationDrawerView
import android.support.wearable.view.drawer.WearableNavigationDrawer
import android.util.Log
import hansffu.ontime.FavoritesFragment
import hansffu.ontime.NavigationActivity
import hansffu.ontime.NearbyFragment
import hansffu.ontime.R

class MainNavigationAdapter(private val navigationActivity: NavigationActivity) : WearableNavigationDrawerView.WearableNavigationDrawerAdapter() {

    private val TAG = "MainNavigationAdapter"

    override fun getItemText(i: Int): String =
            navigationActivity.getString(MenuItem.values()[i].textId)

    override fun getItemDrawable(i: Int): Drawable? =
            navigationActivity.getDrawable(MenuItem.values()[i].iconId)


    override fun getCount() = MenuItem.values().size

}

internal enum class MenuItem(
        internal val textId: Int,
        internal val iconId: Int,
        internal val contentFragment: Class<out Fragment>) {

    FAVORITES(R.string.drawer_favorites, R.drawable.ic_favorite_white_48dp, FavoritesFragment::class.java),
    NEARBY(R.string.drawer_nearby, R.drawable.ic_near_me_white_48dp, NearbyFragment::class.java)
}