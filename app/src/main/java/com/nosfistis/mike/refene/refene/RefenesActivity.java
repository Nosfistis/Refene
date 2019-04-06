package com.nosfistis.mike.refene.refene;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
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
import com.nosfistis.mike.refene.database.Refene;
import com.nosfistis.mike.refene.shared.RecyclerViewAdapter;
import com.nosfistis.mike.refene.viewmodel.RefeneViewModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Objects;

public class RefenesActivity extends AppCompatActivity implements ActionMode.Callback {

    public static final String REFENE_ID = "refId";

    private static final int NEW_PURCHASE_REQUEST = 1;

    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mAdapter;
    private ArrayList<String> nameDataset = new ArrayList<>();
    private ArrayList<Float> totalDataset = new ArrayList<>();
    private ArrayList<Long> idDataset = new ArrayList<>();
    private ArrayList<Float> ownedDataset = new ArrayList<>();
    private float totalSum = 0;
    private long refID;
    private ActionMode actionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.refenes_activity);
        mRecyclerView = findViewById(R.id.my_recycler_view);

        RefeneViewModel refeneViewModel = ViewModelProviders.of(this).get(RefeneViewModel.class);

        Intent intent = getIntent();
        refID = intent.getLongExtra(RefenesActivity.REFENE_ID, (long) -1);

        Refene refene = null;

        if (refID == -1) {
            throw new IllegalArgumentException("Refene id not provided");
        }

        refene = refeneViewModel.getRefeneById(refID);

//            List<Transaction> data = db.getAllRefeneTransactions(refID);
//            db.close();
//
//            for (Transaction transaction : data) {
//                nameDataset.add(transaction.getPerson().getName());
//                totalDataset.add(transaction.getPrice());
//                idDataset.add(transaction.getPerson().getId());
//
//                totalSum += transaction.getPrice();
//            }

        setTitle(String.valueOf(refID));

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), layoutManager.getOrientation()));
        GestureDetector mGestureDetector = new GestureDetector(this, new RecyclerViewOnGestureListener());
        mAdapter = new RecyclerViewAdapter(nameDataset, totalDataset, ownedDataset, mGestureDetector);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(mAdapter);

        updateCalculations();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateRecords();
    }

    private void updateRecords() {
//        db.open();
//        List<Transaction> transactions = db.getAllRefeneTransactions(refID);
//        db.close();
//
//        totalSum = 0;

//        for (Transaction transaction : transactions) {
//            int index = idDataset.indexOf(transaction.getPerson().getId());
//            if (index == -1) {
//                nameDataset.add(transaction.getPerson().getName());
//                totalDataset.add(transaction.getPrice());
//                idDataset.add(transaction.getPerson().getId());
//            } else {
//                nameDataset.set(index, transaction.getPerson().getName());
//                totalDataset.set(index, transaction.getPrice());
//                idDataset.set(index, transaction.getPerson().getId());
//            }
//
//            totalSum += transaction.getPrice();
//        }
        updateCalculations();
    }

    private void updateCalculations() {
        float share = totalSum / nameDataset.size();
        ownedDataset.clear();
        for (float t : totalDataset) {
            ownedDataset.add(share - t);
        }
        mAdapter.notifyDataSetChanged();
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance();
        ((TextView) findViewById(R.id.totalText)).setText(numberFormat.format(totalSum));
    }

    public void onAddButtonClick(View view) {
        Intent intent = new Intent(this, NewPurchaseActivity.class);
        intent.putExtra(NewPurchaseActivity.REFENE_ID, refID);
        startActivityForResult(intent, NEW_PURCHASE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NEW_PURCHASE_REQUEST && resultCode == RESULT_OK) {
            updateRecords();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_refenes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
    public void onDestroyActionMode(ActionMode actionMode) {
        this.actionMode = null;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            // TODO: Edit refenes name
            case R.id.context_menu_edit:
                actionMode.finish();
                return true;
            // Delete selected items from list and database
            case R.id.context_menu_delete:
//                db.open();
//                List<Integer> selections = mAdapter.getSelectedItems();
//                for (int i : selections) {
//                    db.removePersonFromRefene(idDataset.get(i), refID);
//                    totalSum -= totalDataset.get(i);
//                    mAdapter.removeData(i);
//                    idDataset.remove(i);
//                    updateCalculations();
//                }
//                db.close();
                actionMode.finish();
                return true;
            default:
                return false;
        }
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
                TextView nameView = view.findViewById(R.id.nameText);
                Intent intent = new Intent(view.getContext(), PersonalBidsActivity.class);
                intent.putExtra("person", nameView.getText().toString());
                intent.putExtra("id", idDataset.get(mRecyclerView.getChildAdapterPosition(view)));
                intent.putExtra(RefenesActivity.REFENE_ID, refID);
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
            mAdapter.toggleSelection(mRecyclerView.getChildAdapterPosition(Objects.requireNonNull(view)));

            super.onLongPress(e);
        }
    }
}
