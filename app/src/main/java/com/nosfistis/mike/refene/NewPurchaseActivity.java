package com.nosfistis.mike.refene;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class NewPurchaseActivity extends AppCompatActivity {
    String refID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_purchase);

        refID = getIntent().getStringExtra("refID");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_purchase, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }

    public void submitPurchase(View view) {
        EditText descriptionText = (EditText) findViewById(R.id.descriptionText);
        EditText priceText = (EditText) findViewById(R.id.priceText);
        EditText nameText = (EditText) findViewById(R.id.personText);

        DatabaseHandler db = MainActivity.db;
        db.open();
        int pid = (int) db.addTransaction(nameText.getText().toString(),
                descriptionText.getText().toString(),
                priceText.getText().toString(),
                refID);
        float sum = db.getPersonSum(pid, Integer.parseInt(refID));
        db.close();

        Intent intent = new Intent();
        intent.putExtra("newsum", sum);
        intent.putExtra("person_id", pid);
        intent.putExtra("name", nameText.getText().toString());
        if (pid != -1) {
            setResult(RESULT_OK, intent);
        }
        finish();
    }
}
