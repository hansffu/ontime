package hansffu.ontime.adapter;

import android.support.v7.widget.RecyclerView;
import android.support.wearable.view.WearableRecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import hansffu.ontime.R;
import hansffu.ontime.model.Stop;

/**
 * Created by hansffu on 04.02.17.
 */

public class StopViewAdapter extends WearableRecyclerView.Adapter {

    private List<Stop> stops;
    private ItemSelectedListener itemSelectedListener;

    public StopViewAdapter(List<Stop> stops) {
        this.stops = stops;
    }

    public void updateStops(List<Stop> stops) {
        this.stops = stops;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stop_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (!stops.isEmpty() && holder instanceof ViewHolder) {
            ViewHolder viewHolder = (ViewHolder) holder;
            viewHolder.textView.setText(stops.get(position).getName());
            viewHolder.bind(position, itemSelectedListener);
        }
    }

    public void setListener(ItemSelectedListener itemSelectedListener) {
        this.itemSelectedListener = itemSelectedListener;
    }

    @Override
    public int getItemCount() {
        return stops.size();
    }

    public List<Stop> getStops() {
        return stops;
    }

    public interface ItemSelectedListener {
        void onItemSelected(int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textView;

        ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text_item);
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
}
