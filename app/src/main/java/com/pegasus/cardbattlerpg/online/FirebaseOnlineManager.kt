package com.pegasus.cardbattlerpg.online

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.pegasus.cardbattlerpg.entity.CardEntity
import com.pegasus.cardbattlerpg.entity.HeroEntity
import com.pegasus.cardbattlerpg.entity.PlayerEntity
import com.pegasus.cardbattlerpg.model.RankingPlayer
import kotlinx.coroutines.tasks.await

class FirebaseOnlineManager(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val realtimeDb: FirebaseDatabase = FirebaseDatabase.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    suspend fun ensureSignedIn(): String {
        val current = auth.currentUser
        if (current != null) return current.uid
        val result = auth.signInAnonymously().await()
        return result.user?.uid ?: error("Login online gagal")
    }

    fun currentUid(): String? = auth.currentUser?.uid
    fun isLoggedIn(): Boolean = auth.currentUser != null

    suspend fun registerWithEmail(username: String, email: String, password: String): String {
        require(username.isNotBlank()) { "Nama player tidak boleh kosong" }
        require(email.isNotBlank()) { "Email tidak boleh kosong" }
        require(password.length >= 6) { "Password minimal 6 karakter" }

        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val uid = result.user?.uid ?: error("Register online gagal")
        createOrUpdateUserDocument(uid, username.trim(), email.trim(), loginType = "email")
        return uid
    }

    suspend fun loginWithEmail(email: String, password: String): String {
        require(email.isNotBlank()) { "Email tidak boleh kosong" }
        require(password.isNotBlank()) { "Password tidak boleh kosong" }

        val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
        val uid = result.user?.uid ?: error("Login online gagal")
        firestore.collection("users").document(uid).update(
            mapOf(
                "lastLoginAt" to System.currentTimeMillis(),
                "online" to true
            )
        ).await()
        return uid
    }

    suspend fun loginAsGuest(username: String): String {
        val result = auth.signInAnonymously().await()
        val uid = result.user?.uid ?: error("Guest online gagal")
        createOrUpdateUserDocument(uid, username.ifBlank { "Guest Player" }, "", loginType = "guest")
        return uid
    }

    suspend fun createOrUpdateUserDocument(uid: String, username: String, email: String, loginType: String) {
        val data = hashMapOf(
            "uid" to uid,
            "username" to username,
            "email" to email,
            "loginType" to loginType,
            "level" to 1,
            "gold" to 1000,
            "diamond" to 100,
            "avatar" to "avatar_1",
            "selectedHero" to "hero_unknown",
            "online" to true,
            "createdAt" to System.currentTimeMillis(),
            "lastLoginAt" to System.currentTimeMillis(),
            "updatedAt" to System.currentTimeMillis()
        )
        firestore.collection("users").document(uid).set(data).await()
        realtimeDb.reference.child("online_status").child(uid).setValue(data).await()
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun updateOnlineStatus(username: String, online: Boolean = true) {
        val uid = ensureSignedIn()
        realtimeDb.reference.child("online_status").child(uid).setValue(
            mapOf(
                "uid" to uid,
                "username" to username,
                "online" to online,
                "updatedAt" to System.currentTimeMillis()
            )
        ).await()
    }

    suspend fun signInGuest(): String = ensureSignedIn()

    suspend fun savePlayerProfile(player: PlayerEntity, power: Int) {
        uploadPlayerProfile(player, null, power)
    }

    suspend fun saveCards(cards: List<CardEntity>) {
        val uid = ensureSignedIn()
        firestore.collection("users").document(uid).collection("cards").document("collection")
            .set(mapOf("list" to cards)).await()
    }

    suspend fun saveInventory(inventory: List<Any>) {
        val uid = ensureSignedIn()
        firestore.collection("users").document(uid).collection("inventory").document("items")
            .set(mapOf("list" to inventory)).await()
    }

    suspend fun uploadPlayerProfile(
        player: PlayerEntity,
        selectedHero: HeroEntity?,
        power: Int
    ): String {
        val uid = ensureSignedIn()
        val profile = OnlinePlayerProfile(
            uid = uid,
            username = player.name,
            heroName = selectedHero?.name ?: "No Hero",
            heroImage = selectedHero?.image ?: "hero_unknown",
            level = player.level,
            power = power,
            arenaPoint = player.arenaPoint,
            online = true
        )
        firestore.collection("users").document(uid).set(profile, com.google.firebase.firestore.SetOptions.merge()).await()
        realtimeDb.reference.child("online_status").child(uid).setValue(profile).await()
        return uid
    }

    suspend fun uploadRanking(ranking: RankingPlayer, selectedHero: HeroEntity?): String {
        val uid = ensureSignedIn()
        val data = hashMapOf(
            "uid" to uid,
            "playerName" to ranking.playerName,
            "username" to ranking.playerName,
            "level" to ranking.level,
            "power" to ranking.power,
            "gold" to ranking.gold,
            "diamond" to ranking.diamond,
            "ownedCards" to ranking.ownedCards,
            "completedStages" to ranking.completedStages,
            "arenaPoint" to ranking.arenaPoint,
            "heroName" to (selectedHero?.name ?: ranking.heroName.ifBlank { "No Hero" }),
            "heroImage" to (selectedHero?.image ?: ranking.heroImage.ifBlank { "hero_unknown" }),
            "updatedAt" to System.currentTimeMillis()
        )
        firestore.collection("rankings").document(uid).set(data).await()
        return "Ranking berhasil diupload realtime"
    }

    fun listenRankings(onChanged: (List<Map<String, Any>>) -> Unit): () -> Unit {
        val listener = firestore.collection("rankings")
            .orderBy("power", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onChanged(emptyList())
                    return@addSnapshotListener
                }
                val rows = snapshot?.documents?.map { it.data.orEmpty() }.orEmpty()
                onChanged(rows)
            }
        return { listener.remove() }
    }

    suspend fun uploadSave(
        player: PlayerEntity,
        selectedHero: HeroEntity?,
        ownedCards: List<CardEntity>,
        ranking: RankingPlayer?
    ): String {
        val uid = ensureSignedIn()
        val save = OnlineSaveData(
            uid = uid,
            player = player,
            selectedHero = selectedHero,
            ownedCards = ownedCards,
            ranking = ranking
        )
        firestore.collection("users").document(uid)
            .collection("saves").document("main")
            .set(save)
            .await()
        uploadPlayerProfile(player, selectedHero, ranking?.power ?: 0)
        return "Cloud save berhasil disimpan"
    }

    fun listenSave(uid: String, onChanged: (Map<String, Any>?) -> Unit): () -> Unit {
        val listener = firestore.collection("users").document(uid)
            .collection("saves").document("main")
            .addSnapshotListener { doc, _ -> onChanged(doc?.data) }
        return { listener.remove() }
    }

    suspend fun uploadAvatar(uri: Uri): String {
        val uid = ensureSignedIn()
        val ref = storage.reference.child("avatars/$uid/avatar_${System.currentTimeMillis()}.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    val rtdb get() = realtimeDb.reference
    val db get() = firestore
}
