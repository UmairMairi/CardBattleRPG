package com.pegasus.cardbattlerpg.ui.hero

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.pegasus.cardbattlerpg.entity.HeroEntity
import com.pegasus.cardbattlerpg.entity.PlayerEntity
import com.pegasus.cardbattlerpg.repository.GameRepository
import kotlinx.coroutines.launch

@Composable
fun heroDrawableIdByName(name: String): Int {
    val context = LocalContext.current
    return remember(name) {
        context.resources.getIdentifier(name, "drawable", context.packageName)
    }
}

@Composable
fun safeHeroImage(hero: HeroEntity): Int {
    val res = heroDrawableIdByName(hero.image)
    return if (res != 0) res else R.drawable.hero_unknown
}

@Composable
fun HeroScreen(
    repository: GameRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var heroes by remember { mutableStateOf<List<HeroEntity>>(emptyList()) }
    var player by remember { mutableStateOf<PlayerEntity?>(null) }
    var selectedHero by remember { mutableStateOf<HeroEntity?>(null) }
    var message by remember { mutableStateOf("") }

    suspend fun reload() {
        heroes = repository.getAllHeroes()
        player = repository.getPlayer()
    }

    LaunchedEffect(Unit) {
        reload()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.hero_background),
            contentDescription = "Hero Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        PremiumHeroBackgroundOverlay()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            HeroHeader(
                gold = player?.gold ?: 0,
                totalHero = heroes.size,
                activeHero = heroes.firstOrNull { it.selected }?.name ?: "-",
                message = message,
                onBack = onBack
            )

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 18.dp)
            ) {
                items(heroes) { hero ->
                    HeroCard(
                        hero = hero,
                        buyPrice = repository.heroGoldPrice(hero),
                        onSelect = {
                            scope.launch {
                                repository.selectHero(hero.id)
                                message = if (hero.unlocked) "${hero.name} dipilih sebagai hero aktif" else "${hero.name} masih terkunci"
                                reload()
                            }
                        },
                        onLevelUp = {
                            scope.launch {
                                message = repository.levelUpHero(hero)
                                reload()
                            }
                        },
                        onBuy = {
                            scope.launch {
                                message = repository.buyHeroWithGold(hero)
                                reload()
                            }
                        },
                        onDetail = {
                            selectedHero = hero
                        }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = selectedHero != null,
            enter = scaleIn(
                initialScale = 0.88f,
                animationSpec = tween(380, easing = EaseOutBack)
            ) + fadeIn(tween(260))
        ) {
            selectedHero?.let { hero ->
                HeroDetailDialog(
                    hero = hero,
                    onDismiss = { selectedHero = null }
                )
            }
        }
    }
}

@Composable
fun BoxScope.PremiumHeroBackgroundOverlay() {
    val infinite = rememberInfiniteTransition(label = "hero_bg_fx")
    val glowAlpha by infinite.animateFloat(
        initialValue = 0.24f,
        targetValue = 0.56f,
        animationSpec = infiniteRepeatable(
            animation = tween(1900, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_glow_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xF7050009),
                        Color(0xB8240043),
                        Color(0xF008000F)
                    )
                )
            )
    )

    Box(
        modifier = Modifier
            .size(340.dp)
            .offset(x = 150.dp, y = (-120).dp)
            .alpha(glowAlpha)
            .blur(30.dp)
            .background(
                Brush.radialGradient(
                    listOf(
                        Color(0xFFFFD66B),
                        Color(0x554B1478),
                        Color.Transparent
                    )
                ),
                CircleShape
            )
    )

    Box(
        modifier = Modifier
            .size(300.dp)
            .offset(x = (-140).dp, y = 260.dp)
            .alpha(glowAlpha * 0.72f)
            .blur(34.dp)
            .background(
                Brush.radialGradient(
                    listOf(
                        Color(0xFFB56CFF),
                        Color.Transparent
                    )
                ),
                CircleShape
            )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .align(Alignment.BottomCenter)
            .alpha(0.10f)
            .blur(26.dp)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        Color(0x664B1478),
                        Color.Transparent
                    )
                )
            )
    )
}

