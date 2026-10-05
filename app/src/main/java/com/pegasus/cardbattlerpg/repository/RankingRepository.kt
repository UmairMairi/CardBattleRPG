package com.pegasus.cardbattlerpg.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pegasus.cardbattlerpg.model.RankingPlayer
import com.pegasus.cardbattlerpg.online.FirebaseOnlineManager
import kotlinx.coroutines.tasks.await

class RankingRepository(
    private val firebase: FirebaseOnlineManager = FirebaseOnlineManager(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun uploadRanking(playerId: String, ranking: RankingPlayer): String {
        return try {
            val uid = firebase.ensureSignedIn()
            val rankingWithUid = ranking.copy(uid = uid, updatedAt = System.currentTimeMillis())
            firestore.collection("rankings").document(uid).set(rankingWithUid).await()
            "Ranking realtime berhasil diupload"
        } catch (e: Exception) {
            "Gagal upload ranking: ${e.localizedMessage ?: "Online error"}"
        }
    }

    suspend fun getTopRanking(): List<RankingPlayer> {
        return try {
            val snapshot = firestore.collection("rankings")
                .orderBy("power", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc -> doc.toObject(RankingPlayer::class.java)?.copy(uid = doc.id) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun listenTopRanking(onChanged: (List<RankingPlayer>) -> Unit): () -> Unit {
        val listener = firestore.collection("rankings")
            .orderBy("power", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onChanged(emptyList())
                    return@addSnapshotListener
                }
                val rows = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(RankingPlayer::class.java)?.copy(uid = doc.id)
                }.orEmpty()
                onChanged(rows)
            }
        return { listener.remove() }
    }
}
