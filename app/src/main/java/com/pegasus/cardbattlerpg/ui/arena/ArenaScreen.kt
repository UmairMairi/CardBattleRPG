package com.pegasus.cardbattlerpg.ui.arena

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.rotate
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import android.os.Build
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pegasus.cardbattlerpg.R
import com.pegasus.cardbattlerpg.entity.CardEntity
import com.pegasus.cardbattlerpg.entity.HeroEntity
import com.pegasus.cardbattlerpg.model.ArenaOpponent
import com.pegasus.cardbattlerpg.online.OnlineBattleManager
import com.pegasus.cardbattlerpg.online.OnlineBattleRoom
import com.pegasus.cardbattlerpg.repository.GameRepository
import com.pegasus.cardbattlerpg.utils.GameAudioManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

@Composable
fun arenaDrawableIdByName(name: String): Int {
    val context = LocalContext.current
    return remember(name) {
        context.resources.getIdentifier(name.removeSuffix(".png"), "drawable", context.packageName)
    }
}

@Composable
fun safeArenaHeroImage(imageName: String): Int {
    val res = arenaDrawableIdByName(imageName)
    return if (res != 0) res else R.drawable.hero_unknown
}

@Composable
fun safeArenaCardImage(card: CardEntity): Int {
    val res = arenaDrawableIdByName(card.image)
    return if (res != 0) res else R.drawable.card_unknown
}

@Composable
fun safeArenaOpponentImage(opponent: ArenaOpponent): Int {
    val res = arenaDrawableIdByName(opponent.image)
    return if (res != 0) res else R.drawable.enemy_unknown
}

