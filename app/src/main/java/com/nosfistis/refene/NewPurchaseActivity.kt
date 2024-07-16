package com.nosfistis.refene

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class NewPurchaseActivity : AppCompatActivity() {
    private var refID: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_purchase)

        val name = intent.getStringExtra("name")

        if (name != null) {
            val nameText: EditText = findViewById(R.id.personText)
            nameText.setText(name)
        }

        refID = intent.getLongExtra("refID", -1)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_new_purchase, menu)
        return true
    }

    fun onOptionsItemSelected(item: MenuItem?): Boolean? {
        return item?.let { super.onOptionsItemSelected(it) }
    }

    fun submitPurchase(view: View?) {
        val descriptionText: EditText = findViewById(R.id.descriptionText)
        val priceText: EditText = findViewById(R.id.priceText)
        val nameText: EditText = findViewById(R.id.personText)

        val description: String = descriptionText.getText().toString()
        val name: String = nameText.getText().toString()
        val price: String = priceText.getText().toString()

        if (TextUtils.getTrimmedLength(nameText.getText()) == 0
            && TextUtils.getTrimmedLength(priceText.getText()) == 0
        ) {
            finish()
            return
        } else if (TextUtils.getTrimmedLength(nameText.getText()) == 0) {
            Toast.makeText(this@NewPurchaseActivity, R.string.toast_empty_name, Toast.LENGTH_LONG)
                .show()
            return
        } else if (TextUtils.getTrimmedLength(priceText.getText()) == 0) {
            Toast.makeText(this@NewPurchaseActivity, R.string.toast_empty_price, Toast.LENGTH_SHORT)
                .show()
            return
        }

        val db = DatabaseHandler(this)
        db.open()
        val pid = db.addTransaction(name, description, price, refID)
        db.close()

        val intent = Intent()
        intent.putExtra("price", price)
        intent.putExtra("person_id", pid)
        intent.putExtra("description", description)
        if (pid != -1L) {
            setResult(RESULT_OK, intent)
        }
        finish()
    }
}
