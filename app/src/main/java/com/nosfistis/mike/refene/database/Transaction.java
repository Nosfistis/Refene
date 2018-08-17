package com.nosfistis.mike.refene.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Date;

import static android.arch.persistence.room.ForeignKey.CASCADE;

/**
 * Created by Mike on 1/5/2017.
 */
@Entity(
        foreignKeys = {
                @ForeignKey(entity = Refene.class, parentColumns = "id", childColumns = "refene_id", onDelete = CASCADE),
                @ForeignKey(entity = Participant.class, parentColumns = "id", childColumns = "participant_id", onDelete = CASCADE)
        },
        tableName = "transactions"
)
public class Transaction {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "refene_id", index = true)
    private long refeneId;

    @ColumnInfo(name = "participant_id", index = true)
    private long participantId;

    @NonNull
    private Float price;

    private String description;

    @ColumnInfo(name = "created_at")
    private Date createdAt;

    public Transaction(long id, float price, String description) {
        this.id = id;
        this.price = price;
        this.description = description;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getRefeneId() {
        return refeneId;
    }

    public void setRefeneId(long refeneId) {
        this.refeneId = refeneId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public long getParticipantId() {
        return participantId;
    }

    public void setParticipantId(long participantId) {
        this.participantId = participantId;
    }
}
