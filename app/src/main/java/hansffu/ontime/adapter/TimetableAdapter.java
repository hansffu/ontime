package hansffu.ontime.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import hansffu.ontime.R;
import hansffu.ontime.model.Departure;

/**
 * Created by hansffu on 04.02.17.
 */

public class TimetableAdapter extends RecyclerView.Adapter {

    private static final int TYPE_HEADER = 111;
    private static final int TYPE_ITEM = 333;
    private final String TAG = "TimetableAdapter";
    private List<List<Departure>> departures;
    private String stopName;

    public TimetableAdapter(String stopName, List<List<Departure>> departures) {
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

    private static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView lineNumber;
        private TextView destination;
        private TextView departsIn;

        ViewHolder(View itemView) {
            super(itemView);
            lineNumber = (TextView) itemView.findViewById(R.id.line_number);
            destination = (TextView) itemView.findViewById(R.id.destination);
            departsIn = (TextView) itemView.findViewById(R.id.departs_in);
        }

        void update(List<Departure> lineDepartures) {
            this.lineNumber.setText(lineDepartures.get(0).getLineNumber());
            this.destination.setText(lineDepartures.get(0).getDestination());
            StringBuilder times = new StringBuilder();
            for (Departure departure : lineDepartures) {
                long timeMins = (departure.getTime().getTime() - new Date().getTime()) / 60000;
                if (timeMins <= 0) {
                    times.append("Nå  ");
                } else if (timeMins >= 20) {
                    times.append(new SimpleDateFormat("HH:mm").format(departure.getTime())).append("  ");
//                    times.append(SimpleDateFormat.getTimeInstance().format(departure.getTime())).append("  ");

                } else {
                    times.append(timeMins + "\u00A0min  ");
                }
            }
            this.departsIn.setText(times.toString());
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
