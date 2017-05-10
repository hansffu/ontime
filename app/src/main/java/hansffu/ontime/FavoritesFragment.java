package hansffu.ontime;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WearableRecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import hansffu.ontime.adapter.StopViewAdapter;
import hansffu.ontime.model.Stop;
import hansffu.ontime.service.FavoriteService;

import static hansffu.ontime.TimetableActivity.STOP_ID;
import static hansffu.ontime.TimetableActivity.STOP_NAME;


public class FavoritesFragment extends Fragment {

    private WearableRecyclerView mStopListView;
    private FavoriteService favoriteService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.stop_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mStopListView = (WearableRecyclerView) getView().findViewById(R.id.stop_list);
        favoriteService = new FavoriteService(getContext());
    }

    @Override
    public void onResume(){
        super.onResume();
        setListContent();
    }

    private void setListContent() {
        final List<Stop> stops = favoriteService.getFavorites();

        StopViewAdapter adapter = new StopViewAdapter(stops);
        adapter.setListener(new StopViewAdapter.ItemSelectedListener() {
            @Override
            public void onItemSelected(int position) {
                Intent startTimetableActivity = new Intent(FavoritesFragment.this.getActivity(), TimetableActivity.class);
                startTimetableActivity.putExtra(STOP_ID, stops.get(position).getId());
                startTimetableActivity.putExtra(STOP_NAME, stops.get(position).getName());
                FavoritesFragment.this.startActivity(startTimetableActivity);
            }
        });

        mStopListView.setAdapter(adapter);
    }


}