package hansffu.ontime.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.view.WearableRecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import hansffu.ontime.R;
import hansffu.ontime.model.Departure;

import static android.support.v7.widget.LinearLayoutManager.HORIZONTAL;
import static android.support.v7.widget.LinearLayoutManager.VERTICAL;

/**
 * Created by hansffu on 04.02.17.
 */

public class TimetableAdapter extends RecyclerView.Adapter {

    private static final int TYPE_HEADER = 111;
    private static final int TYPE_ITEM = 333;
    private final String TAG = "TimetableAdapter";
    private List<List<Departure>> departures;
    private Context context;
    private String stopName;

    public TimetableAdapter(Context context, String stopName, List<List<Departure>> departures) {
        this.context = context;
        this.stopName = stopName;
        this.departures = departures;
    }

    public void setDepartures(List<List<Departure>> departures) {
        this.departures = departures;
        notifyDataSetChanged();
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            return new HeaderViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.timetable_list_header, parent, false));
        }

        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.timetable_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int index) {
        int lookupStoplistIndex = index;
        if (stopName != null) {
            lookupStoplistIndex--;
            if (index == 0 && holder instanceof HeaderViewHolder) {//is header
                HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                headerViewHolder.update(stopName);
            }
        }
        if (!departures.isEmpty() && holder instanceof ViewHolder) {
            ViewHolder viewHolder = (ViewHolder) holder;
            viewHolder.update(departures.get(lookupStoplistIndex));
//            viewHolder.setBrighterColor(lookupStoplistIndex % 2 == 0);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && stopName != null) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        int itemCount = departures.size();
        if (stopName != null) {
            itemCount++;
        }
        return itemCount;
    }

    private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final StopTimesAdapter stopTimesAdapter;
        private TextView lineNumber;
        private TextView destination;
        private WearableRecyclerView departsInList;
        private View itemView;
        private LinearLayoutManager stopTimesLayoutManager;
        boolean isExpanded = false;

        ViewHolder(View itemView) {
            super(itemView);
            lineNumber = (TextView) itemView.findViewById(R.id.line_number);
            destination = (TextView) itemView.findViewById(R.id.destination);
//            departsIn = (TextView) itemView.findViewById(R.id.departs_in);
            departsInList = (WearableRecyclerView) itemView.findViewById(R.id.departs_in_list);
            this.itemView = itemView;
            stopTimesAdapter = new StopTimesAdapter(this);
            departsInList.setAdapter(stopTimesAdapter);

            stopTimesLayoutManager = new LinearLayoutManager(context, HORIZONTAL, false);
            departsInList.setLayoutManager(stopTimesLayoutManager);
            itemView.setOnClickListener(this);
        }

        void update(List<Departure> lineDepartures) {
            this.lineNumber.setText(lineDepartures.get(0).getLineNumber());
            this.destination.setText(lineDepartures.get(0).getDestination());
            List<String> times = new ArrayList<>(lineDepartures.size());
            for (Departure departure : lineDepartures) {
                long timeMins = (departure.getTime().getTime() - new Date().getTime()) / 60000;
                if (timeMins <= 0) {
                    times.add("Nå");
                } else if (timeMins >= 20) {
                    times.add(new SimpleDateFormat("HH:mm").format(departure.getTime()));
//                    times.append(SimpleDateFormat.getTimeInstance().format(departure.getTime())).append("  ");

                } else {
                    times.add(timeMins + "\u00A0min");
                }
            }
            stopTimesAdapter.update(times);

        }


        @Override
        public void onClick(View v) {
            toggleExpanded();
        }

        private void toggleExpanded() {
            isExpanded = !isExpanded;
            stopTimesLayoutManager.setOrientation(isExpanded ? VERTICAL : HORIZONTAL);
            stopTimesAdapter.setExpanded(isExpanded);
            setBrighterColor(isExpanded);
            destination.setMaxLines(isExpanded ? 3 : 1);
        }

        void setBrighterColor(boolean brighterColor) {
            itemView.setBackgroundColor(context.getColor(brighterColor ? R.color.light_background : R.color.dark_background));
        }

    }

    //our header/footer RecyclerView.ViewHolder is just a FrameLayout
    private static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView stopNameView;

        HeaderViewHolder(View itemView) {
            super(itemView);
            stopNameView = (TextView) itemView.findViewById(R.id.short_stop_name);
        }

        void update(String stopName) {
            this.stopNameView.setText(stopName);
        }


    }

}
