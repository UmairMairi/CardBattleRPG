package com.pegasus.cardbattlerpg.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "equipments")
data class EquipmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,
    val type: String,
    val rarity: String,

    val attackBonus: Int = 0,
    val defenseBonus: Int = 0,
    val hpBonus: Int = 0,

    val image: String = "eq_unknown",

    val equipped: Boolean = false,

    val unlocked: Boolean = false,

    val unlockPriceGold: Int = 0
)