package com.nosfistis.mike.refene.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface RefeneDao {

    @Query("SELECT * FROM refenes")
    LiveData<List<Refene>> getAll();

    @Insert(onConflict = OnConflictStrategy.FAIL)
    public void insertRefene(Refene... refenes);

    @Delete
    public void deleteRefene(Refene... refenes);
}
