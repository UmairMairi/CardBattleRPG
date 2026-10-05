package com.pegasus.cardbattlerpg.database

import androidx.room.*
import com.pegasus.cardbattlerpg.entity.StoryStageEntity

@Dao
interface StoryDao {

    @Query("SELECT COUNT(*) FROM story_stages")
    suspend fun countStages(): Int

    @Query("SELECT * FROM story_stages ORDER BY chapter ASC, stage ASC")
    suspend fun getAllStages(): List<StoryStageEntity>

    @Query("SELECT * FROM story_stages WHERE id = :stageId LIMIT 1")
    suspend fun getStageById(stageId: Int): StoryStageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStages(stages: List<StoryStageEntity>)

    @Query("UPDATE story_stages SET completed = 1 WHERE id = :stageId")
    suspend fun completeStage(stageId: Int)

    @Query("UPDATE story_stages SET unlocked = 1 WHERE id = :stageId")
    suspend fun unlockStage(stageId: Int)

    @Query("SELECT COUNT(*) FROM story_stages WHERE completed = 1")
    suspend fun countCompletedStages(): Int
}