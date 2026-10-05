package com.pegasus.cardbattlerpg.ui.battle

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pegasus.cardbattlerpg.R
import com.pegasus.cardbattlerpg.entity.CardEntity
import com.pegasus.cardbattlerpg.entity.HeroEntity
import com.pegasus.cardbattlerpg.entity.InventoryEntity
import com.pegasus.cardbattlerpg.model.BattleEnemy
import com.pegasus.cardbattlerpg.model.BattleState
import com.pegasus.cardbattlerpg.repository.GameRepository
import com.pegasus.cardbattlerpg.utils.GameAudioManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.cos
import kotlin.math.sin

import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import android.os.Build

@Composable
fun battleDrawableId(name: String): Int {
    val context = LocalContext.current
    return remember(name) {
        context.resources.getIdentifier(name, "drawable", context.packageName)
    }
}

@Composable
fun safeHeroImage(hero: HeroEntity?): Int {
    val res = battleDrawableId(hero?.image ?: "")
    return if (res != 0) res else R.drawable.hero_unknown
}

@Composable
fun safeEnemyImage(enemy: BattleEnemy): Int {
    val fixedImageName = when (enemy.image) {
        "boss_stage_1" -> "enemy_forest_king"
        "boss_stage_1.png" -> "enemy_forest_king"

        "boss_stage_2" -> "enemy_ruins_overlord"
        "boss_stage_2.png" -> "enemy_ruins_overlord"

        "boss_stage_3" -> "enemy_dragon_lord"
        "boss_stage_3.png" -> "enemy_dragon_lord"

        else -> enemy.image.removeSuffix(".png")
    }

    val res = battleDrawableId(fixedImageName)
    return if (res != 0) res else R.drawable.enemy_unknown
}

@Composable
fun safeBattleCardImage(card: CardEntity): Int {
    val res = battleDrawableId(card.image)
    return if (res != 0) res else R.drawable.card_unknown
}

