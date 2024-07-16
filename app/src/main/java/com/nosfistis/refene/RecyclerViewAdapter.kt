package com.nosfistis.refene

import android.util.SparseBooleanArray
import android.view.GestureDetector
import android.view.LayoutInflater.*
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat

class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.ListItemViewHolder?>,
    RecyclerView.OnItemTouchListener {
    private val selectedItems = SparseBooleanArray()
    private var textColumnData: MutableList<String?>
    private var firstNumberColumnData: MutableList<Float>? = null
    private var secondNumberColumnData: MutableList<Float>? = null
    private var gestureDetector: GestureDetector

    internal constructor(nameData: MutableList<String?>?, gestureDetector: GestureDetector) {
        requireNotNull(nameData) { "nameData must not be null" }

        this.gestureDetector = gestureDetector
        this.textColumnData = nameData
    }

    internal constructor(
        nameData: MutableList<String?>?,
        totalData: MutableList<Float>?,
        ownedData: MutableList<Float>?,
        gestureDetector: GestureDetector
    ) {
        requireNotNull(nameData) { "nameData must not be null" }
        requireNotNull(totalData) { "totalData must not be null" }

        this.gestureDetector = gestureDetector
        this.textColumnData = nameData
        this.secondNumberColumnData = ownedData
        this.firstNumberColumnData = totalData
    }

    internal constructor(
        descriptionData: MutableList<String?>?,
        priceData: MutableList<Float>?,
        gestureDetector: GestureDetector
    ) {
        requireNotNull(descriptionData) { "descriptionData must not be null" }
        requireNotNull(priceData) { "priceData must not be null" }

        this.gestureDetector = gestureDetector
        this.textColumnData = descriptionData
        this.firstNumberColumnData = priceData
    }

    fun toggleSelection(pos: Int) {
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos)
        } else {
            selectedItems.put(pos, true)
        }
        notifyItemChanged(pos)
    }

    fun removeData(position: Int) {
        textColumnData.removeAt(position)
        firstNumberColumnData?.removeAt(position)
        secondNumberColumnData?.removeAt(position)
        notifyItemRemoved(position)
        selectedItems.clear()
    }

    fun getSelectedItems(): List<Int> {
        val items: MutableList<Int> = ArrayList<Int>(selectedItems.size())
        for (i in selectedItems.size() - 1 downTo 0) {
            items.add(selectedItems.keyAt(i))
        }
        return items
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListItemViewHolder {
        val v: View = from(parent.context)
            .inflate(R.layout.recycler_view, parent, false)
        return ListItemViewHolder(v)
    }

    override fun getItemCount() = textColumnData.size

    override fun onBindViewHolder(holder: ListItemViewHolder, position: Int) {
        holder.textColumnView.text = textColumnData[position]
        holder.itemView.setActivated(selectedItems.get(position))
        if (firstNumberColumnData != null) {
            val numberFormat = NumberFormat.getCurrencyInstance()
            holder.firstNumberColumnView.text =
                numberFormat.format(firstNumberColumnData!![position])
        }
        if (secondNumberColumnData != null) {
            val numberFormat = NumberFormat.getCurrencyInstance()
            holder.secondNumberColumnView.text =
                numberFormat.format(secondNumberColumnData!![position])
        }
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(e)
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
    }

    class ListItemViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val secondNumberColumnView: TextView = v.findViewById<View>(R.id.totalOwnedText) as TextView
        val textColumnView: TextView = v.findViewById<View>(R.id.nameText) as TextView
        val firstNumberColumnView: TextView =
            v.findViewById<View>(R.id.personalTotalText) as TextView
    }
}
