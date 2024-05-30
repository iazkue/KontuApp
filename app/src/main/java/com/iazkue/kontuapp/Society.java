package com.iazkue.kontuapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Society {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;

    @Override
    public String toString() {
        return name; // Para que el ArrayAdapter use el nombre de la sociedad
    }

    public String getName() {
        return name;
    }
}
