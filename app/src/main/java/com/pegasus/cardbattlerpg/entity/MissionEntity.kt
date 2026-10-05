package com.pegasus.cardbattlerpg.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "missions")
data class MissionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val type: String,
    val target: Int,
    val progress: Int = 0,
    val rewardGold: Int = 0,
    val rewardDiamond: Int = 0,
    val completed: Boolean = false,
    val claimed: Boolean = false
)