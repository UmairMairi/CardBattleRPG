package com.pegasus.cardbattlerpg.online

import kotlinx.coroutines.tasks.await

// Reporting and blocking for user generated content (Google Play UGC policy).
// Reports land in Firestore `reports/` and must be reviewed by the developer
// (Firebase console → Firestore → reports), ideally within 24 hours.
class ModerationManager(
    private val firebase: FirebaseOnlineManager = FirebaseOnlineManager()
) {
    companion object {
        val REPORT_REASONS = listOf(
            "Spam / iklan",
            "Kata kasar / pelecehan",
            "Ujaran kebencian",
            "Konten seksual",
            "Penipuan / scam",
            "Lainnya"
        )
    }

    suspend fun report(
        type: String,
        reason: String,
        reportedUid: String,
        reportedName: String,
        content: String,
        guildId: String = "",
        messageId: String = ""
    ): String {
        val uid = firebase.ensureSignedIn()
        firebase.db.collection("reports").add(
            mapOf(
                "type" to type,
                "reason" to reason,
                "reporterUid" to uid,
                "reportedUid" to reportedUid,
                "reportedName" to reportedName,
                "content" to content.take(500),
                "guildId" to guildId,
                "messageId" to messageId,
                "status" to "open",
                "createdAt" to System.currentTimeMillis()
            )
        ).await()
        return "Laporan terkirim. Terima kasih, tim kami akan meninjaunya."
    }

    suspend fun blockUser(blockedUid: String, blockedName: String): String {
        val uid = firebase.ensureSignedIn()
        if (blockedUid.isBlank() || blockedUid == uid) return "Player tidak valid"
        firebase.db.collection("users").document(uid)
            .collection("blocked").document(blockedUid)
            .set(
                mapOf(
                    "uid" to blockedUid,
                    "username" to blockedName,
                    "blockedAt" to System.currentTimeMillis()
                )
            ).await()
        return "$blockedName diblokir. Pesan mereka tidak akan ditampilkan lagi."
    }

    suspend fun unblockUser(blockedUid: String): String {
        val uid = firebase.ensureSignedIn()
        firebase.db.collection("users").document(uid)
            .collection("blocked").document(blockedUid)
            .delete().await()
        return "Blokir dibuka"
    }

    fun listenBlocked(onChanged: (List<Map<String, Any>>) -> Unit): () -> Unit {
        val uid = firebase.currentUid() ?: return {}
        return try {
            val listener = firebase.db.collection("users").document(uid)
                .collection("blocked")
                .addSnapshotListener { snapshot, _ ->
                    onChanged(snapshot?.documents?.map { it.data.orEmpty() }.orEmpty())
                }
            val remover: () -> Unit = { listener.remove() }
            remover
        } catch (_: Exception) {
            val fallback: () -> Unit = {}
            fallback
        }
    }
}