@Composable
fun BattleScreen(
    repository: GameRepository,
    storyStageId: Int? = null,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var state by remember { mutableStateOf<BattleState?>(null) }
    var heroAttack by remember { mutableIntStateOf(0) }
    var heroDefense by remember { mutableIntStateOf(0) }

    var effectToken by remember { mutableIntStateOf(0) }
    var effectTarget by remember { mutableStateOf("none") }
    var floatingDamage by remember { mutableStateOf("") }
    var lastSkillName by remember { mutableStateOf("") }
    var currentAttackCard by remember { mutableStateOf<CardEntity?>(null) }
    var battlePotions by remember { mutableStateOf<List<InventoryEntity>>(emptyList()) }

    suspend fun startBattle() {
        val hero = repository.getSelectedHero()
        val equipments = repository.getEquippedItems()
        val deckCards = repository.getBattleDeckCards()
        battlePotions = repository.getInventory().filter { it.itemType == "Consumable" && it.itemName.contains("Potion", ignoreCase = true) }

        effectTarget = "none"
        floatingDamage = ""
        lastSkillName = ""

        if (hero == null) {
            state = null
            return
        }

        val storyStage = storyStageId?.let { repository.getStoryStage(it) }

        val enemy =
            if (storyStage != null) {
                BattleEngine.createEnemyFromStage(storyStage)
            } else {
                BattleEngine.createEnemy()
            }

        val maxHp = BattleEngine.calculateHeroMaxHp(hero, equipments)

        heroAttack = BattleEngine.calculateHeroAttack(hero, equipments)
        heroDefense = BattleEngine.calculateHeroDefense(hero, equipments)

        val hand = deckCards.shuffled().take(5)

        state = BattleState(
            hero = hero,
            heroMaxHp = maxHp,
            heroCurrentHp = maxHp,
            enemy = enemy,
            handCards = hand,
            battleLog = listOf("Battle dimulai melawan ${enemy.name}."),
            isPlayerTurn = true,
            isFinished = false,
            isVictory = false
        )
    }

    fun useCard(card: CardEntity) {
        val current = state ?: return
        if (!current.isPlayerTurn || current.isFinished) return

        GameAudioManager.playAttack()

        val action = BattleEngine.resolveCardAction(
            heroAttack = heroAttack,
            card = card,
            enemy = current.enemy,
            heroCurrentHp = current.heroCurrentHp,
            heroMaxHp = current.heroMaxHp,
            comboCount = current.comboCount
        )

        val damage = action.damage
        effectToken++
        effectTarget = "enemy"
        floatingDamage = if (action.heal > 0) "-$damage  +${action.heal}HP" else "-$damage"
        lastSkillName = action.effectName
        currentAttackCard = card

        val newEnemyHp = max(0, current.enemy.currentHp - damage)
        val enemy = current.enemy.copy(currentHp = newEnemyHp)
        val healedHeroHp = (current.heroCurrentHp + action.heal).coerceAtMost(current.heroMaxHp)

        val logs = current.battleLog + action.logText

        if (newEnemyHp <= 0) {
            GameAudioManager.playVictory()
            state = current.copy(
                enemy = enemy,
                heroCurrentHp = healedHeroHp,
                battleLog = logs + "${enemy.name} dikalahkan!",
                isFinished = true,
                isVictory = true,
                comboCount = current.comboCount + 1,
                criticalHit = action.isCritical,
                lastDamage = damage
            )

            scope.launch {
                val heroId = current.hero?.id ?: return@launch

                repository.giveBattleReward(
                    heroId = heroId,
                    gold = enemy.rewardGold,
                    exp = enemy.rewardExp
                )

                repository.addMissionProgress("First Battle", 1)
                repository.addAchievementProgress("Battle Rookie", 1)

                if (storyStageId != null) {
                    repository.completeStoryStage(storyStageId)
                }
            }

            return
        }

        state = current.copy(
            enemy = enemy,
            heroCurrentHp = healedHeroHp,
            battleLog = logs + "Giliran musuh.",
            isPlayerTurn = false,
            comboCount = current.comboCount + 1,
            criticalHit = action.isCritical,
            lastDamage = damage
        )

        scope.launch {
            delay(900)

            val latest = state ?: return@launch

            val enemyDamage = BattleEngine.calculateEnemyDamage(
                enemyAttack = latest.enemy.attack,
                heroDefense = heroDefense
            )

            GameAudioManager.playEnemyAttack()
            GameAudioManager.playHit()

            effectToken++
            effectTarget = "hero"
            floatingDamage = "-$enemyDamage"
            lastSkillName = latest.enemy.name

            val newHeroHp = max(0, latest.heroCurrentHp - enemyDamage)

            val enemyLog =
                latest.battleLog + "${latest.enemy.name} menyerang, damage $enemyDamage."

            state =
                if (newHeroHp <= 0) {
                    GameAudioManager.playDefeat()
                    latest.copy(
                        heroCurrentHp = newHeroHp,
                        battleLog = enemyLog + "Hero kalah.",
                        isFinished = true,
                        isVictory = false,
                        lastDamage = enemyDamage
                    )
                } else {
                    latest.copy(
                        heroCurrentHp = newHeroHp,
                        battleLog = enemyLog + "Giliran player.",
                        isPlayerTurn = true,
                        lastDamage = enemyDamage
                    )
                }
        }
    }

    fun usePotion(item: InventoryEntity) {
        val current = state ?: return
        if (current.isFinished || current.heroCurrentHp >= current.heroMaxHp) return
        scope.launch {
            GameAudioManager.playPotion()
            val result = repository.useBattlePotion(item, current.heroCurrentHp, current.heroMaxHp)
            battlePotions = repository.getInventory().filter { it.itemType == "Consumable" && it.itemName.contains("Potion", ignoreCase = true) }
            state = current.copy(
                heroCurrentHp = result.second,
                battleLog = current.battleLog + result.first
            )
            effectToken++
            effectTarget = "hero"
            floatingDamage = "+${result.second - current.heroCurrentHp} HP"
            lastSkillName = item.itemName
        }
    }

    LaunchedEffect(Unit) {
        startBattle()
    }

    LaunchedEffect(effectToken) {
        if (effectToken > 0) {
            delay(950)
            effectTarget = "none"
            floatingDamage = ""
            currentAttackCard = null
        }
    }

    val battle = state

    if (battle == null) {
        MissingHeroScreen(onBack = onBack)
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.battle_background),
            contentDescription = "Battle Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        CinematicBattleOverlay()

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            BattleTopBar(
                battle = battle,
                onBack = onBack
            )

            BattleArena(
                battle = battle,
                effectToken = effectToken,
                effectTarget = effectTarget,
                floatingDamage = floatingDamage,
                lastSkillName = lastSkillName,
                currentAttackCard = currentAttackCard,
                modifier = Modifier.weight(1f)
            )

            BattlePotionPanel(
                potions = battlePotions,
                enabled = battle.isPlayerTurn && !battle.isFinished && battle.heroCurrentHp < battle.heroMaxHp,
                onPotionClick = { usePotion(it) }
            )

            BattleHandCards(
                cards = battle.handCards,
                enabled = battle.isPlayerTurn && !battle.isFinished,
                onCardClick = { useCard(it) }
            )

        }

        AnimatedVisibility(
            visible = battle.isFinished,
            enter = scaleIn(
                initialScale = 0.72f,
                animationSpec = tween(560, easing = EaseOutBack)
            ) + fadeIn(tween(420)),
            exit = scaleOut() + fadeOut()
        ) {
            BattleFinishPopup(
                isVictory = battle.isVictory,
                rewardGold = battle.enemy.rewardGold,
                rewardExp = battle.enemy.rewardExp,
                lastLog = battle.battleLog.lastOrNull().orEmpty(),
                onRestart = {
                    GameAudioManager.playClick()
                    scope.launch {
                        startBattle()
                    }
                },
                onExit = {
                    GameAudioManager.playClick()
                    onBack()
                }
            )
        }
    }
}

