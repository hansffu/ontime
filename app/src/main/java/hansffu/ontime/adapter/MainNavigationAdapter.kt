package hansffu.ontime.adapter

import android.graphics.drawable.Drawable
import androidx.fragment.app.Fragment
import androidx.wear.widget.drawer.WearableNavigationDrawerView
import hansffu.ontime.NavigationActivity
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
        internal val iconId: Int) {

    FAVORITES(R.string.drawer_favorites, R.drawable.ic_favorite_white_48dp),
    NEARBY(R.string.drawer_nearby, R.drawable.ic_near_me_white_48dp)
}