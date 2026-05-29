package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BetDao {
    @Query("SELECT * FROM user_account WHERE username = :username LIMIT 1")
    fun getUserAccount(username: String = "kawaken"): Flow<UserAccount?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserAccount(account: UserAccount)

    @Query("SELECT * FROM bet_records ORDER BY timestamp DESC")
    fun getAllBets(): Flow<List<BetRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBet(bet: BetRecord): Long

    @Update
    suspend fun updateBet(bet: BetRecord)

    @Query("DELETE FROM bet_records")
    suspend fun clearHistory()
}
