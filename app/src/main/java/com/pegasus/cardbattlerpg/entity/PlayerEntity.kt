package com.pegasus.cardbattlerpg.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class PlayerEntity(

    @PrimaryKey
    val id: Int = 1,

    // =========================
    // PROFILE
    // =========================

    val name: String,

    val avatar: String = "avatar_1",

    val title: String = "Novice Summoner",

    val bio: String = "",

    // =========================
    // LEVEL & EXP
    // =========================

    val level: Int = 1,

    val exp: Int = 0,

    val maxExp: Int = 100,

    // =========================
    // CURRENCY
    // =========================

    val gold: Int = 1000,

    val diamond: Int = 100,

    val energy: Int = 100,

    val maxEnergy: Int = 100,

    // =========================
    // ARENA
    // =========================

    val arenaPoint: Int = 0,

    val arenaRank: Int = 999999,

    val arenaWin: Int = 0,

    val arenaLose: Int = 0,

    // =========================
    // STORY
    // =========================

    val currentChapter: Int = 1,

    val currentStage: Int = 1,

    val completedStage: Int = 0,

    // =========================
    // COLLECTION
    // =========================

    val ownedCardCount: Int = 0,

    val ownedHeroCount: Int = 0,

    // =========================
    // GUILD
    // =========================

    val guildId: Int? = null,

    val guildDonation: Int = 0,

    // =========================
    // GACHA SYSTEM
    // =========================

    // pity legendary
    val legendaryPity: Int = 0,

    // pity epic
    val epicPity: Int = 0,

    // guarantee banner
    val guaranteedLegendary: Boolean = false,

    // total summon
    val totalSummons: Int = 0,

    // =========================
    // ACHIEVEMENT
    // =========================

    val achievementPoint: Int = 0,

    // =========================
    // POWER
    // =========================

    val totalPower: Int = 0,

    // =========================
    // SETTINGS
    // =========================

    val musicEnabled: Boolean = true,

    val sfxEnabled: Boolean = true,

    val vibrationEnabled: Boolean = true,

    // =========================
    // SAVE INFO
    // =========================

    val createdAt: Long =
        System.currentTimeMillis(),

    val lastLoginAt: Long =
        System.currentTimeMillis(),

    val lastSavedAt: Long =
        System.currentTimeMillis()
)