package com.iazkue.kontuapp;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface AccountDao {
    @Insert
    void insert(Account account);

    @Update
    void update(Account account);

    @Delete
    void delete(Account account);

    @Query("SELECT * FROM Account")
    List<Account> getAllAccounts();

    @Query("SELECT * FROM Account WHERE societyId = :societyId")
    List<Account> getAccountsBySociety(int societyId);

    @Query("SELECT * FROM Account WHERE id = :id")
    Account getAccountById(int id);
}