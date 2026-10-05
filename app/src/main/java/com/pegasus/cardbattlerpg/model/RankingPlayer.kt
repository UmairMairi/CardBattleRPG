package com.pegasus.cardbattlerpg.model

data class RankingPlayer(
    val uid: String = "",
    val playerName: String = "",
    val heroName: String = "No Hero",
    val heroImage: String = "hero_unknown",
    val level: Int = 1,
    val power: Int = 0,
    val gold: Int = 0,
    val diamond: Int = 0,
    val ownedCards: Int = 0,
    val completedStages: Int = 0,
    val arenaPoint: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)
