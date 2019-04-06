package com.nosfistis.mike.refene;

import android.app.Application;
import android.arch.lifecycle.LiveData;

import com.nosfistis.mike.refene.database.RefeneRoomDatabase;
import com.nosfistis.mike.refene.database.Transaction;
import com.nosfistis.mike.refene.database.TransactionDao;

import java.util.List;

public class TransactionRepository {

    private TransactionDao transactionDao;
    private LiveData<List<Transaction>> transactions;

    public TransactionRepository(Application application) {
        RefeneRoomDatabase db = RefeneRoomDatabase.getDatabase(application);
        transactionDao = db.transactionDao();
    }
}