@Composable
fun HeroHeader(
    gold: Int,
    totalHero: Int,
    activeHero: String,
    message: String,
    onBack: () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "hero_header_fx")
    val glow by infinite.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "header_glow"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = Color(0xFFFFD66B),
                spotColor = Color(0xFFB56CFF)
            ),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(
                    Color.White.copy(alpha = 0.20f),
                    Color(0xFFFFD66B).copy(alpha = glow),
                    Color.White.copy(alpha = 0.10f)
                )
            )
        ),
        colors = CardDefaults.cardColors(containerColor = Color(0xE812001F))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF14001E),
                            Color(0xFF4B1478),
                            Color(0xFF100019)
                        )
                    )
                )
                .padding(13.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.height(40.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFD66B).copy(alpha = 0.62f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White,
                        containerColor = Color.Black.copy(alpha = 0.16f)
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp)
                ) {
                    Text("Back", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hero Sanctuary",
                        color = Color.White,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1
                    )

                    Text(
                        text = "Gold $gold • Hero $totalHero • Active $activeHero",
                        color = Color(0xFFFFD66B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )

                    if (message.isNotBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = message,
                            color = Color(0xFFE9D9FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeroCard(
    hero: HeroEntity,
    buyPrice: Int,
    onSelect: () -> Unit,
    onLevelUp: () -> Unit,
    onBuy: () -> Unit,
    onDetail: () -> Unit
) {
    val elementColor = heroElementColor(hero.element)
    val rarityColor = heroRarityColor(hero.rarity)
    val imageRes = safeHeroImage(hero)

    val infinite = rememberInfiniteTransition(label = "hero_card_${hero.id}")
    val breathY by infinite.animateFloat(
        initialValue = 3f,
        targetValue = -4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_breath_y"
    )
    val auraScale by infinite.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_aura_scale"
    )
    val selectedScale by animateFloatAsState(
        targetValue = if (hero.selected) 1.01f else 1f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "selected_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .graphicsLayer {
                scaleX = selectedScale
                scaleY = selectedScale
            }
            .shadow(
                elevation = if (hero.selected) 30.dp else 14.dp,
                shape = RoundedCornerShape(34.dp),
                ambientColor = if (hero.selected) Color(0xFFFFD66B) else rarityColor,
                spotColor = if (hero.selected) Color(0xFFB56CFF) else rarityColor
            )
            .clickable { onDetail() },
        shape = RoundedCornerShape(34.dp),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(
                    Color.White.copy(alpha = 0.17f),
                    rarityColor.copy(alpha = if (hero.selected) 0.95f else 0.55f),
                    Color.White.copy(alpha = 0.08f)
                )
            )
        ),
        colors = CardDefaults.cardColors(containerColor = Color(0xE812001F))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF08000E),
                                elementColor.copy(alpha = 0.44f),
                                Color(0xFF170024)
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.035f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.38f)
                            )
                        )
                    )
            )

            if (hero.selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = (-34).dp)
                        .size(196.dp)
                        .graphicsLayer {
                            scaleX = auraScale
                            scaleY = auraScale
                        }
                        .alpha(0.26f)
                        .blur(20.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFFFFD66B),
                                    elementColor,
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(72.dp)
                    .alpha(if (hero.selected) 0.13f else 0.07f)
                    .blur(18.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.34f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Image(
                painter = painterResource(id = imageRes),
                contentDescription = hero.name,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .height(228.dp)
                    .width(132.dp)
                    .padding(start = 3.dp)
                    .alpha(if (hero.unlocked) 1f else 0.42f)
                    .graphicsLayer {
                        translationY = breathY
                    },
                contentScale = ContentScale.Fit
            )

            if (!hero.unlocked) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 18.dp)
                        .background(Color.Black.copy(alpha = 0.72f), RoundedCornerShape(50.dp))
                        .border(1.dp, Color(0xFFFFD66B), RoundedCornerShape(50.dp))
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text("LOCKED", color = Color(0xFFFFD66B), fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
            }

            if (hero.selected) {
                Text(
                    text = "AKTIF",
                    color = Color(0xFF160021),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp, top = 14.dp)
                        .background(Color(0xFFFFD66B), RoundedCornerShape(50.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }

            Text(
                text = hero.rarity.uppercase(),
                color = rarityColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
                    .background(Color.Black.copy(alpha = 0.38f), RoundedCornerShape(50.dp))
                    .border(1.dp, rarityColor.copy(alpha = 0.56f), RoundedCornerShape(50.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .fillMaxWidth(0.60f)
                    .padding(start = 8.dp, top = 48.dp, end = 12.dp, bottom = 58.dp)
            ) {
                Text(
                    text = hero.name,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1
                )

                Text(
                    text = "${hero.element} • ${hero.role}",
                    color = rarityColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(5.dp))

                HeroLevelProgress(
                    level = hero.level,
                    exp = hero.exp,
                    maxExp = hero.maxExp,
                    color = rarityColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    HeroStatBadge("HP", hero.hp, Color(0xFF6CFF9B))
                    HeroStatBadge("ATK", hero.attack, Color(0xFFFF6B6B))
                    HeroStatBadge("DEF", hero.defense, Color(0xFF82D8FF))
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    HeroStatBadge("SPD", hero.speed, Color(0xFF26C6DA))
                    HeroStatBadge("CRIT", hero.criticalRate, Color(0xFFFFA726))
                }

                Spacer(modifier = Modifier.height(7.dp))

                Text(
                    text = if (hero.skillName.isNotBlank()) hero.skillName else "Basic Strike",
                    color = Color(0xFFFFD66B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .fillMaxWidth(0.60f)
                    .padding(start = 8.dp, end = 12.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!hero.unlocked) {
                    PremiumHeroButton(
                        text = "BUY $buyPrice",
                        enabled = true,
                        onClick = onBuy,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    PremiumHeroButton(
                        text = if (hero.selected) "AKTIF" else "PILIH",
                        enabled = !hero.selected,
                        onClick = onSelect,
                        modifier = Modifier.weight(1f)
                    )
                }

                PremiumHeroButton(
                    text = if (hero.unlocked) "LEVEL" else "LOCK",
                    enabled = hero.unlocked,
                    onClick = onLevelUp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun HeroLevelProgress(
    level: Int,
    exp: Int,
    maxExp: Int,
    color: Color
) {
    val safeMaxExp = maxExp.coerceAtLeast(100)
    val safeExp = exp.coerceAtLeast(0)
    val progress = (safeExp.toFloat() / safeMaxExp.toFloat()).coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(480, easing = FastOutSlowInEasing),
        label = "hero_exp_progress"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "LV.$level",
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "EXP $safeExp/$safeMaxExp",
                color = Color(0xFFE9D9FF),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(13.dp)
                .background(Color.Black.copy(alpha = 0.66f), RoundedCornerShape(50.dp))
                .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(50.dp))
                .padding(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                color.copy(alpha = 0.78f),
                                Color.White.copy(alpha = 0.46f),
                                color.copy(alpha = 0.92f)
                            )
                        ),
                        RoundedCornerShape(50.dp)
                    )
            )
        }
    }
}

@Composable
fun PremiumHeroButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(13.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFFD66B),
            contentColor = Color(0xFF160021),
            disabledContainerColor = Color.White.copy(alpha = 0.13f),
            disabledContentColor = Color(0xFFFFD66B)
        ),
        contentPadding = PaddingValues(horizontal = 10.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1
        )
    }
}

