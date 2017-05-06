package hansffu.ontime.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.drawable.Drawable;
import android.support.wearable.view.drawer.WearableNavigationDrawer;
import android.util.Log;

import hansffu.ontime.FavoritesFragment;
import hansffu.ontime.NavigationActivity;
import hansffu.ontime.NearbyFragment;
import hansffu.ontime.R;

/**
 * Created by hansffu on 4/22/17.
 */

public class MainNavigationAdapter extends WearableNavigationDrawer.WearableNavigationDrawerAdapter {

    private final String TAG = "MainNavigationAdapter";
    private NavigationActivity navigationActivity;

    public MainNavigationAdapter(NavigationActivity navigationActivity) {
        this.navigationActivity = navigationActivity;
        onItemSelected(0);
    }

    @Override
    public String getItemText(int i) {
        if (i >= MenuItem.values().length) {
            throw new IllegalArgumentException(String.valueOf(i));
        }
        return navigationActivity.getString(MenuItem.values()[i].textId);
    }

    @Override
    public Drawable getItemDrawable(int i) {
        if (i >= MenuItem.values().length) {
            throw new IllegalArgumentException(String.valueOf(i));
        }
        return navigationActivity.getDrawable(MenuItem.values()[i].iconId);
    }

    @Override
    public void onItemSelected(int i) {
        FragmentManager fragmentManager = navigationActivity.getFragmentManager();
        try {
            fragmentManager.beginTransaction().replace(R.id.content, MenuItem.values()[i].contentFragment.newInstance()).commit();
        } catch (InstantiationException | IllegalAccessException e) {
            Log.e(TAG, "Could not create fragment: " + MenuItem.values()[i].contentFragment.getName());
        }
    }

    @Override
    public int getCount() {
        return MenuItem.values().length;
    }

    private enum MenuItem {
        FAVORITES(R.string.drawer_favorites, R.drawable.ic_favorite_white_48dp, FavoritesFragment.class),
        NEARBY(R.string.drawer_nearby, R.drawable.ic_near_me_white_48dp, NearbyFragment.class);
        //        SEARCH(R.string.drawer_search, R.drawable.ic_search_white_48dp, new FavoritesFragment());
        private int textId;
        private int iconId;
        private Class<? extends Fragment> contentFragment;

        MenuItem(int textId, int iconId, Class<? extends Fragment> contentFragment) {
            this.textId = textId;
            this.iconId = iconId;
            this.contentFragment = contentFragment;
        }
    }
}
