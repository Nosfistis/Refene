package com.nosfistis.mike.refene.database;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import java.util.List;

public class RefeneWithTransactions {

    @Embedded
    public Refene refene;

    @Relation(parentColumn = "id", entityColumn = "refene_id")
    public List<Transaction> transactions;

}
