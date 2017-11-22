package hansffu.ontime.adapter;

import android.support.v7.widget.RecyclerView;
import android.support.wear.widget.WearableRecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import hansffu.ontime.R;
import hansffu.ontime.model.Stop;

public class StopViewAdapter extends WearableRecyclerView.Adapter {

    private static final int TYPE_HEADER = 111;
    private static final int TYPE_ITEM = 333;
    private String headerText, noStopsText=null;
    private final List<Stop> stops;
    private ItemSelectedListener itemSelectedListener;

    public StopViewAdapter(String headerText, List<Stop> stops) {
        this.headerText = headerText;
        this.stops = stops;
    }

    public void updateStops(List<Stop> stops) {
        this.stops.clear();
        this.stops.addAll(stops);
        notifyDataSetChanged();
    }

    public void setNoStopsText(String noStopsText) {
        stops.clear();
        this.noStopsText = noStopsText;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            return new HeaderViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.stop_list_header, parent, false));
        }
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stop_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int lookupStoplistIndex = position;
        if (headerText != null) {
            lookupStoplistIndex--;
            if (position == 0 && holder instanceof HeaderViewHolder) {//is header
                HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                headerViewHolder.update(headerText);
                return;
            }
        }
        if (!stops.isEmpty() && holder instanceof ViewHolder) {
            ViewHolder viewHolder = (ViewHolder) holder;
            viewHolder.textView.setText(stops.get(lookupStoplistIndex).getName());
            viewHolder.bind(lookupStoplistIndex, itemSelectedListener);
        } else if (noStopsText != null && holder instanceof ViewHolder) {
            ViewHolder viewHolder = (ViewHolder) holder;
            viewHolder.textView.setText(noStopsText);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && headerText != null) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }


    public void setListener(ItemSelectedListener itemSelectedListener) {
        this.itemSelectedListener = itemSelectedListener;
    }

    @Override
    public int getItemCount() {
        int size = stops.size();
        if (stops.isEmpty() && noStopsText != null) {
            size = 1;
        }
        if (headerText != null) {
            size++;
        }
        return size;
    }

    public List<Stop> getStops() {
        return stops;
    }

    public void setHeaderText(String headerText) {
        this.headerText = headerText;
    }

    public interface ItemSelectedListener {
        void onItemSelected(int position);
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textView;

        ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.short_stop_name);
        }

        void bind(final int position, final ItemSelectedListener listener) {

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onItemSelected(position);
                    }
                }
            });
        }

    }

    //our header/footer RecyclerView.ViewHolder is just a FrameLayout
    private static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        HeaderViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.stop_list_header);
        }

        void update(String headerText) {
            this.textView.setText(headerText);
        }
    }
}
