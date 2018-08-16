package com.nosfistis.mike.refene;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

import com.nosfistis.mike.refene.shared.RecyclerViewAdapter;
import com.nosfistis.mike.refene.database.DatabaseHandler;
import com.nosfistis.mike.refene.refene.RefenesActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements ActionMode.Callback {

    private static final int NEW_REFENES = 1;

    private static DatabaseHandler db;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter viewAdapter;
    private ActionMode actionMode;
    private List<Long> refenesList;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(this, RefenesActivity.class);
            intent.putExtra("refID", -1);
            startActivityForResult(intent, NEW_REFENES);
        });

        recyclerView = findViewById(R.id.my_recycler_view);

        db = new DatabaseHandler(this);
        db.open();
        refenesList = db.getAllRefenes();
        db.close();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation()));

        List<String> refenesNameList = new ArrayList<>();
        for (long refenesId : refenesList) {
            refenesNameList.add(String.valueOf(refenesId));
        }

        gestureDetector = new GestureDetector(this, new RecyclerViewOnGestureListener());
        viewAdapter = new RecyclerViewAdapter(refenesNameList, gestureDetector);
        recyclerView.setAdapter(viewAdapter);
        recyclerView.addOnItemTouchListener(viewAdapter);
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
                List<Integer> selections = viewAdapter.getSelectedItems();
                for (int i : selections) {
                    db.deleteRefene(refenesList.get(i));
                    viewAdapter.removeData(i);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NEW_REFENES && resultCode == RESULT_OK) {
            refenesList = db.getAllRefenes();

            List<String> refenesNameList = new ArrayList<>();
            for (long refenesId : refenesList) {
                refenesNameList.add(String.valueOf(refenesId));
            }

            viewAdapter = new RecyclerViewAdapter(refenesNameList, gestureDetector);
            recyclerView.swapAdapter(viewAdapter, false);
        }
    }

    private class RecyclerViewOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
            if (view != null) {
                if (actionMode != null) {
                    viewAdapter.toggleSelection(recyclerView.getChildAdapterPosition(view));
                    return super.onSingleTapConfirmed(e);
                }
                Intent intent = new Intent(view.getContext(), RefenesActivity.class);
                intent.putExtra("refID", refenesList.get(recyclerView.getChildAdapterPosition(view)));
                startActivity(intent);
            }
            return super.onSingleTapConfirmed(e);
        }

        public void onLongPress(MotionEvent e) {
            if (actionMode != null) {
                return;
            }

            actionMode = startActionMode(MainActivity.this);

            View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
            viewAdapter.toggleSelection(recyclerView.getChildAdapterPosition(Objects.requireNonNull(view)));

            super.onLongPress(e);
        }
    }

}
