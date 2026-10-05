package com.pegasus.cardbattlerpg.model

data class SaveGameSummary(
    val playerName: String,
    val level: Int,
    val gold: Int,
    val diamond: Int,
    val ownedCards: Int,
    val heroes: Int,
    val equipments: Int,
    val completedStages: Int,
    val lastSavedAt: Long
)