package com.pegasus.cardbattlerpg.database

import androidx.room.*
import com.pegasus.cardbattlerpg.entity.MissionEntity

@Dao
interface MissionDao {

    // ==========================
    // BASIC
    // ==========================

    @Query("SELECT COUNT(*) FROM missions")
    suspend fun countMissions(): Int

    @Query("""
        SELECT *
        FROM missions
        ORDER BY
        claimed ASC,
        completed DESC,
        type ASC,
        id ASC
    """)
    suspend fun getAllMissions(): List<MissionEntity>

    @Query("""
        SELECT *
        FROM missions
        WHERE completed = 1
        AND claimed = 0
        ORDER BY id ASC
    """)
    suspend fun getClaimableMissions(): List<MissionEntity>

    // ==========================
    // FILTER
    // ==========================

    @Query("""
        SELECT *
        FROM missions
        WHERE type = :type
        ORDER BY claimed ASC, completed DESC
    """)
    suspend fun getMissionsByType(
        type: String
    ): List<MissionEntity>

    @Query("""
        SELECT *
        FROM missions
        WHERE completed = 1
    """)
    suspend fun getCompletedMissions(): List<MissionEntity>

    @Query("""
        SELECT *
        FROM missions
        WHERE completed = 0
    """)
    suspend fun getActiveMissions(): List<MissionEntity>

    // ==========================
    // INSERT
    // ==========================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMission(
        mission: MissionEntity
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMissions(
        missions: List<MissionEntity>
    )

    // ==========================
    // UPDATE
    // ==========================

    @Update
    suspend fun updateMission(
        mission: MissionEntity
    )

    // ==========================
    // PROGRESS
    // ==========================

    @Query("""
        UPDATE missions
        SET progress = progress + :amount
        WHERE title = :title
    """)
    suspend fun addProgress(
        title: String,
        amount: Int
    )

    @Query("""
        UPDATE missions
        SET progress =
            CASE
                WHEN progress + :amount > target
                THEN target
                ELSE progress + :amount
            END
        WHERE title = :title
    """)
    suspend fun addProgressSafe(
        title: String,
        amount: Int
    )

    @Query("""
        UPDATE missions
        SET progress = :value
        WHERE title = :title
    """)
    suspend fun setProgress(
        title: String,
        value: Int
    )

    @Query("""
        UPDATE missions
        SET completed = 1
        WHERE progress >= target
    """)
    suspend fun refreshCompletedMissions()

    // ==========================
    // CLAIM REWARD
    // ==========================

    @Query("""
        UPDATE missions
        SET claimed = 1
        WHERE id = :missionId
    """)
    suspend fun claimMission(
        missionId: Int
    )

    @Query("""
        UPDATE missions
        SET claimed = 0
        WHERE type = 'Daily'
    """)
    suspend fun resetDailyClaim()

    @Query("""
        UPDATE missions
        SET claimed = 0
        WHERE type = 'Weekly'
    """)
    suspend fun resetWeeklyClaim()

    // ==========================
    // RESET DAILY
    // ==========================

    @Query("""
        UPDATE missions
        SET
            progress = 0,
            completed = 0,
            claimed = 0
        WHERE type = 'Daily'
    """)
    suspend fun resetDailyMissions()

    // ==========================
    // RESET WEEKLY
    // ==========================

    @Query("""
        UPDATE missions
        SET
            progress = 0,
            completed = 0,
            claimed = 0
        WHERE type = 'Weekly'
    """)
    suspend fun resetWeeklyMissions()

    // ==========================
    // STATISTIC
    // ==========================

    @Query("""
        SELECT COUNT(*)
        FROM missions
        WHERE completed = 1
    """)
    suspend fun countCompletedMissions(): Int

    @Query("""
        SELECT COUNT(*)
        FROM missions
        WHERE completed = 1
        AND claimed = 0
    """)
    suspend fun countClaimableMissions(): Int

    @Query("""
        SELECT COUNT(*)
        FROM missions
        WHERE claimed = 1
    """)
    suspend fun countClaimedMissions(): Int

    // ==========================
    // DELETE
    // ==========================

    @Query("DELETE FROM missions")
    suspend fun clearMissions()

    @Delete
    suspend fun deleteMission(
        mission: MissionEntity
    )
}