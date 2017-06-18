package hansffu.ontime.adapter;

import android.support.v7.widget.RecyclerView;
import android.support.wearable.view.WearableRecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import hansffu.ontime.R;

/**
 * Created by hansffu on 31.05.2017.
 */

public class StopTimesAdapter extends WearableRecyclerView.Adapter {

    private final List<String> timeList;
    private boolean isExpanded = false;
    private View.OnClickListener listener;

    public StopTimesAdapter(View.OnClickListener listener) {
        this.listener = listener;
        this.timeList = new ArrayList<>();
    }

    void update(List<String> times) {
        timeList.clear();
        timeList.addAll(times);
        notifyDataSetChanged();
    }

    void setExpanded(boolean isExpanded) {
        this.isExpanded = isExpanded;
        notifyDataSetChanged();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TextViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.departs_in_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((TextViewHolder)holder).update(timeList.get(position));
    }

    @Override
    public int getItemCount() {
        if(!isExpanded && timeList.size() >= 2) return 2;
        return timeList.size();
    }

    private class TextViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        TextViewHolder(View view) {
            super(view);
            this.textView = (TextView) view.findViewById(R.id.departs_in_item);
            view.setOnClickListener(listener);
        }

        private void update(String time) {
            textView.setText(time);
        }

    }
}
