package com.pegasus.cardbattlerpg.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // ==========================
    // BASIC
    // ==========================

    val title: String,

    val description: String,

    // ==========================
    // CATEGORY
    // Battle
    // Story
    // Summon
    // Hero
    // Equipment
    // Arena
    // Guild
    // Collection
    // Economy
    // Event
    // ==========================

    val category: String = "General",

    // ==========================
    // RARITY
    // Common
    // Rare
    // Epic
    // Legendary
    // Mythic
    // ==========================

    val rarity: String = "Common",

    // ==========================
    // ICON
    // drawable name
    // ==========================

    val icon: String = "achievement_default",

    // ==========================
    // PROGRESS
    // ==========================

    val target: Int,

    val progress: Int = 0,

    // ==========================
    // REWARD
    // ==========================

    val rewardGold: Int = 0,

    val rewardDiamond: Int = 0,

    val rewardItemName: String = "",

    val rewardItemAmount: Int = 0,

    // ==========================
    // STATUS
    // ==========================

    val unlocked: Boolean = false,

    val claimed: Boolean = false,

    // ==========================
    // VISUAL
    // ==========================

    val badge: String = "",

    val hidden: Boolean = false,

    // ==========================
    // TIMESTAMP
    // ==========================

    val createdAt: Long = System.currentTimeMillis(),

    val unlockedAt: Long = 0L,

    val claimedAt: Long = 0L
)