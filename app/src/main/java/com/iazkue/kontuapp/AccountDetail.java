package com.iazkue.kontuapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class AccountDetail {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int accountId;
    public int itemId;
    public String participant;
    public int quantity;
}
