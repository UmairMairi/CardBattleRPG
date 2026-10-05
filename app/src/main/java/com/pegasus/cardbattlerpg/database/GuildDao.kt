package com.pegasus.cardbattlerpg.database

import androidx.room.*
import com.pegasus.cardbattlerpg.entity.GuildChatEntity
import com.pegasus.cardbattlerpg.entity.GuildEntity
import com.pegasus.cardbattlerpg.entity.GuildMemberEntity

@Dao
interface GuildDao {

    @Query("SELECT * FROM guilds ORDER BY level DESC, exp DESC")
    suspend fun getAllGuilds(): List<GuildEntity>

    @Query("SELECT * FROM guilds WHERE id = :guildId LIMIT 1")
    suspend fun getGuildById(guildId: Int): GuildEntity?

    @Query("SELECT * FROM guild_members LIMIT 1")
    suspend fun getMyGuildMember(): GuildMemberEntity?

    @Query("SELECT * FROM guild_members WHERE guildId = :guildId ORDER BY role ASC, power DESC")
    suspend fun getMembers(guildId: Int): List<GuildMemberEntity>

    @Query("SELECT * FROM guild_chat WHERE guildId = :guildId ORDER BY createdAt DESC LIMIT 30")
    suspend fun getChats(guildId: Int): List<GuildChatEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuild(guild: GuildEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: GuildMemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: GuildChatEntity)

    @Query("DELETE FROM guild_members")
    suspend fun leaveGuild()

    @Query("UPDATE guilds SET memberCount = memberCount + 1 WHERE id = :guildId")
    suspend fun increaseMember(guildId: Int)

    @Query("UPDATE guilds SET memberCount = memberCount - 1 WHERE id = :guildId AND memberCount > 0")
    suspend fun decreaseMember(guildId: Int)

    @Query("UPDATE guilds SET goldFund = goldFund + :gold, exp = exp + :exp WHERE id = :guildId")
    suspend fun addDonation(guildId: Int, gold: Int, exp: Int)

    @Query("UPDATE guild_members SET donatedGold = donatedGold + :gold WHERE id = :memberId")
    suspend fun addMemberDonation(memberId: Int, gold: Int)

    @Query("UPDATE guilds SET level = level + 1, exp = 0 WHERE id = :guildId AND exp >= :requiredExp")
    suspend fun levelUpGuild(guildId: Int, requiredExp: Int)
}