package com.iazkue.kontuapp;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface AccountDetailDao {
    @Insert
    void insert(AccountDetail accountDetail);

    @Update
    void update(AccountDetail accountDetail);

    @Delete
    void delete(AccountDetail accountDetail);

    @Query("SELECT * FROM AccountDetail WHERE accountId = :accountId")
    List<AccountDetail> getAccountDetailsByAccount(int accountId);

    @Query("SELECT * FROM AccountDetail WHERE accountId = :accountId")
    List<AccountDetail> getAccountDetailsByAccountId(int accountId);

    @Query("SELECT * FROM AccountDetail WHERE accountId = :accountId AND participant = :participant")
    List<AccountDetail> getAccountDetailsByParticipant(int accountId, String participant);

    @Query("DELETE FROM AccountDetail WHERE accountId = :accountId")
    void deleteAllByAccountId(int accountId);
}
