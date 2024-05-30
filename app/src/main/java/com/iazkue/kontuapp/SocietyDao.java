package com.iazkue.kontuapp;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface SocietyDao {
    @Insert
    void insert(Society society);

    @Update
    void update(Society society);

    @Delete
    void delete(Society society);

    @Query("SELECT * FROM Society")
    List<Society> getAllSocieties();

    @Query("SELECT * FROM Society WHERE id = :societyId LIMIT 1")
    Society getSocietyById(int societyId);
}