@Composable
fun ArenaScreen(
    repository: GameRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val onlineBattleManager = remember { OnlineBattleManager() }

    var playerPower by remember { mutableIntStateOf(0) }
    var arenaPoint by remember { mutableIntStateOf(0) }
    var battleCards by remember { mutableStateOf<List<CardEntity>>(emptyList()) }
    var opponents by remember { mutableStateOf<List<ArenaOpponent>>(emptyList()) }
    var selectedHero by remember { mutableStateOf<HeroEntity?>(null) }
    var message by remember { mutableStateOf("Realtime PvP siap dimainkan") }

    var onlineRoom by remember { mutableStateOf<OnlineBattleRoom?>(null) }
    var isMatchingRealtime by remember { mutableStateOf(false) }
    var showFinishPopup by remember { mutableStateOf(false) }

    // Arena Challenge vs komputer tetap aktif seperti versi sebelumnya.
    // State ini hanya untuk battle offline challenge, tidak mengganggu PvP realtime.
    var challengeOpponent by remember { mutableStateOf<ArenaOpponent?>(null) }
    var challengePlayerHp by remember { mutableIntStateOf(0) }
    var challengePlayerMaxHp by remember { mutableIntStateOf(0) }
    var challengeEnemyHp by remember { mutableIntStateOf(0) }
    var challengeEnemyMaxHp by remember { mutableIntStateOf(0) }
    var challengeLog by remember { mutableStateOf("Pilih lawan Arena Challenge") }
    var challengeCard by remember { mutableStateOf<CardEntity?>(null) }
    var challengeFighting by remember { mutableStateOf(false) }

    suspend fun reload() {
        val player = repository.getPlayer()
        selectedHero = repository.getSelectedHero()
        playerPower = repository.calculatePlayerPower()
        arenaPoint = player?.arenaPoint ?: 0
        battleCards = repository.getBattleDeckCards().ifEmpty { repository.getOwnedCards() }
        opponents = ArenaManager.generateOpponents(playerPower)
    }

    LaunchedEffect(Unit) { reload() }

    DisposableEffect(Unit) {
        val stop = onlineBattleManager.listenMyRoom { room ->
            if (room == null) {
                onlineRoom = null
                isMatchingRealtime = false
                showFinishPopup = false
                return@listenMyRoom
            }

            onlineRoom = room
            isMatchingRealtime = room.status == "waiting"
            message = when (room.status) {
                "waiting" -> "Menunggu player lain masuk arena realtime"
                "active" -> "Battle PvP realtime aktif"
                "finished" -> {
                    showFinishPopup = true
                    if (room.winnerUid == onlineBattleManager.currentUid()) "Victory realtime!" else "Defeat realtime."
                }
                else -> "Realtime room aktif"
            }
        }
        onDispose { stop() }
    }

    LaunchedEffect(onlineRoom?.roomId, onlineRoom?.status) {
        val room = onlineRoom
        if (room != null && room.roomId != "waiting" && room.status == "finished") {
            delay(4200)
            onlineBattleManager.deleteRoom(room.roomId)
            onlineRoom = null
            showFinishPopup = false
            message = "Battle selesai. Room session PvP sudah dihapus."
            reload()
        }
    }

    LaunchedEffect(onlineRoom?.roomId, onlineRoom?.status) {
        val room = onlineRoom
        if (room != null && room.roomId != "waiting" && room.status == "active") {
            onlineBattleManager.registerDisconnectWin(room)
        }
    }

    fun handleArenaBack() {
        scope.launch {
            val room = onlineRoom
            if (room != null) {
                if (room.status == "active") {
                    onlineBattleManager.leaveRoomAsDefeat(room)
                } else if (room.status == "waiting") {
                    onlineBattleManager.cancelMatchmaking()
                }
            }
            onBack()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.arena_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        ArenaPremiumBackground()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 28.dp)
        ) {
            item {
                ArenaHeader(
                    power = playerPower,
                    point = arenaPoint,
                    cardCount = battleCards.size,
                    message = message,
                    onBack = { handleArenaBack() }
                )
            }

            item {
                val room = onlineRoom
                if (room != null && room.status == "active") {
                    RealtimePvPBattleArena(
                        room = room,
                        currentUid = onlineBattleManager.currentUid().orEmpty(),
                        cards = battleCards,
                        onCardAttack = { card ->
                            scope.launch {
                                try {
                                    GameAudioManager.playAttack()
                                    onlineBattleManager.sendCardAttack(room, card)
                                } catch (e: Exception) {
                                    message = "Attack gagal: ${e.localizedMessage ?: "Online error"}"
                                }
                            }
                        }
                    )
                } else {
                    PvPFindMatchCompactPanel(
                        room = room,
                        isMatching = isMatchingRealtime,
                        onFindMatch = {
                            scope.launch {
                                val player = repository.getPlayer()
                                val hero = repository.getSelectedHero()
                                if (player == null) {
                                    message = "Player belum tersedia. Login/register dulu."
                                    return@launch
                                }
                                try {
                                    isMatchingRealtime = true
                                    message = "Mencari player realtime..."
                                    onlineRoom = onlineBattleManager.findRealtimeMatch(player, hero, playerPower)
                                } catch (e: Exception) {
                                    isMatchingRealtime = false
                                    onlineRoom = null
                                    message = "Find match gagal: ${e.localizedMessage ?: "Online error"}"
                                }
                            }
                        },
                        onCancel = {
                            scope.launch {
                                onlineBattleManager.cancelMatchmaking()
                                isMatchingRealtime = false
                                onlineRoom = null
                                message = "Matchmaking dibatalkan"
                            }
                        }
                    )
                }
            }

            item {
                Text(
                    text = "Arena Challenge",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            challengeOpponent?.let { opponent ->
                item {
                    ArenaChallengeLivePanel(
                        opponent = opponent,
                        playerName = selectedHero?.name ?: "Hero",
                        playerHeroImage = selectedHero?.image ?: "hero_unknown",
                        playerHp = challengePlayerHp,
                        playerMaxHp = challengePlayerMaxHp,
                        enemyHp = challengeEnemyHp,
                        enemyMaxHp = challengeEnemyMaxHp,
                        log = challengeLog,
                        usedCard = challengeCard,
                        fighting = challengeFighting,
                        onClose = {
                            if (!challengeFighting) {
                                challengeOpponent = null
                                challengeCard = null
                                challengeLog = "Pilih lawan Arena Challenge"
                            }
                        }
                    )
                }
            }

            items(opponents) { opponent ->
                PremiumArenaOpponentCard(
                    opponent = opponent,
                    enabled = onlineRoom?.status != "active" && onlineRoom?.status != "waiting" && !challengeFighting,
                    onFight = {
                        scope.launch {
                            if (challengeFighting) return@launch
                            challengeOpponent = opponent
                            challengeFighting = true
                            challengeCard = null
                            challengePlayerMaxHp = max(900, playerPower / 2)
                            challengePlayerHp = challengePlayerMaxHp
                            challengeEnemyMaxHp = opponent.hp
                            challengeEnemyHp = opponent.hp
                            challengeLog = "Arena Challenge dimulai melawan ${opponent.name}"
                            try {
                                val result = ArenaManager.liveFight(
                                    playerPower = playerPower,
                                    opponent = opponent,
                                    cards = battleCards
                                ) { playerHp, enemyHp, playerMaxHp, enemyMaxHp, log, usedCard ->
                                    challengePlayerHp = playerHp
                                    challengeEnemyHp = enemyHp
                                    challengePlayerMaxHp = playerMaxHp
                                    challengeEnemyMaxHp = enemyMaxHp
                                    challengeLog = log
                                    challengeCard = usedCard
                                    if (usedCard != null) GameAudioManager.playAttack()
                                }
                                if (result.isVictory) {
                                    repository.giveArenaReward(result.rewardGold, result.rewardPoint)
                                    GameAudioManager.playVictory()
                                } else {
                                    GameAudioManager.playDefeat()
                                }
                                message = result.message
                                challengeLog = result.message
                                challengeFighting = false
                                reload()
                            } catch (e: Exception) {
                                challengeFighting = false
                                challengeLog = "Arena Challenge gagal: ${e.localizedMessage ?: "error"}"
                                message = challengeLog
                            }
                        }
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = showFinishPopup && onlineRoom?.status == "finished",
            enter = scaleIn(initialScale = 0.74f, animationSpec = tween(520, easing = EaseOutBack)) + fadeIn(tween(360)),
            exit = scaleOut() + fadeOut()
        ) {
            val room = onlineRoom
            val isWin = room?.winnerUid == onlineBattleManager.currentUid()
            PvPFinishPopup(
                victory = isWin,
                message = room?.lastAction.orEmpty(),
                onClose = {
                    scope.launch {
                        val current = onlineRoom
                        if (current != null) onlineBattleManager.deleteRoom(current.roomId)
                        onlineRoom = null
                        showFinishPopup = false
                        reload()
                    }
                }
            )
        }
    }
}


@Composable
fun PvPFindMatchCompactPanel(
    room: OnlineBattleRoom?,
    isMatching: Boolean,
    onFindMatch: () -> Unit,
    onCancel: () -> Unit
) {
    val waiting = isMatching || room?.status == "waiting"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(18.dp, RoundedCornerShape(28.dp), ambientColor = Color(0xFF4FC3F7), spotColor = Color(0xFFB56CFF)),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, Color(0xFF6FE7FF)),
        colors = CardDefaults.cardColors(containerColor = Color(0xDD050015))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Color(0xEE062140), Color(0xEF100019))))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Realtime PvP",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = if (waiting) "Mencari lawan realtime..." else "Tekan Find Match untuk PvP realtime",
                    color = Color(0xFF7DE7FF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Button(
                onClick = if (waiting) onCancel else onFindMatch,
                modifier = Modifier
                    .width(132.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (waiting) Color(0xFFFF6B6B) else Color(0xFF6FE7FF),
                    contentColor = Color(0xFF080014)
                )
            ) {
                Text(if (waiting) "Cancel" else "Find Match", fontWeight = FontWeight.Black, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun RealtimePvPBattleArena(
    room: OnlineBattleRoom,
    currentUid: String,
    cards: List<CardEntity>,
    onCardAttack: (CardEntity) -> Unit
) {
    val isPlayer1 = currentUid == room.player1Uid
    val myName = if (isPlayer1) room.player1Name else room.player2Name
    val myHero = if (isPlayer1) room.player1Hero else room.player2Hero
    val myHeroImage = if (isPlayer1) room.player1HeroImage else room.player2HeroImage
    val myHp = if (isPlayer1) room.player1Hp else room.player2Hp
    val myMaxHp = if (isPlayer1) room.player1MaxHp else room.player2MaxHp
    val enemyName = if (isPlayer1) room.player2Name else room.player1Name
    val enemyHero = if (isPlayer1) room.player2Hero else room.player1Hero
    val enemyHeroImage = if (isPlayer1) room.player2HeroImage else room.player1HeroImage
    val enemyHp = if (isPlayer1) room.player2Hp else room.player1Hp
    val enemyMaxHp = if (isPlayer1) room.player2MaxHp else room.player1MaxHp
    val myTurn = room.currentTurnUid == currentUid
    val lastTargetMe = room.lastTargetUid == currentUid
    val lastTargetEnemy = room.lastTargetUid.isNotBlank() && room.lastTargetUid != currentUid
    val lastActionToken = "${room.updatedAt}_${room.lastDamage}_${room.lastTargetUid}_${room.lastSkillName}"

    LaunchedEffect(lastActionToken) {
        if (room.lastDamage > 0 && room.lastTargetUid.isNotBlank()) {
            if (lastTargetMe) {
                GameAudioManager.playEnemyAttack()
                delay(80)
                GameAudioManager.playHit()
            } else if (lastTargetEnemy) {
                delay(80)
                GameAudioManager.playHit()
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                26.dp,
                RoundedCornerShape(34.dp),
                ambientColor = if (myTurn) Color(0xFFFFD66B) else Color(0xFFFF3D68),
                spotColor = Color(0xFF4FC3F7)
            ),
        shape = RoundedCornerShape(34.dp),
        border = BorderStroke(1.dp, if (myTurn) Color(0xFFFFD66B) else Color(0xFFFF7B91)),
        colors = CardDefaults.cardColors(containerColor = Color(0xEA090012))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xDD14001E), Color(0xDD061B34), Color(0xEF050008))))
                .padding(10.dp)
        ) {
            PvPBattleTopBar(
                title = "Realtime Battle Arena",
                subtitle = if (myTurn) "Giliran kamu • pilih kartu terbaik" else "Menunggu serangan lawan realtime"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(522.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(28.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.battle_background),
                    contentDescription = "PvP Battle Background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xC8040007),
                                    Color(0x22000000),
                                    Color(0xE908000F)
                                )
                            )
                        )
                )

                PvPHeroUnit(
                    name = enemyName.ifBlank { "Opponent" },
                    heroName = enemyHero.ifBlank { "Hero" },
                    heroImage = enemyHeroImage,
                    hp = enemyHp,
                    maxHp = enemyMaxHp,
                    tag = "ENEMY",
                    hpColor = Color(0xFFFF3D68),
                    isHit = lastTargetEnemy,
                    isAttacking = room.lastAttackerUid == currentUid,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                )

                PvPHeroUnit(
                    name = myName.ifBlank { "You" },
                    heroName = myHero.ifBlank { "Hero" },
                    heroImage = myHeroImage,
                    hp = myHp,
                    maxHp = myMaxHp,
                    tag = "YOU",
                    hpColor = Color(0xFF55E27A),
                    isHit = lastTargetMe,
                    isAttacking = room.lastAttackerUid.isNotBlank() && room.lastAttackerUid != currentUid,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 10.dp, bottom = 74.dp)
                )

                Text(
                    text = "VS",
                    color = Color(0xFFFFD66B),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.align(Alignment.Center)
                )

                if (room.lastDamage > 0) {
                    PvPAttackSlashEffect(
                        token = lastActionToken,
                        targetEnemy = lastTargetEnemy,
                        element = room.lastEffectElement,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    // GIF serangan hanya muncul di target lawan saat kita menyerang.
                    // Untuk saat hero kita terkena hit, efek GIF default merah dimatikan agar tidak ada lingkaran merah yang tertinggal.
                    PvPAttackGifEffect(
                        token = lastActionToken,
                        gifName = room.lastCardAttackGif,
                        visible = lastTargetEnemy,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 104.dp)
                    )

                    PvPClawDamageEffect(
                        token = lastActionToken,
                        visible = lastTargetEnemy || lastTargetMe,
                        modifier = Modifier
                            .align(if (lastTargetEnemy) Alignment.TopCenter else Alignment.BottomStart)
                            .padding(
                                top = if (lastTargetEnemy) 144.dp else 0.dp,
                                start = if (lastTargetMe) 96.dp else 0.dp,
                                bottom = if (lastTargetMe) 196.dp else 0.dp
                            )
                    )

                    FloatingPvPDamage(
                        damage = room.lastDamage,
                        isEnemyTarget = lastTargetEnemy,
                        critical = room.lastCritical,
                        modifier = Modifier.align(if (lastTargetEnemy) Alignment.TopCenter else Alignment.BottomStart)
                    )
                }

                if (room.lastSkillName.isNotBlank()) {
                    PvPSkillBurst(
                        text = room.lastSkillName,
                        element = room.lastEffectElement,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Text(
                    text = room.lastAction.ifBlank { "Battle dimulai. Gunakan kartu terbaikmu." },
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 14.dp, start = 12.dp, end = 12.dp)
                        .background(Color.Black.copy(alpha = 0.56f), RoundedCornerShape(50.dp))
                        .border(1.dp, Color(0xFFFFD66B).copy(alpha = 0.54f), RoundedCornerShape(50.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            PvPHandCards(
                cards = cards.take(5),
                enabled = myTurn && room.status == "active",
                onCardClick = onCardAttack
            )
        }
    }
}

@Composable
fun PvPBattleTopBar(title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Black)
            Text(subtitle, color = Color(0xFFFFD66B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        TurnOrb(text = if (subtitle.startsWith("Giliran")) "YOU" else "WAIT")
    }
}

@Composable
fun TurnOrb(text: String) {
    val infinite = rememberInfiniteTransition(label = "pvp_turn_orb")
    val scale by infinite.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "orb_scale"
    )
    Box(
        modifier = Modifier
            .size(48.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .background(Brush.radialGradient(listOf(Color(0xFF6CFF9B), Color(0xFF160021))), CircleShape)
            .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
        contentAlignment = Alignment.Center
    ) { Text(text, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black) }
}

@Composable
fun PvPHeroUnit(
    name: String,
    heroName: String,
    heroImage: String,
    hp: Int,
    maxHp: Int,
    tag: String,
    hpColor: Color,
    isHit: Boolean,
    isAttacking: Boolean,
    modifier: Modifier = Modifier
) {
    val imageRes = safeArenaHeroImage(heroImage)
    val infinite = rememberInfiniteTransition(label = "pvp_breath_$tag")
    val breathY by infinite.animateFloat(
        initialValue = -3f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(1450, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "breath_y"
    )
    val breathScale by infinite.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(tween(1450, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "breath_scale"
    )
    val hitShake by animateFloatAsState(
        targetValue = if (isHit) 1f else 0f,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "pvp_hit_shake"
    )
    val lunge by animateFloatAsState(
        targetValue = if (isAttacking) 1f else 0f,
        animationSpec = tween(520, easing = EaseOutBack),
        label = "pvp_attack_lunge"
    )

    Column(
        modifier = modifier
            .width(210.dp)
            .graphicsLayer {
                translationX = hitShake * 16f + if (tag == "YOU") lunge * 48f else -lunge * 30f
                translationY = if (tag == "YOU") -lunge * 18f else lunge * 28f
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PvPHPBar(name = name, hp = hp, maxHp = maxHp, color = hpColor, tag = tag)
        Spacer(modifier = Modifier.height(6.dp))
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = heroName,
                modifier = Modifier
                    .height(if (tag == "ENEMY") 138.dp else 132.dp)
                    .fillMaxWidth()
                    .graphicsLayer {
                        translationY = breathY
                        scaleX = breathScale
                        scaleY = breathScale
                    },
                contentScale = ContentScale.Fit
            )

            // Efek bulatan/lingkaran merah saat hit dihapus total.
            // Damage visual sekarang hanya memakai shake + claw effect terpisah,
            // supaya tidak ada artifact merah yang tertinggal di karakter.
        }
        Text(
            text = heroName,
            color = if (tag == "YOU") Color(0xFF6CFF9B) else Color(0xFFFFD66B),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.background(Color.Black.copy(alpha = 0.36f), RoundedCornerShape(50.dp)).padding(horizontal = 9.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun PvPHPBar(name: String, hp: Int, maxHp: Int, color: Color, tag: String) {
    val progress = if (maxHp <= 0) 0f else (hp.toFloat() / maxHp.toFloat()).coerceIn(0f, 1f)
    val animated by animateFloatAsState(targetValue = progress, animationSpec = tween(480), label = "pvp_hp")
    Card(
        modifier = Modifier.width(200.dp),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.65f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xDA040008))
    ) {
        Column(modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text(tag, color = color, fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
            Spacer(modifier = Modifier.height(5.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(Color.Black.copy(alpha = 0.78f), RoundedCornerShape(50.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(50.dp))
                    .padding(3.dp)
            ) {
                LinearProgressIndicator(progress = { animated }, modifier = Modifier.fillMaxSize(), color = color, trackColor = Color.Transparent)
                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(animated).background(Brush.horizontalGradient(listOf(color, Color.White.copy(alpha = 0.35f), color)), RoundedCornerShape(50.dp)))
                Text("$hp/$maxHp", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.Center).background(Color.Black.copy(alpha = 0.70f), RoundedCornerShape(50.dp)).padding(horizontal = 8.dp, vertical = 2.dp))
            }
        }
    }
}

@Composable
fun FloatingPvPDamage(
    damage: Int,
    isEnemyTarget: Boolean,
    critical: Boolean = false,
    modifier: Modifier = Modifier
) {
    var show by remember("$damage-$isEnemyTarget-$critical") { mutableStateOf(true) }
    var play by remember("$damage-$isEnemyTarget-$critical") { mutableStateOf(false) }
    LaunchedEffect("$damage-$isEnemyTarget-$critical") {
        show = true
        play = false
        delay(24)
        play = true
        delay(850)
        show = false
    }
    if (!show) return

    val progress by animateFloatAsState(
        targetValue = if (play) 1f else 0f,
        animationSpec = tween(850, easing = EaseOutBack),
        label = "pvp_float_damage"
    )
    Text(
        text = if (critical) "CRIT -$damage" else "-$damage",
        color = if (isEnemyTarget) Color(0xFFFFD66B) else Color(0xFFFF5D78),
        fontSize = if (critical) 26.sp else 30.sp,
        fontWeight = FontWeight.Black,
        modifier = modifier
            .padding(top = if (isEnemyTarget) 112.dp else 250.dp, start = if (isEnemyTarget) 0.dp else 74.dp)
            .offset(y = (-progress * 42).dp)
            .graphicsLayer {
                alpha = (1f - progress).coerceIn(0f, 1f)
                scaleX = 0.8f + progress * 0.35f
                scaleY = 0.8f + progress * 0.35f
            }
            .shadow(12.dp, CircleShape)
    )
}

@Composable
fun PvPAttackSlashEffect(
    token: String,
    targetEnemy: Boolean,
    element: String,
    modifier: Modifier = Modifier
) {
    if (token.isBlank()) return

    // Efek slash panjang kasar dihapus total.
    // Diganti burst pendek + partikel agar tidak muncul garis kuning panjang di layar.
    var show by remember(token) { mutableStateOf(true) }
    var play by remember(token) { mutableStateOf(false) }
    LaunchedEffect(token) {
        show = true
        play = false
        delay(24)
        play = true
        delay(520)
        show = false
    }
    if (!show) return

    val progress by animateFloatAsState(
        targetValue = if (play) 1f else 0f,
        animationSpec = tween(520, easing = FastOutSlowInEasing),
        label = "pvp_clean_hit_$token"
    )
    val color = arenaElementColor(element)

    Box(
        modifier = modifier
            .size(170.dp)
            .graphicsLayer {
                alpha = (1f - progress).coerceIn(0f, 1f)
                translationX = if (targetEnemy) progress * 42f else -progress * 42f
                translationY = if (targetEnemy) -progress * 34f else progress * 34f
                scaleX = 0.88f + progress * 0.28f
                scaleY = 0.88f + progress * 0.28f
            },
        contentAlignment = Alignment.Center
    ) {
        repeat(5) { index ->
            val angle = -46f + index * 23f
            Box(
                modifier = Modifier
                    .offset(
                        x = ((index - 2) * 10).dp,
                        y = ((index % 2) * 8 - 4).dp
                    )
                    .width((42 - index * 2).dp)
                    .height(6.dp)
                    .rotate(angle)
                    .blur(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                color.copy(alpha = 0.88f),
                                Color.White.copy(alpha = 0.72f),
                                Color.Transparent
                            )
                        ),
                        RoundedCornerShape(50.dp)
                    )
            )
        }

        // Radial circle glow dihapus agar tidak muncul bulatan merah/kuning kasar saat hit.
    }
}

@Composable
fun PvPClawDamageEffect(
    token: String,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    if (!visible || token.isBlank()) return
    var show by remember(token) { mutableStateOf(true) }
    var play by remember(token) { mutableStateOf(false) }
    LaunchedEffect(token) {
        show = true
        play = false
        delay(24)
        play = true
        delay(760)
        show = false
    }
    if (!show) return

    val progress by animateFloatAsState(
        targetValue = if (play) 1f else 0f,
        animationSpec = tween(760, easing = EaseOutBack),
        label = "pvp_claw_$token"
    )
    Box(
        modifier = modifier
            .size(98.dp)
            .graphicsLayer {
                alpha = (1f - progress).coerceIn(0f, 1f)
                scaleX = 0.82f + progress * 0.24f
                scaleY = 0.82f + progress * 0.24f
            },
        contentAlignment = Alignment.Center
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .offset(x = ((index - 1) * 13).dp)
                    .width(8.dp)
                    .height(64.dp)
                    .rotate(-24f)
                    .background(
                        Brush.verticalGradient(listOf(Color.Transparent, Color(0xFFFFE1E6), Color(0xFFFF2446), Color(0xFF7A0012), Color.Transparent)),
                        RoundedCornerShape(50.dp)
                    )
            )
        }
    }
}

@Composable
fun PvPSkillBurst(text: String, element: String, modifier: Modifier = Modifier) {
    if (text.isBlank()) return
    var show by remember(text) { mutableStateOf(true) }
    var play by remember(text) { mutableStateOf(false) }
    LaunchedEffect(text) {
        show = true
        play = false
        delay(24)
        play = true
        delay(900)
        show = false
    }
    if (!show) return

    val progress by animateFloatAsState(
        targetValue = if (play) 1f else 0f,
        animationSpec = tween(900, easing = EaseOutBack),
        label = "pvp_skill_burst"
    )
    val color = arenaElementColor(element)
    Text(
        text = text.uppercase(),
        color = color,
        fontSize = 13.sp,
        fontWeight = FontWeight.Black,
        textAlign = TextAlign.Center,
        modifier = modifier
            .padding(top = 74.dp)
            .graphicsLayer {
                alpha = (1f - progress).coerceIn(0f, 1f)
                scaleX = 0.92f + progress * 0.16f
                scaleY = 0.92f + progress * 0.16f
            }
            .background(Color.Black.copy(alpha = 0.52f), RoundedCornerShape(50.dp))
            .border(1.dp, color.copy(alpha = 0.72f), RoundedCornerShape(50.dp))
            .padding(horizontal = 16.dp, vertical = 7.dp)
    )
}

@Composable
fun PvPAttackGifEffect(
    token: String,
    gifName: String,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    if (!visible || token.isBlank()) return

    // GIF hanya ditampilkan sekali setiap hit. Setelah durasi selesai, composable disembunyikan
    // supaya animasi GIF tidak looping terus di arena.
    var show by remember(token) { mutableStateOf(true) }
    var play by remember(token) { mutableStateOf(false) }
    LaunchedEffect(token) {
        show = true
        play = false
        delay(24)
        play = true
        delay(620)
        show = false
    }
    if (!show) return

    val context = LocalContext.current
    val fixedName = gifName.removeSuffix(".gif").removeSuffix(".png")

    // Jangan tampilkan gif_default_attack karena asset default ini sering berupa
    // lingkaran merah dan terlihat tertinggal saat hit. GIF hanya muncul jika
    // kartu punya attackGif sendiri yang valid.
    if (fixedName.isBlank() || fixedName == "gif_default_attack") return

    val gifRes = remember(fixedName) {
        context.resources.getIdentifier(fixedName, "drawable", context.packageName)
    }
    if (gifRes == 0) return
    val progress by animateFloatAsState(
        targetValue = if (play) 1f else 0f,
        animationSpec = tween(620, easing = FastOutSlowInEasing),
        label = "pvp_gif_once_$token"
    )

    Box(
        modifier = modifier
            .size(188.dp)
            .graphicsLayer {
                alpha = (1f - progress * 1.35f).coerceIn(0f, 1f)
                scaleX = 0.82f + progress * 0.22f
                scaleY = 0.82f + progress * 0.22f
                translationY = -progress * 6f
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(gifRes)
                .decoderFactory(
                    if (Build.VERSION.SDK_INT >= 28) ImageDecoderDecoder.Factory() else GifDecoder.Factory()
                )
                .build(),
            contentDescription = fixedName,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun PvPHandCards(cards: List<CardEntity>, enabled: Boolean, onCardClick: (CardEntity) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(174.dp),
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(1.dp, Color(0xFFFFD66B).copy(alpha = 0.36f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xDF100019))
    ) {
        Column(modifier = Modifier.background(Brush.verticalGradient(listOf(Color(0xEE1A0027), Color(0xF006000A)))).padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Kartu Tangan", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    Text(if (enabled) "Tap kartu untuk menyerang" else "Kartu hanya terlihat oleh pemilik akun • menunggu giliran", color = if (enabled) Color(0xFFFFD66B) else Color(0xFFFF7B91), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Text("${cards.size} Cards", color = Color(0xFFEBD9FF), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(50.dp)).padding(horizontal = 10.dp, vertical = 5.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(horizontal = 2.dp)) {
                items(cards) { card -> PvPBattleCardItem(card, enabled) { onCardClick(card) } }
            }
        }
    }
}

@Composable
fun PvPBattleCardItem(card: CardEntity, enabled: Boolean, onClick: () -> Unit) {
    val imageRes = safeArenaCardImage(card)
    val rarityColor = arenaRarityColor(card.rarity)
    Card(
        modifier = Modifier
            .width(112.dp)
            .height(128.dp)
            .graphicsLayer { alpha = if (enabled) 1f else 0.52f; rotationZ = if (enabled) -2f else 0f }
            .shadow(if (enabled) 18.dp else 4.dp, RoundedCornerShape(20.dp), ambientColor = rarityColor, spotColor = rarityColor)
            .clickable { if (enabled) onClick() },
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, if (enabled) rarityColor.copy(alpha = 0.70f) else Color.White.copy(alpha = 0.12f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13001E))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(painter = painterResource(id = imageRes), contentDescription = card.name, modifier = Modifier.fillMaxWidth().height(70.dp), contentScale = ContentScale.Crop)
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0x7712001F), Color(0xF008000F)))))
            Text(card.mana.toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.TopStart).padding(7.dp).background(rarityColor, CircleShape).padding(horizontal = 8.dp, vertical = 4.dp))
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)) {
                Text(card.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(card.skillName.ifBlank { "Attack" }, color = Color(0xFFFFD66B), fontSize = 8.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("ATK ${card.attack}", color = Color(0xFFFF7777), fontSize = 7.sp, fontWeight = FontWeight.Black)
                    Text("HP ${card.hp}", color = Color(0xFF6CFF9B), fontSize = 7.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun PvPFinishPopup(victory: Boolean, message: String, onClose: () -> Unit) {
    val main = if (victory) Color(0xFFFFD66B) else Color(0xFFFF3D68)
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.62f)).padding(24.dp), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.fillMaxWidth().shadow(28.dp, RoundedCornerShape(34.dp), ambientColor = main, spotColor = main),
            shape = RoundedCornerShape(34.dp),
            border = BorderStroke(1.dp, main.copy(alpha = 0.75f)),
            colors = CardDefaults.cardColors(containerColor = Color(0xF20B0010))
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (victory) "VICTORY" else "DEFEAT", color = main, fontSize = 34.sp, fontWeight = FontWeight.Black)
                Text(message.ifBlank { if (victory) "Kamu menang PvP realtime" else "Kamu kalah PvP realtime" }, color = Color.White, fontSize = 13.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(18.dp))
                Button(onClick = onClose, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = main, contentColor = Color(0xFF160021))) {
                    Text("OK", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun ArenaPremiumBackground() {
    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xE8060010), Color(0xAA26004A), Color(0xF708000F)))))
    Box(modifier = Modifier.size(360.dp).offset(x = 130.dp, y = (-120).dp).alpha(0.22f).blur(28.dp).background(Brush.radialGradient(listOf(Color(0xFFFFD66B), Color.Transparent)), CircleShape))
}

