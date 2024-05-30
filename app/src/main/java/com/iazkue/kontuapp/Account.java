package com.iazkue.kontuapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity
public class Account {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public double totalCost;
    public String participants;
    public int societyId;
    public Date dateCreated;

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(dateCreated) + " | " + participants;
    }
}
