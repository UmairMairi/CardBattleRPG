package com.pegasus.cardbattlerpg.ui.collection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
import com.pegasus.cardbattlerpg.repository.GameRepository

@Composable
fun drawableIdByName(name: String): Int {
    val context = LocalContext.current
    return remember(name) {
        context.resources.getIdentifier(name, "drawable", context.packageName)
    }
}

@Composable
fun safeCardImage(card: CardEntity): Int {
    val imageRes = drawableIdByName(card.image)
    return if (imageRes != 0) imageRes else R.drawable.card_unknown
}

@Composable
fun CollectionScreen(
    repository: GameRepository,
    onBack: () -> Unit
) {
    var cards by remember { mutableStateOf<List<CardEntity>>(emptyList()) }
    var selectedRarity by remember { mutableStateOf("All") }
    var selectedCard by remember { mutableStateOf<CardEntity?>(null) }

    LaunchedEffect(Unit) {
        cards = repository.getAllCards()
    }

    val rarities = listOf("All", "Common", "Rare", "Epic", "Legendary")
    val filteredCards = if (selectedRarity == "All") cards else cards.filter { it.rarity == selectedRarity }
    val ownedCards = cards.count { it.owned }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.collection_background),
            contentDescription = "Collection Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xF7050010),
                            Color(0xD0190036),
                            Color(0xF5000007)
                        )
                    )
                )
        )

        PremiumAuraBlob(
            modifier = Modifier
                .size(390.dp)
                .align(Alignment.TopEnd)
                .offset(x = 155.dp, y = (-140).dp),
            color = Color(0xFFFFD66B),
            alpha = 0.34f
        )

        PremiumAuraBlob(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-130).dp, y = 95.dp),
            color = Color(0xFFB56CFF),
            alpha = 0.25f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            PremiumCollectionHeader(
                totalCards = cards.size,
                ownedCards = ownedCards,
                selectedRarity = selectedRarity,
                onBack = onBack
            )

            Spacer(modifier = Modifier.height(12.dp))

            PremiumRarityFilter(
                rarities = rarities,
                selectedRarity = selectedRarity,
                onSelect = { selectedRarity = it }
            )

            Spacer(modifier = Modifier.height(14.dp))

            AnimatedVisibility(visible = filteredCards.isEmpty()) {
                EmptyCollectionState(selectedRarity = selectedRarity)
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 28.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredCards) { card ->
                    RealCardItem(
                        card = card,
                        onClick = { selectedCard = card }
                    )
                }
            }
        }

        selectedCard?.let { card ->
            PremiumCardDetailDialog(
                card = card,
                onDismiss = { selectedCard = null }
            )
        }
    }
}

@Composable
fun PremiumAuraBlob(
    modifier: Modifier,
    color: Color,
    alpha: Float
) {
    Box(
        modifier = modifier
            .alpha(alpha)
            .background(
                Brush.radialGradient(
                    listOf(color, color.copy(alpha = 0.12f), Color.Transparent)
                ),
                CircleShape
            )
    )
}

