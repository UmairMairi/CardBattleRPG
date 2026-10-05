package com.pegasus.cardbattlerpg.online

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.pegasus.cardbattlerpg.entity.CardEntity
import com.pegasus.cardbattlerpg.entity.HeroEntity
import com.pegasus.cardbattlerpg.entity.PlayerEntity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.math.max
import kotlin.random.Random

class OnlineBattleManager(
    private val firebase: FirebaseOnlineManager = FirebaseOnlineManager()
) {

    private suspend fun safeRemove(path: String) {
        try { firebase.rtdb.child(path).removeValue().await() } catch (_: Exception) {}
    }

    private suspend fun safeUpdate(path: String, values: Map<String, Any>) {
        try { firebase.rtdb.child(path).updateChildren(values).await() } catch (_: Exception) {}
    }

    private suspend fun safeSet(path: String, value: Any) {
        try { firebase.rtdb.child(path).setValue(value).await() } catch (_: Exception) {}
    }
    fun currentUid(): String? = firebase.currentUid()

    private fun DataSnapshot.safeString(key: String, defaultValue: String = ""): String {
        val v = child(key).value ?: return defaultValue
        return when (v) {
            is Map<*, *> -> defaultValue
            is List<*> -> defaultValue
            else -> v.toString()
        }
    }

    private fun DataSnapshot.safeInt(key: String, defaultValue: Int = 0): Int {
        val v = child(key).value ?: return defaultValue
        return when (v) {
            is Number -> v.toInt()
            is String -> v.toIntOrNull() ?: defaultValue
            else -> defaultValue
        }
    }

    private fun DataSnapshot.safeLong(key: String, defaultValue: Long = 0L): Long {
        val v = child(key).value ?: return defaultValue
        return when (v) {
            is Number -> v.toLong()
            is String -> v.toLongOrNull() ?: defaultValue
            else -> defaultValue
        }
    }

    private fun DataSnapshot.toMatchmakingPlayerSafe(): MatchmakingPlayer? {
        val uid = safeString("uid").ifBlank { key.orEmpty() }
        if (uid.isBlank()) return null
        return MatchmakingPlayer(
            uid = uid,
            username = safeString("username", "Player"),
            heroName = safeString("heroName", safeString("hero", "Hero")),
            heroImage = safeString("heroImage", "hero_unknown"),
            power = safeInt("power", 900),
            createdAt = safeLong("createdAt", System.currentTimeMillis())
        )
    }

    private fun DataSnapshot.toOnlineBattleRoomSafe(): OnlineBattleRoom? {
        val roomId = safeString("roomId").ifBlank { key.orEmpty() }
        if (roomId.isBlank()) return null
        return OnlineBattleRoom(
            roomId = roomId,
            status = safeString("status", "waiting"),
            player1Uid = safeString("player1Uid"),
            player1Name = safeString("player1Name", safeString("player1Username", "Player 1")),
            player1Hero = safeString("player1Hero", "Hero"),
            player1HeroImage = safeString("player1HeroImage", "hero_unknown"),
            player1Hp = safeInt("player1Hp", 1000),
            player1MaxHp = safeInt("player1MaxHp", 1000).coerceAtLeast(1),
            player2Uid = safeString("player2Uid"),
            player2Name = safeString("player2Name", safeString("player2Username", "Player 2")),
            player2Hero = safeString("player2Hero", "Hero"),
            player2HeroImage = safeString("player2HeroImage", "hero_unknown"),
            player2Hp = safeInt("player2Hp", 1000),
            player2MaxHp = safeInt("player2MaxHp", 1000).coerceAtLeast(1),
            currentTurnUid = safeString("currentTurnUid"),
            winnerUid = safeString("winnerUid"),
            lastAction = safeString("lastAction"),
            lastAttackerUid = safeString("lastAttackerUid"),
            lastTargetUid = safeString("lastTargetUid"),
            lastDamage = safeInt("lastDamage", 0),
            lastCardName = safeString("lastCardName"),
            lastCardImage = safeString("lastCardImage"),
            lastCardAttackGif = safeString("lastCardAttackGif", "gif_default_attack"),
            lastSkillName = safeString("lastSkillName"),
            lastEffectElement = safeString("lastEffectElement"),
            lastCritical = child("lastCritical").value as? Boolean ?: false,
            finishedAt = safeLong("finishedAt", 0L),
            updatedAt = safeLong("updatedAt", System.currentTimeMillis())
        )
    }

    suspend fun cleanupMyFinishedRooms() {
        val uid = firebase.currentUid() ?: return
        try {
            val roomsRef = firebase.rtdb.child("battle_rooms")
            val snapshot = roomsRef.get().await()
            snapshot.children.forEach { child ->
                val room = try { child.toOnlineBattleRoomSafe() } catch (_: Exception) { null }
                if (room != null && (room.player1Uid == uid || room.player2Uid == uid)) {
                    if (room.status == "finished" || room.status == "closed" || room.status == "deleted") {
                        try { child.ref.removeValue().await() } catch (_: Exception) {}
                    }
                }
            }
        } catch (_: Exception) {
            // Jangan sampai error Firebase permission/network membuat Arena force close.
        }
    }

    suspend fun findRealtimeMatch(player: PlayerEntity, hero: HeroEntity?, power: Int): OnlineBattleRoom {
        return try {
            val uid = firebase.ensureSignedIn()
            cleanupMyFinishedRooms()
            try { firebase.uploadPlayerProfile(player, hero, power) } catch (_: Exception) {}

            val queueRef = firebase.rtdb.child("matchmaking")
            val queueSnapshot = try { queueRef.get().await() } catch (_: Exception) { null }
            val opponentSnap = queueSnapshot?.children?.firstOrNull { child ->
                child.key != uid && try { child.toMatchmakingPlayerSafe() != null } catch (_: Exception) { false }
            }

            if (opponentSnap != null) {
                val opponent = opponentSnap.toMatchmakingPlayerSafe() ?: MatchmakingPlayer(uid = opponentSnap.key.orEmpty())
                val roomId = firebase.rtdb.child("battle_rooms").push().key ?: "room_${System.currentTimeMillis()}"
                val p1MaxHp = max(900, opponent.power / 2)
                val p2MaxHp = max(900, power / 2)
                val room = OnlineBattleRoom(
                    roomId = roomId,
                    status = "active",
                    player1Uid = opponent.uid,
                    player1Name = opponent.username.ifBlank { "Player 1" },
                    player1Hero = opponent.heroName.ifBlank { "Hero" },
                    player1HeroImage = opponent.heroImage.ifBlank { "hero_unknown" },
                    player1Hp = p1MaxHp,
                    player1MaxHp = p1MaxHp,
                    player2Uid = uid,
                    player2Name = player.name.ifBlank { "Player 2" },
                    player2Hero = hero?.name ?: "Hero",
                    player2HeroImage = hero?.image ?: "hero_unknown",
                    player2Hp = p2MaxHp,
                    player2MaxHp = p2MaxHp,
                    currentTurnUid = opponent.uid,
                    lastAction = "Match ditemukan. Giliran ${opponent.username.ifBlank { "Player 1" }}",
                    updatedAt = System.currentTimeMillis()
                )
                firebase.rtdb.child("battle_rooms").child(roomId).setValue(room).await()
                registerDisconnectWin(room)
                try { queueRef.child(opponent.uid).removeValue().await() } catch (_: Exception) {}
                try { queueRef.child(uid).removeValue().await() } catch (_: Exception) {}
                room
            } else {
                val waiting = MatchmakingPlayer(
                    uid = uid,
                    username = player.name.ifBlank { "Player" },
                    heroName = hero?.name ?: "Hero",
                    heroImage = hero?.image ?: "hero_unknown",
                    power = power,
                    createdAt = System.currentTimeMillis()
                )
                try { queueRef.child(uid).setValue(waiting).await() } catch (_: Exception) {}
                try { queueRef.child(uid).onDisconnect().removeValue() } catch (_: Exception) {}
                OnlineBattleRoom(
                    roomId = "waiting",
                    status = "waiting",
                    player1Uid = uid,
                    player1Name = player.name.ifBlank { "Player" },
                    player1Hero = hero?.name ?: "Hero",
                    player1HeroImage = hero?.image ?: "hero_unknown",
                    player1Hp = max(900, power / 2),
                    player1MaxHp = max(900, power / 2),
                    currentTurnUid = uid,
                    lastAction = "Menunggu player lain masuk arena realtime"
                )
            }
        } catch (e: Exception) {
            OnlineBattleRoom(
                roomId = "error",
                status = "error",
                lastAction = "Arena online belum siap: ${e.localizedMessage ?: "network/rules error"}"
            )
        }
    }

    suspend fun cancelMatchmaking() {
        val uid = firebase.currentUid() ?: return
        try { firebase.rtdb.child("matchmaking").child(uid).removeValue().await() } catch (_: Exception) {}
    }

    fun listenMyRoom(onRoomChanged: (OnlineBattleRoom?) -> Unit): () -> Unit {
        return try {
            val uid = firebase.currentUid() ?: return {}
            val ref = firebase.rtdb.child("battle_rooms")
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val room = snapshot.children.mapNotNull { child ->
                            try { child.toOnlineBattleRoomSafe() } catch (_: Exception) { null }
                        }.firstOrNull { it.player1Uid == uid || it.player2Uid == uid }
                        onRoomChanged(room)
                    } catch (_: Exception) {
                        onRoomChanged(null)
                    }
                }
                override fun onCancelled(error: DatabaseError) { onRoomChanged(null) }
            }
            ref.addValueEventListener(listener)
            val remover: () -> Unit = { ref.removeEventListener(listener) }
            remover
        } catch (_: Exception) {
            onRoomChanged(null)
            val noop: () -> Unit = {}
            noop
        }
    }

    fun listenRoom(roomId: String, onRoomChanged: (OnlineBattleRoom?) -> Unit): () -> Unit {
        return try {
            val ref = firebase.rtdb.child("battle_rooms").child(roomId)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try { onRoomChanged(snapshot.toOnlineBattleRoomSafe()) } catch (_: Exception) { onRoomChanged(null) }
                }
                override fun onCancelled(error: DatabaseError) { onRoomChanged(null) }
            }
            ref.addValueEventListener(listener)
            val remover: () -> Unit = { ref.removeEventListener(listener) }
            remover
        } catch (_: Exception) {
            onRoomChanged(null)
            val noop: () -> Unit = {}
            noop
        }
    }

    fun observeRoom(roomId: String): Flow<OnlineBattleRoom?> = callbackFlow {
        val ref = firebase.rtdb.child("battle_rooms").child(roomId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try { trySend(snapshot.toOnlineBattleRoomSafe()) } catch (_: Exception) { trySend(null) }
            }
            override fun onCancelled(error: DatabaseError) { trySend(null) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun enterMatchmaking(playerName: String, avatar: String, power: Int, hero: HeroEntity?, deck: List<CardEntity>): String {
        val player = PlayerEntity(name = playerName, avatar = avatar)
        val room = findRealtimeMatch(player, hero, power)
        return if (room.roomId == "waiting") "QUEUE" else room.roomId
    }

    suspend fun createPrivateRoom(playerName: String, avatar: String, hero: HeroEntity?, deck: List<CardEntity>): String {
        return try {
        val uid = firebase.ensureSignedIn()
        val roomId = "private_${uid}_${System.currentTimeMillis()}"
        val p1MaxHp = 1000
        val room = OnlineBattleRoom(
            roomId = roomId,
            status = "waiting",
            player1Uid = uid,
            player1Name = playerName,
            player1Hero = hero?.name ?: "Hero",
            player1HeroImage = hero?.image ?: "hero_unknown",
            player1Hp = p1MaxHp,
            player1MaxHp = p1MaxHp,
            currentTurnUid = uid,
            lastAction = "Private room created. Waiting for opponent..."
        )
        firebase.rtdb.child("battle_rooms").child(roomId).setValue(room).await()
        roomId
        } catch (_: Exception) {
            ""
        }
    }

    suspend fun sendCardAction(roomId: String, card: CardEntity, actionType: String) {
        try {
            val roomRef = firebase.rtdb.child("battle_rooms").child(roomId)
            val snapshot = roomRef.get().await()
            val room = snapshot.toOnlineBattleRoomSafe() ?: return
            sendCardAttack(room, card, actionType)
        } catch (_: Exception) {}
    }

    suspend fun sendAttack(room: OnlineBattleRoom, skillName: String = "Basic Attack") {
        sendCardAttack(room, null, skillName)
    }

    suspend fun sendCardAttack(room: OnlineBattleRoom, card: CardEntity?, fallbackSkill: String = "Basic Attack") {
        try {
        val uid = firebase.currentUid() ?: return
        if (room.status != "active") return
        if (room.currentTurnUid != uid) return

        val isPlayer1 = uid == room.player1Uid
        val attackerName = if (isPlayer1) room.player1Name else room.player2Name
        val skillName = card?.ultimateSkillName?.takeIf { it.isNotBlank() && (card.mana >= 8 || Random.nextInt(100) < 18) }
            ?: card?.skillName?.takeIf { it.isNotBlank() }
            ?: fallbackSkill
        val attackerPower = if (isPlayer1) room.player1MaxHp * 2 else room.player2MaxHp * 2
        val targetPower = if (isPlayer1) room.player2MaxHp * 2 else room.player1MaxHp * 2
        val powerDamage = max(35, attackerPower / 42)
        val defenseReduce = max(0, targetPower / 120)
        val cardDamage = if (card != null) {
            val roleBonus = when (card.role) {
                "Assassin" -> 38
                "Mage" -> 32
                "Archer" -> 26
                "Warrior" -> 22
                "Tank" -> 14
                "Support" -> 12
                else -> 16
            }
            val rarityBonus = when (card.rarity) {
                "Legendary" -> 85
                "Epic" -> 55
                "Rare" -> 32
                else -> 18
            }
            val passiveBonus = if (card.passiveSkillName.isNotBlank()) 35 else 0
            val ultimateBonus = if (skillName == card.ultimateSkillName && card.ultimateSkillName.isNotBlank()) 95 else 0
            card.attack + (card.mana * 14) + (card.star * 10) + roleBonus + rarityBonus + passiveBonus + ultimateBonus + Random.nextInt(30, 110)
        } else {
            Random.nextInt(85, 170)
        }
        val critical = card != null && Random.nextInt(100) < (card.criticalRate + if (card.role == "Assassin") 12 else 0).coerceIn(0, 75)
        val critBonus = if (critical) max(30, card?.criticalDamage ?: 150) / 2 else 0
        val damage = max(45, cardDamage + powerDamage + critBonus - defenseReduce)

        val nextP1Hp = if (isPlayer1) room.player1Hp else (room.player1Hp - damage).coerceAtLeast(0)
        val nextP2Hp = if (isPlayer1) (room.player2Hp - damage).coerceAtLeast(0) else room.player2Hp
        val winner = when {
            nextP1Hp <= 0 -> room.player2Uid
            nextP2Hp <= 0 -> room.player1Uid
            else -> ""
        }
        val nextTurn = if (isPlayer1) room.player2Uid else room.player1Uid
        val target = if (isPlayer1) room.player2Uid else room.player1Uid
        val now = System.currentTimeMillis()
        val updated = room.copy(
            player1Hp = nextP1Hp,
            player2Hp = nextP2Hp,
            currentTurnUid = if (winner.isBlank()) nextTurn else "",
            winnerUid = winner,
            status = if (winner.isBlank()) "active" else "finished",
            lastAction = "${attackerName.ifBlank { "Player" }} menggunakan $skillName dan memberi $damage damage${if (critical) " • CRITICAL" else ""}",
            lastAttackerUid = uid,
            lastTargetUid = target,
            lastDamage = damage,
            lastCardName = card?.name.orEmpty(),
            lastCardImage = card?.image.orEmpty(),
            lastCardAttackGif = card?.attackGif?.ifBlank { "gif_${card.image.removePrefix("card_")}_attack" } ?: "gif_default_attack",
            lastSkillName = skillName,
            lastEffectElement = card?.element.orEmpty(),
            lastCritical = critical,
            finishedAt = if (winner.isBlank()) 0L else now,
            updatedAt = now
        )
        val roomRef = firebase.rtdb.child("battle_rooms").child(room.roomId)
        roomRef.setValue(updated).await()
        try {
            roomRef.child("actions").push().setValue(
                OnlineBattleAction(
                    uid = uid,
                    username = attackerName.ifBlank { "Player" },
                    type = "attack",
                    cardName = card?.name.orEmpty(),
                    skillName = skillName,
                    damage = damage,
                    targetUid = target
                )
            ).await()
        } catch (_: Exception) {}
        } catch (_: Exception) {
            // Safe guard: jangan force close saat room sudah dihapus / koneksi putus / rules menolak.
        }
    }


    fun registerDisconnectWin(room: OnlineBattleRoom) {
        val uid = firebase.currentUid() ?: return
        if (room.roomId.isBlank() || room.roomId == "waiting" || room.status != "active") return
        val opponentUid = when (uid) {
            room.player1Uid -> room.player2Uid
            room.player2Uid -> room.player1Uid
            else -> return
        }
        if (opponentUid.isBlank()) return
        val roomRef = firebase.rtdb.child("battle_rooms").child(room.roomId)
        val now = System.currentTimeMillis()
        try {
            roomRef.child("presence").child(uid).setValue(
                mapOf(
                    "uid" to uid,
                    "online" to true,
                    "updatedAt" to now
                )
            )
        } catch (_: Exception) {}
        try { roomRef.child("presence").child(uid).onDisconnect().setValue(
            mapOf(
                "uid" to uid,
                "online" to false,
                "updatedAt" to now
            )
        ) } catch (_: Exception) {}
        try { roomRef.onDisconnect().updateChildren(
            mapOf<String, Any>(
                "status" to "finished",
                "winnerUid" to opponentUid,
                "currentTurnUid" to "",
                "lastAction" to "Lawan disconnected / keluar dari arena. Player yang masih online menang otomatis.",
                "lastAttackerUid" to "system",
                "lastTargetUid" to uid,
                "lastDamage" to 0,
                "lastSkillName" to "Disconnected Victory",
                "finishedAt" to now,
                "updatedAt" to now
            )
        ) } catch (_: Exception) {}
    }

    suspend fun leaveRoomAsDefeat(room: OnlineBattleRoom) {
        try {
        val uid = firebase.currentUid() ?: return
        if (room.roomId.isBlank() || room.roomId == "waiting") {
            cancelMatchmaking()
            return
        }
        val opponentUid = when (uid) {
            room.player1Uid -> room.player2Uid
            room.player2Uid -> room.player1Uid
            else -> return
        }
        val now = System.currentTimeMillis()
        firebase.rtdb.child("battle_rooms").child(room.roomId).updateChildren(
            mapOf<String, Any>(
                "status" to "finished",
                "winnerUid" to opponentUid,
                "currentTurnUid" to "",
                "lastAction" to "Lawan keluar dari pertandingan. Player yang masih online menang otomatis.",
                "lastAttackerUid" to "system",
                "lastTargetUid" to uid,
                "lastDamage" to 0,
                "lastSkillName" to "Forfeit Victory",
                "finishedAt" to now,
                "updatedAt" to now
            )
        ).await()
        try { firebase.rtdb.child("matchmaking").child(uid).removeValue().await() } catch (_: Exception) {}
        } catch (_: Exception) {}
    }

    suspend fun deleteRoom(roomId: String) {
        if (roomId.isBlank() || roomId == "waiting" || roomId == "error") return
        try { firebase.rtdb.child("battle_rooms").child(roomId).removeValue().await() } catch (_: Exception) {}
    }
}
