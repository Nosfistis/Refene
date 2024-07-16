package com.nosfistis.refene

import android.content.ContentValues
import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase

/**
 * Created by Mike on 8/8/2015.
 */
class DatabaseHandler(context: Context?) {
    private val dbHelper = MySQLiteHelper(context)
    private var database: SQLiteDatabase? = null

    @Throws(SQLException::class)
    fun open() {
        database = dbHelper.writableDatabase
    }

    fun close() {
        dbHelper.close()
    }

    /**
     * Inserts a new person to the database.
     *
     * @param name The person's name.
     * @return The database insertion unique ID.
     */
    fun addPerson(name: String?): Long {
        val values = ContentValues()
        values.put(MySQLiteHelper.COLUMN_NAME, name)
        return database!!.insert(MySQLiteHelper.PEOPLE_TABLE, null, values)
    }

    /**
     * Inserts a blank transaction group in the database.
     *
     * @return Returns the insertion unique database ID.
     */
    fun addRefene(): Long {
        return database!!.insert(
            MySQLiteHelper.REFENES_TABLE,
            MySQLiteHelper.COLUMN_MODTIME,
            null
        )
    }

    /**
     * Inserts a new transaction to the database.
     *
     * @param name        The person's name.
     * @param description The payment's description. Optional.
     * @param price       The payment's price.
     * @param refID       The unique database ID of the transaction group.
     * @return Returns the insertion unique database ID.
     */
    fun addTransaction(name: String, description: String?, price: String?, refID: Long): Long {
        val cursor = database!!.query(
            MySQLiteHelper.PEOPLE_TABLE,
            arrayOf(MySQLiteHelper.COLUMN_PID),
            "name=?",
            arrayOf(name), null, null, null
        )

        val pid: Long
        if (cursor.count == 0) {
            pid = addPerson(name)
        } else {
            cursor.moveToFirst()
            val columnPid = cursor.getColumnIndex(MySQLiteHelper.COLUMN_PID)

            if (columnPid > 0) {
                pid = cursor.getInt(columnPid).toLong()
            } else {
                pid = 0
            }
        }

        cursor.close()

        val values = ContentValues()
        values.put(MySQLiteHelper.COLUMN_RID, refID)
        values.put(MySQLiteHelper.COLUMN_DESCRIPTION, description)
        values.put(MySQLiteHelper.COLUMN_PRICE, price)
        values.put(MySQLiteHelper.COLUMN_PID, pid)

        database!!.insert(MySQLiteHelper.TRANSACTIONS_TABLE, null, values)

        return pid
    }

    /**
     * Deletes a particular group's details and transactions from all tables
     *
     * @param refID The unique database ID of the transaction group.
     */
    fun deleteRefene(refID: Long) {
        database!!.delete(
            MySQLiteHelper.TRANSACTIONS_TABLE, MySQLiteHelper.COLUMN_RID + "=?",
            arrayOf(refID.toString())
        )
        database!!.delete(
            MySQLiteHelper.REFENES_TABLE, MySQLiteHelper.COLUMN_RID + "=?",
            arrayOf(refID.toString())
        )
    }

    fun removePersonFromRefene(pid: Long, refID: Long) {
        database!!.delete(
            MySQLiteHelper.TRANSACTIONS_TABLE,
            MySQLiteHelper.COLUMN_RID + "=? AND " + MySQLiteHelper.COLUMN_PID + "=?",
            arrayOf(refID.toString(), pid.toString())
        )
        val cursor = database!!.query(
            MySQLiteHelper.TRANSACTIONS_TABLE,
            arrayOf(MySQLiteHelper.COLUMN_ID),
            MySQLiteHelper.COLUMN_PID + "=?",
            arrayOf(pid.toString()),
            null,
            null,
            null
        )

        if (cursor.count == 0) {
            database!!.delete(
                MySQLiteHelper.PEOPLE_TABLE, MySQLiteHelper.COLUMN_PID + "=?",
                arrayOf(pid.toString())
            )
        }
        cursor.close()
    }

    fun deleteTransaction(id: Long) {
        database!!.delete(
            MySQLiteHelper.TRANSACTIONS_TABLE, MySQLiteHelper.COLUMN_ID + "=?",
            arrayOf(id.toString())
        )
    }