@Composable
fun HeroStatBadge(
    label: String,
    value: Int,
    color: Color
) {
    Text(
        text = "$label $value",
        color = color,
        fontSize = 9.sp,
        fontWeight = FontWeight.Black,
        maxLines = 1,
        modifier = Modifier
            .background(
                color.copy(alpha = 0.16f),
                RoundedCornerShape(50.dp)
            )
            .border(1.dp, color.copy(alpha = 0.20f), RoundedCornerShape(50.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    )
}

@Composable
fun HeroDetailDialog(
    hero: HeroEntity,
    onDismiss: () -> Unit
) {
    val imageRes = safeHeroImage(hero)
    val rarityColor = heroRarityColor(hero.rarity)
    val elementColor = heroElementColor(hero.element)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            PremiumHeroButton(
                text = "Tutup",
                enabled = true,
                onClick = onDismiss,
                modifier = Modifier.width(120.dp)
            )
        },
        containerColor = Color(0xF20B0010),
        shape = RoundedCornerShape(32.dp),
        title = {
            Column {
                Text(
                    text = hero.name,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 26.sp
                )
                Text(
                    text = "${hero.rarity} • ${hero.element} • ${hero.role}",
                    color = rarityColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 620.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .shadow(
                            elevation = 24.dp,
                            shape = RoundedCornerShape(30.dp),
                            ambientColor = rarityColor,
                            spotColor = elementColor
                        ),
                    shape = RoundedCornerShape(30.dp),
                    border = BorderStroke(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.18f),
                                rarityColor.copy(alpha = 0.82f),
                                Color.White.copy(alpha = 0.10f)
                            )
                        )
                    ),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF12001F))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            elementColor.copy(alpha = 0.46f),
                                            Color(0xFF170024),
                                            Color(0xFF050009)
                                        )
                                    )
                                )
                        )

                        Image(
                            painter = painterResource(id = imageRes),
                            contentDescription = hero.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            contentScale = ContentScale.Fit
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Transparent,
                                            Color.Transparent,
                                            Color(0xEE08000F)
                                        )
                                    )
                                )
                        )

                        Text(
                            text = if (hero.selected) "ACTIVE HERO" else "RESERVE HERO",
                            color = if (hero.selected) Color(0xFFFFD66B) else Color(0xFFD7C3F2),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(14.dp)
                                .background(Color.Black.copy(alpha = 0.42f), RoundedCornerShape(50.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                HeroDetailSection(
                    title = "Battle Status",
                    color = rarityColor,
                    lines = listOf(
                        "Level: ${hero.level}",
                        "EXP: ${hero.exp}/${hero.maxExp}",
                        "HP: ${hero.hp}",
                        "Attack: ${hero.attack}",
                        "Defense: ${hero.defense}",
                        "Speed: ${hero.speed}",
                        "Critical: ${hero.criticalRate}%",
                        "Critical Damage: ${hero.criticalDamage}%"
                    )
                )

                if (hero.skillName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HeroSkillBox(
                        title = "Skill",
                        skillName = hero.skillName,
                        description = hero.skillDescription,
                        color = rarityColor
                    )
                }

                if (hero.ultimateName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HeroSkillBox(
                        title = "Ultimate",
                        skillName = hero.ultimateName,
                        description = hero.ultimateDescription,
                        color = Color(0xFFFF5252)
                    )
                }
            }
        }
    )
}

