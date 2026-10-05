package com.pegasus.cardbattlerpg.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "guilds")
data class GuildEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val guildName: String,
    val description: String,
    val leaderName: String,
    val level: Int = 1,
    val exp: Int = 0,
    val memberCount: Int = 1,
    val goldFund: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)