@Composable
fun MissingHeroScreen(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF07000D),
                        Color(0xFF210038),
                        Color(0xFF07000D)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(22.dp)
                .shadow(24.dp, RoundedCornerShape(32.dp)),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xEE160021))
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Hero Belum Dipilih",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Pilih hero terlebih dahulu sebelum masuk Battle Arena.",
                    color = Color(0xFFEBD9FF),
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                PremiumBattleButton(
                    text = "Kembali",
                    onClick = onBack
                )
            }
        }
    }
}

@Composable
fun CinematicBattleOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xCC040006),
                        Color(0x22000000),
                        Color(0xF008000F)
                    )
                )
            )
    )
}

@Composable
fun BattleTopBar(
    battle: BattleState,
    onBack: () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "top_bar_fx")
    val glow by infinite.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "turn_glow"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 6.dp)
            .shadow(
                elevation = 22.dp,
                shape = RoundedCornerShape(30.dp),
                ambientColor = if (battle.isPlayerTurn) Color(0xFFFFD66B) else Color(0xFFFF3D68),
                spotColor = if (battle.isPlayerTurn) Color(0xFFB56CFF) else Color(0xFFFF3D68)
            ),
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(
                    Color.White.copy(alpha = 0.22f),
                    if (battle.isPlayerTurn) Color(0xFFFFD66B).copy(alpha = glow) else Color(0xFFFF3D68).copy(alpha = glow),
                    Color.White.copy(alpha = 0.12f)
                )
            )
        ),
        colors = CardDefaults.cardColors(containerColor = Color(0xDD160021))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF14001E),
                            Color(0xFF4B1478),
                            Color(0xFF120018)
                        )
                    )
                )
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.height(40.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFD66B).copy(alpha = 0.55f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White,
                        containerColor = Color.Black.copy(alpha = 0.18f)
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp)
                ) {
                    Text("Back", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Battle Arena",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = if (battle.isPlayerTurn) "Giliran Player • Pilih kartu terbaik" else "Giliran Musuh • Bertahan!",
                        color = if (battle.isPlayerTurn) Color(0xFFFFD66B) else Color(0xFFFF7B91),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                TurnIndicator(
                    isPlayerTurn = battle.isPlayerTurn,
                    isFinished = battle.isFinished
                )
            }
        }
    }
}

@Composable
fun TurnIndicator(
    isPlayerTurn: Boolean,
    isFinished: Boolean
) {
    val infinite = rememberInfiniteTransition(label = "turn_indicator")
    val scale by infinite.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "turn_scale"
    )

    val color =
        if (isFinished) Color(0xFFFFD66B)
        else if (isPlayerTurn) Color(0xFF6CFF9B)
        else Color(0xFFFF3D68)

    Box(
        modifier = Modifier
            .size(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .background(
                Brush.radialGradient(
                    listOf(color, Color(0xFF160021))
                ),
                CircleShape
            )
            .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isFinished) "END" else if (isPlayerTurn) "YOU" else "EN",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun BattleArena(
    battle: BattleState,
    effectToken: Int,
    effectTarget: String,
    floatingDamage: String,
    lastSkillName: String,
    currentAttackCard: CardEntity?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 2.dp)
    ) {
        EnemyUnit(
            enemy = battle.enemy,
            isHit = effectTarget == "enemy",
            isAttacking = effectTarget == "hero",
            isDestroyed = battle.isFinished && battle.isVictory,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 0.dp)
        )

        AttackGifOnEnemy(
            token = effectToken,
            visible = effectTarget == "enemy",
            card = currentAttackCard,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 82.dp)
        )

        HeroUnit(
            hero = battle.hero,
            hp = battle.heroCurrentHp,
            maxHp = battle.heroMaxHp,
            isHit = effectTarget == "hero",
            isAttacking = effectTarget == "enemy",
            isDefeated = battle.isFinished && !battle.isVictory,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 8.dp, bottom = 10.dp)
        )

        AttackSlashEffect(
            token = effectToken,
            target = effectTarget,
            modifier = Modifier.align(Alignment.Center)
        )

        BloodSprayEffect(
            token = effectToken,
            target = effectTarget,
            modifier = Modifier.align(
                if (effectTarget == "enemy") Alignment.TopCenter else Alignment.BottomStart
            )
        )

        FloatingDamageText(
            token = effectToken,
            target = effectTarget,
            damageText = floatingDamage,
            modifier = Modifier.align(
                if (effectTarget == "enemy") Alignment.TopCenter else Alignment.BottomStart
            )
        )

        if (lastSkillName.isNotBlank() && effectTarget != "none") {
            SkillNameBurst(
                text = lastSkillName,
                target = effectTarget,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        BattleCommandBubble(
            text = when {
                battle.isFinished && battle.isVictory -> "Musuh hancur! Victory!"
                battle.isFinished && !battle.isVictory -> "Hero tumbang... Defeat!"
                battle.isPlayerTurn -> "Pilih kartu untuk menyerang!"
                else -> "Musuh sedang menyerang..."
            },
            isPlayerTurn = battle.isPlayerTurn,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 2.dp)
        )
    }
}

