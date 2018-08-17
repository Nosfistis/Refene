package com.nosfistis.mike.refene.refene;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.nosfistis.mike.refene.R;
import com.nosfistis.mike.refene.database.Transaction;
import com.nosfistis.mike.refene.shared.RecyclerViewAdapter;
import com.nosfistis.mike.refene.database.DatabaseHandler;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class PersonalBidsActivity extends AppCompatActivity implements ActionMode.Callback {
	
	private static final int NEW_PURCHASE_REQUEST = 1;
	
	private RecyclerView mRecyclerView;
	private RecyclerViewAdapter mAdapter;
	private ArrayList<String> descriptionDataset = new ArrayList<>();
	private ArrayList<Float> priceDataset = new ArrayList<>();
	private ArrayList<Long> idDataset = new ArrayList<>();
	private String person;
	private long personId;
	private ActionMode actionMode;
	private float totalSum = 0;
	private long refID;
	private DatabaseHandler db;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.refenes_activity);
		
		Intent intent = getIntent();
		person = intent.getStringExtra("person");
		setTitle(person);
		mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
		
		refID = intent.getLongExtra("refID", -1);
		personId = intent.getLongExtra("id", -1);
		
//		db = new DatabaseHandler(this);
//		db.open();
//		List<Transaction> data = db.getPersonTransactions(personId, refID);
//		db.close();
//
//		for (Transaction transaction : data) {
//			float price = transaction.getPrice();
//			descriptionDataset.add(transaction.getDescription());
//			priceDataset.add(price);
//			idDataset.add(transaction.getId());
//			totalSum += price;
//		}
//
		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setLayoutManager(layoutManager);
		mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(),
				layoutManager.getOrientation()));
		GestureDetector mGestureDetector = new GestureDetector(this, new RecyclerViewOnGestureListener());
		mAdapter = new RecyclerViewAdapter(descriptionDataset, priceDataset, mGestureDetector);
		mRecyclerView.setAdapter(mAdapter);
		mRecyclerView.addOnItemTouchListener(mAdapter);
		
		updateCalculations();
	}
	
	private void updateRecords() {
		db.open();
		List<Transaction> transactions = db.getPersonTransactions(personId, refID);
		db.close();
		
		for (Transaction transaction : transactions) {
			int index = idDataset.indexOf(transaction.getId());
			if (index == -1) {
				float price = transaction.getPrice();
				descriptionDataset.add(transaction.getDescription());
				priceDataset.add(price);
				idDataset.add(transaction.getId());
				totalSum += price;
			}
		}
		mAdapter.notifyDataSetChanged();
		updateCalculations();
	}
	
	private void updateCalculations() {
		NumberFormat numberFormat = NumberFormat.getCurrencyInstance();
		((TextView) findViewById(R.id.totalText)).setText(numberFormat.format(totalSum));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_personal_bids, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		
		if (id == R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == NEW_PURCHASE_REQUEST && resultCode == RESULT_OK) {
			personId = data.getLongExtra("person_id", -1);
			updateRecords();
		}
	}
	
	@Override
	public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
		MenuInflater inflater = actionMode.getMenuInflater();
		inflater.inflate(R.menu.context_menu, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
		return false;
	}
	
	@Override
	public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
		switch (menuItem.getItemId()) {
			// Delete selected items from list and database
			case R.id.context_menu_delete:
				List<Integer> selections = mAdapter.getSelectedItems();
				db.open();
				for (int i : selections) {
					db.deleteTransaction(idDataset.get(i));
					totalSum -= priceDataset.get(i);
					mAdapter.removeData(i);
					idDataset.remove(i);
				}
				db.close();
				updateCalculations();
				actionMode.finish();
				return true;
			default:
				return false;
		}
	}
	
	@Override
	public void onDestroyActionMode(ActionMode actionMode) {
		this.actionMode = null;
	}
	
	public void onAddButtonClick(View view) {
		Intent intent = new Intent(this, NewPurchaseActivity.class);
		intent.putExtra("refID", refID);
		intent.putExtra("name", person);
		startActivityForResult(intent, NEW_PURCHASE_REQUEST);
	}
	
	private class RecyclerViewOnGestureListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			View view = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
			if (view != null) {
				if (actionMode != null) {
					mAdapter.toggleSelection(mRecyclerView.getChildAdapterPosition(view));
					return super.onSingleTapConfirmed(e);
				}
			}
			return super.onSingleTapConfirmed(e);
		}
		
		public void onLongPress(MotionEvent e) {
			if (actionMode != null) {
				return;
			}
			
			actionMode = startActionMode(PersonalBidsActivity.this);
			
			View view = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
			mAdapter.toggleSelection(mRecyclerView.getChildAdapterPosition(view));
			
			super.onLongPress(e);
		}
	}
}
