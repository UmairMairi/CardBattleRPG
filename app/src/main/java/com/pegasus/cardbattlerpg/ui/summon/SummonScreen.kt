package com.pegasus.cardbattlerpg.ui.summon

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
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
import com.pegasus.cardbattlerpg.entity.PlayerEntity
import com.pegasus.cardbattlerpg.repository.GameRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun summonDrawableIdByName(name: String): Int {
    val context = LocalContext.current
    return remember(name) {
        context.resources.getIdentifier(name, "drawable", context.packageName)
    }
}

@Composable
fun safeSummonImage(card: CardEntity): Int {
    val res = summonDrawableIdByName(card.image)
    return if (res != 0) res else R.drawable.card_unknown
}

@Composable
fun SmoothShineEffect(
    shineOffset: Float,
    modifier: Modifier = Modifier,
    alpha: Float = 0.08f,
    height: Int = 280,
    width: Int = 70
) {
    Box(
        modifier = modifier
            .offset(x = shineOffset.dp)
            .width(width.dp)
            .height(height.dp)
            .rotate(18f)
            .alpha(alpha)
            .blur(10.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.02f),
                        Color.White.copy(alpha = 0.14f),
                        Color.White.copy(alpha = 0.03f),
                        Color.Transparent
                    )
                ),
                RoundedCornerShape(90.dp)
            )
    )
}

