package com.iazkue.kontuapp;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ItemPriceDao {
    @Insert
    void insert(ItemPrice itemPrice);

    @Query("SELECT * FROM ItemPrice WHERE societyId = :societyId AND itemId = :itemId ORDER BY id DESC LIMIT 1")
    ItemPrice getLastPrice(int societyId, int itemId);

    @Query("SELECT * FROM ItemPrice WHERE societyId = :societyId")
    List<ItemPrice> getItemsBySociety(int societyId);

    @Query("DELETE FROM ItemPrice WHERE itemId = :itemId")
    void deleteItemPricesByItemId(int itemId);
}