@Composable
fun ArenaHeader(power: Int, point: Int, cardCount: Int, message: String, onBack: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(22.dp, RoundedCornerShape(32.dp), ambientColor = Color(0xFFB56CFF), spotColor = Color(0xFFFFD66B)),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.dp, Color(0x77FFD66B)),
        colors = CardDefaults.cardColors(containerColor = Color(0xE014001F))
    ) {
        Row(modifier = Modifier.background(Brush.horizontalGradient(listOf(Color(0xFF120019), Color(0xFF50148A), Color(0xFF0B0012)))).padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = onBack, modifier = Modifier.height(42.dp), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color(0x99FFD66B)), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White, containerColor = Color.Black.copy(alpha = 0.18f))) { Text("Back", fontSize = 12.sp, fontWeight = FontWeight.Black) }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("PvP Arena", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
                Text("Power $power • Point $point • Cards $cardCount", color = Color(0xFFFFD66B), fontSize = 12.sp, fontWeight = FontWeight.Black, maxLines = 1)
                Text(message, color = Color(0xFFEBD9FF), fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun PremiumArenaOpponentCard(opponent: ArenaOpponent, enabled: Boolean, onFight: () -> Unit) {
    val imageRes = safeArenaOpponentImage(opponent)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(148.dp)
            .alpha(if (enabled) 1f else 0.50f)
            .shadow(16.dp, RoundedCornerShape(30.dp), ambientColor = Color(0xFFFFD66B), spotColor = Color(0xFFB56CFF)),
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(1.dp, Color(0x77FFD66B)),
        colors = CardDefaults.cardColors(containerColor = Color(0xDD160021))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(listOf(Color(0xFF120019), Color(0xFF42105E), Color(0xFF08000F))))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = opponent.name,
                modifier = Modifier
                    .size(88.dp)
                    .background(Color.Black.copy(alpha = 0.36f), RoundedCornerShape(24.dp))
                    .padding(6.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "#${opponent.rank} ${opponent.name}",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text("${opponent.element} • ${opponent.role}", color = Color(0xFFFFD66B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
                    ArenaMiniPill("PWR ${opponent.power}", Color(0xFFFFD66B), Modifier.weight(1f))
                    ArenaMiniPill("HP ${opponent.hp}", Color(0xFF6CFF9B), Modifier.weight(1f))
                }
                Spacer(Modifier.height(5.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
                    ArenaMiniPill("ATK ${opponent.attack}", Color(0xFFFF6B6B), Modifier.weight(1f))
                    ArenaMiniPill("DEF ${opponent.defense}", Color(0xFF82D8FF), Modifier.weight(1f))
                }
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onFight,
                enabled = enabled,
                modifier = Modifier.width(74.dp).height(46.dp),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7FA7E8), contentColor = Color.White)
            ) { Text("Fight", fontWeight = FontWeight.Black, fontSize = 12.sp, maxLines = 1) }
        }
    }
}

