package hansffu.ontime

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import hansffu.ontime.adapter.MainNavigationAdapter
import hansffu.ontime.adapter.MenuItem
import hansffu.ontime.service.FavoriteService
import kotlinx.android.synthetic.main.activity_navigation.*

class NavigationActivity : FragmentActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        top_navigation_drawer.setAdapter(MainNavigationAdapter(this))
        top_navigation_drawer.addOnItemSelectedListener { onItemSelected(it) }
        top_navigation_drawer.controller.peekDrawer()

        if (FavoriteService(this).favorites.isEmpty()) {
            onItemSelected(1)
        } else {
            onItemSelected(0)
        }

    }

    private fun onItemSelected(i: Int) {
        val transaction = supportFragmentManager.beginTransaction().apply {
            replace(R.id.content, MenuItem.values()[i].contentFragment.newInstance())
            addToBackStack(null)
        }
        transaction.commit()
    }
}
