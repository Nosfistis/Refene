package com.nosfistis.mike.refene;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ActionMode.Callback {

    private static DatabaseHandler db;
    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mAdapter;
    private ActionMode actionMode;
    private List<Long> refenesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mRecyclerView = findViewById(R.id.my_recycler_view);

        db = new DatabaseHandler(this);
        db.open();
        refenesList = db.getAllRefenes();
        db.close();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(),
                layoutManager.getOrientation()));

        List<String> refenesNameList = new ArrayList<>();
        for (long refenesId : refenesList) {
            refenesNameList.add(String.valueOf(refenesId));
        }

        GestureDetector mGestureDetector = new GestureDetector(this, new RecyclerViewOnGestureListener());
        mAdapter = new RecyclerViewAdapter(refenesNameList, mGestureDetector);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        // Inflate a menu resource providing context menu items
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
                db.open();
                List<Integer> selections = mAdapter.getSelectedItems();
                for (int i : selections) {
                    db.deleteRefene(refenesList.get(i));
                    mAdapter.removeData(i);
                    //TODO: update refenesList
                }
                db.close();
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
                Intent intent = new Intent(view.getContext(), RefenesActivity.class);
                intent.putExtra("refID", refenesList.get(mRecyclerView.getChildAdapterPosition(view)));
                startActivity(intent);
            }
            return super.onSingleTapConfirmed(e);
        }

        public void onLongPress(MotionEvent e) {
            if (actionMode != null) {
                return;
            }

            actionMode = startActionMode(MainActivity.this);

            View view = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
            mAdapter.toggleSelection(mRecyclerView.getChildAdapterPosition(view));

            super.onLongPress(e);
        }
    }

}
