package com.pegasus.cardbattlerpg.ui.summon

import kotlin.random.Random

object SummonManager {

    // ==========================
    // GACHA RATE STYLE GENSHIN
    // ==========================

    private const val LEGENDARY_BASE_RATE = 0.6
    private const val EPIC_BASE_RATE = 5.1

    private const val LEGENDARY_SOFT_PITY_START = 75
    private const val LEGENDARY_HARD_PITY = 90

    private const val EPIC_HARD_PITY = 10

    fun summonCost(): Int = 160

    fun rollRarity(
        legendaryPity: Int,
        epicPity: Int
    ): String {

        // Hard pity Legendary
        if (legendaryPity >= LEGENDARY_HARD_PITY - 1) {
            return "Legendary"
        }

        // Hard pity Epic
        if (epicPity >= EPIC_HARD_PITY - 1) {
            return rollEpicOrHigher(
                legendaryPity = legendaryPity
            )
        }

        val legendaryRate = calculateLegendaryRate(
            pity = legendaryPity
        )

        val roll = Random.nextDouble(0.0, 100.0)

        return when {
            roll < legendaryRate -> "Legendary"
            roll < legendaryRate + EPIC_BASE_RATE -> "Epic"
            roll < legendaryRate + EPIC_BASE_RATE + 20.0 -> "Rare"
            else -> "Common"
        }
    }

    private fun rollEpicOrHigher(
        legendaryPity: Int
    ): String {
        val legendaryRate = calculateLegendaryRate(
            pity = legendaryPity
        )

        val roll = Random.nextDouble(0.0, 100.0)

        return if (roll < legendaryRate) {
            "Legendary"
        } else {
            "Epic"
        }
    }

    private fun calculateLegendaryRate(
        pity: Int
    ): Double {
        if (pity < LEGENDARY_SOFT_PITY_START) {
            return LEGENDARY_BASE_RATE
        }

        val softPityBonus =
            (pity - LEGENDARY_SOFT_PITY_START + 1) * 6.0

        return (LEGENDARY_BASE_RATE + softPityBonus)
            .coerceAtMost(100.0)
    }
}