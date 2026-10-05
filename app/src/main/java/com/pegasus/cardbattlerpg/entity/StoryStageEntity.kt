package com.pegasus.cardbattlerpg.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "story_stages")
data class StoryStageEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // =========================
    // CHAPTER INFO
    // =========================

    val chapter: Int,

    val stage: Int,

    val title: String,

    // =========================
    // STORY
    // =========================

    val storyText: String = "",

    val storyEndingText: String = "",

    // =========================
    // IMAGE
    // =========================

    // ex:
    // story_chapter_1
    // story_chapter_2
    // story_chapter_3
    val backgroundImage: String = "story_chapter_1",

    // ex:
    // story_stage_1
    // story_stage_2
    // story_stage_3
    val stageImage: String = "story_stage_1",

    // ex:
    // boss_stage_1
    // boss_stage_2
    val bossImage: String = "",

    // =========================
    // ENEMY
    // =========================

    val enemyName: String,

    val enemyHp: Int,

    val enemyAttack: Int,

    val enemyDefense: Int,

    val enemyCriticalRate: Int = 0,

    val enemyCriticalDamage: Int = 150,

    // =========================
    // STAGE TYPE
    // =========================

    val isBoss: Boolean = false,

    val isElite: Boolean = false,

    val isSecretStage: Boolean = false,

    // =========================
    // DIFFICULTY
    // =========================

    // NORMAL
    // HARD
    // NIGHTMARE
    // HELL
    val difficulty: String = "NORMAL",

    // =========================
    // REQUIREMENT
    // =========================

    val recommendedPower: Int = 0,

    val requiredPlayerLevel: Int = 1,

    val energyCost: Int = 5,

    // =========================
    // REWARD
    // =========================

    val rewardGold: Int,

    val rewardExp: Int,

    val rewardDiamond: Int = 0,

    // card drop
    val rewardCardId: Int? = null,

    // hero unlock
    val rewardHeroId: Int? = null,

    // equipment drop
    val rewardEquipmentId: Int? = null,

    // item drop
    val rewardItemId: Int? = null,

    val rewardItemAmount: Int = 0,

    // =========================
    // DROP RATE
    // =========================

    val cardDropRate: Int = 0,

    val equipmentDropRate: Int = 0,

    val heroDropRate: Int = 0,

    // =========================
    // STAR SYSTEM
    // =========================

    val star1Completed: Boolean = false,

    val star2Completed: Boolean = false,

    val star3Completed: Boolean = false,

    // =========================
    // UNLOCK
    // =========================

    val unlocked: Boolean = false,

    val completed: Boolean = false,

    // =========================
    // SAVE
    // =========================

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis()
)