@Composable
fun SummonScreen(
    repository: GameRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var player by remember { mutableStateOf<PlayerEntity?>(null) }
    var message by remember { mutableStateOf("") }
    var resultCards by remember { mutableStateOf<List<CardEntity>>(emptyList()) }
    var isSummoning by remember { mutableStateOf(false) }
    var revealCard by remember { mutableStateOf(false) }

    var summonTicket by remember { mutableStateOf(0) }
    var epicTicket by remember { mutableStateOf(0) }
    var legendaryTicket by remember { mutableStateOf(0) }

    suspend fun reload() {
        player = repository.getPlayer()
        summonTicket = repository.getTicketAmount("Summon Ticket")
        epicTicket = repository.getTicketAmount("Epic Ticket")
        legendaryTicket = repository.getTicketAmount("Legendary Ticket")
    }

    suspend fun runSingleSummon(
        loadingText: String,
        summonAction: suspend () -> Pair<String, CardEntity?>
    ) {
        if (isSummoning) return

        isSummoning = true
        revealCard = false
        resultCards = emptyList()
        message = loadingText

        delay(900)

        val result = summonAction()
        message = result.first
        resultCards = result.second?.let { listOf(it) } ?: emptyList()

        reload()
        delay(260)

        revealCard = true
        isSummoning = false
    }

    LaunchedEffect(Unit) {
        reload()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "summon_premium_fx")

    val portalRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6800, easing = LinearEasing)
        ),
        label = "portal_rotation"
    )

    val reverseRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(9200, easing = LinearEasing)
        ),
        label = "reverse_rotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.40f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val shineOffset by infiniteTransition.animateFloat(
        initialValue = -380f,
        targetValue = 680f,
        animationSpec = infiniteRepeatable(
            animation = tween(3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "smooth_shine_offset"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.summon_background),
            contentDescription = "Summon Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xF6050009),
                            Color(0xC71B0032),
                            Color(0xF608000F)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .size(360.dp)
                .align(Alignment.TopEnd)
                .offset(x = 128.dp, y = (-130).dp)
                .alpha(0.20f)
                .blur(28.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFFB56CFF),
                            Color(0x664FC3F7),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-120).dp, y = 70.dp)
                .alpha(0.16f)
                .blur(30.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFFFFD66B),
                            Color(0x332B0344),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            SummonHeader(
                diamond = player?.diamond ?: 0,
                message = message,
                onBack = onBack
            )

            Spacer(modifier = Modifier.height(14.dp))

            SummonBannerCard(
                isSummoning = isSummoning,
                portalRotation = portalRotation,
                reverseRotation = reverseRotation,
                pulseScale = pulseScale,
                glowAlpha = glowAlpha,
                shineOffset = shineOffset,
                onSummon1 = {
                    scope.launch {
                        runSingleSummon(
                            loadingText = "Portal Spirit terbuka..."
                        ) {
                            repository.summonCard()
                        }
                    }
                },
                onSummon10 = {
                    if (isSummoning) return@SummonBannerCard

                    scope.launch {
                        isSummoning = true
                        revealCard = false
                        resultCards = emptyList()
                        message = "10x Spirit Portal terbuka..."

                        delay(1300)

                        val result = repository.summonCards(10)
                        message = result.first
                        resultCards = result.second

                        reload()
                        delay(260)

                        revealCard = true
                        isSummoning = false
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            TicketSummonPanel(
                summonTicket = summonTicket,
                epicTicket = epicTicket,
                legendaryTicket = legendaryTicket,
                isSummoning = isSummoning,
                onSummonTicket = { ticketName ->
                    scope.launch {
                        runSingleSummon(
                            loadingText = "Membuka $ticketName..."
                        ) {
                            repository.summonWithTicket(ticketName)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

        }

        AnimatedVisibility(
            visible = resultCards.isNotEmpty() && revealCard,
            enter = fadeIn(animationSpec = tween(360, easing = FastOutSlowInEasing)) +
                    scaleIn(
                        initialScale = 0.72f,
                        animationSpec = tween(620, easing = EaseOutBack)
                    ) +
                    slideInVertically(
                        initialOffsetY = { it / 5 },
                        animationSpec = tween(620, easing = EaseOutBack)
                    ),
            exit = fadeOut(animationSpec = tween(240, easing = FastOutSlowInEasing)) +
                    scaleOut(
                        targetScale = 0.88f,
                        animationSpec = tween(240, easing = FastOutSlowInEasing)
                    ) +
                    slideOutVertically(
                        targetOffsetY = { it / 8 },
                        animationSpec = tween(240, easing = FastOutSlowInEasing)
                    )
        ) {
            SummonRewardPopup(
                cards = resultCards,
                onClose = {
                    resultCards = emptyList()
                    revealCard = false
                }
            )
        }
    }
}

@Composable
fun SummonRewardPopup(
    cards: List<CardEntity>,
    onClose: () -> Unit
) {
    val bestCard = cards.maxByOrNull { summonRarityPower(it.rarity) } ?: return
    val bestColor = summonRarityColor(bestCard.rarity)

    var startFx by remember(cards) { mutableStateOf(false) }
    var showHeader by remember(cards) { mutableStateOf(false) }
    var showCard by remember(cards) { mutableStateOf(false) }
    var showButton by remember(cards) { mutableStateOf(false) }

    LaunchedEffect(cards) {
        startFx = false
        showHeader = false
        showCard = false
        showButton = false
        delay(40)
        startFx = true
        delay(130)
        showHeader = true
        delay(180)
        showCard = true
        delay(260)
        showButton = true
    }

    val overlayAlpha by animateFloatAsState(
        targetValue = if (startFx) 0.78f else 0f,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "summon_popup_overlay_alpha"
    )

    val popupScale by animateFloatAsState(
        targetValue = if (startFx) 1f else 0.78f,
        animationSpec = tween(620, easing = EaseOutBack),
        label = "summon_popup_scale"
    )

    val popupAlpha by animateFloatAsState(
        targetValue = if (startFx) 1f else 0f,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "summon_popup_alpha"
    )

    val popupOffset by animateFloatAsState(
        targetValue = if (startFx) 0f else 44f,
        animationSpec = tween(620, easing = EaseOutBack),
        label = "summon_popup_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = overlayAlpha))
            .padding(horizontal = 14.dp, vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 720.dp)
                .graphicsLayer {
                    scaleX = popupScale
                    scaleY = popupScale
                    alpha = popupAlpha
                    translationY = popupOffset
                }
                .shadow(
                    elevation = 36.dp,
                    shape = RoundedCornerShape(34.dp),
                    ambientColor = bestColor,
                    spotColor = bestColor
                ),
            shape = RoundedCornerShape(34.dp),
            border = BorderStroke(
                1.dp,
                Brush.horizontalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.30f),
                        bestColor.copy(alpha = 0.95f),
                        Color(0xFFFFD66B).copy(alpha = 0.72f),
                        Color.White.copy(alpha = 0.14f)
                    )
                )
            ),
            colors = CardDefaults.cardColors(containerColor = Color(0xF80A0010))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                bestColor.copy(alpha = 0.22f),
                                Color(0xFF170024),
                                Color(0xFF050008)
                            )
                        )
                    )
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = showHeader,
                    enter = fadeIn(tween(320, easing = FastOutSlowInEasing)) +
                            scaleIn(initialScale = 0.86f, animationSpec = tween(420, easing = EaseOutBack)),
                    exit = fadeOut(tween(160))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (cards.size == 1) "SUMMON RESULT" else "10x SUMMON RESULT",
                            color = Color.White,
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = if (cards.size == 1) "Kartu berhasil didapatkan" else "${cards.size} kartu berhasil didapatkan",
                            color = Color(0xFFFFD66B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                AnimatedVisibility(
                    visible = showCard,
                    enter = fadeIn(tween(420, easing = FastOutSlowInEasing)) +
                            scaleIn(initialScale = 0.64f, animationSpec = tween(720, easing = EaseOutBack)) +
                            slideInVertically(initialOffsetY = { it / 6 }, animationSpec = tween(720, easing = EaseOutBack)),
                    exit = fadeOut(tween(180)) + scaleOut(targetScale = 0.90f, animationSpec = tween(180))
                ) {
                    if (cards.size == 1) {
                        SummonPopupCollectionCard(
                            card = cards.first(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(500.dp)
                        )
                    } else {
                        SummonPopupCollectionGrid(cards = cards)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                AnimatedVisibility(
                    visible = showButton,
                    enter = fadeIn(tween(280, easing = FastOutSlowInEasing)) +
                            slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(360, easing = EaseOutBack)),
                    exit = fadeOut(tween(140))
                ) {
                    Button(
                        onClick = onClose,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD66B),
                            contentColor = Color(0xFF170024)
                        )
                    ) {
                        Text(
                            text = "OK",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummonPopupCollectionGrid(cards: List<CardEntity>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(462.dp)
    ) {
        items(cards, key = { "${it.id}_${it.name}_${it.rarity}" }) { card ->
            SummonPopupCollectionCard(
                card = card,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(224.dp),
                compact = true
            )
        }
    }
}

@Composable
fun SummonPopupCollectionCard(
    card: CardEntity,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val rarityColor = summonRarityColor(card.rarity)
    val imageRes = safeSummonImage(card)

    val infinite = rememberInfiniteTransition(label = "summon_popup_card_${card.id}")
    val glow by infinite.animateFloat(
        initialValue = 0.62f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "summon_popup_glow"
    )

    var cardReady by remember(card.id, card.name) { mutableStateOf(false) }

    LaunchedEffect(card.id, card.name) {
        cardReady = false
        delay(if (compact) 80 else 120)
        cardReady = true
    }

    val cardScale by animateFloatAsState(
        targetValue = if (cardReady) 1f else 0.72f,
        animationSpec = tween(if (compact) 520 else 720, easing = EaseOutBack),
        label = "summon_collection_card_scale"
    )

    val cardAlpha by animateFloatAsState(
        targetValue = if (cardReady) 1f else 0f,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "summon_collection_card_alpha"
    )

    val cardRotation by animateFloatAsState(
        targetValue = if (cardReady) 0f else -8f,
        animationSpec = tween(if (compact) 520 else 720, easing = EaseOutBack),
        label = "summon_collection_card_rotation"
    )

    val imageHeight = if (compact) 126.dp else 320.dp
    val bottomHeight = if (compact) 108.dp else 190.dp

    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
                alpha = cardAlpha
                rotationZ = cardRotation
                cameraDistance = 18f * density
            }
            .shadow(
                elevation = if (compact) 16.dp else 30.dp,
                shape = RoundedCornerShape(if (compact) 24.dp else 30.dp),
                ambientColor = rarityColor.copy(alpha = glow),
                spotColor = rarityColor.copy(alpha = glow)
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        rarityColor.copy(alpha = 0.95f),
                        Color.White.copy(alpha = 0.20f),
                        rarityColor.copy(alpha = 0.62f)
                    )
                ),
                shape = RoundedCornerShape(if (compact) 24.dp else 30.dp)
            ),
        shape = RoundedCornerShape(if (compact) 24.dp else 30.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF07000D))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            rarityColor.copy(alpha = 0.55f),
                            Color(0xFF220035),
                            Color(0xFF060009)
                        )
                    )
                )
                .padding(if (compact) 5.dp else 7.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(if (compact) 19.dp else 24.dp))
                    .background(Color(0xFF08000F))
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = card.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(imageHeight)
                        .align(Alignment.TopCenter),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(imageHeight)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color(0x2212001F),
                                    Color(0xDD08000F)
                                )
                            )
                        )
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(if (compact) 7.dp else 12.dp),
                    color = rarityColor.copy(alpha = 0.96f),
                    shape = RoundedCornerShape(50.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                ) {
                    Text(
                        text = card.rarity.uppercase(),
                        color = Color.White,
                        fontSize = if (compact) 8.sp else 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(
                            horizontal = if (compact) 8.dp else 12.dp,
                            vertical = if (compact) 3.dp else 5.dp
                        ),
                        maxLines = 1
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(if (compact) 7.dp else 12.dp)
                        .size(if (compact) 34.dp else 48.dp)
                        .shadow(12.dp, CircleShape, ambientColor = rarityColor, spotColor = rarityColor)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.86f),
                                    rarityColor,
                                    Color(0xFF13001F)
                                )
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.42f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = card.mana.toString(),
                        color = Color.White,
                        fontSize = if (compact) 12.sp else 17.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(bottomHeight)
                        .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xF51B0030),
                                    Color(0xFF07000D)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            Color.White.copy(alpha = 0.10f),
                            RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
                        )
                        .padding(if (compact) 8.dp else 13.dp)
                ) {
                    Text(
                        text = card.name,
                        color = Color.White,
                        fontSize = if (compact) 13.sp else 22.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = card.title.ifBlank { card.skillName.ifBlank { "Spirit Card" } },
                        color = rarityColor,
                        fontSize = if (compact) 9.sp else 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(if (compact) 4.dp else 7.dp))

                    Row {
                        repeat(card.star.coerceIn(1, 10)) {
                            Text("★", color = Color(0xFFFFD700), fontSize = if (compact) 9.sp else 13.sp)
                        }
                    }

                    Text(
                        text = "${card.element} • ${card.role}",
                        color = Color(0xFFEBD8FF),
                        fontSize = if (compact) 9.sp else 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    if (compact) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            SummonStatBadge("ATK", card.attack, Color(0xFFFF6B6B), Modifier.weight(1f), compact = true)
                            SummonStatBadge("HP", card.hp, Color(0xFF6CFF9B), Modifier.weight(1f), compact = true)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            SummonStatBadge("ATK", card.attack, Color(0xFFFF6B6B), Modifier.weight(1f))
                            SummonStatBadge("DEF", card.defense, Color(0xFF82D8FF), Modifier.weight(1f))
                            SummonStatBadge("HP", card.hp, Color(0xFF6CFF9B), Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun SummonHeader(
    diamond: Int,
    message: String,
    onBack: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 26.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = Color(0xFFB56CFF),
                spotColor = Color(0xFFFFD66B)
            ),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(
                    Color.White.copy(alpha = 0.22f),
                    Color(0xFFFFD66B).copy(alpha = 0.72f),
                    Color(0xFFB56CFF).copy(alpha = 0.42f),
                    Color.White.copy(alpha = 0.10f)
                )
            )
        ),
        colors = CardDefaults.cardColors(containerColor = Color(0xE014001F))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF0B0012),
                            Color(0xFF2A073F),
                            Color(0xFF43106F),
                            Color(0xFF13001E)
                        )
                    )
                )
                .padding(13.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 20.dp, y = (-42).dp)
                    .alpha(0.22f)
                    .blur(18.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFFFD66B), Color.Transparent)
                        ),
                        CircleShape
                    )
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.height(40.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0x99FFD66B)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.18f),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp)
                ) {
                    Text("Back", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mystic Summon",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Diamond $diamond",
                            color = Color(0xFFFFD66B),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = "• Spirit Portal",
                            color = Color(0xFFEBD9FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    if (message.isNotBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = message,
                            color = Color(0xFFEBD9FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummonBannerCard(
    isSummoning: Boolean,
    portalRotation: Float,
    reverseRotation: Float,
    pulseScale: Float,
    glowAlpha: Float,
    shineOffset: Float,
    onSummon1: () -> Unit,
    onSummon10: () -> Unit
) {
    val cost1 = SummonManager.summonCost()
    val cost10 = cost1 * 10

    val summonScale by animateFloatAsState(
        targetValue = if (isSummoning) 1.35f else 1f,
        animationSpec = tween(650, easing = EaseOutBack),
        label = "summon_portal_zoom"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(330.dp)
            .shadow(
                elevation = 30.dp,
                shape = RoundedCornerShape(36.dp),
                ambientColor = Color(0xFFB56CFF),
                spotColor = Color(0xFFFFD66B)
            ),
        shape = RoundedCornerShape(36.dp),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(
                    Color.White.copy(alpha = 0.18f),
                    Color(0xFFB56CFF).copy(alpha = 0.72f),
                    Color(0xFFFFD66B).copy(alpha = 0.52f),
                    Color.White.copy(alpha = 0.08f)
                )
            )
        ),
        colors = CardDefaults.cardColors(containerColor = Color(0xE0160021))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF2B0344),
                            Color(0xFF140020),
                            Color(0xFF07000D)
                        )
                    )
                )
        ) {
            Text(
                text = if (isSummoning) "OPENING PORTAL..." else "SPIRIT CARD BANNER",
                color = Color.White,
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 22.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
            )

            Text(
                text = "Legendary 0.6% • Epic 5.1% • Soft Pity 75 • Hard Pity 90",
                color = Color(0xFFFFD66B),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 54.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
            )

            AnimatedPortal(
                rotation1 = portalRotation,
                rotation2 = reverseRotation,
                pulseScale = pulseScale * summonScale,
                glowAlpha = glowAlpha,
                isSummoning = isSummoning,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 24.dp)
            )

            // Shine portal dihapus agar tidak muncul kotak/transparan di area portal.

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 18.dp, vertical = 20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PremiumSummonButton(
                    text = if (isSummoning) "..." else "1x • $cost1",
                    enabled = !isSummoning,
                    modifier = Modifier.weight(1f),
                    onClick = onSummon1
                )

                PremiumSummonButton(
                    text = if (isSummoning) "..." else "10x • $cost10",
                    enabled = !isSummoning,
                    modifier = Modifier.weight(1f),
                    onClick = onSummon10
                )
            }
        }
    }
}

