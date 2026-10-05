package com.pegasus.cardbattlerpg.model

data class ArenaResult(
    val isVictory: Boolean,
    val message: String,
    val rewardGold: Int = 0,
    val rewardPoint: Int = 0
)