@Composable
fun AttackGifOnEnemy(
    token: Int,
    visible: Boolean,
    card: CardEntity?,
    modifier: Modifier = Modifier
) {
    if (!visible || token <= 0 || card == null) return

    val gifRes = safeAttackGif(card)

    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 920,
            easing = FastOutSlowInEasing
        ),
        label = "attack_gif_$token"
    )

    val context = LocalContext.current

    Box(
        modifier = modifier
            .size(190.dp)
            .graphicsLayer {
                alpha = (1f - progress * 0.18f).coerceIn(0f, 1f)
                scaleX = 0.82f + progress * 0.28f
                scaleY = 0.82f + progress * 0.28f
                translationY = -progress * 8f
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(gifRes)
                .decoderFactory(
                    if (Build.VERSION.SDK_INT >= 28) {
                        ImageDecoderDecoder.Factory()
                    } else {
                        GifDecoder.Factory()
                    }
                )
                .build(),
            contentDescription = card.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun BattleGroundGlow(modifier: Modifier = Modifier) {
    // No-op: removed arena glow to prevent transparent square artifacts behind characters.
}

@Composable
fun EnemyUnit(
    enemy: BattleEnemy,
    isHit: Boolean,
    isAttacking: Boolean,
    isDestroyed: Boolean,
    modifier: Modifier = Modifier
) {
    val imageRes = safeEnemyImage(enemy)

    val breath = rememberInfiniteTransition(label = "enemy_breath")
    val breathY by breath.animateFloat(
        initialValue = -2f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1550, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "enemy_breath_y"
    )
    val breathScale by breath.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(1550, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "enemy_breath_scale"
    )
    val attackLunge by animateFloatAsState(
        targetValue = if (isAttacking) 1f else 0f,
        animationSpec = keyframes {
            durationMillis = 520
            0f at 0
            1f at 160
            0f at 520
        },
        label = "enemy_attack_lunge"
    )

    val hitShake by animateFloatAsState(
        targetValue = if (isHit) 1f else 0f,
        animationSpec = keyframes {
            durationMillis = 420
            0f at 0
            1f at 80
            -1f at 160
            0.7f at 240
            0f at 420
        },
        label = "enemy_hit_shake"
    )

    val destroyedAlpha by animateFloatAsState(
        targetValue = if (isDestroyed) 0f else 1f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "enemy_destroy_alpha"
    )

    val destroyedScale by animateFloatAsState(
        targetValue = if (isDestroyed) 1.35f else 1f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "enemy_destroy_scale"
    )

    val destroyedRotation by animateFloatAsState(
        targetValue = if (isDestroyed) -18f else 0f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "enemy_destroy_rotate"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationX = hitShake * 12f
                alpha = destroyedAlpha
                scaleX = destroyedScale
                scaleY = destroyedScale
                rotationZ = destroyedRotation
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PremiumBattleHpBar(
            name = enemy.name,
            hp = enemy.currentHp,
            maxHp = enemy.maxHp,
            color = Color(0xFFFF3D68),
            secondaryColor = Color(0xFFFFD66B),
            width = 236,
            tag = if (enemy.isBoss) "BOSS" else enemy.enemyType.uppercase()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = enemy.name,
                modifier = Modifier
                    .height(if (enemy.isBoss) 166.dp else 134.dp)
                    .fillMaxWidth(0.76f)
                    .graphicsLayer {
                        translationY = breathY + attackLunge * 36f
                        translationX = -attackLunge * 18f
                        scaleX = breathScale
                        scaleY = breathScale
                    },
                contentScale = ContentScale.Fit
            )

            if (isHit) {
                ClawWoundEffect(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = 8.dp, y = (-8).dp)
                )
            }

            if (isDestroyed) {
                DestroyParticles()
            }
        }

        Text(
            text = "${enemy.element} • ${enemy.enemyType}",
            color = Color(0xFFFFD66B),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.32f), RoundedCornerShape(50.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun DestroyParticles() {
    val infinite = rememberInfiniteTransition(label = "destroy_particles")
    val burst by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "burst"
    )

    repeat(10) { index ->
        val angle = index * 36f
        Box(
            modifier = Modifier
                .size((6 + index % 4).dp)
                .rotate(angle)
                .offset(
                    x = ((index % 5) * 18 * burst).dp,
                    y = ((index % 3) * -20 * burst).dp
                )
                .alpha(1f - burst)
                .background(
                    if (index % 2 == 0) Color(0xFFFFD66B) else Color(0xFFB56CFF),
                    CircleShape
                )
        )
    }
}

@Composable
fun HeroUnit(
    hero: HeroEntity?,
    hp: Int,
    maxHp: Int,
    isHit: Boolean,
    isAttacking: Boolean,
    isDefeated: Boolean,
    modifier: Modifier = Modifier
) {
    val imageRes = safeHeroImage(hero)

    val breath = rememberInfiniteTransition(label = "hero_breath")
    val breathY by breath.animateFloat(
        initialValue = 3f,
        targetValue = -4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1450, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_breath_y"
    )
    val breathScale by breath.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(1450, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_breath_scale"
    )
    val attackLunge by animateFloatAsState(
        targetValue = if (isAttacking) 1f else 0f,
        animationSpec = keyframes {
            durationMillis = 520
            0f at 0
            1f at 160
            0f at 520
        },
        label = "hero_attack_lunge"
    )

    val hitShake by animateFloatAsState(
        targetValue = if (isHit) 1f else 0f,
        animationSpec = keyframes {
            durationMillis = 420
            0f at 0
            -1f at 80
            1f at 160
            -0.7f at 240
            0f at 420
        },
        label = "hero_hit_shake"
    )

    val defeatedAlpha by animateFloatAsState(
        targetValue = if (isDefeated) 0.35f else 1f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "hero_defeated_alpha"
    )

    val defeatedRotation by animateFloatAsState(
        targetValue = if (isDefeated) 10f else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "hero_defeated_rotation"
    )

    Column(
        modifier = modifier
            .width(190.dp)
            .graphicsLayer {
                translationX = hitShake * 12f
                alpha = defeatedAlpha
                rotationZ = defeatedRotation
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PremiumBattleHpBar(
            name = hero?.name ?: "Hero",
            hp = hp,
            maxHp = maxHp,
            color = Color(0xFF55E27A),
            secondaryColor = Color(0xFF4FC3F7),
            width = 184,
            tag = "HERO"
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = hero?.name ?: "Hero",
                modifier = Modifier
                    .height(128.dp)
                    .fillMaxWidth()
                    .graphicsLayer {
                        translationY = breathY - attackLunge * 20f
                        translationX = attackLunge * 58f
                        scaleX = breathScale
                        scaleY = breathScale
                    },
                contentScale = ContentScale.Fit
            )

            if (isHit) {
                ClawWoundEffect(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = 12.dp, y = (-4).dp)
                )
            }
        }
    }
}

@Composable
fun PremiumBattleHpBar(
    name: String,
    hp: Int,
    maxHp: Int,
    color: Color,
    secondaryColor: Color,
    width: Int,
    tag: String
) {
    val progress = if (maxHp <= 0) 0f else hp.toFloat() / maxHp.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(480, easing = FastOutSlowInEasing),
        label = "hp_progress"
    )

    val hpColor =
        when {
            progress <= 0.25f -> Color(0xFFFF3D68)
            progress <= 0.55f -> Color(0xFFFFB74D)
            else -> color
        }

    Card(
        modifier = Modifier
            .width(width.dp)
            .shadow(12.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(
                    Color.White.copy(alpha = 0.22f),
                    secondaryColor.copy(alpha = 0.8f),
                    Color.White.copy(alpha = 0.12f)
                )
            )
        ),
        colors = CardDefaults.cardColors(containerColor = Color(0xD9040008))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = tag,
                    color = secondaryColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(Color.Black.copy(alpha = 0.76f), RoundedCornerShape(50.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(50.dp))
                    .padding(3.dp)
            ) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxSize()
                        .clipCompat(RoundedCornerShape(50.dp)),
                    color = hpColor,
                    trackColor = Color.Transparent
                )

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    hpColor.copy(alpha = 0.72f),
                                    Color.White.copy(alpha = 0.42f),
                                    hpColor.copy(alpha = 0.92f)
                                )
                            ),
                            RoundedCornerShape(50.dp)
                        )
                )

                Text(
                    text = "$hp/$maxHp",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(Color.Black.copy(alpha = 0.72f), RoundedCornerShape(50.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

fun Modifier.clipCompat(shape: RoundedCornerShape): Modifier {
    return this.background(Color.Transparent, shape)
}


@Composable
fun BloodSprayEffect(
    token: Int,
    target: String,
    modifier: Modifier = Modifier
) {
    if (token <= 0 || target == "none") return

    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(620, easing = FastOutSlowInEasing),
        label = "blood_spray_$token"
    )

    val baseTop = if (target == "enemy") 142.dp else 230.dp
    val baseStart = if (target == "hero") 76.dp else 0.dp

    Box(
        modifier = modifier
            .padding(top = baseTop, start = baseStart)
            .size(150.dp)
            .graphicsLayer {
                alpha = (1f - progress).coerceIn(0f, 1f)
                scaleX = 0.82f + progress * 0.35f
                scaleY = 0.82f + progress * 0.35f
            },
        contentAlignment = Alignment.Center
    ) {
        repeat(12) { index ->
            val direction = if (target == "enemy") -1f else 1f
            val angle = (-70f + index * 13f)
            val distance = (18f + (index % 5) * 9f) * progress
            val x = (cos(Math.toRadians(angle.toDouble())).toFloat() * distance * direction).dp
            val y = (sin(Math.toRadians(angle.toDouble())).toFloat() * distance).dp

            Box(
                modifier = Modifier
                    .offset(x = x, y = y)
                    .size((4 + index % 3).dp, (7 + index % 4).dp)
                    .rotate(angle + 90f)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFFF1E3C),
                                Color(0xFF7A0012)
                            )
                        ),
                        RoundedCornerShape(50.dp)
                    )
            )
        }

        Box(
            modifier = Modifier
                .width(96.dp)
                .height(18.dp)
                .rotate(if (target == "enemy") -18f else 18f)
                .alpha(0.76f)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color(0xFFFF1E3C),
                            Color(0xFFFFD0D7),
                            Color(0xFF9B0018),
                            Color.Transparent
                        )
                    ),
                    RoundedCornerShape(50.dp)
                )
        )
    }
}

