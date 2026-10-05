package com.pegasus.cardbattlerpg.ui.battle

import com.pegasus.cardbattlerpg.entity.CardEntity
import com.pegasus.cardbattlerpg.entity.EquipmentEntity
import com.pegasus.cardbattlerpg.entity.HeroEntity
import com.pegasus.cardbattlerpg.model.BattleEnemy
import kotlin.math.max
import kotlin.random.Random



data class BattleActionResult(
    val damage: Int,
    val heal: Int = 0,
    val selfDamage: Int = 0,
    val isCritical: Boolean = false,
    val isUltimate: Boolean = false,
    val logText: String,
    val effectName: String
)

object BattleEngine {

    fun createEnemy(): BattleEnemy {
        return randomEnemyByLevel(1)
    }

    fun enemyPool(): List<BattleEnemy> {
        return listOf(

            // ==========================
            // CHAPTER 1 — AWAKENING FOREST
            // ==========================

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

            // ==========================
            // CHAPTER 2 — CRYSTAL RUINS
            // ==========================

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

            // ==========================
            // CHAPTER 3 — DRAGON KINGDOM
            // ==========================

            BattleEnemy("Dragon Scout", 21, 5000, 5000, 350, 280, 5000, 850, "enemy_dragon_scout", "Fire", "Dragon"),
            BattleEnemy("Fire Drake", 22, 5400, 5400, 380, 300, 5500, 900, "enemy_fire_drake", "Fire", "Dragon"),
            BattleEnemy("Dragon Hatchling", 23, 6000, 6000, 410, 330, 6200, 980, "enemy_dragon_hatchling", "Fire", "Elite"),
            BattleEnemy("Sky Guardian", 24, 6700, 6700, 450, 360, 7000, 1100, "enemy_sky_guardian", "Wind", "Normal"),
            BattleEnemy("Dragon Lord", 30, 8000, 8000, 520, 420, 10000, 1500, "enemy_dragon_lord", "Fire", "Boss", true),

            // ==========================
            // EXTRA RANDOM BATTLE ENEMY
            // ==========================

            BattleEnemy("Shadow Beast", 1, 650, 650, 55, 25, 350, 60, "enemy_shadow_beast", "Dark", "Normal"),
            BattleEnemy("Skeleton Soldier", 4, 850, 850, 78, 38, 720, 105, "enemy_skeleton_soldier", "Dark", "Undead"),
            BattleEnemy("Ice Wraith", 8, 1800, 1800, 145, 80, 1550, 220, "enemy_ice_wraith", "Ice", "Elite"),
            BattleEnemy("Thunder Wyvern", 10, 2300, 2300, 180, 95, 2100, 300, "enemy_thunder_wyvern", "Lightning", "Dragon")
        )
    }

    fun randomEnemyByLevel(level: Int): BattleEnemy {
        val pool = enemyPool().filter { it.level <= level + 2 && !it.isBoss }

        return if (pool.isNotEmpty()) {
            pool.random()
        } else {
            enemyPool().first()
        }
    }

    fun calculateHeroMaxHp(
        hero: HeroEntity,
        equipments: List<EquipmentEntity>
    ): Int {
        val flat = equipments.sumOf { it.hpBonus }
        val armorBoost = equipments.filter { it.equipped && it.type == "Armor" }.sumOf { rarityBonusPercent(it.rarity) }
        val amuletBoost = equipments.filter { it.equipped && it.type == "Amulet" }.sumOf { rarityBonusPercent(it.rarity) / 2 }
        return ((hero.hp + flat) * (100 + armorBoost + amuletBoost) / 100f).toInt().coerceAtLeast(1)
    }

    fun calculateHeroAttack(
        hero: HeroEntity,
        equipments: List<EquipmentEntity>
    ): Int {
        val flat = equipments.sumOf { it.attackBonus }
        val weaponBoost = equipments.filter { it.equipped && it.type == "Weapon" }.sumOf { rarityBonusPercent(it.rarity) }
        val ringBoost = equipments.filter { it.equipped && it.type == "Ring" }.sumOf { rarityBonusPercent(it.rarity) / 2 }
        return ((hero.attack + flat) * (100 + weaponBoost + ringBoost) / 100f).toInt().coerceAtLeast(1)
    }

    fun calculateHeroDefense(
        hero: HeroEntity,
        equipments: List<EquipmentEntity>
    ): Int {
        val flat = equipments.sumOf { it.defenseBonus }
        val armorBoost = equipments.filter { it.equipped && it.type == "Armor" }.sumOf { rarityBonusPercent(it.rarity) }
        val bootsBoost = equipments.filter { it.equipped && it.type == "Boots" }.sumOf { rarityBonusPercent(it.rarity) / 2 }
        return ((hero.defense + flat) * (100 + armorBoost + bootsBoost) / 100f).toInt().coerceAtLeast(0)
    }

