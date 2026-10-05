package com.pegasus.cardbattlerpg.model

import com.pegasus.cardbattlerpg.entity.CardEntity
import com.pegasus.cardbattlerpg.entity.HeroEntity

data class BattleState(
    val hero: HeroEntity?,
    val heroMaxHp: Int,
    val heroCurrentHp: Int,

    val enemy: BattleEnemy,

    val handCards: List<CardEntity>,

    val battleLog: List<String>,

    val isPlayerTurn: Boolean,

    val isFinished: Boolean,

    val isVictory: Boolean,

    // tambahan untuk UI battle lebih lengkap
    val turnCount: Int = 1,
    val comboCount: Int = 0,
    val lastDamage: Int = 0,
    val criticalHit: Boolean = false
)