@Composable
fun ArenaMiniPill(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(24.dp)
            .background(color.copy(alpha = 0.13f), RoundedCornerShape(50.dp))
            .border(1.dp, color.copy(alpha = 0.30f), RoundedCornerShape(50.dp))
            .padding(horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 7.sp,
            lineHeight = 7.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Clip
        )
    }
}

@Composable
fun ArenaChallengeLivePanel(
    opponent: ArenaOpponent,
    playerName: String,
    playerHeroImage: String,
    playerHp: Int,
    playerMaxHp: Int,
    enemyHp: Int,
    enemyMaxHp: Int,
    log: String,
    usedCard: CardEntity?,
    fighting: Boolean,
    onClose: () -> Unit
) {
    val enemyImage = safeArenaOpponentImage(opponent)
    val heroImage = safeArenaHeroImage(playerHeroImage)
    val cardColor = usedCard?.let { arenaRarityColor(it.rarity) } ?: Color(0xFFFFD66B)

    val breath = rememberInfiniteTransition(label = "arena_challenge_breath")
    val enemyBreathY by breath.animateFloat(
        initialValue = -3f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1450, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "challenge_enemy_breath_y"
    )
    val enemyBreathScale by breath.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(1450, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "challenge_enemy_breath_scale"
    )
    val heroBreathY by breath.animateFloat(
        initialValue = 4f,
        targetValue = -4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1350, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "challenge_hero_breath_y"
    )
    val heroBreathScale by breath.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(1350, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "challenge_hero_breath_scale"
    )

    val hitToken = "${usedCard?.name.orEmpty()}_${enemyHp}_${playerHp}_${log}"
    val hitProgress by animateFloatAsState(
        targetValue = if (usedCard != null && fighting) 1f else 0f,
        animationSpec = tween(520, easing = FastOutSlowInEasing),
        label = "challenge_hit_progress_$hitToken"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(24.dp, RoundedCornerShape(32.dp), ambientColor = cardColor, spotColor = Color(0xFFB56CFF)),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.dp, cardColor.copy(alpha = 0.70f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xEE090012))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(486.dp)
                .background(Brush.verticalGradient(listOf(Color(0xDD14001E), Color(0xDD061B34), Color(0xEF050008))))
        ) {
            Image(
                painter = painterResource(id = R.drawable.battle_background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xB8040007), Color.Transparent, Color(0xE908000F)))))

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PvPHPBar(name = opponent.name, hp = enemyHp, maxHp = enemyMaxHp, color = Color(0xFFFF3D68), tag = "CPU")
                Spacer(Modifier.height(6.dp))
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = enemyImage),
                        contentDescription = opponent.name,
                        modifier = Modifier
                            .height(126.dp)
                            .fillMaxWidth(0.72f)
                            .graphicsLayer {
                                translationY = enemyBreathY
                                scaleX = enemyBreathScale
                                scaleY = enemyBreathScale
                                translationX = if (usedCard != null && fighting) hitProgress * 10f else 0f
                            },
                        contentScale = ContentScale.Fit
                    )
                    if (usedCard != null && fighting) {
                        Box(
                            modifier = Modifier
                                .size(108.dp)
                                .graphicsLayer {
                                    alpha = (1f - hitProgress).coerceIn(0f, 1f)
                                    scaleX = 0.8f + hitProgress * 0.45f
                                    scaleY = 0.8f + hitProgress * 0.45f
                                    rotationZ = -18f
                                }
                                .background(
                                    Brush.radialGradient(
                                        listOf(cardColor.copy(alpha = 0.80f), Color.White.copy(alpha = 0.22f), Color.Transparent)
                                    ),
                                    CircleShape
                                )
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp, bottom = 126.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PvPHPBar(name = playerName, hp = playerHp, maxHp = playerMaxHp, color = Color(0xFF55E27A), tag = "YOU")
                Spacer(Modifier.height(6.dp))
                Image(
                    painter = painterResource(id = heroImage),
                    contentDescription = playerName,
                    modifier = Modifier
                        .width(170.dp)
                        .height(118.dp)
                        .graphicsLayer {
                            translationY = heroBreathY
                            scaleX = heroBreathScale
                            scaleY = heroBreathScale
                        },
                    contentScale = ContentScale.Fit
                )
            }

            usedCard?.let { card ->
                PvPAttackGifEffect(
                    token = "challenge_${card.name}_${enemyHp}_${playerHp}",
                    gifName = card.attackGif.ifBlank { "gif_default_attack" },
                    visible = fighting,
                    modifier = Modifier.align(Alignment.Center).padding(bottom = 66.dp)
                )

                ChallengeUsedCardBanner(
                    card = card,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(start = 14.dp, end = 14.dp, bottom = 64.dp)
                )
            }

            Text(
                text = log,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 12.dp, end = 12.dp, bottom = 16.dp)
                    .background(Color.Black.copy(alpha = 0.58f), RoundedCornerShape(50.dp))
                    .border(1.dp, cardColor.copy(alpha = 0.60f), RoundedCornerShape(50.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )

            if (!fighting) {
                Button(
                    onClick = onClose,
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).height(38.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD66B), contentColor = Color(0xFF160021))
                ) { Text("Close", fontSize = 11.sp, fontWeight = FontWeight.Black) }
            }
        }
    }
}

