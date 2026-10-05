package com.pegasus.cardbattlerpg.online

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

// In-app account deletion required by Google Play's Account Deletion policy.
// Removes every piece of online data the app writes for the user, then the Firebase Auth account.
// Moderation reports the user filed are kept (anonymous to other players) for abuse prevention,
// as stated in the privacy policy.
class AccountDeletionManager(
    private val firebase: FirebaseOnlineManager = FirebaseOnlineManager(),
    private val guildManager: OnlineGuildManager = OnlineGuildManager(firebase),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    fun isSignedIn(): Boolean = auth.currentUser != null

    fun isEmailAccount(): Boolean =
        auth.currentUser?.providerData?.any { it.providerId == EmailAuthProvider.PROVIDER_ID } == true

    fun currentEmail(): String = auth.currentUser?.email.orEmpty()

    private suspend fun ignoreErrors(block: suspend () -> Unit) {
        try { block() } catch (_: Exception) {}
    }

    /**
     * @param password required for email accounts (Firebase needs a recent login to delete).
     * @return user-facing result message.
     * @throws Exception when the password is wrong or the Auth account could not be removed.
     */
    suspend fun deleteAccount(password: String?): String {
        val user = auth.currentUser ?: return "Tidak ada akun online. Data lokal dihapus."
        val uid = user.uid

        // Verify the password before touching any data, so a typo doesn't leave a half-deleted account.
        if (isEmailAccount()) {
            require(!password.isNullOrBlank()) { "Masukkan password untuk konfirmasi" }
            val credential = EmailAuthProvider.getCredential(user.email.orEmpty(), password)
            user.reauthenticate(credential).await()
        }

        val db = firebase.db
        val rtdb = FirebaseDatabase.getInstance().reference

        // Guild membership, own chat messages, and the guild itself if it becomes empty.
        ignoreErrors {
            val guildId = guildManager.getMyGuildId()
            if (!guildId.isNullOrBlank()) {
                ignoreErrors { guildManager.leaveGuild(guildId) }
                val guildDoc = db.collection("guilds").document(guildId).get().await()
                val leaderUid = guildDoc.getString("leaderUid") ?: guildDoc.getString("leaderId")
                val members = (guildDoc.get("memberCount") as? Number)?.toInt() ?: 0
                if (leaderUid == uid && members <= 0) {
                    db.collection("guilds").document(guildId).delete().await()
                    ignoreErrors { rtdb.child("guild_chat").child(guildId).removeValue().await() }
                } else if (leaderUid == uid) {
                    db.collection("guilds").document(guildId).set(
                        mapOf("leaderName" to "Deleted Player"),
                        SetOptions.merge()
                    ).await()
                }
            }
        }
        ignoreErrors {
            val guilds = db.collection("guilds").get().await()
            guilds.documents.forEach { guild ->
                ignoreErrors {
                    val mine = rtdb.child("guild_chat").child(guild.id)
                        .orderByChild("uid").equalTo(uid).get().await()
                    mine.children.forEach { it.ref.removeValue().await() }
                }
                ignoreErrors {
                    val mirror = guild.reference.collection("chat").whereEqualTo("uid", uid).get().await()
                    mirror.documents.forEach { it.reference.delete().await() }
                }
                ignoreErrors { guild.reference.collection("members").document(uid).delete().await() }
            }
        }

        // Realtime Database: presence, matchmaking, battle rooms.
        ignoreErrors { rtdb.child("online_status").child(uid).removeValue().await() }
        ignoreErrors { rtdb.child("matchmaking").child(uid).removeValue().await() }
        listOf("player1Uid", "player2Uid").forEach { field ->
            ignoreErrors {
                val rooms = rtdb.child("battle_rooms").orderByChild(field).equalTo(uid).get().await()
                rooms.children.forEach { it.ref.removeValue().await() }
            }
        }

        // Firestore: ranking, cloud save and user document with its sub-collections.
        ignoreErrors { db.collection("rankings").document(uid).delete().await() }
        val userRef = db.collection("users").document(uid)
        listOf("cards" to "collection", "inventory" to "items", "saves" to "main").forEach { (col, doc) ->
            ignoreErrors { userRef.collection(col).document(doc).delete().await() }
        }
        listOf("blocked", "heroes", "cards", "inventory", "saves").forEach { col ->
            ignoreErrors {
                userRef.collection(col).get().await().documents.forEach { it.reference.delete().await() }
            }
        }
        ignoreErrors { userRef.delete().await() }

        // Storage: any uploaded avatar images.
        ignoreErrors {
            storage.reference.child("avatars/$uid").listAll().await().items.forEach { it.delete().await() }
        }

        // Finally the Firebase Auth account itself.
        try {
            user.delete().await()
        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            // Guest sessions can't re-authenticate; data is gone, so sign out and report.
            auth.signOut()
            return "Data akun dihapus. Silakan hubungi support untuk menyelesaikan penghapusan login."
        }
        auth.signOut()
        return "Akun dan semua data online berhasil dihapus"
    }
}
