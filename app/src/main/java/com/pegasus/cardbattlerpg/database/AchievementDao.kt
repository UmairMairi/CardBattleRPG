package com.pegasus.cardbattlerpg.database

import androidx.room.*
import com.pegasus.cardbattlerpg.entity.AchievementEntity

@Dao
interface AchievementDao {

    // ==========================
    // COUNT
    // ==========================

    @Query("SELECT COUNT(*) FROM achievements")
    suspend fun countAchievements(): Int

    @Query("SELECT COUNT(*) FROM achievements WHERE unlocked = 1")
    suspend fun countUnlockedAchievements(): Int

    @Query("SELECT COUNT(*) FROM achievements WHERE unlocked = 1 AND claimed = 0")
    suspend fun countClaimableAchievements(): Int

    @Query("SELECT COUNT(*) FROM achievements WHERE claimed = 1")
    suspend fun countClaimedAchievements(): Int

    // ==========================
    // GET
    // ==========================

    @Query("""
        SELECT *
        FROM achievements
        ORDER BY
            claimed ASC,
            unlocked DESC,
            id ASC
    """)
    suspend fun getAllAchievements(): List<AchievementEntity>

    @Query("""
        SELECT *
        FROM achievements
        WHERE unlocked = 1
        AND claimed = 0
        ORDER BY id ASC
    """)
    suspend fun getClaimableAchievements(): List<AchievementEntity>

    @Query("""
        SELECT *
        FROM achievements
        WHERE unlocked = 1
        ORDER BY claimed ASC, id ASC
    """)
    suspend fun getUnlockedAchievements(): List<AchievementEntity>

    @Query("""
        SELECT *
        FROM achievements
        WHERE unlocked = 0
        ORDER BY id ASC
    """)
    suspend fun getLockedAchievements(): List<AchievementEntity>

    @Query("""
        SELECT *
        FROM achievements
        WHERE id = :achievementId
        LIMIT 1
    """)
    suspend fun getAchievementById(
        achievementId: Int
    ): AchievementEntity?

    @Query("""
        SELECT *
        FROM achievements
        WHERE title = :title
        LIMIT 1
    """)
    suspend fun getAchievementByTitle(
        title: String
    ): AchievementEntity?

    @Query("""
        SELECT *
        FROM achievements
        WHERE title LIKE '%' || :keyword || '%'
           OR description LIKE '%' || :keyword || '%'
        ORDER BY unlocked DESC, claimed ASC, id ASC
    """)
    suspend fun searchAchievements(
        keyword: String
    ): List<AchievementEntity>

    // ==========================
    // INSERT
    // ==========================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(
        achievement: AchievementEntity
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(
        achievements: List<AchievementEntity>
    )

    // ==========================
    // UPDATE
    // ==========================

    @Update
    suspend fun updateAchievement(
        achievement: AchievementEntity
    )

    @Query("""
        UPDATE achievements
        SET claimed = 1
        WHERE id = :achievementId
    """)
    suspend fun claimAchievement(
        achievementId: Int
    )

    // ==========================
    // PROGRESS
    // ==========================

    @Query("""
        UPDATE achievements
        SET progress = progress + :amount
        WHERE title = :title
    """)
    suspend fun addProgress(
        title: String,
        amount: Int
    )

    @Query("""
        UPDATE achievements
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
        UPDATE achievements
        SET progress = :value
        WHERE title = :title
    """)
    suspend fun setProgress(
        title: String,
        value: Int
    )

    @Query("""
        UPDATE achievements
        SET unlocked = 1
        WHERE progress >= target
    """)
    suspend fun refreshUnlockedAchievements()

    // ==========================
    // RESET
    // ==========================

    @Query("""
        UPDATE achievements
        SET
            progress = 0,
            unlocked = 0,
            claimed = 0
    """)
    suspend fun resetAllAchievements()

    @Query("""
        UPDATE achievements
        SET
            progress = 0,
            unlocked = 0,
            claimed = 0
        WHERE title = :title
    """)
    suspend fun resetAchievement(
        title: String
    )

    // ==========================
    // DELETE
    // ==========================

    @Delete
    suspend fun deleteAchievement(
        achievement: AchievementEntity
    )

    @Query("DELETE FROM achievements")
    suspend fun clearAchievements()
}