package com.nosfistis.mike.refene.database;

import android.arch.persistence.room.Relation;

public class TransactionWithPerson {

    private Float price;

    @Relation(parentColumn = "person_id", entityColumn = "person_id", projection = {"name"})
    private Participant participant;
}
