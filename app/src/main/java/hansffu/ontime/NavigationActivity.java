package hansffu.ontime;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.drawer.WearableDrawerLayout;
import android.support.wearable.view.drawer.WearableNavigationDrawer;
import android.view.Gravity;

import hansffu.ontime.adapter.MainNavigationAdapter;
import hansffu.ontime.service.FavoriteService;

public class NavigationActivity extends WearableActivity {

    private WearableDrawerLayout mWearableDrawerLayout;
    private WearableNavigationDrawer mWearableNavigationDrawer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        // Main Wearable Drawer Layout that wraps all content
        mWearableDrawerLayout = (WearableDrawerLayout) findViewById(R.id.drawer_layout);
        mWearableDrawerLayout.peekDrawer(Gravity.BOTTOM);
        mWearableDrawerLayout.peekDrawer(Gravity.TOP);

        // Top Navigation Drawer
        mWearableNavigationDrawer = (WearableNavigationDrawer) findViewById(R.id.top_navigation_drawer);
        mWearableNavigationDrawer.setAdapter(new MainNavigationAdapter(this));

        // Peeks Navigation drawer on the top.
        mWearableDrawerLayout.peekDrawer(Gravity.TOP);

        if (new FavoriteService(this).getFavorites().isEmpty()) {
            mWearableNavigationDrawer.setCurrentItem(1, false);
        }

    }
}