@Composable
fun TicketSummonPanel(
    summonTicket: Int,
    epicTicket: Int,
    legendaryTicket: Int,
    isSummoning: Boolean,
    onSummonTicket: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 18.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color(0xFFFFD66B),
                spotColor = Color(0xFFB56CFF)
            ),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(
                    Color.White.copy(alpha = 0.12f),
                    Color(0xFFFFD66B).copy(alpha = 0.36f),
                    Color(0xFFB56CFF).copy(alpha = 0.28f)
                )
            )
        ),
        colors = CardDefaults.cardColors(containerColor = Color(0xDD12001F))
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF100019),
                            Color(0xFF2B0344),
                            Color(0xFF08000F)
                        )
                    )
                )
                .padding(12.dp)
        ) {
            Text(
                text = "Ticket Summon",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Epic Ticket pasti Epic • Legendary Ticket pasti Legendary",
                color = Color(0xFFFFD66B),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TicketButton(
                    title = "Normal",
                    count = summonTicket,
                    color = Color(0xFFFFD66B),
                    enabled = !isSummoning && summonTicket > 0,
                    modifier = Modifier.weight(1f),
                    onClick = { onSummonTicket("Summon Ticket") }
                )

                TicketButton(
                    title = "Epic",
                    count = epicTicket,
                    color = Color(0xFFB56CFF),
                    enabled = !isSummoning && epicTicket > 0,
                    modifier = Modifier.weight(1f),
                    onClick = { onSummonTicket("Epic Ticket") }
                )

                TicketButton(
                    title = "Legend",
                    count = legendaryTicket,
                    color = Color(0xFFFFD700),
                    enabled = !isSummoning && legendaryTicket > 0,
                    modifier = Modifier.weight(1f),
                    onClick = { onSummonTicket("Legendary Ticket") }
                )
            }
        }
    }
}

