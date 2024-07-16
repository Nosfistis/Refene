package com.nosfistis.refene

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ActionMode
import android.view.GestureDetector
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat

class RefenesActivity : AppCompatActivity(), ActionMode.Callback {
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: RecyclerViewAdapter? = null
    private val nameDataset = ArrayList<String?>()
    private val totalDataset = ArrayList<Float>()
    private val idDataset = ArrayList<Long>()
    private val ownedDataset = ArrayList<Float>()
    private var totalSum = 0f
    private var refID: Long = 0
    private var actionMode: ActionMode? = null
    private var db: DatabaseHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.refenes_activity)
        mRecyclerView = findViewById(R.id.my_recycler_view)

        db = DatabaseHandler(this)

        refID = intent.getLongExtra("refID", -1)

        if (refID != -1L) {
            db!!.open()
            val data: List<Transaction> =
                db!!.getAllRefeneTransactions(refID)
            db!!.close()

            for (transaction in data) {
                val person = transaction.getPerson()
                if (person != null) {
                    nameDataset.add(person.name)
                    idDataset.add(person.id)
                }
                totalDataset.add(transaction.price)

                totalSum += transaction.price
            }
        } else {
            db!!.open()
            refID = db!!.addRefene()
            db!!.close()
        }

        title = refID.toString()

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
        mAdapter = RecyclerViewAdapter(nameDataset, totalDataset, ownedDataset, mGestureDetector)
        mRecyclerView!!.setAdapter(mAdapter)
        mRecyclerView!!.addOnItemTouchListener(mAdapter!!)

        updateCalculations()
    }

    override fun onResume() {
        super.onResume()
        updateRecords()
    }

    private fun updateRecords() {
        db!!.open()
        val transactions: List<Transaction> =
            db!!.getAllRefeneTransactions(refID)
        db!!.close()

        totalSum = 0f

        for (transaction in transactions) {
            val index = idDataset.indexOf(transaction.getPerson()!!.id)
            if (index == -1) {
                nameDataset.add(transaction.getPerson()!!.name)
                totalDataset.add(transaction.price)
                idDataset.add(transaction.getPerson()!!.id)
            } else {
                nameDataset[index] = transaction.getPerson()!!.name
                totalDataset[index] = transaction.price
                idDataset[index] = transaction.getPerson()!!.id
            }

            totalSum += transaction.price
        }
        updateCalculations()
    }

    private fun updateCalculations() {
        val share = totalSum / nameDataset.size
        ownedDataset.clear()
        for (t in totalDataset) {
            ownedDataset.add(share - t)
        }
        mAdapter!!.notifyDataSetChanged()
        val numberFormat = NumberFormat.getCurrencyInstance()
        (findViewById<TextView>(R.id.totalText)!!).text = numberFormat.format(totalSum.toDouble())
    }

    fun onAddButtonClick(view: View?) {
        val intent: Intent = Intent(this, NewPurchaseActivity::class.java)
        intent.putExtra("refID", refID)
        startActivityForResult(intent, NEW_PURCHASE_REQUEST)
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == NEW_PURCHASE_REQUEST && resultCode == RESULT_OK) {
            updateRecords()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_refenes, menu)
        return true
    }

    override fun onCreateActionMode(actionMode: ActionMode, menu: Menu): Boolean {
        val inflater = actionMode.menuInflater
        inflater.inflate(R.menu.context_menu, menu)
        return true
    }

    override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu): Boolean {
        return false
    }

    override fun onDestroyActionMode(actionMode: ActionMode) {
        this.actionMode = null
    }

    override fun onActionItemClicked(actionMode: ActionMode, menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.context_menu_edit -> {
                actionMode.finish()
                return true
            }

            R.id.context_menu_delete -> {
                db!!.open()
                val selections: List<Int> = mAdapter!!.getSelectedItems()
                for (i in selections) {
                    db!!.removePersonFromRefene(idDataset[i], refID)
                    totalSum -= totalDataset[i]
                    mAdapter!!.removeData(i)
                    idDataset.removeAt(i)
                    updateCalculations()
                }
                db!!.close()
                actionMode.finish()
                return true
            }

            else -> return false
        }
    }

    private inner class RecyclerViewOnGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            val view = mRecyclerView!!.findChildViewUnder(e.x, e.y)
            if (view != null) {
                if (actionMode != null) {
                    mAdapter!!.toggleSelection(mRecyclerView!!.getChildAdapterPosition(view))
                    return super.onSingleTapConfirmed(e)
                }
                val nameView = view.findViewById<View>(R.id.nameText) as TextView
                val intent = Intent(view.context, PersonalBidsActivity::class.java)
                intent.putExtra("person", nameView.text.toString())
                intent.putExtra("id", idDataset[mRecyclerView!!.getChildAdapterPosition(view)])
                intent.putExtra("refID", refID)
                startActivity(intent)
            }
            return super.onSingleTapConfirmed(e)
        }

        override fun onLongPress(e: MotionEvent) {
            if (actionMode != null) {
                return
            }

            actionMode = startActionMode(this@RefenesActivity)

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