@Composable
fun PremiumCollectionHeader(
    totalCards: Int,
    ownedCards: Int,
    selectedRarity: String,
    onBack: () -> Unit
) {
    val progress = if (totalCards == 0) 0f else ownedCards.toFloat() / totalCards.toFloat()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 22.dp,
                shape = RoundedCornerShape(30.dp),
                ambientColor = Color(0x80FFD66B),
                spotColor = Color(0x80B56CFF)
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFFFFE7A3), Color(0xFFB56CFF), Color(0x55FFFFFF))
                ),
                shape = RoundedCornerShape(30.dp)
            ),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xE812001F))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF14001F), Color(0xFF50148A), Color(0xFF100016))
                    )
                )
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(80.dp)
                    .align(Alignment.TopEnd)
                    .alpha(0.35f)
                    .background(
                        Brush.radialGradient(listOf(Color.White, Color.Transparent)),
                        CircleShape
                    )
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(listOf(Color(0xFFFFE7A3), Color(0xFFB8751E)))
                        )
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("‹", color = Color(0xFF1A061F), fontSize = 30.sp, fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.width(13.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Card Collection",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$selectedRarity Gallery • Owned $ownedCards / $totalCards",
                        color = Color(0xFFFFD66B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress.coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFFFD66B), Color(0xFFFF8A00), Color(0xFFB56CFF))
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumRarityFilter(
    rarities: List<String>,
    selectedRarity: String,
    onSelect: (String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        rarities.forEach { rarity ->
            val selected = selectedRarity == rarity
            val color = rarityColor(rarity)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(
                        if (selected) Brush.horizontalGradient(listOf(color, Color(0xFF6A1BA6)))
                        else Brush.horizontalGradient(listOf(Color(0x77100018), Color(0x5533004F)))
                    )
                    .border(
                        width = 1.dp,
                        color = if (selected) Color.White.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(50.dp)
                    )
                    .clickable { onSelect(rarity) }
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            ) {
                Text(
                    text = rarity,
                    color = if (selected) Color.White else Color(0xFFEAD7FF),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun EmptyCollectionState(selectedRarity: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xCC100018))
    ) {
        Text(
            text = "Belum ada kartu untuk kategori $selectedRarity",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        )
    }
}

@Composable
fun RealCardItem(
    card: CardEntity,
    onClick: () -> Unit
) {
    val rarityColor = rarityColor(card.rarity)
    val safeImageRes = safeCardImage(card)
    val alphaValue = if (card.owned) 1f else 0.42f
    val elevationValue = if (card.owned) 26.dp else 8.dp

    val transition = rememberInfiniteTransition(label = "card_soft_glow")
    val softGlow by transition.animateFloat(
        initialValue = 0.06f,
        targetValue = 0.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "soft_glow"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(366.dp)
            .graphicsLayer {
                rotationX = if (card.owned) 2.4f else 0f
                rotationY = if (card.owned) -1.8f else 0f
                cameraDistance = 20f * density
            }
            .shadow(
                elevation = elevationValue,
                shape = RoundedCornerShape(28.dp),
                ambientColor = if (card.owned) rarityColor else Color.Black,
                spotColor = if (card.owned) rarityColor else Color.Black
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF07000D))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            rarityColor.copy(alpha = if (card.owned) 0.98f else 0.38f),
                            Color(0xFF2B0046),
                            Color(0xFF07000D)
                        )
                    )
                )
                .padding(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(23.dp))
                    .background(Color(0xFF08000F))
            ) {
                Image(
                    painter = painterResource(id = safeImageRes),
                    contentDescription = card.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(168.dp)
                        .alpha(alphaValue),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(168.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0x3312001F), Color(0xE008000F))
                            )
                        )
                )

                // Shining kasar yang bergerak sudah dihapus total.
                // Diganti dengan glow lembut diam agar tidak muncul garis/kotak kasar.
                if (card.owned) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .height(96.dp)
                            .alpha(softGlow)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.18f),
                                        rarityColor.copy(alpha = 0.10f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                PremiumRarityPill(
                    rarity = card.rarity,
                    color = rarityColor,
                    owned = card.owned,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                )

                ManaOrb(
                    mana = card.mana,
                    color = rarityColor,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                )

                if (!card.owned) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color.Black.copy(alpha = 0.72f))
                            .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(50.dp))
                            .padding(horizontal = 18.dp, vertical = 9.dp)
                    ) {
                        Text("LOCKED", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(206.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(
                            Brush.verticalGradient(listOf(Color(0xF51B0030), Color(0xFF07000D)))
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.10f),
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        )
                        .padding(11.dp)
                ) {
                    Text(
                        text = card.name,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = if (card.title.isNotBlank()) card.title else "${card.element} ${card.role}",
                        color = rarityColor,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(modifier = Modifier.padding(top = 3.dp)) {
                        repeat(card.star.coerceIn(1, 10)) {
                            Text("★", color = Color(0xFFFFD700), fontSize = 10.sp)
                        }
                    }

                    Text(
                        text = "${card.element} • ${card.role}",
                        color = Color(0xFFEBD8FF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    StatGridNoCut(
                        atk = card.attack,
                        defense = card.defense,
                        hp = card.hp,
                        speed = card.speed
                    )
                }
            }
        }
    }
}

@Composable
fun PremiumRarityPill(
    rarity: String,
    color: Color,
    owned: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(color.copy(alpha = if (owned) 0.94f else 0.45f))
            .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(50.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = rarity.uppercase(),
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1
        )
    }
}

@Composable
fun ManaOrb(
    mana: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(42.dp)
            .shadow(12.dp, CircleShape, ambientColor = color, spotColor = color)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.8f), color, Color(0xFF190026))))
            .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = mana.toString(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
    }
}

@Composable
fun StatGridNoCut(
    atk: Int,
    defense: Int,
    hp: Int,
    speed: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            StatBadgeNoCut("ATK", atk, Color(0xFFFF6B6B), Modifier.weight(1f))
            StatBadgeNoCut("DEF", defense, Color(0xFF82D8FF), Modifier.weight(1f))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            StatBadgeNoCut("HP", hp, Color(0xFF6CFF9B), Modifier.weight(1f))
            StatBadgeNoCut("SPD", speed, Color(0xFF26C6DA), Modifier.weight(1f))
        }
    }
}

@Composable
fun StatBadgeNoCut(
    label: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(28.dp)
            .fillMaxWidth(),
        color = color.copy(alpha = 0.16f),
        shape = RoundedCornerShape(50.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.36f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 1.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$label $value",
                color = color,
                fontSize = 6.sp,
                lineHeight = 7.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Clip
            )
        }
    }
}


