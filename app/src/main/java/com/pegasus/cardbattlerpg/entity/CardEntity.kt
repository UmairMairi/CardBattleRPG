package com.pegasus.cardbattlerpg.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cards")
data class CardEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // =========================
    // BASIC
    // =========================

    val name: String,

    val title: String = "",

    val description: String = "",

    // =========================
    // IMAGE
    // =========================

    val image: String,

    val portraitImage: String = "",

    val cardFrame: String = "",

    // =========================
    // CLASSIFICATION
    // =========================

    val rarity: String,

    // Fire
    // Water
    // Nature
    // Earth
    // Wind
    // Lightning
    // Ice
    // Light
    // Dark
    val element: String,

    // Warrior
    // Mage
    // Archer
    // Assassin
    // Tank
    // Support
    val role: String = "Warrior",

    // Human
    // Dragon
    // Demon
    // Angel
    // Beast
    // Undead
    val race: String = "Human",

    // =========================
    // LEVEL SYSTEM
    // =========================

    val level: Int = 1,

    val maxLevel: Int = 100,

    val star: Int = 1,

    val maxStar: Int = 10,

    val awakenLevel: Int = 0,

    // =========================
    // STATS
    // =========================

    val attackGif: String = "gif_default_attack",

    val attack: Int,

    val defense: Int,

    val hp: Int,

    val mana: Int,

    val speed: Int = 100,

    val criticalRate: Int = 5,

    val criticalDamage: Int = 150,

    val accuracy: Int = 100,

    val dodge: Int = 0,

    val lifesteal: Int = 0,

    val blockRate: Int = 0,

    // =========================
    // SKILL
    // =========================

    val skillName: String,

    val skillDescription: String,

    val passiveSkillName: String = "",

    val passiveSkillDescription: String = "",

    val ultimateSkillName: String = "",

    val ultimateSkillDescription: String = "",

    // =========================
    // COST
    // =========================

    val summonCost: Int = 0,

    val upgradeCost: Int = 0,

    val sellPrice: Int = 0,

    // =========================
    // REWARD
    // =========================

    val expReward: Int = 0,

    val goldReward: Int = 0,

    // =========================
    // EQUIPMENT
    // =========================

    val weaponSlot: Boolean = true,

    val armorSlot: Boolean = true,

    val accessorySlot: Boolean = true,

    // =========================
    // COLLECTION
    // =========================

    val collectionBonusAttack: Int = 0,

    val collectionBonusHp: Int = 0,

    val collectionBonusDefense: Int = 0,

    // =========================
    // GAME MODE SCORE
    // =========================

    val pveRating: Int = 0,

    val pvpRating: Int = 0,

    // =========================
    // PLAYER DATA
    // =========================

    val owned: Boolean = false,

    val favorite: Boolean = false,

    val locked: Boolean = false,

    val equipped: Boolean = false,

    // =========================
    // GACHA
    // =========================

    val obtainableFromGacha: Boolean = true,

    val featuredBanner: Boolean = false,

    val dropRate: Double = 0.0,

    // =========================
    // LORE
    // =========================

    val lore: String = "",

    val voiceActor: String = "",

    // =========================
    // SYSTEM
    // =========================

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis()
)