package com.nosfistis.mike.refene;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class RefenesActivity extends AppCompatActivity implements ActionMode.Callback {
    private static final int NEW_PURCHASE_REQUEST = 1;
    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mAdapter;
    private ArrayList<String> nameDataset = new ArrayList<>();
    private ArrayList<Float> totalDataset = new ArrayList<>();
    private ArrayList<Integer> idDataset = new ArrayList<>();
    private ArrayList<Float> ownedDataset = new ArrayList<>();
    private float totalSum;
    private String refID;
    private ActionMode actionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.refenes_activity);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        Intent intent = getIntent();
        DatabaseHandler db = MainActivity.db;
        refID = intent.getStringExtra("refid");
        setTitle(refID);

        if (refID != null) {
            db.open();
            List<String[]> data = db.getAllRefeneTransactions(refID);
            db.close();
            Log.d("APP", "Refid: " + refID);

            for (int i = 0; i < data.size(); i++) {
                String[] row = data.get(i);
                float total = Float.parseFloat(row[1]);
                nameDataset.add(row[0]);
                totalDataset.add(total);
                idDataset.add(Integer.parseInt(row[2]));

                totalSum += total;
            }
        } else {
            Log.d("APP", "No refID");
            db.open();
            refID = db.addRefene() + "";
            db.close();

            totalSum = 0;
        }
        ((TextView) findViewById(R.id.totalText)).setText(totalSum + "");

        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.abc_list_divider_mtrl_alpha)));
        GestureDetector mGestureDetector = new GestureDetector(this, new RecyclerViewOnGestureListener());
        mAdapter = new RecyclerViewAdapter(nameDataset, totalDataset, ownedDataset, mGestureDetector);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(mAdapter);

        calculateOwned();
    }

    @Override
    public void onRestart() {
        super.onRestart();
    }

    private void calculateOwned() {
        float share = totalSum / nameDataset.size();
        ownedDataset.clear();
        for (float t : totalDataset) {
            ownedDataset.add(share - t);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_refenes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {
            Intent intent = new Intent(this, NewPurchaseActivity.class);
            intent.putExtra("refID", refID);
            startActivityForResult(intent, NEW_PURCHASE_REQUEST);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NEW_PURCHASE_REQUEST) {
            if (resultCode == RESULT_OK) {

                float newTotal = data.getFloatExtra("newsum", -1);
                String name = data.getStringExtra("name");
                int id = data.getIntExtra("person_id", -1);

                int index = idDataset.indexOf(id);

                if (index == -1) {
                    totalDataset.add(newTotal);
                    nameDataset.add(name);
                    idDataset.add(id);
                    totalSum += newTotal;
                    calculateOwned();

                    //mAdapter.updateData(nameDataset, totalDataset, ownedDataset);
                    //mAdapter.notifyItemInserted(idDataset.size() - 1);
                } else {
                    totalSum -= totalDataset.get(index);
                    totalDataset.set(index, newTotal);
                    totalSum += newTotal;
                    calculateOwned();

                    //mAdapter.updateData(nameDataset, totalDataset, ownedDataset);
                    //mAdapter.notifyItemChanged(index);
                }

                ((TextView) findViewById(R.id.totalText)).setText(totalSum + "");
            }
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
            case R.id.context_menu_edit:
                actionMode.finish();
                return true;
            case R.id.context_menu_delete:
                MainActivity.db.open();
                List<Integer> selections = mAdapter.getSelectedItems();
                for (int i : selections) {
                    MainActivity.db.removePersonFromRefene(idDataset.get(i), refID);
                    totalSum -= totalDataset.get(i);
                    mAdapter.removeData(i);
                    calculateOwned();
                    //TODO: update lists
                }
                MainActivity.db.close();
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
                TextView nameView = (TextView) view.findViewById(R.id.nameText);
                Intent intent = new Intent(view.getContext(), PersonalBidsActivity.class);
                intent.putExtra("person", nameView.getText().toString());
                intent.putExtra("id", idDataset.get(mRecyclerView.getChildAdapterPosition(view)));
                intent.putExtra("refID", refID);
                startActivity(intent);
            }
            return super.onSingleTapConfirmed(e);
        }

        public void onLongPress(MotionEvent e) {
            if (actionMode != null) {
                return;
            }

            actionMode = startActionMode(RefenesActivity.this);

            View view = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
            mAdapter.toggleSelection(mRecyclerView.getChildAdapterPosition(view));

            super.onLongPress(e);
        }
    }
}
