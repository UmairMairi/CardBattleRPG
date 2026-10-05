package com.pegasus.cardbattlerpg.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "heroes")
data class HeroEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,
    val element: String,

    val level: Int = 1,
    val exp: Int = 0,
    val maxExp: Int = 100,

    val hp: Int,
    val attack: Int,
    val defense: Int,

    val image: String = "hero_unknown",

    val role: String = "Warrior",
    val rarity: String = "Rare",

    val speed: Int = 100,
    val criticalRate: Int = 5,
    val criticalDamage: Int = 150,

    val skillName: String = "",
    val skillDescription: String = "",

    val ultimateName: String = "",
    val ultimateDescription: String = "",

    val selected: Boolean = false,
    val unlocked: Boolean = true,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)