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
	private List<String> textColumnData;
	private List<Float> firstNumberColumnData;
	private List<Float> secondNumberColumnData;
	private GestureDetector gestureDetector;
	
	RecyclerViewAdapter(List<String> nameData, GestureDetector gestureDetector) {
		if (nameData == null) {
			throw new IllegalArgumentException("nameData must not be null");
		}
		
		this.gestureDetector = gestureDetector;
		this.textColumnData = nameData;
	}
	
	RecyclerViewAdapter(List<String> nameData, List<Float> totalData, List<Float> ownedData, GestureDetector gestureDetector) {
		if (nameData == null) {
			throw new IllegalArgumentException("nameData must not be null");
		} else if (totalData == null) {
			throw new IllegalArgumentException("totalData must not be null");
		} else if (ownedData == null) {
			throw new IllegalArgumentException("ownedData must not be null");
		}
		
		this.gestureDetector = gestureDetector;
		this.textColumnData = nameData;
		this.secondNumberColumnData = ownedData;
		this.firstNumberColumnData = totalData;
	}
	
	RecyclerViewAdapter(List<String> descriptionData, List<Float> priceData, GestureDetector gestureDetector) {
		if (descriptionData == null) {
			throw new IllegalArgumentException("descriptionData must not be null");
		} else if (priceData == null) {
			throw new IllegalArgumentException("priceData must not be null");
		}
		
		this.gestureDetector = gestureDetector;
		this.textColumnData = descriptionData;
		this.firstNumberColumnData = priceData;
	}
	
	void toggleSelection(int pos) {
		if (selectedItems.get(pos, false)) {
			selectedItems.delete(pos);
		} else {
			selectedItems.put(pos, true);
		}
		notifyItemChanged(pos);
	}
	
	void removeData(int position) {
		textColumnData.remove(position);
		if (firstNumberColumnData != null) {
			firstNumberColumnData.remove(position);
		}
		if (secondNumberColumnData != null) {
			secondNumberColumnData.remove(position);
		}
		notifyItemRemoved(position);
		selectedItems.clear();
	}
	
	List<Integer> getSelectedItems() {
		List<Integer> items = new ArrayList<>(selectedItems.size());
		for (int i = selectedItems.size() - 1; i >= 0; i--) {
			items.add(selectedItems.keyAt(i));
		}
		return items;
	}
	
	@Override
	public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.recycler_view, parent, false);
		ListItemViewHolder vh = new ListItemViewHolder(v);
		return vh;
	}
	
	@Override
	public void onBindViewHolder(ListItemViewHolder holder, int position) {
		holder.textColumnView.setText(textColumnData.get(position));
		holder.itemView.setActivated(selectedItems.get(position));
		if (firstNumberColumnData != null) {
			NumberFormat numberFormat = NumberFormat.getCurrencyInstance();
			holder.firstNumberColumnView.setText(numberFormat.format(firstNumberColumnData.get(position)));
		}
		if (secondNumberColumnData != null) {
			NumberFormat numberFormat = NumberFormat.getCurrencyInstance();
			holder.secondNumberColumnView.setText(numberFormat.format(secondNumberColumnData.get(position)));
		}
	}
	
	@Override
	public int getItemCount() {
		return textColumnData.size();
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
	
	final static class ListItemViewHolder extends RecyclerView.ViewHolder {
		final TextView secondNumberColumnView;
		final TextView textColumnView;
		final TextView firstNumberColumnView;
		
		ListItemViewHolder(View v) {
			super(v);
			textColumnView = (TextView) v.findViewById(R.id.nameText);
			firstNumberColumnView = (TextView) v.findViewById(R.id.personalTotalText);
			secondNumberColumnView = (TextView) v.findViewById(R.id.totalOwnedText);
		}
	}
}
