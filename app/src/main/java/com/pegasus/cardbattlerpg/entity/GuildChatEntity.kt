package com.pegasus.cardbattlerpg.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "guild_chat")
data class GuildChatEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val guildId: Int,
    val senderName: String,
    val message: String,
    val createdAt: Long = System.currentTimeMillis()
)