package com.pegasus.cardbattlerpg.model

data class BattleEnemy(
    val name: String,
    val level: Int,
    val maxHp: Int,
    var currentHp: Int,
    val attack: Int,
    val defense: Int,
    val rewardGold: Int,
    val rewardExp: Int,

    // gambar PNG transparan dari drawable
    val image: String = "enemy_shadow_beast",

    // element enemy
    val element: String = "Dark",

    // Normal, Elite, Boss, Dragon, Undead, Poison
    val enemyType: String = "Normal",

    val isBoss: Boolean = false
)