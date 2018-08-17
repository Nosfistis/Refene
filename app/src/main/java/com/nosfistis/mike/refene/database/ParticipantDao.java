package com.nosfistis.mike.refene.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;

@Dao
public interface ParticipantDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertParticipant(Participant... participants);

    @Delete
    public void deleteParticipant(Participant... participants);
}
