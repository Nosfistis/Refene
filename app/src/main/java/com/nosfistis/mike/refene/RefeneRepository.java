package com.nosfistis.mike.refene;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;

import com.nosfistis.mike.refene.database.Refene;
import com.nosfistis.mike.refene.database.RefeneDao;
import com.nosfistis.mike.refene.database.RefeneRoomDatabase;
import com.nosfistis.mike.refene.database.RefeneWithTransactions;

import java.util.List;

public class RefeneRepository {

    private RefeneDao refeneDao;
    private LiveData<List<Refene>> refenes;
    private MutableLiveData<Long> lastInsertId = new MutableLiveData<>();

    public RefeneRepository(Application application) {
        RefeneRoomDatabase db = RefeneRoomDatabase.getDatabase(application);
        refeneDao = db.refeneDao();
        refenes = refeneDao.getAll();
    }

    public LiveData<List<Refene>> getAllRefenes() {
        return refenes;
    }

    public MutableLiveData<Long> getLastInsertId() {
        return lastInsertId;
    }

    public void insert(Refene refene) {
        new insertAsyncTask(refeneDao, lastInsertId).execute(refene);
    }

    private static class insertAsyncTask extends AsyncTask<Refene, Void, Long> {

        private RefeneDao mAsyncTaskDao;
        private MutableLiveData<Long> insertId;

        insertAsyncTask(RefeneDao dao, MutableLiveData<Long> insertLiveData) {
            mAsyncTaskDao = dao;
            insertId = insertLiveData;
        }

        @Override
        protected Long doInBackground(final Refene... params) {
            return mAsyncTaskDao.insert(params[0]);
        }

        @Override
        protected void onPostExecute(Long insertId) {
            this.insertId.setValue(insertId);
        }
    }

}