@Composable
fun TicketButton(
    title: String,
    count: Int,
    color: Color,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(62.dp)
            .shadow(
                elevation = if (enabled) 12.dp else 2.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = color,
                spotColor = color
            ),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) color else Color(0xFF34243D),
            contentColor = if (enabled) Color(0xFF14001F) else Color.White.copy(alpha = 0.60f),
            disabledContainerColor = Color(0xFF34243D),
            disabledContentColor = Color.White.copy(alpha = 0.60f)
        ),
        contentPadding = PaddingValues(horizontal = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "x$count",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun AnimatedPortal(
    rotation1: Float,
    rotation2: Float,
    pulseScale: Float,
    glowAlpha: Float,
    isSummoning: Boolean,
    modifier: Modifier = Modifier
) {
    val coreFadeTransition = rememberInfiniteTransition(label = "portal_core_fade")

    val coreAlpha by coreFadeTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_alpha"
    )

    val coreScale by coreFadeTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_scale"
    )

    Box(
        modifier = modifier
            .size(230.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.portal_glow),
            contentDescription = null,
            modifier = Modifier
                .size(250.dp)
                .clip(CircleShape)
                .alpha(glowAlpha)
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
        )

        Image(
            painter = painterResource(id = R.drawable.portal_outer),
            contentDescription = null,
            modifier = Modifier
                .size(225.dp)
                .clip(CircleShape)
                .rotate(rotation1)
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
        )

        Image(
            painter = painterResource(id = R.drawable.portal_magic_ring),
            contentDescription = null,
            modifier = Modifier
                .size(195.dp)
                .clip(CircleShape)
                .rotate(rotation2)
                .alpha(0.95f)
        )

        Image(
            painter = painterResource(id = R.drawable.portal_particles),
            contentDescription = null,
            modifier = Modifier
                .size(190.dp)
                .clip(CircleShape)
                .rotate(rotation1 * 1.8f)
                .alpha(if (isSummoning) 1f else 0.82f)
                .graphicsLayer {
                    scaleX = if (isSummoning) pulseScale + 0.12f else pulseScale
                    scaleY = if (isSummoning) pulseScale + 0.12f else pulseScale
                }
        )

        Image(
            painter = painterResource(id = R.drawable.portal_inner),
            contentDescription = null,
            modifier = Modifier
                .size(145.dp)
                .clip(CircleShape)
                .rotate(rotation2 * 1.25f)
                .alpha(0.96f)
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
        )

        Image(
            painter = painterResource(id = R.drawable.portal_core),
            contentDescription = null,
            modifier = Modifier
                .size(if (isSummoning) 92.dp else 72.dp)
                .clip(CircleShape)
                .alpha(coreAlpha)
                .graphicsLayer {
                    scaleX = coreScale
                    scaleY = coreScale
                }
        )

        AnimatedVisibility(
            visible = isSummoning,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(420))
        ) {
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(CircleShape)
                    .alpha(0.18f)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color.White.copy(alpha = 0.72f),
                                Color(0xFF4FC3F7).copy(alpha = 0.36f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
        }
    }
}

