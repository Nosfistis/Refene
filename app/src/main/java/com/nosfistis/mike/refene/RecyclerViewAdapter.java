package com.nosfistis.mike.refene;

import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mike on 16/8/2015.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ListItemViewHolder> {
    private SparseBooleanArray selectedItems = new SparseBooleanArray();
    private List<String> data = new ArrayList<>();

    RecyclerViewAdapter(List<String> modelData) {
        if (modelData == null) {
            throw new IllegalArgumentException("modelData must not be null");
        }
        data = modelData;
    }

    public void toggleSelection(int pos) {
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
        } else {
            selectedItems.put(pos, true);
        }
        notifyItemChanged(pos);
    }

    public void removeData(int position) {
        data.remove(position);
        notifyItemRemoved(position);
        selectedItems.clear();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selectedItems.size());
        for (int i = selectedItems.size() - 1; i >= 0; i--) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ListItemViewHolder vh = new ListItemViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ListItemViewHolder holder, int position) {
        holder.nameView.setText(data.get(position));
        holder.itemView.setActivated(selectedItems.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public final static class ListItemViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView nameView;
        public TextView totalView;

        public ListItemViewHolder(View v) {
            super(v);
            nameView = (TextView) v.findViewById(R.id.nameText);
            totalView = (TextView) v.findViewById(R.id.personalTotalText);
        }
    }
}