@Composable
fun ChallengeUsedCardBanner(
    card: CardEntity,
    modifier: Modifier = Modifier
) {
    val imageRes = safeArenaCardImage(card)
    val rarityColor = arenaRarityColor(card.rarity)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .shadow(14.dp, RoundedCornerShape(18.dp), ambientColor = rarityColor, spotColor = rarityColor),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, rarityColor.copy(alpha = 0.62f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xDD100019))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(listOf(Color(0xEE100019), rarityColor.copy(alpha = 0.24f), Color(0xEE050008))))
                .padding(horizontal = 9.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = card.name,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, rarityColor.copy(alpha = 0.50f), RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(9.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Kartu digunakan",
                    color = Color(0xFFFFD66B),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1
                )
                Text(
                    text = card.name,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = card.skillName.ifBlank { card.rarity },
                    color = rarityColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = "ATK ${card.attack}",
                color = Color(0xFFFF6B6B),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .background(Color(0x22FF6B6B), RoundedCornerShape(50.dp))
                    .border(1.dp, Color(0x55FF6B6B), RoundedCornerShape(50.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}


fun arenaRarityColor(rarity: String): Color {
    return when (rarity) {
        "Legendary" -> Color(0xFFFFD700)
        "Epic" -> Color(0xFFB56CFF)
        "Rare" -> Color(0xFF4FC3F7)
        else -> Color(0xFFC9C9C9)
    }
}

fun arenaElementColor(element: String): Color {
    return when (element) {
        "Fire" -> Color(0xFFFF5252)
        "Water" -> Color(0xFF42A5F5)
        "Nature" -> Color(0xFF66BB6A)
        "Dark" -> Color(0xFF7E57C2)
        "Light" -> Color(0xFFFFEE58)
        "Ice" -> Color(0xFF81D4FA)
        "Earth" -> Color(0xFF8D6E63)
        "Wind" -> Color(0xFFB2DFDB)
        "Lightning" -> Color(0xFFFFF176)
        else -> Color(0xFFFFD66B)
    }
}
