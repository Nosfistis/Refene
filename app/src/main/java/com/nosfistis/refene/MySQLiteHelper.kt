package com.nosfistis.refene

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log


class MySQLiteHelper
/**
 * Create a helper object to create, open, and/or manage a database.
 * This method always returns very quickly.  The database is not actually
 * created or opened until one of [.getWritableDatabase] or
 * [.getReadableDatabase] is called.
 *
 * @param context to use to open or create the database
 */(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    private val DATABASE_TRANSACTIONS_CREATE =
        (("create table " + TRANSACTIONS_TABLE + "("
                + COLUMN_ID + " integer primary key autoincrement, "
                + COLUMN_RID + " integer, "
                + COLUMN_PID + " integer, "
                + COLUMN_DESCRIPTION + " text default '" + R.string.untitled).toString() + "', "
                + COLUMN_PRICE + " float not null, "
                + COLUMN_TIME + " datetime default current_timestamp, " //+ COLUMN_MODTIME + " datetime on update current_timestamp, "
                + "foreign key (" + COLUMN_PID + ") references " + PEOPLE_TABLE + " (" + COLUMN_PID + "), "
                + "foreign key (" + COLUMN_RID + ") references " + REFENES_TABLE + " (" + COLUMN_RID + ") "
                + ");")

    private val DATABASE_PEOPLE_CREATE = ("create table " + PEOPLE_TABLE + "("
            + COLUMN_PID + " integer primary key autoincrement, "
            + COLUMN_NAME + " text"
            + ");")

    private val DATABASE_REFENES_CREATE = ("create table " + REFENES_TABLE + "("
            + COLUMN_RID + " integer primary key autoincrement, "
            + COLUMN_MODTIME + " integer "
            + ");")

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(DATABASE_REFENES_CREATE)
        db.execSQL(DATABASE_TRANSACTIONS_CREATE)
        db.execSQL(DATABASE_PEOPLE_CREATE)
    }

    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     * The SQLite ALTER TABLE documentation can be found
     * [here](http://sqlite.org/lang_altertable.html). If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.w(
            APPTAG,
            "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data"
        )
        db.execSQL("DROP TABLE IF EXISTS " + REFENES_TABLE)
        db.execSQL("DROP TABLE IF EXISTS " + TRANSACTIONS_TABLE)
        db.execSQL("DROP TABLE IF EXISTS " + PEOPLE_TABLE)
        onCreate(db)
    }

    companion object {
        const val APPTAG = "Refenes"
        const val TRANSACTIONS_TABLE: String = "transactions"
        const val COLUMN_ID: String = "transaction_id"
        const val COLUMN_RID: String = "refenes_id"
        const val COLUMN_PID: String = "person_id"
        const val COLUMN_DESCRIPTION: String = "description"
        const val COLUMN_PRICE: String = "price"
        const val COLUMN_TIME: String = "creation_time"
        const val COLUMN_MODTIME: String = "modification_time"
        const val PEOPLE_TABLE: String = "person"
        const val COLUMN_NAME: String = "name"
        const val REFENES_TABLE: String = "refenes"
        private const val DATABASE_NAME = "refenes.db"
        private const val DATABASE_VERSION = 1
    }
}