package com.pegasus.cardbattlerpg.online

import com.pegasus.cardbattlerpg.entity.CardEntity
import com.pegasus.cardbattlerpg.entity.HeroEntity
import com.pegasus.cardbattlerpg.entity.PlayerEntity
import com.pegasus.cardbattlerpg.model.RankingPlayer

data class OnlinePlayerProfile(
    val uid: String = "",
    val username: String = "",
    val heroName: String = "",
    val heroImage: String = "hero_unknown",
    val level: Int = 1,
    val power: Int = 0,
    val arenaPoint: Int = 0,
    val online: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

data class OnlineSaveData(
    val uid: String = "",
    val player: PlayerEntity? = null,
    val selectedHero: HeroEntity? = null,
    val ownedCards: List<CardEntity> = emptyList(),
    val ranking: RankingPlayer? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

data class MatchmakingPlayer(
    val uid: String = "",
    val username: String = "",
    val heroName: String = "",
    val heroImage: String = "hero_unknown",
    val power: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

data class OnlineBattleRoom(
    val roomId: String = "",
    val status: String = "waiting",
    val player1Uid: String = "",
    val player1Name: String = "",
    val player1Hero: String = "",
    val player1HeroImage: String = "hero_unknown",
    val player1Hp: Int = 1000,
    val player1MaxHp: Int = 1000,
    val player2Uid: String = "",
    val player2Name: String = "",
    val player2Hero: String = "",
    val player2HeroImage: String = "hero_unknown",
    val player2Hp: Int = 1000,
    val player2MaxHp: Int = 1000,
    val currentTurnUid: String = "",
    val winnerUid: String = "",
    val lastAction: String = "",
    val lastAttackerUid: String = "",
    val lastTargetUid: String = "",
    val lastDamage: Int = 0,
    val lastCardName: String = "",
    val lastCardImage: String = "",
    val lastCardAttackGif: String = "gif_default_attack",
    val lastSkillName: String = "",
    val lastEffectElement: String = "",
    val lastCritical: Boolean = false,
    val finishedAt: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis()
)

data class OnlineBattleAction(
    val uid: String = "",
    val username: String = "",
    val type: String = "attack",
    val cardName: String = "",
    val skillName: String = "",
    val damage: Int = 0,
    val targetUid: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class OnlineGuildSummary(
    val guildId: String = "",
    val name: String = "",
    val description: String = "",
    val leaderUid: String = "",
    val leaderName: String = "",
    val level: Int = 1,
    val exp: Int = 0,
    val goldFund: Int = 0,
    val memberCount: Int = 1,
    val updatedAt: Long = System.currentTimeMillis()
)

data class OnlineGuildMember(
    val uid: String = "",
    val username: String = "",
    val avatar: String = "avatar_1",
    val heroImage: String = "hero_unknown",
    val power: Int = 0,
    val role: String = "Member",
    val donatedGold: Int = 0,
    val joinedAt: Long = System.currentTimeMillis()
)

data class OnlineGuildChat(
    val uid: String = "",
    val username: String = "",
    val message: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
