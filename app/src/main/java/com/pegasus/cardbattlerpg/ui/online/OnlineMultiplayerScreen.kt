package com.pegasus.cardbattlerpg.ui.online

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pegasus.cardbattlerpg.entity.CardEntity
import com.pegasus.cardbattlerpg.entity.HeroEntity
import com.pegasus.cardbattlerpg.online.FirebaseOnlineManager
import com.pegasus.cardbattlerpg.online.OnlineBattleManager
import com.pegasus.cardbattlerpg.online.OnlineBattleRoom
import com.pegasus.cardbattlerpg.repository.GameRepository
import kotlinx.coroutines.launch

@Composable
fun OnlineMultiplayerScreen(
    repository: GameRepository,
    onBack: () -> Unit,
    onOpenRoom: (String) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val onlineManager = remember { FirebaseOnlineManager() }
    val battleManager = remember { OnlineBattleManager(onlineManager) }

    var playerName by remember { mutableStateOf("Guest Player") }
    var message by remember { mutableStateOf("Online Ready") }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val p = repository.getPlayer()
        playerName = p?.name ?: "Guest Player"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF050008), Color(0xFF220035), Color(0xFF050008))
                )
            )
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                border = BorderStroke(1.dp, Color(0x88FFD66B)),
                colors = CardDefaults.cardColors(containerColor = Color(0xDD12001F))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFF100019), Color(0xFF4B1478), Color(0xFF12001F)))
                        )
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onBack, shape = RoundedCornerShape(16.dp)) {
                        Text("Back")
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Online Multiplayer", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Black)
                        Text(message, color = Color(0xFFFFD66B), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            OnlineActionButton(
                title = "Login Guest + Upload Cloud Save",
                subtitle = "Menyimpan Player, Cards, Inventory ke Firestore",
                enabled = !loading
            ) {
                scope.launch {
                    loading = true
                    message = try {
                        onlineManager.signInGuest()
                        val player = repository.getPlayer()
                        val power = repository.calculatePlayerPower()
                        if (player != null) onlineManager.savePlayerProfile(player, power)
                        onlineManager.saveCards(repository.getOwnedCards())
                        onlineManager.saveInventory(repository.getInventory())
                        "Cloud save berhasil aktif"
                    } catch (e: Exception) {
                        "Online error: ${e.message}"
                    }
                    loading = false
                }
            }

            Spacer(Modifier.height(10.dp))

            OnlineActionButton(
                title = "Cari PvP Realtime",
                subtitle = "Masuk matchmaking queue lalu membuat battle room realtime",
                enabled = !loading
            ) {
                scope.launch {
                    loading = true
                    message = try {
                        onlineManager.signInGuest()
                        val hero = repository.getSelectedHero()
                        val deck = repository.getBattleDeckCards()
                        val room = battleManager.enterMatchmaking(
                            playerName = playerName,
                            avatar = repository.getPlayer()?.avatar ?: "avatar_1",
                            power = repository.calculatePlayerPower(),
                            hero = hero,
                            deck = deck
                        )
                        if (room == "QUEUE") {
                            "Menunggu lawan di matchmaking..."
                        } else {
                            onOpenRoom(room)
                            "Room dibuat: $room"
                        }
                    } catch (e: Exception) {
                        "Matchmaking error: ${e.message}"
                    }
                    loading = false
                }
            }

            Spacer(Modifier.height(10.dp))

            OnlineActionButton(
                title = "Buat Private Room",
                subtitle = "Buat room untuk dimainkan dengan teman",
                enabled = !loading
            ) {
                scope.launch {
                    loading = true
                    message = try {
                        onlineManager.signInGuest()
                        val room = battleManager.createPrivateRoom(
                            playerName = playerName,
                            avatar = repository.getPlayer()?.avatar ?: "avatar_1",
                            hero = repository.getSelectedHero(),
                            deck = repository.getBattleDeckCards()
                        )
                        onOpenRoom(room)
                        "Private room dibuat: $room"
                    } catch (e: Exception) {
                        "Private room error: ${e.message}"
                    }
                    loading = false
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = "Catatan: tambahkan google-services.json dari console ke folder app/ dan aktifkan Authentication, Firestore, Realtime Database, dan Storage.",
                color = Color(0xFFEBD9FF),
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun OnlineActionButton(
    title: String,
    subtitle: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xCC100019))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                Text(subtitle, color = Color(0xFFFFD66B), fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Button(
                onClick = onClick,
                enabled = enabled,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD66B), contentColor = Color(0xFF160021))
            ) {
                Text("Start", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun OnlineBattleRoomScreen(
    roomId: String,
    deck: List<CardEntity>,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val onlineManager = remember { FirebaseOnlineManager() }
    val battleManager = remember { OnlineBattleManager(onlineManager) }
    var room by remember { mutableStateOf<OnlineBattleRoom?>(null) }
    var message by remember { mutableStateOf("Room: $roomId") }

    LaunchedEffect(roomId) {
        battleManager.observeRoom(roomId).collect { room = it }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF050008), Color(0xFF220035), Color(0xFF050008))))
            .padding(14.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = onBack) { Text("Back") }
                Spacer(Modifier.width(10.dp))
                Text("Realtime Battle", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
            }
            Text(message, color = Color(0xFFFFD66B), fontSize = 11.sp)
            Spacer(Modifier.height(14.dp))

            val r = room
            if (r == null) {
                Text("Memuat room...", color = Color.White)
            } else {
                BattlePlayerPanel("Player 1", r.player1Name, r.player1Hp, r.player1MaxHp, r.currentTurnUid == r.player1Uid)
                Spacer(Modifier.height(10.dp))
                BattlePlayerPanel("Player 2", r.player2Name, r.player2Hp, r.player2MaxHp, r.currentTurnUid == r.player2Uid)
                Spacer(Modifier.height(12.dp))
                Text("Last Action: ${r.lastAction}", color = Color(0xFFEBD9FF), fontSize = 12.sp)
                Spacer(Modifier.height(12.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(deck.take(6)) { card ->
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        battleManager.sendCardAction(roomId, card, "active_skill")
                                    } catch (e: Exception) {
                                        message = "Action error: ${e.message}"
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text("${card.name} • ${card.skillName.ifBlank { "Attack" }}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BattlePlayerPanel(label: String, name: String, hp: Int, maxHp: Int, active: Boolean) {
    val progress = if (maxHp <= 0) 0f else hp.toFloat() / maxHp.toFloat()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = if (active) Color(0xDD2B0344) else Color(0xBB100019)),
        border = BorderStroke(1.dp, if (active) Color(0xFFFFD66B) else Color.White.copy(alpha = 0.12f))
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("$label • $name", color = Color.White, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(6.dp))
            Box(Modifier.fillMaxWidth().height(14.dp).background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(50.dp))) {
                Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).fillMaxHeight().background(Color(0xFF6CFF9B), RoundedCornerShape(50.dp)))
                Text("HP $hp/$maxHp", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