@Composable
fun HeroDetailSection(
    title: String,
    color: Color,
    lines: List<String>
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.28f)),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                color = color,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            lines.chunked(2).forEach { rowLines ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowLines.forEach { line ->
                        Text(
                            text = line,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.Black.copy(alpha = 0.24f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 7.dp)
                        )
                    }

                    if (rowLines.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(7.dp))
            }
        }
    }
}

@Composable
fun HeroSkillBox(
    title: String,
    skillName: String,
    description: String,
    color: Color
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.34f)),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                color = color,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = skillName,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                color = Color(0xFFEBD9FF),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 17.sp
            )
        }
    }
}

fun heroElementColor(element: String): Color {
    return when (element) {
        "Fire" -> Color(0xFFFF7043)
        "Water" -> Color(0xFF4FC3F7)
        "Nature" -> Color(0xFF66BB6A)
        "Dark" -> Color(0xFF9C27B0)
        "Light" -> Color(0xFFFFD54F)
        "Thunder" -> Color(0xFFFFD54F)
        else -> Color(0xFFBDBDBD)
    }
}

fun heroRarityColor(rarity: String): Color {
    return when (rarity) {
        "Legendary" -> Color(0xFFFFD700)
        "Epic" -> Color(0xFFB56CFF)
        "Rare" -> Color(0xFF4FC3F7)
        else -> Color(0xFFC9C9C9)
    }
}
