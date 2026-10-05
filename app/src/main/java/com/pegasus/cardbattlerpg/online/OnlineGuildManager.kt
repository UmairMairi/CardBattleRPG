package com.pegasus.cardbattlerpg.online

import com.google.firebase.firestore.SetOptions
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.pegasus.cardbattlerpg.entity.PlayerEntity
import com.pegasus.cardbattlerpg.utils.ContentFilter
import kotlinx.coroutines.tasks.await

class OnlineGuildManager(
    private val firebase: FirebaseOnlineManager = FirebaseOnlineManager()
) {
    private fun Any?.safeString(defaultValue: String = ""): String {
        return when (this) {
            null -> defaultValue
            is Map<*, *> -> defaultValue
            is List<*> -> defaultValue
            else -> toString()
        }
    }

    private fun Any?.safeInt(defaultValue: Int = 0): Int {
        return when (this) {
            is Number -> toInt()
            is String -> toIntOrNull() ?: defaultValue
            else -> defaultValue
        }
    }

    suspend fun getMyGuildId(): String? {
        val uid = firebase.currentUid() ?: firebase.ensureSignedIn()

        // Primary source: users/{uid}.guildId. This avoids collectionGroup rules/index issues
        // and makes the UI respond immediately after join/create/leave.
        try {
            val userGuildId = firebase.db.collection("users")
                .document(uid)
                .get()
                .await()
                .get("guildId")
                .safeString()
            if (userGuildId.isNotBlank()) return userGuildId
        } catch (_: Exception) {
            // Continue with fallback below.
        }

        // Fallback for older data that already has members subcollections but no users.guildId yet.
        return try {
            val result = firebase.db.collectionGroup("members")
                .whereEqualTo("uid", uid)
                .limit(1)
                .get()
                .await()
            val foundGuildId = result.documents.firstOrNull()?.reference?.parent?.parent?.id
            if (!foundGuildId.isNullOrBlank()) {
                firebase.db.collection("users").document(uid)
                    .set(mapOf("guildId" to foundGuildId), SetOptions.merge())
                    .await()
            }
            foundGuildId
        } catch (_: Exception) {
            null
        }
    }

    suspend fun createGuild(name: String, description: String, player: PlayerEntity, power: Int, heroImage: String): String {
        val uid = firebase.ensureSignedIn()
        val existingGuildId = getMyGuildId()
        if (!existingGuildId.isNullOrBlank()) return "Kamu sudah berada di guild online"
        ContentFilter.validateName(name, "Nama guild")?.let { return it }
        if (description.length > ContentFilter.MAX_DESCRIPTION_LENGTH) {
            return "Deskripsi maksimal ${ContentFilter.MAX_DESCRIPTION_LENGTH} karakter"
        }
        if (ContentFilter.containsBlockedWord(description)) return "Deskripsi mengandung kata yang tidak pantas"

        val guildId = firebase.db.collection("guilds").document().id
        val guild = mapOf(
            "guildId" to guildId,
            "name" to name.ifBlank { "New Guild" },
            "guildName" to name.ifBlank { "New Guild" },
            "description" to description.ifBlank { "Card Battle RPG Guild" },
            "leaderUid" to uid,
            "leaderId" to uid,
            "leaderName" to player.name,
            "level" to 1,
            "exp" to 0,
            "goldFund" to 0,
            "memberCount" to 0,
            "maxMembers" to 30,
            "updatedAt" to System.currentTimeMillis(),
            "createdAt" to System.currentTimeMillis()
        )
        firebase.db.collection("guilds").document(guildId).set(guild, SetOptions.merge()).await()
        joinGuild(guildId, player, power, heroImage, role = "Leader")
        return "Guild online berhasil dibuat dan kamu masuk sebagai Leader"
    }

    suspend fun joinGuild(guildId: String, player: PlayerEntity, power: Int, heroImage: String, role: String = "Member"): String {
        val uid = firebase.ensureSignedIn()
        val existingGuildId = getMyGuildId()
        if (!existingGuildId.isNullOrBlank() && existingGuildId != guildId) return "Keluar guild dulu sebelum join guild lain"

        val guildRef = firebase.db.collection("guilds").document(guildId)
        val guildDoc = guildRef.get().await()
        if (!guildDoc.exists()) return "Guild online tidak ditemukan"

        val memberRef = guildRef.collection("members").document(uid)
        if (existingGuildId == guildId || memberRef.get().await().exists()) {
            val guildName = guildDoc.get("guildName").safeString(guildDoc.get("name").safeString("Online Guild"))
            firebase.db.collection("users").document(uid).set(
                mapOf(
                    "guildId" to guildId,
                    "guildName" to guildName,
                    "updatedAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            ).await()
            return "Kamu sudah berada di guild ini"
        }

        val member = mapOf(
            "uid" to uid,
            "username" to player.name,
            "avatar" to player.avatar,
            "heroImage" to heroImage,
            "power" to power,
            "role" to role,
            "donatedGold" to 0,
            "joinedAt" to System.currentTimeMillis()
        )
        memberRef.set(member, SetOptions.merge()).await()
        val currentCount = guildDoc.get("memberCount").safeInt(0)
        val guildName = guildDoc.get("guildName").safeString(guildDoc.get("name").safeString("Online Guild"))
        guildRef.set(
            mapOf(
                "guildId" to guildId,
                "memberCount" to (currentCount + 1).coerceAtLeast(1),
                "updatedAt" to System.currentTimeMillis()
            ),
            SetOptions.merge()
        ).await()
        firebase.db.collection("users").document(uid).set(
            mapOf(
                "guildId" to guildId,
                "guildName" to guildName,
                "guildRole" to role,
                "updatedAt" to System.currentTimeMillis()
            ),
            SetOptions.merge()
        ).await()
        // Chat is optional. Do not fail join/create if chat rules are not ready yet.
        try {
            sendChat(guildId, "System", "${player.name} bergabung ke guild.")
        } catch (_: Exception) {
        }
        return "Berhasil masuk guild online"
    }

    suspend fun leaveGuild(guildId: String): String {
        val uid = firebase.currentUid() ?: firebase.ensureSignedIn()
        val guildRef = firebase.db.collection("guilds").document(guildId)
        val memberRef = guildRef.collection("members").document(uid)
        if (!memberRef.get().await().exists()) return "Kamu belum masuk guild ini"
        memberRef.delete().await()
        firebase.db.collection("users").document(uid).set(
            mapOf(
                "guildId" to "",
                "guildName" to "",
                "guildRole" to "",
                "updatedAt" to System.currentTimeMillis()
            ),
            SetOptions.merge()
        ).await()
        val currentCount = guildRef.get().await().get("memberCount").safeInt(1)
        guildRef.set(
            mapOf(
                "memberCount" to (currentCount - 1).coerceAtLeast(0),
                "updatedAt" to System.currentTimeMillis()
            ),
            SetOptions.merge()
        ).await()
        return "Berhasil keluar dari guild online"
    }

    suspend fun donate(guildId: String, amount: Int, username: String): String {
        if (amount <= 0) return "Nominal donasi tidak valid"
        val uid = firebase.currentUid() ?: firebase.ensureSignedIn()
        val userRef = firebase.db.collection("users").document(uid)
        val guildRef = firebase.db.collection("guilds").document(guildId)
        val guildDoc = guildRef.get().await()
        if (!guildDoc.exists()) return "Guild tidak ditemukan"

        val userDoc = userRef.get().await()
        val currentGold = userDoc.get("gold").safeInt(Int.MAX_VALUE)
        if (currentGold != Int.MAX_VALUE && currentGold < amount) {
            return "Gold tidak cukup untuk donasi"
        }

        val oldFund = guildDoc.get("goldFund").safeInt(guildDoc.get("fund").safeInt(0))
        val oldExp = guildDoc.get("exp").safeInt(0)
        var newExp = oldExp + (amount / 10).coerceAtLeast(1)
        var newLevel = guildDoc.get("level").safeInt(1).coerceAtLeast(1)
        val requiredExp = 1000
        while (newExp >= requiredExp) {
            newExp -= requiredExp
            newLevel += 1
        }

        val batch = firebase.db.batch()
        batch.set(
            guildRef,
            mapOf(
                "goldFund" to oldFund + amount,
                "fund" to oldFund + amount,
                "exp" to newExp,
                "level" to newLevel,
                "updatedAt" to System.currentTimeMillis()
            ),
            SetOptions.merge()
        )
        if (currentGold != Int.MAX_VALUE) {
            batch.set(userRef, mapOf("gold" to (currentGold - amount).coerceAtLeast(0)), SetOptions.merge())
        }
        val memberRef = guildRef.collection("members").document(uid)
        val oldDonate = memberRef.get().await().get("donatedGold").safeInt(0)
        batch.set(memberRef, mapOf("donatedGold" to oldDonate + amount), SetOptions.merge())
        batch.commit().await()

        sendChat(guildId, username, "Donasi $amount Gold ke guild. EXP guild bertambah.")
        return if (newLevel > guildDoc.get("level").safeInt(1)) {
            "Donasi berhasil. Guild naik ke level $newLevel"
        } else {
            "Donasi berhasil. Guild EXP $newExp/$requiredExp"
        }
    }

    suspend fun sendChat(guildId: String, username: String, message: String): String {
        val uid = firebase.currentUid() ?: firebase.ensureSignedIn()
        val trimmed = message.trim()
        if (trimmed.isBlank()) return "Pesan tidak boleh kosong"
        if (trimmed.length > ContentFilter.MAX_CHAT_LENGTH) {
            return "Pesan maksimal ${ContentFilter.MAX_CHAT_LENGTH} karakter"
        }
        val cleanMessage = ContentFilter.mask(trimmed)
        val data = mapOf(
            "uid" to uid,
            "username" to username.ifBlank { "Player" },
            "message" to cleanMessage,
            "createdAt" to System.currentTimeMillis()
        )

        // Primary chat uses Realtime Database so messages appear instantly and match existing guild_chat rules.
        FirebaseDatabase.getInstance().reference
            .child("guild_chat")
            .child(guildId)
            .push()
            .setValue(data)
            .await()

        // Optional Firestore mirror. Ignore failure so restrictive Firestore chat rules do not break Send button.
        try {
            firebase.db.collection("guilds").document(guildId)
                .collection("chat").add(data).await()
        } catch (_: Exception) {
        }
        return "Chat guild terkirim"
    }

    fun listenGuilds(onChanged: (List<Map<String, Any>>) -> Unit): () -> Unit {
        return try {
            val listener = firebase.db.collection("guilds")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        onChanged(emptyList())
                        return@addSnapshotListener
                    }
                    val rows = snapshot?.documents?.map { doc ->
                        val data = doc.data.orEmpty().toMutableMap()
                        val id = data["guildId"].safeString(doc.id).ifBlank { doc.id }
                        val name = data["name"].safeString().ifBlank { data["guildName"].safeString("Realtime Guild") }
                        data["docId"] = doc.id
                        data["guildId"] = id
                        data["name"] = name
                        data["leaderName"] = data["leaderName"].safeString("-")
                        data["memberCount"] = data["memberCount"].safeInt(0)
                        data
                    }?.sortedWith(compareByDescending<Map<String, Any>> { it["level"].safeInt(1) }.thenBy { it["name"].safeString() }).orEmpty()
                    onChanged(rows)
                }
            val remover: () -> Unit = { listener.remove() }
            remover
        } catch (_: Exception) {
            onChanged(emptyList())
            val fallback: () -> Unit = {}
            fallback
        }
    }

    fun listenMembers(guildId: String, onChanged: (List<Map<String, Any>>) -> Unit): () -> Unit {
        if (guildId.isBlank()) return {}
        return try {
            val listener = firebase.db.collection("guilds").document(guildId)
                .collection("members")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        onChanged(emptyList())
                        return@addSnapshotListener
                    }
                    val rows = snapshot?.documents?.map { it.data.orEmpty() }
                        ?.sortedByDescending { it["power"].safeInt(0) }.orEmpty()
                    onChanged(rows)
                }
            val remover: () -> Unit = { listener.remove() }
            remover
        } catch (_: Exception) {
            onChanged(emptyList())
            val fallback: () -> Unit = {}
            fallback
        }
    }

    fun listenGuildChat(guildId: String, onChanged: (List<Map<String, Any>>) -> Unit): () -> Unit {
        if (guildId.isBlank()) return {}
        return try {
            val ref = FirebaseDatabase.getInstance().reference
                .child("guild_chat")
                .child(guildId)
                .limitToLast(50)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val rows = snapshot.children.mapNotNull { child ->
                        val value = child.value as? Map<*, *> ?: return@mapNotNull null
                        value.entries.associate { it.key.toString() to (it.value ?: "") } +
                            ("id" to child.key.orEmpty())
                    }.sortedByDescending { it["createdAt"].safeInt(0) }
                    onChanged(rows)
                }

                override fun onCancelled(error: DatabaseError) {
                    onChanged(emptyList())
                }
            }
            ref.addValueEventListener(listener)
            val remover: () -> Unit = { ref.removeEventListener(listener) }
            remover
        } catch (_: Exception) {
            onChanged(emptyList())
            val fallback: () -> Unit = {}
            fallback
        }
    }
}