@Composable
fun PremiumSummonButton(
    text: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFFD66B),
            contentColor = Color(0xFF18001F),
            disabledContainerColor = Color(0x884B3058),
            disabledContentColor = Color(0xAAFFFFFF)
        )
    ) {
        Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun SummonResultGrid(
    cards: List<CardEntity>,
    reveal: Boolean,
    shineOffset: Float
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(cards) { card ->
            SmallSummonResultCard(card, reveal, shineOffset)
        }
    }
}

@Composable
fun SmallSummonResultCard(
    card: CardEntity,
    reveal: Boolean,
    shineOffset: Float
) {
    val rarityColor = summonRarityColor(card.rarity)
    val safeImageRes = safeSummonImage(card)

    val scale by animateFloatAsState(
        targetValue = if (reveal) 1f else 0.50f,
        animationSpec = tween(540, easing = EaseOutBack),
        label = "small_card_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(222.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationX = 4f
                cameraDistance = 14f * density
            }
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = rarityColor,
                spotColor = rarityColor
            ),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF07000D))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = safeImageRes),
                contentDescription = card.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(124.dp),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0xAA12001F),
                                Color(0xF008000F)
                            )
                        )
                    )
            )

            SmoothShineEffect(
                shineOffset = shineOffset,
                alpha = 0.06f,
                height = 230,
                width = 64,
                modifier = Modifier.align(Alignment.Center)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
            ) {
                Text(
                    text = card.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${card.element} • ${card.role}",
                    color = rarityColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    SummonStatBadge("ATK", card.attack, Color(0xFFFF6B6B))
                    SummonStatBadge("HP", card.hp, Color(0xFF6CFF9B))
                }
            }
        }
    }
}

