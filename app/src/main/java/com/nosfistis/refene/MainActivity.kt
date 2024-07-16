package com.nosfistis.refene

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Objects

class MainActivity : AppCompatActivity(), ActionMode.Callback {
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: RecyclerViewAdapter? = null
    private var actionMode: ActionMode? = null
    private var refenesList: List<Long>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this, RefenesActivity::class.java)
            intent.putExtra("refID", -1)
            startActivityForResult(intent, NEW_REFENES)
        }

        mRecyclerView = findViewById(R.id.my_recycler_view)

        db = DatabaseHandler(this)
        db!!.open()
        refenesList = db!!.allRefenes
        db!!.close()

        val layoutManager = LinearLayoutManager(this)
        mRecyclerView!!.setHasFixedSize(true)
        mRecyclerView!!.setLayoutManager(layoutManager)
        mRecyclerView!!.addItemDecoration(
            DividerItemDecoration(
                mRecyclerView!!.context,
                layoutManager.orientation
            )
        )

        val refenesNameList: MutableList<String?> = ArrayList()
        for (refenesId in refenesList!!) {
            refenesNameList.add(refenesId.toString())
        }

        val mGestureDetector = GestureDetector(this, RecyclerViewOnGestureListener())
        mAdapter = RecyclerViewAdapter(refenesNameList, mGestureDetector)
        mRecyclerView!!.setAdapter(mAdapter)
        mRecyclerView!!.addOnItemTouchListener(mAdapter!!)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
//
//        if (id == R.id.action_settings) {
//            return true
//        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateActionMode(actionMode: ActionMode, menu: Menu): Boolean {
        // Inflate a menu resource providing context menu items
        actionMode.menuInflater.inflate(R.menu.context_menu, menu)
        return true
    }

    override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu): Boolean {
        return false
    }

    override fun onActionItemClicked(actionMode: ActionMode, menuItem: MenuItem): Boolean {
        Log.v("Main", "Inflate")
        when (menuItem.itemId) {
            R.id.context_menu_edit -> {
                actionMode.finish()
                return true
            }

            R.id.context_menu_delete -> {
                db!!.open()
                val selections: List<Int> = mAdapter!!.getSelectedItems()
                for (i in selections) {
                    db!!.deleteRefene(refenesList!![i])
                    mAdapter!!.removeData(i)
                    //TODO: update refenesList
                }
                db!!.close()
                actionMode.finish()
                return true
            }

            else -> return false
        }
    }

    override fun onDestroyActionMode(actionMode: ActionMode) {
        this.actionMode = null
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == NEW_REFENES && resultCode == RESULT_OK) {
            refenesList = db!!.allRefenes
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
                val intent = Intent(view.context, RefenesActivity::class.java)
                intent.putExtra(
                    "refID",
                    refenesList!![mRecyclerView!!.getChildAdapterPosition(view)]
                )
                startActivity(intent)
            }
            return super.onSingleTapConfirmed(e)
        }

        override fun onLongPress(e: MotionEvent) {
            if (actionMode != null) {
                return
            }

            actionMode = startSupportActionMode(this@MainActivity)

            val view = mRecyclerView!!.findChildViewUnder(e.x, e.y)

            if (view != null) {
                mAdapter!!.toggleSelection(
                    mRecyclerView!!.getChildAdapterPosition(
                        Objects.requireNonNull(
                            view
                        )
                    )
                )
            }

            super.onLongPress(e)
        }
    }

    companion object {
        private const val NEW_REFENES = 1

        private var db: DatabaseHandler? = null
    }
}
