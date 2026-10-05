package com.pegasus.cardbattlerpg.ui.arena

import com.pegasus.cardbattlerpg.entity.CardEntity
import com.pegasus.cardbattlerpg.model.ArenaOpponent
import com.pegasus.cardbattlerpg.model.ArenaResult
import com.pegasus.cardbattlerpg.model.BattleEnemy
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.random.Random

object ArenaManager {

    // ==========================
    // ENEMY POOL SAMA DENGAN BATTLE
    // ==========================

    fun enemyPool(): List<BattleEnemy> {
        return listOf(

            // CHAPTER 1 — AWAKENING FOREST
            BattleEnemy("Forest Slime", 1, 450, 450, 35, 12, 300, 40, "enemy_forest_slime", "Nature", "Normal"),
            BattleEnemy("Dark Wolf", 2, 580, 580, 48, 18, 450, 60, "enemy_dark_wolf", "Dark", "Normal"),
            BattleEnemy("Poison Toad", 3, 650, 650, 55, 25, 550, 75, "enemy_poison_toad", "Nature", "Poison"),
            BattleEnemy("Shrine Guardian", 4, 720, 720, 62, 32, 650, 90, "enemy_shrine_guardian", "Light", "Normal"),
            BattleEnemy("Shadow Spider", 5, 800, 800, 68, 38, 750, 100, "enemy_shadow_spider", "Dark", "Elite"),
            BattleEnemy("Tree Spirit", 6, 900, 900, 75, 45, 850, 120, "enemy_tree_spirit", "Nature", "Normal"),
            BattleEnemy("Water Serpent", 7, 1000, 1000, 82, 52, 950, 140, "enemy_water_serpent", "Water", "Normal"),
            BattleEnemy("Night Beast", 8, 1100, 1100, 90, 60, 1050, 160, "enemy_night_beast", "Dark", "Normal"),
            BattleEnemy("Corrupted Dryad", 9, 1250, 1250, 105, 70, 1200, 180, "enemy_corrupted_dryad", "Nature", "Elite"),
            BattleEnemy("Forest King", 10, 1600, 1600, 130, 90, 1500, 250, "enemy_forest_king", "Nature", "Boss", true),

            // CHAPTER 2 — CRYSTAL RUINS
            BattleEnemy("Crystal Golem", 11, 1800, 1800, 145, 100, 1700, 280, "enemy_crystal_golem", "Ice", "Normal"),
            BattleEnemy("Crystal Fragment", 12, 1900, 1900, 155, 110, 1800, 300, "enemy_crystal_fragment", "Ice", "Normal"),
            BattleEnemy("Tunnel Worm", 13, 2100, 2100, 170, 120, 2000, 330, "enemy_tunnel_worm", "Earth", "Normal"),
            BattleEnemy("Crystal Knight", 14, 2300, 2300, 180, 130, 2200, 360, "enemy_crystal_knight", "Ice", "Elite"),
            BattleEnemy("Mirror Spirit", 15, 2500, 2500, 190, 140, 2400, 400, "enemy_mirror_spirit", "Light", "Normal"),
            BattleEnemy("Crystal Core", 16, 2800, 2800, 210, 160, 2600, 450, "enemy_crystal_core", "Ice", "Normal"),
            BattleEnemy("Dark Resonator", 17, 3100, 3100, 225, 175, 2900, 500, "enemy_dark_resonator", "Dark", "Normal"),
            BattleEnemy("Ancient Soldier", 18, 3400, 3400, 245, 190, 3200, 550, "enemy_ancient_soldier", "Earth", "Elite"),
            BattleEnemy("Crystal Emperor", 19, 3800, 3800, 270, 210, 3600, 620, "enemy_crystal_emperor", "Ice", "Elite"),
            BattleEnemy("Ruins Overlord", 20, 4500, 4500, 320, 260, 4500, 800, "enemy_ruins_overlord", "Earth", "Boss", true),

            // CHAPTER 3 — DRAGON KINGDOM
            BattleEnemy("Dragon Scout", 21, 5000, 5000, 350, 280, 5000, 850, "enemy_dragon_scout", "Fire", "Dragon"),
            BattleEnemy("Fire Drake", 22, 5400, 5400, 380, 300, 5500, 900, "enemy_fire_drake", "Fire", "Dragon"),
            BattleEnemy("Dragon Hatchling", 23, 6000, 6000, 410, 330, 6200, 980, "enemy_dragon_hatchling", "Fire", "Elite"),
            BattleEnemy("Sky Guardian", 24, 6700, 6700, 450, 360, 7000, 1100, "enemy_sky_guardian", "Wind", "Normal"),
            BattleEnemy("Dragon Lord", 30, 8000, 8000, 520, 420, 10000, 1500, "enemy_dragon_lord", "Fire", "Boss", true),

            // EXTRA RANDOM BATTLE ENEMY
            BattleEnemy("Shadow Beast", 1, 650, 650, 55, 25, 350, 60, "enemy_shadow_beast", "Dark", "Normal"),
            BattleEnemy("Skeleton Soldier", 4, 850, 850, 78, 38, 720, 105, "enemy_skeleton_soldier", "Dark", "Undead"),
            BattleEnemy("Ice Wraith", 8, 1800, 1800, 145, 80, 1550, 220, "enemy_ice_wraith", "Ice", "Elite"),
            BattleEnemy("Thunder Wyvern", 10, 2300, 2300, 180, 95, 2100, 300, "enemy_thunder_wyvern", "Lightning", "Dragon")
        )
    }

    // ==========================
    // GENERATE RANDOM OPPONENT
    // ==========================