@Composable
fun PremiumCardDetailDialog(
    card: CardEntity,
    onDismiss: () -> Unit
) {
    val rarityColor = rarityColor(card.rarity)
    val safeImageRes = safeCardImage(card)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = rarityColor)
            ) {
                Text("Tutup", color = Color(0xFF14001F), fontWeight = FontWeight.Black)
            }
        },
        containerColor = Color(0xFF11001B),
        shape = RoundedCornerShape(30.dp),
        title = {
            Column {
                Text(
                    text = card.name,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${card.rarity} • ${card.element} • ${card.role}",
                    color = rarityColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
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
                        .height(330.dp)
                        .shadow(24.dp, RoundedCornerShape(30.dp), ambientColor = rarityColor, spotColor = rarityColor)
                        .border(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.45f), rarityColor)),
                            shape = RoundedCornerShape(30.dp)
                        ),
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF140020))
                ) {
                    Box {
                        Image(
                            painter = painterResource(id = safeImageRes),
                            contentDescription = card.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(if (card.owned) 1f else 0.42f),
                            contentScale = ContentScale.Crop
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color(0xAA12001F), Color(0xF008000F))
                                    )
                                )
                        )

                        PremiumRarityPill(
                            rarity = card.rarity,
                            color = rarityColor,
                            owned = card.owned,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp)
                        )

                        ManaOrb(
                            mana = card.mana,
                            color = rarityColor,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                        )

                        if (!card.owned) {
                            Text(
                                text = "LOCKED CARD",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(50.dp))
                                    .padding(horizontal = 18.dp, vertical = 9.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (card.title.isNotBlank()) {
                    Text(card.title, color = rarityColor, fontWeight = FontWeight.Black, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Row {
                    repeat(card.star.coerceIn(1, 10)) {
                        Text("★", color = Color(0xFFFFD700), fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                DetailSectionTitle("Identity", rarityColor)
                DetailInfoGrid(
                    listOf(
                        "Element" to card.element,
                        "Role" to card.role,
                        "Race" to card.race,
                        "Level" to "${card.level}/${card.maxLevel}",
                        "Awaken" to card.awakenLevel.toString()
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                DetailSectionTitle("Battle Stats", rarityColor)
                DetailInfoGrid(
                    listOf(
                        "Attack" to card.attack.toString(),
                        "Defense" to card.defense.toString(),
                        "HP" to card.hp.toString(),
                        "Mana" to card.mana.toString(),
                        "Speed" to card.speed.toString(),
                        "Critical" to "${card.criticalRate}%",
                        "Crit Dmg" to "${card.criticalDamage}%",
                        "Dodge" to "${card.dodge}%",
                        "Lifesteal" to "${card.lifesteal}%",
                        "Block" to "${card.blockRate}%"
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                SkillBlock("Active Skill", card.skillName, card.skillDescription, rarityColor)

                if (card.passiveSkillName.isNotBlank()) {
                    SkillBlock("Passive Skill", card.passiveSkillName, card.passiveSkillDescription, Color(0xFF82D8FF))
                }

                if (card.ultimateSkillName.isNotBlank()) {
                    SkillBlock("Ultimate Skill", card.ultimateSkillName, card.ultimateSkillDescription, Color(0xFFFF5252))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    RatingBadge("PvE", card.pveRating, Color(0xFF6CFF9B), Modifier.weight(1f))
                    RatingBadge("PvP", card.pvpRating, Color(0xFFFFD66B), Modifier.weight(1f))
                }

                if (card.lore.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailSectionTitle("Lore", rarityColor)
                    Text(card.lore, color = Color(0xFFEBD8FF), fontSize = 13.sp, lineHeight = 18.sp)
                }
            }
        }
    )
}

@Composable
fun DetailSectionTitle(title: String, color: Color) {
    Text(title, color = color, fontWeight = FontWeight.Black, fontSize = 14.sp)
}

@Composable
fun DetailInfoGrid(items: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        items.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                rowItems.forEach { item ->
                    DetailInfoPill(item.first, item.second, Modifier.weight(1f))
                }
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun DetailInfoPill(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(label, color = Color(0xFFBFADE0), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun SkillBlock(title: String, name: String, desc: String, color: Color) {
    Spacer(modifier = Modifier.height(12.dp))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.10f))
            .border(1.dp, color.copy(alpha = 0.24f), RoundedCornerShape(20.dp))
            .padding(12.dp)
    ) {
        Text(title, color = color, fontWeight = FontWeight.Black, fontSize = 13.sp)
        Text(name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
        Text(desc, color = Color(0xFFEBD8FF), fontSize = 12.sp, lineHeight = 17.sp)
    }
}

@Composable
fun RatingBadge(label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(color.copy(alpha = 0.13f))
            .border(1.dp, color.copy(alpha = 0.30f), RoundedCornerShape(18.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("$label $value", color = color, fontWeight = FontWeight.Black)
    }
}

fun rarityColor(rarity: String): Color {
    return when (rarity) {
        "Legendary" -> Color(0xFFFFD700)
        "Epic" -> Color(0xFFB56CFF)
        "Rare" -> Color(0xFF4FC3F7)
        "Common" -> Color(0xFFC9C9C9)
        else -> Color(0xFFFFD66B)
    }
}