@Composable
fun ClawWoundEffect(
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(720, easing = EaseOutBack),
        label = "claw_wound_fx"
    )

    Box(
        modifier = modifier
            .size(92.dp)
            .graphicsLayer {
                alpha = (1f - progress * 0.08f).coerceIn(0f, 1f)
                scaleX = 0.82f + progress * 0.22f
                scaleY = 0.82f + progress * 0.22f
            },
        contentAlignment = Alignment.Center
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .offset(x = ((index - 1) * 13).dp, y = 0.dp)
                    .width(8.dp)
                    .height(62.dp)
                    .rotate(-24f)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0xFFFFE1E6).copy(alpha = 0.88f),
                                Color(0xFFFF2446),
                                Color(0xFF7A0012),
                                Color.Transparent
                            )
                        ),
                        RoundedCornerShape(50.dp)
                    )
            )
        }

        repeat(6) { index ->
            val x = ((index % 3) * 18 - 18).dp
            val y = ((index / 3) * 18 + 16).dp
            Box(
                modifier = Modifier
                    .offset(x = x, y = y)
                    .size((5 + index % 2).dp)
                    .alpha((0.92f - progress * 0.45f).coerceIn(0f, 1f))
                    .background(Color(0xFFD40022), CircleShape)
            )
        }
    }
}