@Composable
fun SummonResultCard(
    card: CardEntity,
    reveal: Boolean,
    shineOffset: Float
) {
    val rarityColor = summonRarityColor(card.rarity)
    val safeImageRes = safeSummonImage(card)

    val flip by animateFloatAsState(
        targetValue = if (reveal) 0f else 90f,
        animationSpec = tween(720, easing = EaseOutBack),
        label = "flip_card"
    )

    val scale by animateFloatAsState(
        targetValue = if (reveal) 1f else 0.52f,
        animationSpec = tween(620, easing = EaseOutBack),
        label = "scale_card"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(352.dp)
            .graphicsLayer {
                rotationY = flip
                scaleX = scale
                scaleY = scale
                cameraDistance = 16f * density
            }
            .shadow(
                elevation = 30.dp,
                shape = RoundedCornerShape(34.dp),
                ambientColor = rarityColor,
                spotColor = rarityColor
            ),
        shape = RoundedCornerShape(34.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF07000D))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = safeImageRes),
                contentDescription = card.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(194.dp),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0xAA12001F),
                                Color(0xF008000F)
                            )
                        )
                    )
            )

            SmoothShineEffect(
                shineOffset = shineOffset,
                alpha = 0.08f,
                height = 360,
                width = 84,
                modifier = Modifier.align(Alignment.Center)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xF0160021),
                                Color(0xFF08000F)
                            )
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = card.name,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${card.element} • ${card.role}",
                    color = rarityColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = card.skillName,
                    color = Color(0xFFFFD66B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SummonStatBadge("ATK", card.attack, Color(0xFFFF6B6B))
                    SummonStatBadge("DEF", card.defense, Color(0xFF82D8FF))
                    SummonStatBadge("HP", card.hp, Color(0xFF6CFF9B))
                }
            }
        }
    }
}

@Composable
fun SummonStatBadge(
    label: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Surface(
        modifier = modifier.height(if (compact) 22.dp else 28.dp),
        color = color.copy(alpha = 0.16f),
        shape = RoundedCornerShape(50.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.34f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (compact) 3.dp else 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$label $value",
                color = color,
                fontSize = if (compact) 7.sp else 10.sp,
                lineHeight = if (compact) 7.sp else 10.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Clip
            )
        }
    }
}

fun summonRarityColor(rarity: String): Color {
    return when (rarity) {
        "Legendary" -> Color(0xFFFFD700)
        "Epic" -> Color(0xFFB56CFF)
        "Rare" -> Color(0xFF4FC3F7)
        else -> Color(0xFFC9C9C9)
    }
}

fun summonRarityPower(rarity: String): Int {
    return when (rarity) {
        "Legendary" -> 3
        "Epic" -> 2
        "Rare" -> 1
        else -> 0
    }
}