package com.nosfistis.mike.refene;

import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mike on 16/8/2015.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ListItemViewHolder> implements RecyclerView.OnItemTouchListener {
    private SparseBooleanArray selectedItems = new SparseBooleanArray();
    private List<String> nameData;
    private List<Float> totalData;
    private List<Float> ownedData;
    private GestureDetector gestureDetector;

    RecyclerViewAdapter(List<String> nameData, GestureDetector gestureDetector) {
        if (nameData == null) {
            throw new IllegalArgumentException("nameData must not be null");
        }

        this.gestureDetector = gestureDetector;
        this.nameData = nameData;
    }

    RecyclerViewAdapter(List<String> nameData, List<Float> totalData, List<Float> ownedData, GestureDetector gestureDetector) {
        if (nameData == null) {
            throw new IllegalArgumentException("nameData must not be null");
        }

        this.gestureDetector = gestureDetector;
        this.nameData = nameData;
        this.ownedData = ownedData;
        this.totalData = totalData;
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
        nameData.remove(position);
        if (totalData != null && ownedData != null) {
            totalData.remove(position);
            ownedData.remove(position);
        }
        notifyItemRemoved(position);
        selectedItems.clear();
    }

    public void updateData(List<String> nameData, List<Float> totalData, List<Float> ownedData) {
        this.nameData = nameData;
        this.totalData = totalData;
        this.ownedData = ownedData;
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
        holder.nameView.setText(nameData.get(position));
        holder.itemView.setActivated(selectedItems.get(position));
        if (totalData != null && ownedData != null) {
            NumberFormat numberFormat = NumberFormat.getCurrencyInstance();
            holder.totalView.setText(numberFormat.format(totalData.get(position)));
            holder.ownedView.setText(numberFormat.format(ownedData.get(position)));
        }
    }

    @Override
    public int getItemCount() {
        return nameData.size();
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        gestureDetector.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    public final static class ListItemViewHolder extends RecyclerView.ViewHolder {
        public final TextView ownedView;
        public final TextView nameView;
        public final TextView totalView;

        public ListItemViewHolder(View v) {
            super(v);
            nameView = (TextView) v.findViewById(R.id.nameText);
            totalView = (TextView) v.findViewById(R.id.personalTotalText);
            ownedView = (TextView) v.findViewById(R.id.totalOwnedText);
        }
    }
}
