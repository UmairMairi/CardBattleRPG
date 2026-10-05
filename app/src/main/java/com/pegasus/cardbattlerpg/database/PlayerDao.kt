package com.pegasus.cardbattlerpg.database

import androidx.room.*
import com.pegasus.cardbattlerpg.entity.PlayerEntity

@Dao
interface PlayerDao {

    @Query("SELECT * FROM players WHERE id = 1 LIMIT 1")
    suspend fun getPlayer(): PlayerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlayer(player: PlayerEntity)

    @Update
    suspend fun updatePlayer(player: PlayerEntity)

    @Query("DELETE FROM players")
    suspend fun clearPlayer()

    @Query("UPDATE players SET gold = gold + :amount WHERE id = 1")
    suspend fun addGold(amount: Int)

    @Query("UPDATE players SET diamond = diamond - :amount WHERE id = 1")
    suspend fun reduceDiamond(amount: Int)

    @Query("UPDATE players SET gold = gold + :gold, diamond = diamond + :diamond WHERE id = 1")
    suspend fun addReward(gold: Int, diamond: Int)

    @Query("UPDATE players SET lastSavedAt = :time WHERE id = 1")
    suspend fun updateLastSavedAt(time: Long)

    @Query("UPDATE players SET gold = gold + :gold, diamond = diamond + :diamond, lastSavedAt = :time WHERE id = 1")
    suspend fun addRewardWithSaveTime(gold: Int, diamond: Int, time: Long)

    @Query("UPDATE players SET gold = gold + :gold, arenaPoint = arenaPoint + :point WHERE id = 1")
    suspend fun addArenaReward(gold: Int, point: Int)

    @Query("""
UPDATE players
SET legendaryPity = :value
WHERE id = 1
""")
    suspend fun updateLegendaryPity(
        value: Int
    )

    @Query("""
UPDATE players
SET epicPity = :value
WHERE id = 1
""")
    suspend fun updateEpicPity(
        value: Int
    )

    @Query("""
UPDATE players
SET totalSummons = totalSummons + 1
WHERE id = 1
""")
    suspend fun increaseSummonCount()

}