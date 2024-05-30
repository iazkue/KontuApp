package com.iazkue.kontuapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ItemPrice {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int societyId;
    public int itemId;
    public double price;
}