package hansffu.ontime.adapter

import android.app.Fragment
import android.graphics.drawable.Drawable
import android.support.wearable.view.drawer.WearableNavigationDrawer
import android.util.Log
import hansffu.ontime.FavoritesFragment
import hansffu.ontime.NavigationActivity
import hansffu.ontime.NearbyFragment
import hansffu.ontime.R

class MainNavigationAdapter(private val navigationActivity: NavigationActivity) : WearableNavigationDrawer.WearableNavigationDrawerAdapter() {

    private val TAG = "MainNavigationAdapter"

    init {
        onItemSelected(0)
    }

    override fun getItemText(i: Int): String =
            navigationActivity.getString(MenuItem.values()[i].textId)

    override fun getItemDrawable(i: Int): Drawable? =
            navigationActivity.getDrawable(MenuItem.values()[i].iconId)

    override fun onItemSelected(i: Int) {
        val fragmentManager = navigationActivity.fragmentManager
        try {
            fragmentManager.beginTransaction().replace(R.id.content, MenuItem.values()[i].contentFragment.newInstance()).commit()
        } catch (e: InstantiationException) {
            Log.e(TAG, "Could not create fragment: " + MenuItem.values()[i].contentFragment.name)
        } catch (e: IllegalAccessException) {
            Log.e(TAG, "Could not create fragment: " + MenuItem.values()[i].contentFragment.name)
        }

    }

    override fun getCount() = MenuItem.values().size

}

internal enum class MenuItem(
        internal val textId: Int,
        internal val iconId: Int,
        internal val contentFragment: Class<out Fragment>) {

    FAVORITES(R.string.drawer_favorites, R.drawable.ic_favorite_white_48dp, FavoritesFragment::class.java),
    NEARBY(R.string.drawer_nearby, R.drawable.ic_near_me_white_48dp, NearbyFragment::class.java)
}