package com.nosfistis.refene

import android.content.Intent
import android.os.Bundle
import android.view.ActionMode
import android.view.GestureDetector
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat

open class PersonalBidsActivity : AppCompatActivity(), ActionMode.Callback {
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: RecyclerViewAdapter? = null
    private val descriptionDataset = ArrayList<String?>()
    private val priceDataset = ArrayList<Float>()
    private val idDataset = ArrayList<Long>()
    private var person: String? = null
    private var personId: Long = 0
    private var actionMode: ActionMode? = null
    private var totalSum = 0f
    private var refID: Long = 0
    private var db: DatabaseHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.refenes_activity)

        person = intent.getStringExtra("person")
        title = person
        mRecyclerView = findViewById(R.id.my_recycler_view)

        refID = intent.getLongExtra("refID", -1)
        personId = intent.getLongExtra("id", -1)

        db = DatabaseHandler(this)
        db!!.open()
        val data: List<Transaction> = db!!.getPersonTransactions(personId, refID)
        db!!.close()

        for (transaction in data) {
            val price: Float = transaction.price
            descriptionDataset.add(transaction.description)
            priceDataset.add(price)
            idDataset.add(transaction.id)
            totalSum += price
        }

        val layoutManager = LinearLayoutManager(this)

        mRecyclerView!!.setHasFixedSize(true)
        mRecyclerView!!.setLayoutManager(layoutManager)
        mRecyclerView!!.addItemDecoration(
            DividerItemDecoration(
                mRecyclerView!!.context,
                layoutManager.orientation
            )
        )
        val mGestureDetector = GestureDetector(this, RecyclerViewOnGestureListener())
        mAdapter = RecyclerViewAdapter(descriptionDataset, priceDataset, mGestureDetector)
        mRecyclerView!!.setAdapter(mAdapter)
        mRecyclerView!!.addOnItemTouchListener(mAdapter!!)

        updateCalculations()
    }

    private fun updateRecords() {
        db!!.open()
        val transactions: List<Transaction> =
            db!!.getPersonTransactions(personId, refID)
        db!!.close()

        for (transaction in transactions) {
            val index = idDataset.indexOf(transaction.id)
            if (index == -1) {
                val price: Float = transaction.price
                descriptionDataset.add(transaction.description)
                priceDataset.add(price)
                idDataset.add(transaction.id)
                totalSum += price
            }
        }
        mAdapter!!.notifyDataSetChanged()
        updateCalculations()
    }

    private fun updateCalculations() {
        val numberFormat = NumberFormat.getCurrencyInstance()
        (findViewById<TextView>(R.id.totalText)!!).text =
            numberFormat.format(totalSum.toDouble())
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_personal_bids, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

//        if (id == R.id.home) {
//            NavUtils.navigateUpFromSameTask(this)
//            return true
//        }

        return super.onOptionsItemSelected(item)
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null && requestCode == NEW_PURCHASE_REQUEST && resultCode == RESULT_OK) {
            personId = data.getLongExtra("person_id", -1)
            updateRecords()
        }
    }

    override fun onCreateActionMode(actionMode: ActionMode, menu: Menu): Boolean {
        val inflater = actionMode.menuInflater
        inflater.inflate(R.menu.context_menu, menu)
        return true
    }

    override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu): Boolean {
        return false
    }

    override fun onActionItemClicked(actionMode: ActionMode, menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.context_menu_delete -> {
                val selections: List<Int> = mAdapter!!.getSelectedItems()
                db!!.open()
                for (i in selections) {
                    db!!.deleteTransaction(idDataset[i])
                    totalSum -= priceDataset[i]
                    mAdapter!!.removeData(i)
                    idDataset.removeAt(i)
                }
                db!!.close()
                updateCalculations()
                actionMode.finish()
                return true
            }

            else -> return false
        }
    }

    override fun onDestroyActionMode(actionMode: ActionMode) {
        this.actionMode = null
    }

    fun onAddButtonClick() {
        val intent = Intent(this, NewPurchaseActivity::class.java)
        intent.putExtra("refID", refID)
        intent.putExtra("name", person)
        startActivityForResult(intent, NEW_PURCHASE_REQUEST)
    }

    private inner class RecyclerViewOnGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            val view = mRecyclerView?.findChildViewUnder(e.x, e.y)

            if (view != null) {
                if (actionMode != null) {
                    mAdapter!!.toggleSelection(mRecyclerView!!.getChildAdapterPosition(view))
                    return super.onSingleTapConfirmed(e)
                }
            }
            return super.onSingleTapConfirmed(e)
        }

        override fun onLongPress(e: MotionEvent) {
            if (actionMode != null) {
                return
            }

            actionMode = startActionMode(this@PersonalBidsActivity)

            val view = mRecyclerView!!.findChildViewUnder(e.x, e.y)

            if (view != null) {
                mAdapter!!.toggleSelection(mRecyclerView!!.getChildAdapterPosition(view))
            }

            super.onLongPress(e)
        }
    }

    companion object {
        private const val NEW_PURCHASE_REQUEST = 1
    }
}
