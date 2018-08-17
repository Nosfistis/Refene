package com.nosfistis.mike.refene.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Database Participant entity class
 */
@Entity(tableName = "participants")
public class Participant {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String name;

    public Participant(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
