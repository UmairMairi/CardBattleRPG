package com.pegasus.cardbattlerpg.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "guild_members")
data class GuildMemberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val guildId: Int,
    val playerName: String,
    val role: String = "Member",
    val power: Int = 0,
    val donatedGold: Int = 0,
    val joinedAt: Long = System.currentTimeMillis()
)