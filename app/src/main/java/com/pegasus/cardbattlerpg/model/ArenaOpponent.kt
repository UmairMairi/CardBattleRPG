package com.pegasus.cardbattlerpg.model

data class ArenaOpponent(
    val name: String,
    val rank: Int,
    val power: Int,
    val hp: Int,
    val attack: Int,
    val defense: Int,
    val rewardGold: Int,
    val rewardPoint: Int,
    val image: String = "enemy_shadow_beast",
    val element: String = "Dark",
    val role: String = "Fighter"
)