    /**
     * Returns all transactions and summed information about them connected to a group.
     *
     * @param refID The unique database ID of the transaction group.
     * @return A list of results containing transactions which
     * include person names, database id's and payments.
     */
    fun getAllRefeneTransactions(refID: Long): List<Transaction> {
        val cursor = database!!.rawQuery(
            "SELECT " + MySQLiteHelper.COLUMN_NAME
                    + ", " + MySQLiteHelper.PEOPLE_TABLE + "." + MySQLiteHelper.COLUMN_PID
                    + ", SUM(" + MySQLiteHelper.COLUMN_PRICE + ") AS " + MySQLiteHelper.COLUMN_PRICE
                    + " FROM " + MySQLiteHelper.TRANSACTIONS_TABLE
                    + " INNER JOIN " + MySQLiteHelper.PEOPLE_TABLE
                    + " ON " + MySQLiteHelper.TRANSACTIONS_TABLE + "." + MySQLiteHelper.COLUMN_PID + "=" + MySQLiteHelper.PEOPLE_TABLE + "." + MySQLiteHelper.COLUMN_PID
                    + " INNER JOIN " + MySQLiteHelper.REFENES_TABLE
                    + " ON " + MySQLiteHelper.TRANSACTIONS_TABLE + "." + MySQLiteHelper.COLUMN_RID + "=" + MySQLiteHelper.REFENES_TABLE + "." + MySQLiteHelper.COLUMN_RID
                    + " WHERE " + MySQLiteHelper.TRANSACTIONS_TABLE + "." + MySQLiteHelper.COLUMN_RID + "=?"
                    + " GROUP BY " + MySQLiteHelper.COLUMN_NAME, arrayOf(refID.toString())
        )

        cursor.moveToFirst()
        val transactions: MutableList<Transaction> = ArrayList<Transaction>()
        while (!cursor.isAfterLast) {
            val person = Person(
                cursor.getInt(cursor.getColumnIndex(MySQLiteHelper.COLUMN_PID) as Int),
                cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_NAME) as Int)
            )
            val transaction = Transaction(
                cursor.getFloat(cursor.getColumnIndex(MySQLiteHelper.COLUMN_PRICE) as Int),
                person
            )
            transactions.add(transaction)
            cursor.moveToNext()
        }

        cursor.close()

        return transactions
    }

    /**
     * Get information about a single transaction.
     *
     * @param id The unique database ID of the transaction.
     * @return An array with the description, price and the person ID that paid for the transaction.
     */
    fun getTransaction(id: Long): Array<String> {
        val cursor = database!!.query(
            MySQLiteHelper.TRANSACTIONS_TABLE,
            arrayOf(
                MySQLiteHelper.COLUMN_DESCRIPTION,
                MySQLiteHelper.COLUMN_PRICE,
                MySQLiteHelper.COLUMN_PID
            ),
            MySQLiteHelper.COLUMN_ID + "=?",
            arrayOf("" + id),
            null,
            null,
            null
        )

        cursor.moveToFirst()
        return arrayOf(
            cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_DESCRIPTION) as Int),
            cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_PRICE) as Int)
        )
    }

    /**
     * @param pid The unique database ID of the person.
     * @return A list of results containing transaction
     * descriptions and payments.
     */
    fun getPersonTransactions(pid: Long, rid: Long): List<Transaction> {
        val cursor = database!!.query(
            MySQLiteHelper.TRANSACTIONS_TABLE,
            arrayOf(
                MySQLiteHelper.COLUMN_DESCRIPTION,
                MySQLiteHelper.COLUMN_PRICE,
                MySQLiteHelper.COLUMN_ID
            ),
            MySQLiteHelper.COLUMN_PID + "=? AND " + MySQLiteHelper.COLUMN_RID + "=?",
            arrayOf(pid.toString(), rid.toString()),
            null,
            null,
            MySQLiteHelper.COLUMN_TIME + " DESC"
        )

        val transactions: MutableList<Transaction> = ArrayList<Transaction>()
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val transaction = Transaction(
                cursor.getLong(cursor.getColumnIndex(MySQLiteHelper.COLUMN_ID) as Int),
                cursor.getFloat(cursor.getColumnIndex(MySQLiteHelper.COLUMN_PRICE) as Int),
                cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_DESCRIPTION) as Int)
            )
            transactions.add(transaction)
            cursor.moveToNext()
        }

        cursor.close()

        return transactions
    }

    /**
     * Returns a person's total payments on a particular group.
     *
     * @param pid The person's unique database ID.
     * @param rid The transaction group's unique database ID.
     * @return The total sum of the person's payments.
     */
    fun getPersonSum(pid: Long, rid: Long): Float {
        val cursor = database!!.rawQuery(
            "SELECT " + MySQLiteHelper.COLUMN_NAME
                    + ", SUM(" + MySQLiteHelper.COLUMN_PRICE + ") AS " + MySQLiteHelper.COLUMN_PRICE
                    + " FROM " + MySQLiteHelper.TRANSACTIONS_TABLE
                    + " INNER JOIN " + MySQLiteHelper.PEOPLE_TABLE
                    + " ON " + MySQLiteHelper.TRANSACTIONS_TABLE + "." + MySQLiteHelper.COLUMN_PID + "=" +
                    MySQLiteHelper.PEOPLE_TABLE + "." + MySQLiteHelper.COLUMN_PID
                    + " INNER JOIN " + MySQLiteHelper.REFENES_TABLE
                    + " ON " + MySQLiteHelper.TRANSACTIONS_TABLE + "." + MySQLiteHelper.COLUMN_RID + "=" +
                    MySQLiteHelper.REFENES_TABLE + "." + MySQLiteHelper.COLUMN_RID
                    + " WHERE " + MySQLiteHelper.TRANSACTIONS_TABLE + "." + MySQLiteHelper.COLUMN_PID + "=? AND "
                    + MySQLiteHelper.TRANSACTIONS_TABLE + "." + MySQLiteHelper.COLUMN_RID + "=?"
                    + " GROUP BY " + MySQLiteHelper.TRANSACTIONS_TABLE + "." + MySQLiteHelper.COLUMN_PID,
            arrayOf(pid.toString(), rid.toString())
        )

        cursor.moveToFirst()
        val sum = cursor.getFloat((cursor.getColumnIndex(MySQLiteHelper.COLUMN_PRICE)).toInt())
        cursor.close()

        return sum
    }

    val allRefenes: List<Long>
        /**
         * @return A list of all the transaction group database ID's.
         */
        get() {
            val cursor = database!!.query(
                MySQLiteHelper.REFENES_TABLE,
                arrayOf(MySQLiteHelper.COLUMN_RID),
                null,
                null,
                null,
                null,
                null
            )

            val refenes: MutableList<Long> = ArrayList()
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                val columnRid = cursor.getColumnIndex(MySQLiteHelper.COLUMN_RID)

                if (columnRid >= 0) {
                    refenes.add(cursor.getLong(columnRid))
                }

                cursor.moveToNext()
            }

            cursor.close()

            return refenes
        }
}
