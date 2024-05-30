package com.iazkue.kontuapp;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ItemDao {
    @Insert
    void insert(Item item);

    @Insert
    void insertAll(Item... items);

    @Query("SELECT * FROM Item")
    List<Item> getAllItems();

    @Query("SELECT * FROM Item WHERE id = :id")
    Item getItemById(int id);

    @Query("SELECT * FROM Item WHERE name = :name LIMIT 1")
    Item getItemByName(String name);

    @Query("DELETE FROM Item WHERE id = :id")
    void deleteItemById(int id);
}