    private fun rarityBonusPercent(rarity: String): Int {
        return when (rarity) {
            "Legendary" -> 18
            "Epic" -> 12
            "Rare" -> 8
            else -> 4
        }
    }

    fun calculateCardDamage(
        heroAttack: Int,
        card: CardEntity,
        enemyDefense: Int
    ): Int {
        return resolveCardAction(heroAttack, card, BattleEnemy("",1,1,1,1, enemyDefense,0,0), 1, 1, 0).damage
    }

    fun resolveCardAction(
        heroAttack: Int,
        card: CardEntity,
        enemy: BattleEnemy,
        heroCurrentHp: Int,
        heroMaxHp: Int,
        comboCount: Int
    ): BattleActionResult {
        val elementMultiplier = elementMultiplier(card.element, enemy.element)
        val roleBonus = when (card.role) {
            "Assassin" -> 18
            "Mage" -> 14
            "Archer" -> 10
            "Warrior" -> 8
            else -> 0
        }
        val passiveBonus = when {
            card.passiveSkillName.isNotBlank() && card.element == "Dark" -> 14
            card.passiveSkillName.isNotBlank() && card.element == "Fire" -> 12
            card.passiveSkillName.isNotBlank() -> 8
            else -> 0
        }
        val ultimateReady = comboCount >= 2 || card.mana >= 8 || card.ultimateSkillName.isNotBlank() && Random.nextInt(100) < 18
        val ultimateBonus = if (ultimateReady) 55 else 0
        val critChance = (card.criticalRate + if (card.role == "Assassin") 14 else 0).coerceIn(0, 75)
        val critical = Random.nextInt(100) < critChance

        var raw = heroAttack + card.attack + Random.nextInt(8, 24)
        raw = (raw * elementMultiplier / 100f).toInt()
        raw = (raw * (100 + roleBonus + passiveBonus + ultimateBonus) / 100f).toInt()
        if (critical) raw = (raw * card.criticalDamage / 100f).toInt()

        val damage = max(1, raw - enemy.defense)
        val healByRole = when (card.role) {
            "Support" -> (heroMaxHp * 0.16f).toInt()
            "Tank" -> (heroMaxHp * 0.07f).toInt()
            else -> 0
        }
        val lifestealHeal = if (card.lifesteal > 0) damage * card.lifesteal / 100 else 0
        val lightHeal = if (card.element == "Light") (heroMaxHp * 0.05f).toInt() else 0
        val heal = (healByRole + lifestealHeal + lightHeal).coerceAtLeast(0)

        val effect = if (ultimateReady && card.ultimateSkillName.isNotBlank()) card.ultimateSkillName else card.skillName.ifBlank { card.name }
        val parts = mutableListOf<String>()
        parts += "${card.name} memakai $effect dan memberi $damage damage"
        if (critical) parts += "CRITICAL"
        if (ultimateReady) parts += "ULTIMATE"
        if (heal > 0) parts += "heal +$heal HP"

        return BattleActionResult(
            damage = damage,
            heal = heal,
            isCritical = critical,
            isUltimate = ultimateReady,
            logText = parts.joinToString(" • "),
            effectName = effect
        )
    }

    private fun elementMultiplier(cardElement: String, enemyElement: String): Int {
        return when (cardElement to enemyElement) {
            "Fire" to "Nature", "Water" to "Fire", "Nature" to "Water",
            "Earth" to "Lightning", "Lightning" to "Water", "Ice" to "Wind",
            "Wind" to "Earth", "Light" to "Dark", "Dark" to "Light" -> 125
            "Nature" to "Fire", "Fire" to "Water", "Water" to "Nature",
            "Lightning" to "Earth", "Dark" to "Dark", "Light" to "Light" -> 85
            else -> 100
        }
    }

    fun calculateEnemyDamage(
        enemyAttack: Int,
        heroDefense: Int
    ): Int {
        val baseDamage = enemyAttack + Random.nextInt(3, 14)
        return max(1, baseDamage - heroDefense)
    }

    fun createEnemyFromStage(
        stage: com.pegasus.cardbattlerpg.entity.StoryStageEntity
    ): BattleEnemy {
        val imageName =
            if (stage.isBoss && stage.bossImage.isNotBlank()) {
                stage.bossImage
            } else {
                "enemy_" + stage.enemyName
                    .lowercase()
                    .replace(" ", "_")
                    .replace("-", "_")
            }

        return BattleEnemy(
            name = stage.enemyName,
            level = stage.stage,
            maxHp = stage.enemyHp,
            currentHp = stage.enemyHp,
            attack = stage.enemyAttack,
            defense = stage.enemyDefense,
            rewardGold = stage.rewardGold,
            rewardExp = stage.rewardExp,
            image = imageName,
            element = "Dark",
            enemyType = if (stage.isBoss) "Boss" else if (stage.isElite) "Elite" else "Normal",
            isBoss = stage.isBoss
        )
    }
}