@Composable
fun AttackSlashEffect(
    token: Int,
    target: String,
    modifier: Modifier = Modifier
) {
    if (token <= 0 || target == "none") return

    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(520, easing = FastOutSlowInEasing),
        label = "attack_slash_$token"
    )

    val color = if (target == "enemy") Color(0xFFFFD66B) else Color(0xFFFF3D68)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .graphicsLayer {
                alpha = 1f - progress
                translationX = if (target == "enemy") progress * 110f - 54f else -progress * 110f + 54f
                translationY = if (target == "enemy") -progress * 70f else progress * 60f
                rotationZ = if (target == "enemy") -18f else 18f
                scaleX = 0.8f + progress * 0.55f
                scaleY = 0.8f + progress * 0.55f
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(230.dp)
                .height(18.dp)
                .blur(2.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            color,
                            Color.White,
                            color,
                            Color.Transparent
                        )
                    ),
                    RoundedCornerShape(50.dp)
                )
        )

        Box(
            modifier = Modifier
                .width(120.dp)
                .height(120.dp)
                .alpha(0.42f)
                .background(
                    Brush.radialGradient(
                        listOf(color, Color.Transparent)
                    ),
                    CircleShape
                )
        )
    }
}

@Composable
fun FloatingDamageText(
    token: Int,
    target: String,
    damageText: String,
    modifier: Modifier = Modifier
) {
    if (token <= 0 || target == "none" || damageText.isBlank()) return

    val rise by animateDpAsState(
        targetValue = 0.dp,
        animationSpec = tween(1),
        label = "damage_rise_start"
    )

    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(850, easing = EaseOutBack),
        label = "damage_float_$token"
    )

    Text(
        text = damageText,
        color = if (target == "enemy") Color(0xFFFFD66B) else Color(0xFFFF5D78),
        fontSize = 30.sp,
        fontWeight = FontWeight.Black,
        modifier = modifier
            .padding(
                top = if (target == "enemy") 120.dp else 260.dp,
                start = if (target == "hero") 88.dp else 0.dp
            )
            .offset(y = rise - (progress * 46).dp)
            .graphicsLayer {
                alpha = 1f - (progress * 0.15f)
                scaleX = 0.7f + progress * 0.45f
                scaleY = 0.7f + progress * 0.45f
            }
            .shadow(10.dp, CircleShape)
    )
}

