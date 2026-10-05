package com.pegasus.cardbattlerpg.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory")
data class InventoryEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // ==========================
    // BASIC
    // ==========================

    val itemName: String,

    val itemType: String,

    val image: String,

    val quantity: Int = 1,

    // ==========================
    // DESCRIPTION
    // ==========================

    val description: String = "",

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
    // BADGE
    // NEW
    // HOT
    // LIMITED
    // EVENT
    // ==========================

    val badge: String = "",

    // ==========================
    // SOURCE
    // Shop
    // Summon
    // Chest
    // Event
    // Quest
    // ==========================

    val source: String = "Shop",

    // ==========================
    // SELL VALUE
    // ==========================

    val sellGold: Int = 0,

    // ==========================
    // LOCK ITEM
    // ==========================

    val locked: Boolean = false,

    // ==========================
    // FAVORITE
    // ==========================

    val favorite: Boolean = false,

    // ==========================
    // GLOW COLOR
    // ==========================

    val glowColor: String = "#FFD66B",

    // ==========================
    // CREATED
    // ==========================

    val createdAt: Long =
        System.currentTimeMillis()
)