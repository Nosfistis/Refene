package com.nosfistis.mike.refene;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class NewPurchaseActivity extends AppCompatActivity {
	long refID;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_purchase);
		
		String name = getIntent().getStringExtra("name");
		
		if (name != null) {
			EditText nameText = (EditText) findViewById(R.id.personText);
			nameText.setText(name);
		}
		
		refID = getIntent().getLongExtra("refID", -1);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_new_purchase, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
	
	public void submitPurchase(View view) {
		EditText descriptionText = (EditText) findViewById(R.id.descriptionText);
		EditText priceText = (EditText) findViewById(R.id.priceText);
		EditText nameText = (EditText) findViewById(R.id.personText);
		
		String description = descriptionText.getText().toString();
		String name = nameText.getText().toString();
		String price = priceText.getText().toString();
		
		if (TextUtils.getTrimmedLength(nameText.getText()) == 0
				&& TextUtils.getTrimmedLength(priceText.getText()) == 0) {
			finish();
			return;
		} else if (TextUtils.getTrimmedLength(nameText.getText()) == 0) {
			Toast.makeText(NewPurchaseActivity.this, R.string.toast_empty_name, Toast.LENGTH_LONG).show();
			return;
		} else if (TextUtils.getTrimmedLength(priceText.getText()) == 0) {
			Toast.makeText(NewPurchaseActivity.this, R.string.toast_empty_price, Toast.LENGTH_SHORT).show();
			return;
		}
		
		DatabaseHandler db = new DatabaseHandler(this);
		db.open();
		long pid = db.addTransaction(name, description, price, refID);
		db.close();
		
		Intent intent = new Intent();
		intent.putExtra("price", price);
		intent.putExtra("person_id", pid);
		intent.putExtra("description", description);
		if (pid != -1) {
			setResult(RESULT_OK, intent);
		}
		finish();
	}
}