@Composable
fun SkillNameBurst(
    text: String,
    target: String,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(720, easing = EaseOutBack),
        label = "skill_name_burst"
    )

    Text(
        text = text.uppercase(),
        color = if (target == "enemy") Color(0xFFFFD66B) else Color(0xFFFF7B91),
        fontSize = 13.sp,
        fontWeight = FontWeight.ExtraBold,
        textAlign = TextAlign.Center,
        modifier = modifier
            .padding(top = if (target == "enemy") 20.dp else 160.dp)
            .graphicsLayer {
                alpha = 1f - progress * 0.35f
                scaleX = 0.92f + progress * 0.18f
                scaleY = 0.92f + progress * 0.18f
            }
            .background(Color.Black.copy(alpha = 0.48f), RoundedCornerShape(50.dp))
            .border(
                1.dp,
                if (target == "enemy") Color(0xFFFFD66B).copy(alpha = 0.65f) else Color(0xFFFF7B91).copy(alpha = 0.65f),
                RoundedCornerShape(50.dp)
            )
            .padding(horizontal = 16.dp, vertical = 7.dp)
    )
}

@Composable
fun BattleCommandBubble(
    text: String,
    isPlayerTurn: Boolean,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "command_bubble")
    val glow by infinite.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "command_glow"
    )

    Text(
        text = text,
        color = Color.White,
        fontSize = 14.sp,
        fontWeight = FontWeight.ExtraBold,
        textAlign = TextAlign.Center,
        modifier = modifier
            .shadow(
                12.dp,
                RoundedCornerShape(50.dp),
                ambientColor = if (isPlayerTurn) Color(0xFFFFD66B) else Color(0xFFFF3D68),
                spotColor = if (isPlayerTurn) Color(0xFFFFD66B) else Color(0xFFFF3D68)
            )
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color.Black.copy(alpha = 0.62f),
                        if (isPlayerTurn) Color(0x884B1478) else Color(0x884A0816),
                        Color.Black.copy(alpha = 0.62f)
                    )
                ),
                RoundedCornerShape(50.dp)
            )
            .border(
                1.dp,
                if (isPlayerTurn) Color(0xFFFFD66B).copy(alpha = glow) else Color(0xFFFF5D78).copy(alpha = glow),
                RoundedCornerShape(50.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}


@Composable
fun BattlePotionPanel(
    potions: List<InventoryEntity>,
    enabled: Boolean,
    onPotionClick: (InventoryEntity) -> Unit
) {
    if (potions.isEmpty()) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 2.dp)
            .shadow(12.dp, RoundedCornerShape(24.dp), ambientColor = Color(0xFF6CFF9B), spotColor = Color(0xFF4FC3F7)),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color(0x446CFF9B)),
        colors = CardDefaults.cardColors(containerColor = Color(0xCC08000F))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Color(0xAA06140A), Color(0xAA160021))))
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Potion",
                color = Color(0xFF6CFF9B),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(potions, key = { it.id }) { item ->
                    Surface(
                        modifier = Modifier.clickable { if (enabled) onPotionClick(item) },
                        color = if (enabled) Color(0x226CFF9B) else Color.White.copy(alpha = 0.06f),
                        shape = RoundedCornerShape(50.dp),
                        border = BorderStroke(1.dp, if (enabled) Color(0x886CFF9B) else Color.White.copy(alpha = 0.14f))
                    ) {
                        Text(
                            text = "${item.itemName} x${item.quantity}",
                            color = if (enabled) Color.White else Color.White.copy(alpha = 0.42f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BattleHandCards(
    cards: List<CardEntity>,
    enabled: Boolean,
    onCardClick: (CardEntity) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(184.dp)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .shadow(
                20.dp,
                RoundedCornerShape(32.dp),
                ambientColor = Color(0xFFB56CFF),
                spotColor = Color(0xFFFFD66B)
            ),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(
                    Color.White.copy(alpha = 0.16f),
                    Color(0xFFFFD66B).copy(alpha = 0.35f),
                    Color.White.copy(alpha = 0.10f)
                )
            )
        ),
        colors = CardDefaults.cardColors(containerColor = Color(0xDF100019))
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xEE1A0027),
                            Color(0xF006000A)
                        )
                    )
                )
                .padding(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Kartu Tangan",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = if (enabled) "Tap kartu untuk menyerang" else "Menunggu giliran...",
                        color = if (enabled) Color(0xFFFFD66B) else Color(0xFFFF7B91),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "${cards.size} Cards",
                    color = Color(0xFFEBD9FF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(50.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(9.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp)
            ) {
                items(cards) { card ->
                    BattleCardItem(
                        card = card,
                        enabled = enabled,
                        onClick = { onCardClick(card) }
                    )
                }
            }
        }
    }
}

