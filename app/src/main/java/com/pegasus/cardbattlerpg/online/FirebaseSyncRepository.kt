package com.pegasus.cardbattlerpg.online

import com.pegasus.cardbattlerpg.repository.GameRepository

class FirebaseSyncRepository(
    private val gameRepository: GameRepository,
    private val firebase: FirebaseOnlineManager = FirebaseOnlineManager()
) {
    suspend fun syncFullSave(): String {
        return try {
            val player = gameRepository.getPlayer() ?: return "Player belum tersedia"
            val hero = gameRepository.getSelectedHero()
            val cards = gameRepository.getOwnedCards()
            val ranking = gameRepository.buildRankingPlayer()
            firebase.uploadSave(player = player, selectedHero = hero, ownedCards = cards, ranking = ranking)
        } catch (e: Exception) {
            "Gagal sync online: ${e.localizedMessage ?: "Online error"}"
        }
    }

    suspend fun syncRanking(): String {
        return try {
            val ranking = gameRepository.buildRankingPlayer() ?: return "Ranking belum tersedia"
            val hero = gameRepository.getSelectedHero()
            firebase.uploadRanking(ranking, hero)
        } catch (e: Exception) {
            "Gagal sync ranking: ${e.localizedMessage ?: "Online error"}"
        }
    }

    suspend fun syncOnlineProfile(): String {
        return try {
            val player = gameRepository.getPlayer() ?: return "Player belum tersedia"
            val hero = gameRepository.getSelectedHero()
            val power = gameRepository.calculatePlayerPower()
            firebase.uploadPlayerProfile(player, hero, power)
            "Profile online berhasil disinkronkan"
        } catch (e: Exception) {
            "Gagal sync profile: ${e.localizedMessage ?: "Online error"}"
        }
    }
}
