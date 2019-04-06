package com.nosfistis.mike.refene.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.VisibleForTesting;

import com.nosfistis.mike.refene.R;

@Database(entities = {Participant.class, Refene.class, Transaction.class}, version = 2)
@TypeConverters({Converters.class})
public abstract class RefeneRoomDatabase extends RoomDatabase {

    public abstract RefeneDao refeneDao();
    public abstract ParticipantDao participantDao();
    public abstract TransactionDao transactionDao();

    private static RefeneRoomDatabase INSTANCE;

    public static RefeneRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (RefeneRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), RefeneRoomDatabase.class, "refenes.db")
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }

        return INSTANCE;
    }

    /**
     * Migrate from:
     * version 1 - using the SQLiteDatabase API
     * to
     * version 2 - using Room
     */
    @VisibleForTesting
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE participants (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL)");

            database.execSQL("UPDATE person SET name = 'unknown' || person_id WHERE name IS NULL");

            database.execSQL("INSERT INTO participants (id, name) SELECT person_id, name FROM person");

            database.execSQL("ALTER TABLE refenes RENAME TO refenes_old");

            database.execSQL(
                    "CREATE TABLE refenes (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                            "created_at INTEGER DEFAULT CURRENT_TIMESTAMP)"
            );

            database.execSQL("INSERT INTO refenes(id) SELECT refenes_id FROM refenes_old");

            database.execSQL(
                    "CREATE TABLE transactions_new (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                            "refene_id INTEGER," +
                            "participant_id INTEGER," +
                            "description TEXT DEFAULT '" + R.string.untitled + "'," +
                            "price FLOAT NOT NULL," +
                            "created_at INTEGER DEFAULT CURRENT_TIMESTAMP," +
                            "FOREIGN KEY (refene_id) REFERENCES refenes(person_id)," +
                            "FOREIGN KEY (participant_id) REFERENCES participants)"
            );

            database.execSQL("INSERT INTO transactions_new (id, refene_id, participant_id, description, price, created_at) " +
                    "SELECT transaction_id, refenes_id, person_id, description, price, creation_time FROM transactions");

            database.execSQL("DROP TABLE transactions");
            database.execSQL("DROP TABLE refenes_old");
            database.execSQL("DROP TABLE person");

            database.execSQL("ALTER TABLE transactions_new RENAME TO transactions");
        }
    };
}