@Composable
fun BattleCardItem(
    card: CardEntity,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val imageRes = safeBattleCardImage(card)
    val rarityColor = battleRarityColor(card.rarity)

    val pressScale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.96f,
        animationSpec = tween(300),
        label = "card_enabled_scale"
    )

    Card(
        modifier = Modifier
            .width(116.dp)
            .height(142.dp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
                rotationZ = if (enabled) -2.5f else 0f
                rotationX = if (enabled) 4f else 0f
                cameraDistance = 14f * density
            }
            .shadow(
                elevation = if (enabled) 20.dp else 5.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = if (enabled) rarityColor else Color.Black,
                spotColor = if (enabled) rarityColor else Color.Black
            )
            .clickable {
                if (enabled) onClick()
            },
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            1.dp,
            if (enabled) rarityColor.copy(alpha = 0.72f) else Color.White.copy(alpha = 0.10f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) Color(0xFF13001E) else Color(0xFF111111)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = card.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp)
                    .alpha(if (enabled) 1f else 0.36f),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0x7712001F),
                                Color(0xF008000F)
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(7.dp)
                    .size(28.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(rarityColor, Color(0xFF190026))
                        ),
                        CircleShape
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.28f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = card.mana.toString(),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Text(
                text = card.rarity.uppercase(),
                color = rarityColor,
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(7.dp)
                    .background(Color.Black.copy(alpha = 0.42f), RoundedCornerShape(50.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(9.dp)
            ) {
                Text(
                    text = card.name,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )

                Text(
                    text = card.skillName,
                    color = Color(0xFFFFD66B),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    MiniBattleStat("ATK", card.attack, Color(0xFFFF7777))
                    MiniBattleStat("HP", card.hp, Color(0xFF6CFF9B))
                }
            }
        }
    }
}

@Composable
fun MiniBattleStat(
    label: String,
    value: Int,
    color: Color
) {
    Text(
        text = "$label $value",
        color = color,
        fontSize = 8.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier
            .background(color.copy(alpha = 0.14f), RoundedCornerShape(50.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    )
}

@Composable
fun BattleFinishPopup(
    isVictory: Boolean,
    rewardGold: Int,
    rewardExp: Int,
    lastLog: String,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "finish_popup_fx")
    val pulse by infinite.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1250, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "finish_icon_pulse"
    )

    val mainColor = if (isVictory) Color(0xFFFFD66B) else Color(0xFFFF3D68)
    val subColor = if (isVictory) Color(0xFF6CFF9B) else Color(0xFFFFB3C0)
    val title = if (isVictory) "VICTORY" else "DEFEAT"
    val subtitle = if (isVictory) {
        "Reward: $rewardGold Gold • $rewardExp EXP"
    } else {
        lastLog.ifBlank { "Hero tumbang. Perkuat deck dan coba lagi." }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.62f))
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 340.dp)
                .shadow(
                    28.dp,
                    RoundedCornerShape(34.dp),
                    ambientColor = mainColor,
                    spotColor = mainColor
                ),
            shape = RoundedCornerShape(34.dp),
            border = BorderStroke(
                1.dp,
                Brush.horizontalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.20f),
                        mainColor.copy(alpha = 0.75f),
                        Color.White.copy(alpha = 0.12f)
                    )
                )
            ),
            colors = CardDefaults.cardColors(containerColor = Color(0xF20B0010))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                mainColor.copy(alpha = 0.18f),
                                Color(0xF014001E),
                                Color(0xF006000A)
                            )
                        )
                    )
                    .padding(horizontal = 22.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(78.dp)
                        .graphicsLayer {
                            scaleX = pulse
                            scaleY = pulse
                        }
                        .background(
                            Brush.radialGradient(
                                listOf(mainColor, Color(0xFF170024))
                            ),
                            CircleShape
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.34f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isVictory) "★" else "!",
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = subtitle,
                    color = subColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PremiumBattleButton(
                        text = "Battle Lagi",
                        onClick = onRestart,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedButton(
                        onClick = onExit,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(17.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.32f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White.copy(alpha = 0.08f),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Keluar", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}


@Composable
fun PremiumBattleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(46.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFFD66B),
            contentColor = Color(0xFF160021)
        ),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

fun battleRarityColor(rarity: String): Color {
    return when (rarity) {
        "Legendary" -> Color(0xFFFFD700)
        "Epic" -> Color(0xFFB56CFF)
        "Rare" -> Color(0xFF4FC3F7)
        else -> Color(0xFFC9C9C9)
    }
}

@Composable
fun safeAttackGif(card: CardEntity?): Int {
    if (card == null) return R.drawable.gif_default_attack

    val context = LocalContext.current

    val gifName = if (card.attackGif.isNotBlank()) {
        card.attackGif
    } else {
        "gif_${card.image.removePrefix("card_")}_attack"
    }

    val res = remember(gifName) {
        context.resources.getIdentifier(
            gifName,
            "drawable",
            context.packageName
        )
    }

    return if (res != 0) res else R.drawable.gif_default_attack
}