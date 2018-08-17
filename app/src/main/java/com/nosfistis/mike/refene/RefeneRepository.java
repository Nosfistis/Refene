package com.nosfistis.mike.refene;

import android.app.Application;
import android.arch.lifecycle.LiveData;

import com.nosfistis.mike.refene.database.Refene;
import com.nosfistis.mike.refene.database.RefeneDao;
import com.nosfistis.mike.refene.database.RefeneRoomDatabase;

import java.util.List;

public class RefeneRepository {

    private RefeneDao refeneDao;
    private LiveData<List<Refene>> refenes;

    public RefeneRepository(Application application) {
        RefeneRoomDatabase db = RefeneRoomDatabase.getDatabase(application);
        refeneDao = db.refeneDao();
        refenes = refeneDao.getAll();
    }

    public LiveData<List<Refene>> getAllRefenes() {
        return refenes;
    }

}
