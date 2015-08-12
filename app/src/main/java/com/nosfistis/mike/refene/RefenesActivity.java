package com.nosfistis.mike.refene;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class RefenesActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<String> nameDataset = new ArrayList<>();
    private ArrayList<Float> totalDataset = new ArrayList<>();
    private ArrayList<Integer> idDataset = new ArrayList<>();
    private float totalSum;
    private String refID;

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

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.abc_list_divider_mtrl_alpha)));

        // set on item click listener
        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        TextView nameView = (TextView) view.findViewById(R.id.name);
                        Intent intent = new Intent(view.getContext(), PersonalBidsActivity.class);
                        intent.putExtra("person", nameView.getText().toString());
                        intent.putExtra("id", idDataset.get(position));
                        intent.putExtra("refID", refID);
                        startActivity(intent);
                    }
                })
        );

        mAdapter = new RecyclerView.Adapter<ViewHolder>() {
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // create a new view
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recycler_view, parent, false);
                // set the view's size, margins, paddings and layout parameters

                ViewHolder vh = new ViewHolder(v);
                return vh;
            }

            @Override
            public void onBindViewHolder(ViewHolder holder, int position) {
                // - get element from your dataset at this position
                // - replace the contents of the view with that element
                holder.nameView.setText(nameDataset.get(position));
                holder.totalView.setText(totalDataset.get(position).toString());
            }

            @Override
            public int getItemCount() {
                return nameDataset.size();
            }
        };
        mRecyclerView.setAdapter(mAdapter);
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView nameView;
        public TextView totalView;

        public ViewHolder(View v) {
            super(v);
            nameView = (TextView) v.findViewById(R.id.name);
            totalView = (TextView) v.findViewById(R.id.total);
        }
    }

    @Override
    public void onRestart() {
        Log.d("APP", "Activity restarts");
        super.onRestart();
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
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
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
                } else {
                    totalSum -= totalDataset.get(index);
                    totalDataset.set(index, newTotal);
                    totalSum += newTotal;
                }

                mAdapter.notifyItemChanged(index);
            }
        }
    }
}
