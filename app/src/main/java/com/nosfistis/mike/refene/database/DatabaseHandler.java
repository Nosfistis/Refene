package com.nosfistis.mike.refene.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.nosfistis.mike.refene.refene.Person;
import com.nosfistis.mike.refene.R;
import com.nosfistis.mike.refene.refene.Transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mike on 8/8/2015.
 */
public class DatabaseHandler {
	private static final String APPTAG = "Refenes";
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	
	public DatabaseHandler(Context context) {
		dbHelper = new MySQLiteHelper(context);
	}
	
	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}
	
	public void close() {
		dbHelper.close();
	}
	
	/**
	 * Inserts a new person to the database.
	 *
	 * @param name The person's name.
	 * @return The database insertion unique ID.
	 */
	public long addPerson(String name) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_NAME, name);
		return database.insert(MySQLiteHelper.PEOPLE_TABLE, null, values);
	}
	
	/**
	 * Inserts a blank transaction group in the database.
	 *
	 * @return Returns the insertion unique database ID.
	 */
	public long addRefene() {
		return database.insert(MySQLiteHelper.REFENES_TABLE, MySQLiteHelper.COLUMN_MODTIME, null);
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
	public long addTransaction(String name, String description, String price, long refID) {
		Cursor cursor = database.query(MySQLiteHelper.PEOPLE_TABLE,
				new String[]{MySQLiteHelper.COLUMN_PID},
				"name=?",
				new String[]{name}, null, null, null);
		
		long pid;
		if (cursor.getCount() == 0) {
			pid = addPerson(name);
		} else {
			cursor.moveToFirst();
			pid = cursor.getInt(cursor.getColumnIndex(MySQLiteHelper.COLUMN_PID));
		}
		
		cursor.close();
		
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_RID, refID);
		values.put(MySQLiteHelper.COLUMN_DESCRIPTION, description);
		values.put(MySQLiteHelper.COLUMN_PRICE, price);
		values.put(MySQLiteHelper.COLUMN_PID, pid);
		
		database.insert(MySQLiteHelper.TRANSACTIONS_TABLE, null, values);
		
		return pid;
	}
	
	/**
	 * Deletes a particular group's details and transactions from all tables
	 *
	 * @param refID The unique database ID of the transaction group.
	 */
	public void deleteRefene(long refID) {
		database.delete(MySQLiteHelper.TRANSACTIONS_TABLE, MySQLiteHelper.COLUMN_RID + "=?",
				new String[]{String.valueOf(refID)});
		database.delete(MySQLiteHelper.REFENES_TABLE, MySQLiteHelper.COLUMN_RID + "=?",
				new String[]{String.valueOf(refID)});
	}
	
	public void removePersonFromRefene(long pid, long refID) {
		database.delete(MySQLiteHelper.TRANSACTIONS_TABLE,
				MySQLiteHelper.COLUMN_RID + "=? AND " + MySQLiteHelper.COLUMN_PID + "=?",
				new String[]{String.valueOf(refID), String.valueOf(pid)});
		Cursor cursor = database.query(MySQLiteHelper.TRANSACTIONS_TABLE,
				new String[]{MySQLiteHelper.COLUMN_ID},
				MySQLiteHelper.COLUMN_PID + "=?",
				new String[]{String.valueOf(pid)},
				null,
				null,
				null);
		
		if (cursor.getCount() == 0) {
			database.delete(MySQLiteHelper.PEOPLE_TABLE, MySQLiteHelper.COLUMN_PID + "=?",
					new String[]{String.valueOf(pid)});
		}
		cursor.close();
	}
	
	public void deleteTransaction(long id) {
		database.delete(MySQLiteHelper.TRANSACTIONS_TABLE, MySQLiteHelper.COLUMN_ID + "=?",
				new String[]{String.valueOf(id)});
	}
	
	/**
	 * Returns all transactions and summed information about them connected to a group.
	 *
	 * @param refID The unique database ID of the transaction group.
	 * @return A list of results containing transactions which
	 * include person names, database id's and payments.
	 */
	public List<Transaction> getAllRefeneTransactions(long refID) {
		Cursor cursor = database.rawQuery("SELECT " + MySQLiteHelper.COLUMN_NAME
				+ ", " + MySQLiteHelper.PEOPLE_TABLE + "." + MySQLiteHelper.COLUMN_PID
				+ ", SUM(" + MySQLiteHelper.COLUMN_PRICE + ") AS " + MySQLiteHelper.COLUMN_PRICE
				+ " FROM " + MySQLiteHelper.TRANSACTIONS_TABLE
				+ " INNER JOIN " + MySQLiteHelper.PEOPLE_TABLE
				+ " ON " + MySQLiteHelper.TRANSACTIONS_TABLE + "." + MySQLiteHelper.COLUMN_PID + "=" + MySQLiteHelper
				.PEOPLE_TABLE + "." + MySQLiteHelper.COLUMN_PID
				+ " INNER JOIN " + MySQLiteHelper.REFENES_TABLE
				+ " ON " + MySQLiteHelper.TRANSACTIONS_TABLE + "." + MySQLiteHelper.COLUMN_RID + "=" + MySQLiteHelper
				.REFENES_TABLE + "." + MySQLiteHelper.COLUMN_RID
				+ " WHERE " + MySQLiteHelper.TRANSACTIONS_TABLE + "." + MySQLiteHelper.COLUMN_RID + "=?"
				+ " GROUP BY " + MySQLiteHelper.COLUMN_NAME, new String[]{String.valueOf(refID)});
		
		cursor.moveToFirst();
		List<Transaction> transactions = new ArrayList<>();
		while (!cursor.isAfterLast()) {
			Person person = new Person(cursor.getInt(cursor.getColumnIndex(MySQLiteHelper.COLUMN_PID)),
					cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_NAME)));
			Transaction transaction = new Transaction(
					cursor.getFloat(cursor.getColumnIndex(MySQLiteHelper.COLUMN_PRICE)),
					person);
			transactions.add(transaction);
			cursor.moveToNext();
		}
		
		cursor.close();
		
		return transactions;
	}
	
	/**
	 * Get information about a single transaction.
	 *
	 * @param id The unique database ID of the transaction.
	 * @return An array with the description, price and the person ID that paid for the transaction.
	 */
	public String[] getTransaction(long id) {
		Cursor cursor = database.query(MySQLiteHelper.TRANSACTIONS_TABLE,
				new String[]{MySQLiteHelper.COLUMN_DESCRIPTION, MySQLiteHelper.COLUMN_PRICE, MySQLiteHelper
						.COLUMN_PID},
				MySQLiteHelper.COLUMN_ID + "=?",
				new String[]{"" + id},
				null,
				null,
				null);
		
		cursor.moveToFirst();
		return new String[]{cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_DESCRIPTION)),
				cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_PRICE))};
	}
	
	/**
	 * @param pid The unique database ID of the person.
	 * @return A list of results containing transaction
	 * descriptions and payments.
	 */
	public List<Transaction> getPersonTransactions(long pid, long rid) {
		Cursor cursor = database.query(MySQLiteHelper.TRANSACTIONS_TABLE,
				new String[]{MySQLiteHelper.COLUMN_DESCRIPTION, MySQLiteHelper.COLUMN_PRICE, MySQLiteHelper.COLUMN_ID},
				MySQLiteHelper.COLUMN_PID + "=? AND " + MySQLiteHelper.COLUMN_RID + "=?",
				new String[]{String.valueOf(pid), String.valueOf(rid)},
				null,
				null,
				MySQLiteHelper.COLUMN_TIME + " DESC");
		
		List<Transaction> transactions = new ArrayList<>();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Transaction transaction = new Transaction(
					cursor.getLong(cursor.getColumnIndex(MySQLiteHelper.COLUMN_ID)),
					cursor.getFloat(cursor.getColumnIndex(MySQLiteHelper.COLUMN_PRICE)),
					cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_DESCRIPTION)));
			transactions.add(transaction);
			cursor.moveToNext();
		}
		
		cursor.close();
		
		return transactions;
	}
	
	/**
	 * Returns a person's total payments on a particular group.
	 *
	 * @param pid The person's unique database ID.
	 * @param rid The transaction group's unique database ID.
	 * @return The total sum of the person's payments.
	 */
	
	public float getPersonSum(long pid, long rid) {
		Cursor cursor = database.rawQuery("SELECT " + MySQLiteHelper.COLUMN_NAME
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
				new String[]{String.valueOf(pid), String.valueOf(rid)});
		
		cursor.moveToFirst();
		float sum = cursor.getFloat(cursor.getColumnIndex(MySQLiteHelper.COLUMN_PRICE));
		cursor.close();
		
		return sum;
	}
	
	/**
	 * @return A list of all the transaction group database ID's.
	 */
	public List<Long> getAllRefenes() {
		Cursor cursor = database.query(MySQLiteHelper.REFENES_TABLE,
				new String[]{MySQLiteHelper.COLUMN_RID},
				null,
				null,
				null,
				null,
				null);
		
		List<Long> refenes = new ArrayList<>();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			refenes.add(cursor.getLong(cursor.getColumnIndex(MySQLiteHelper.COLUMN_RID)));
			cursor.moveToNext();
		}
		
		cursor.close();
		
		return refenes;
	}
	
	private class MySQLiteHelper extends SQLiteOpenHelper {
		private static final String TRANSACTIONS_TABLE = "transactions";
		private static final String COLUMN_ID = "transaction_id";
		private static final String COLUMN_RID = "refenes_id";
		private static final String COLUMN_PID = "person_id";
		private static final String COLUMN_DESCRIPTION = "description";
		private static final String COLUMN_PRICE = "price";
		private static final String COLUMN_TIME = "creation_time";
		private static final String COLUMN_MODTIME = "modification_time";
		private static final String PEOPLE_TABLE = "person";
		private static final String COLUMN_NAME = "name";
		private static final String REFENES_TABLE = "refenes";
		private static final String DATABASE_NAME = "refenes.db";
		private static final int DATABASE_VERSION = 1;
		private final String DATABASE_TRANSACTIONS_CREATE = "create table " + TRANSACTIONS_TABLE + "("
				+ COLUMN_ID + " integer primary key autoincrement, "
				+ COLUMN_RID + " integer, "
				+ COLUMN_PID + " integer, "
				+ COLUMN_DESCRIPTION + " text default '" + R.string.untitled + "', "
				+ COLUMN_PRICE + " float not null, "
				+ COLUMN_TIME + " datetime default current_timestamp, "
				//+ COLUMN_MODTIME + " datetime on update current_timestamp, "
				+ "foreign key (" + COLUMN_PID + ") references " + PEOPLE_TABLE + " (" + COLUMN_PID + "), "
				+ "foreign key (" + COLUMN_RID + ") references " + REFENES_TABLE + " (" + COLUMN_RID + ") "
				+ ");";
		
		private final String DATABASE_PEOPLE_CREATE = "create table " + PEOPLE_TABLE + "("
				+ COLUMN_PID + " integer primary key autoincrement, "
				+ COLUMN_NAME + " text"
				+ ");";
		
		private final String DATABASE_REFENES_CREATE = "create table " + REFENES_TABLE + "("
				+ COLUMN_RID + " integer primary key autoincrement, "
				+ COLUMN_MODTIME + " integer "
				+ ");";
		
		/**
		 * Create a helper object to create, open, and/or manage a database.
		 * This method always returns very quickly.  The database is not actually
		 * created or opened until one of {@link #getWritableDatabase} or
		 * {@link #getReadableDatabase} is called.
		 *
		 * @param context to use to open or create the database
		 */
		MySQLiteHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		/**
		 * Called when the database is created for the first time. This is where the
		 * creation of tables and the initial population of the tables should happen.
		 *
		 * @param db The database.
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_REFENES_CREATE);
			db.execSQL(DATABASE_TRANSACTIONS_CREATE);
			db.execSQL(DATABASE_PEOPLE_CREATE);
			database = db;
		}
		
		/**
		 * Called when the database needs to be upgraded. The implementation
		 * should use this method to drop tables, add tables, or do anything else it
		 * needs to upgrade to the new schema version.
		 * The SQLite ALTER TABLE documentation can be found
		 * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
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
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(APPTAG,
					"Upgrading database from version " + oldVersion + " to "
							+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + REFENES_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + TRANSACTIONS_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + PEOPLE_TABLE);
			onCreate(db);
		}
	}
}