    fun generateOpponents(playerPower: Int): List<ArenaOpponent> {
        return enemyPool()
            .shuffled()
            .take(5)
            .mapIndexed { index, enemy ->

                val powerBoost = Random.nextInt(-500, 2600)

                val power =
                    (
                            playerPower +
                                    enemy.attack * 20 +
                                    enemy.defense * 15 +
                                    enemy.maxHp / 8 +
                                    powerBoost
                            ).coerceAtLeast(500)

                ArenaOpponent(
                    name = enemy.name,
                    rank = index + 1,
                    power = power,
                    hp = max(enemy.maxHp, enemy.maxHp + power / 25),
                    attack = max(enemy.attack, enemy.attack + power / 130),
                    defense = max(enemy.defense, enemy.defense + power / 200),
                    rewardGold = enemy.rewardGold,
                    rewardPoint = max(8, enemy.rewardExp / 12),
                    image = enemy.image,
                    element = enemy.element,
                    role = if (enemy.isBoss) "Boss" else enemy.enemyType
                )
            }
    }

    // ==========================
    // FIGHT LAMA — MASIH SUPPORT
    // ==========================

    fun fight(
        playerPower: Int,
        opponent: ArenaOpponent
    ): ArenaResult {
        val playerRoll =
            playerPower + Random.nextInt(50, 250)

        val enemyRoll =
            opponent.power + Random.nextInt(50, 250)

        return if (playerRoll >= enemyRoll) {
            ArenaResult(
                isVictory = true,
                message = "Victory melawan ${opponent.name}",
                rewardGold = opponent.rewardGold,
                rewardPoint = opponent.rewardPoint
            )
        } else {
            ArenaResult(
                isVictory = false,
                message = "Defeat melawan ${opponent.name}",
                rewardGold = 0,
                rewardPoint = 0
            )
        }
    }

    // ==========================
    // LIVE AUTO BATTLE DENGAN CARD
    // ==========================

    suspend fun liveFight(
        playerPower: Int,
        opponent: ArenaOpponent,
        cards: List<CardEntity>,
        onUpdate: (
            playerHp: Int,
            enemyHp: Int,
            playerMaxHp: Int,
            enemyMaxHp: Int,
            log: String,
            usedCard: CardEntity?
        ) -> Unit
    ): ArenaResult {

        val playerMaxHp =
            max(1000, playerPower / 8)

        val enemyMaxHp =
            opponent.hp

        var playerHp =
            playerMaxHp

        var enemyHp =
            enemyMaxHp

        onUpdate(
            playerHp,
            enemyHp,
            playerMaxHp,
            enemyMaxHp,
            "Pertarungan dimulai melawan ${opponent.name}",
            null
        )

        delay(700)

        repeat(14) { turn ->

            // PLAYER TURN — AUTO PILIH CARD
            val usedCard =
                cards.randomOrNull()

            val cardDamage =
                if (usedCard != null) {
                    usedCard.attack +
                            usedCard.mana * 10 +
                            Random.nextInt(25, 90)
                } else {
                    Random.nextInt(40, 100)
                }

            val playerDamage =
                max(
                    25,
                    (playerPower / 100) +
                            cardDamage -
                            opponent.defense / 3
                )

            enemyHp =
                (enemyHp - playerDamage)
                    .coerceAtLeast(0)

            onUpdate(
                playerHp,
                enemyHp,
                playerMaxHp,
                enemyMaxHp,
                if (usedCard != null)
                    "Turn ${turn + 1}: ${usedCard.name} digunakan, damage -$playerDamage"
                else
                    "Turn ${turn + 1}: Basic attack, damage -$playerDamage",
                usedCard
            )

            delay(750)

            if (enemyHp <= 0) {
                val result =
                    ArenaResult(
                        isVictory = true,
                        message = "Victory! +${opponent.rewardGold} Gold, +${opponent.rewardPoint} Point",
                        rewardGold = opponent.rewardGold,
                        rewardPoint = opponent.rewardPoint
                    )

                onUpdate(
                    playerHp,
                    enemyHp,
                    playerMaxHp,
                    enemyMaxHp,
                    result.message,
                    null
                )

                return result
            }

            // ENEMY TURN
            val enemyDamage =
                max(
                    20,
                    opponent.attack +
                            Random.nextInt(20, 80) -
                            (playerPower / 250)
                )

            playerHp =
                (playerHp - enemyDamage)
                    .coerceAtLeast(0)

            onUpdate(
                playerHp,
                enemyHp,
                playerMaxHp,
                enemyMaxHp,
                "${opponent.name} menyerang balik -$enemyDamage",
                null
            )

            delay(700)

            if (playerHp <= 0) {
                val result =
                    ArenaResult(
                        isVictory = false,
                        message = "Defeat! ${opponent.name} terlalu kuat.",
                        rewardGold = 0,
                        rewardPoint = 0
                    )

                onUpdate(
                    playerHp,
                    enemyHp,
                    playerMaxHp,
                    enemyMaxHp,
                    result.message,
                    null
                )

                return result
            }
        }

        val victory =
            playerHp >= enemyHp

        val result =
            if (victory) {
                ArenaResult(
                    isVictory = true,
                    message = "Victory by decision! +${opponent.rewardGold} Gold, +${opponent.rewardPoint} Point",
                    rewardGold = opponent.rewardGold,
                    rewardPoint = opponent.rewardPoint
                )
            } else {
                ArenaResult(
                    isVictory = false,
                    message = "Defeat by decision!",
                    rewardGold = 0,
                    rewardPoint = 0
                )
            }

        onUpdate(
            playerHp,
            enemyHp,
            playerMaxHp,
            enemyMaxHp,
            result.message,
            null
        )

        return result
    }
}