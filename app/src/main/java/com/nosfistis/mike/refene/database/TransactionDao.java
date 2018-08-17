package com.nosfistis.mike.refene.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.ROLLBACK)
    public void insertTransaction(Transaction... transactions);

    @Query("SELECT * FROM transactions WHERE transaction_id=:id LIMIT 1")
    Transaction getTransaction(int id);

    @Query("SELECT transaction_id, price, description FROM transactions" +
            " WHERE refenes_id=:refeneId AND person_id=:personId ORDER BY creation_time DESC")
    List<Transaction> getByRefeneAndPerson(int refeneId, int personId);

    @Query("SELECT name, person_id, SUM(price) AS price FROM transactions" +
            " INNER JOIN Participant ON Participant.person_id = transactions.person_id" +
            " WHERE refenes_id = :refeneId" +
            " GROUP BY person_id")
    List<Transaction> getTransactionsWithPersonalSums(long refeneId);

    @Delete
    public void deleteTransaction(Transaction... transactions);
}
