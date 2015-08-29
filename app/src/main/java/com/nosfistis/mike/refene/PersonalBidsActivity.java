package com.nosfistis.mike.refene;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class PersonalBidsActivity extends AppCompatActivity implements ActionMode.Callback {

    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mAdapter;
    private ArrayList<String> descriptionDataset = new ArrayList<>();
    private ArrayList<Float> priceDataset = new ArrayList<>();
    private ArrayList<Integer> idDataset = new ArrayList<>();
    private String person;
    private ActionMode actionMode;
    private float totalSum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.refenes_activity);

        Intent intent = getIntent();
        person = intent.getStringExtra("person");
        setTitle(person);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        DatabaseHandler db = MainActivity.db;
        db.open();
        List<String[]> data = db.getPersonTransactions(intent.getIntExtra("id", -1), intent.getIntExtra("refID", -1));
        db.close();

        for (int i = 0; i < data.size(); i++) {
            String[] row = data.get(i);
            float price = Float.parseFloat(row[1]);
            descriptionDataset.add(row[0]);
            priceDataset.add(price);
            idDataset.add(Integer.parseInt(row[2]));
            totalSum += price;
        }

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(ContextCompat.getDrawable(this, R.drawable.abc_list_divider_mtrl_alpha)));
        GestureDetector mGestureDetector = new GestureDetector(this, new RecyclerViewOnGestureListener());
        mAdapter = new RecyclerViewAdapter(descriptionDataset, priceDataset, mGestureDetector);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(mAdapter);

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

        if (id == R.id.action_add) {
            Intent intent = new Intent(this, NewPurchaseActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
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
                MainActivity.db.open();
                for (int i : selections) {
                    MainActivity.db.deleteTransaction(idDataset.get(i));
                    totalSum -= priceDataset.get(i);
                    mAdapter.removeData(i);
                    idDataset.remove(i);
                }
                MainActivity.db.close();
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
