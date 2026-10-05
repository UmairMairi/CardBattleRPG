package com.pegasus.cardbattlerpg.ui.deck

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import com.pegasus.cardbattlerpg.entity.DeckCardEntity
import com.pegasus.cardbattlerpg.repository.GameRepository
import kotlinx.coroutines.launch

private const val MAX_DECK_CARD = 30

@Composable
fun deckDrawableIdByName(name: String): Int {
    val context = LocalContext.current
    return remember(name) {
        context.resources.getIdentifier(name, "drawable", context.packageName)
    }
}

@Composable
fun safeDeckCardImage(card: CardEntity): Int {
    val imageRes = deckDrawableIdByName(card.image)
    return if (imageRes != 0) imageRes else R.drawable.card_unknown
}

@Composable
fun DeckBuilderScreen(
    repository: GameRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var ownedCards by remember { mutableStateOf<List<CardEntity>>(emptyList()) }
    var deckCards by remember { mutableStateOf<List<DeckCardEntity>>(emptyList()) }
    var selectedDeckId by remember { mutableStateOf<Int?>(null) }
    var message by remember { mutableStateOf("") }

    suspend fun reload() {
        val deck = repository.getSelectedDeck()
        selectedDeckId = deck?.id
        ownedCards = repository.getOwnedCards()
        deckCards = if (deck != null) repository.getDeckCards(deck.id) else emptyList()
    }

    LaunchedEffect(Unit) {
        reload()
    }

    val deckCardIds = deckCards.map { it.cardId }
    val cardsInDeck = ownedCards.filter { deckCardIds.contains(it.id) }
    val availableCards = ownedCards.sortedWith(
        compareByDescending<CardEntity> { !deckCardIds.contains(it.id) }
            .thenByDescending { rarityPower(it.rarity) }
            .thenByDescending { it.star }
            .thenBy { it.name }
    )

    val infiniteTransition = rememberInfiniteTransition(label = "deck_bg")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.deck_background),
            contentDescription = "Deck Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xF7050009),
                            Color(0xC51A0033),
                            Color(0xE80B0012),
                            Color(0xFA030006)
                        )
                    )
                )
        )

        // Semua blob/glow besar di background dihapus total agar tidak ada bentuk bulat/panjang yang mengganggu.

        // Soft ambient highlight only. Hard moving shine removed to avoid rough rectangular artifacts.

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 18.dp, bottom = 28.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item(span = { GridItemSpan(2) }) {
                DeckHeaderCard(
                    totalDeck = deckCards.size,
                    totalOwned = ownedCards.size,
                    message = message,
                    onBack = onBack
                )
            }

            item(span = { GridItemSpan(2) }) {
                SectionTitleRow(
                    title = "Active Deck",
                    subtitle = "${deckCards.size}/$MAX_DECK_CARD battle cards selected",
                    actionText = "Clear",
                    actionEnabled = deckCards.isNotEmpty(),
                    onAction = {
                        scope.launch {
                            val deckId = selectedDeckId ?: return@launch
                            repository.clearDeck(deckId)
                            message = "Deck berhasil dikosongkan"
                            reload()
                        }
                    }
                )
            }

            item(span = { GridItemSpan(2) }) {
                ActiveDeckPanel(
                    cards = cardsInDeck,
                    deckSize = deckCards.size,
                    onRemove = { card ->
                        scope.launch {
                            val deckId = selectedDeckId ?: return@launch
                            repository.removeCardFromDeck(deckId, card.id)
                            message = "${card.name} dihapus dari deck"
                            reload()
                        }
                    }
                )
            }

            item(span = { GridItemSpan(2) }) {
                SectionTitleRow(
                    title = "Owned Cards",
                    subtitle = "Tap card untuk memasukkan ke deck",
                    actionText = "",
                    actionEnabled = false,
                    onAction = {}
                )
            }

            items(
                items = availableCards,
                key = { it.id }
            ) { card ->
                OwnedCardItem(
                    card = card,
                    isInDeck = deckCardIds.contains(card.id),
                    onClick = {
                        scope.launch {
                            val deckId = selectedDeckId ?: return@launch
                            message = repository.addCardToDeck(deckId, card.id)
                            reload()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DeckHeaderCard(
    totalDeck: Int,
    totalOwned: Int,
    message: String,
    onBack: () -> Unit
) {
    val progress = (totalDeck.toFloat() / MAX_DECK_CARD.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 28.dp,
                shape = RoundedCornerShape(34.dp),
                ambientColor = Color(0xFFFFD66B),
                spotColor = Color(0xFFB56CFF)
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        Color(0xFFFFF1A8),
                        Color(0xFFB56CFF),
                        Color(0x664FC3F7)
                    )
                ),
                shape = RoundedCornerShape(34.dp)
            ),
        shape = RoundedCornerShape(34.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xDD150020))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF2A0445),
                            Color(0xFF55128A),
                            Color(0xFF190026),
                            Color(0xFF09000F)
                        )
                    )
                )
                .padding(15.dp)
        ) {
            // Decorative blob header dihapus total.


            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = onBack,
                        shape = RoundedCornerShape(50.dp),
                        border = BorderStroke(1.dp, Color(0x99FFD66B)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text("Back", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Deck Builder",
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "Build your strongest arena lineup",
                            color = Color(0xFFEAD8FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PremiumMiniInfo("DECK", "$totalDeck/$MAX_DECK_CARD", Color(0xFFFFD66B))
                    PremiumMiniInfo("OWNED", "$totalOwned", Color(0xFF4FC3F7))
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50.dp)),
                    color = Color(0xFFFFD66B),
                    trackColor = Color.White.copy(alpha = 0.16f)
                )

                AnimatedVisibility(
                    visible = message.isNotBlank(),
                    enter = fadeIn() + scaleIn(initialScale = 0.96f)
                ) {
                    Surface(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth(),
                        color = Color.White.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))
                    ) {
                        Text(
                            text = message,
                            color = Color(0xFFFFF1BF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumMiniInfo(
    label: String,
    value: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.14f),
        shape = RoundedCornerShape(50.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.38f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(label, color = Color.White.copy(alpha = 0.78f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(5.dp))
            Text(value, color = color, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun SectionTitleRow(
    title: String,
    subtitle: String,
    actionText: String,
    actionEnabled: Boolean,
    onAction: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 2.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp
            )
            Text(
                text = subtitle,
                color = Color(0xFFCDB8E6),
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )
        }

        if (actionText.isNotBlank()) {
            FilledTonalButton(
                onClick = onAction,
                enabled = actionEnabled,
                shape = RoundedCornerShape(50.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 7.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0x33FFD66B),
                    contentColor = Color(0xFFFFD66B),
                    disabledContainerColor = Color.White.copy(alpha = 0.08f),
                    disabledContentColor = Color.White.copy(alpha = 0.32f)
                )
            ) {
                Text(actionText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ActiveDeckPanel(
    cards: List<CardEntity>,
    deckSize: Int,
    onRemove: (CardEntity) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(286.dp)
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(Color(0x77FFD66B), Color(0x66B56CFF), Color(0x224FC3F7))
                ),
                shape = RoundedCornerShape(30.dp)
            ),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xA80B0014))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.07f),
                            Color(0xAA12001F),
                            Color(0xF207000D)
                        )
                    )
                )
                .padding(12.dp)
        ) {
            if (cards.isEmpty()) {
                EmptyDeckState()
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(cards, key = { it.id }) { card ->
                        MiniDeckCard(
                            card = card,
                            buttonText = "Remove",
                            onClick = { onRemove(card) }
                        )
                    }

                    val emptySlots = (6 - cards.size).coerceAtLeast(0)
                    items(emptySlots) {
                        DeckEmptySlot()
                    }
                }
            }

            AssistChip(
                onClick = {},
                enabled = false,
                label = {
                    Text(
                        "$deckSize/$MAX_DECK_CARD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    disabledContainerColor = Color(0x33FFD66B),
                    disabledLabelColor = Color(0xFFFFD66B)
                ),
                border = BorderStroke(1.dp, Color(0x66FFD66B)),
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}

@Composable
fun EmptyDeckState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(86.dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0x44FFD66B), Color(0x1112001F), Color.Transparent)
                    ),
                    CircleShape
                )
                .border(1.dp, Color(0x55FFD66B), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("✦", color = Color(0xFFFFD66B), fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Deck masih kosong",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Text(
            text = "Pilih kartu dari Owned Cards untuk mulai membangun deck.",
            color = Color(0xFFCDB8E6),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 5.dp)
        )
    }
}

@Composable
fun DeckEmptySlot() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp)
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(18.dp))
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text("+", color = Color.White.copy(alpha = 0.30f), fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun MiniDeckCard(
    card: CardEntity,
    buttonText: String,
    onClick: () -> Unit
) {
    val rarityColor = deckRarityColor(card.rarity)
    val safeImageRes = safeDeckCardImage(card)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp)
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = rarityColor,
                spotColor = rarityColor
            )
            .border(1.dp, rarityColor.copy(alpha = 0.55f), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xDD140020))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = safeImageRes),
                contentDescription = card.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0x4412001F),
                                Color(0xF008000F)
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .background(rarityColor.copy(alpha = 0.90f), RoundedCornerShape(50.dp))
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(card.mana.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
            }

            Text(
                text = card.name,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 8.dp, end = 8.dp, bottom = 39.dp)
            )

            ElevatedButton(
                onClick = onClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .height(34.dp)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(50.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = Color(0xEE24043B),
                    contentColor = Color.White
                )
            ) {
                Text(text = buttonText, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun OwnedCardItem(
    card: CardEntity,
    isInDeck: Boolean,
    onClick: () -> Unit
) {
    val rarityColor = deckRarityColor(card.rarity)
    val safeImageRes = safeDeckCardImage(card)
    val infiniteTransition = rememberInfiniteTransition(label = "owned_${card.id}")
    val glow by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "card_glow"
    )
    val softHighlight by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "card_soft_highlight"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(392.dp)
            .graphicsLayer {
                rotationX = if (isInDeck) 0f else 3.5f
                rotationY = if (isInDeck) 0f else -2.5f
                cameraDistance = 15f * density
            }
            .shadow(
                elevation = if (isInDeck) 6.dp else 24.dp,
                shape = RoundedCornerShape(
                    topStart = 14.dp,
                    topEnd = 34.dp,
                    bottomStart = 34.dp,
                    bottomEnd = 14.dp
                ),
                ambientColor = if (isInDeck) Color.Black else rarityColor.copy(alpha = glow),
                spotColor = if (isInDeck) Color.Black else rarityColor.copy(alpha = glow)
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        rarityColor.copy(alpha = if (isInDeck) 0.20f else 0.90f),
                        Color.White.copy(alpha = if (isInDeck) 0.05f else 0.16f),
                        rarityColor.copy(alpha = if (isInDeck) 0.12f else 0.58f)
                    )
                ),
                shape = RoundedCornerShape(
                    topStart = 14.dp,
                    topEnd = 34.dp,
                    bottomStart = 34.dp,
                    bottomEnd = 14.dp
                )
            )
            .clickable {
                if (!isInDeck) onClick()
            },
        shape = RoundedCornerShape(
            topStart = 14.dp,
            topEnd = 34.dp,
            bottomStart = 34.dp,
            bottomEnd = 14.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isInDeck) Color(0xDD09090B) else Color(0xF112001F)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            rarityColor.copy(alpha = if (isInDeck) 0.08f else 0.42f),
                            Color(0xFF1B0430),
                            Color(0xFF050008)
                        )
                    )
                )
                .padding(7.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color(0xFF07000D),
                        RoundedCornerShape(
                            topStart = 10.dp,
                            topEnd = 27.dp,
                            bottomStart = 27.dp,
                            bottomEnd = 10.dp
                        )
                    )
                    .clip(
                        RoundedCornerShape(
                            topStart = 10.dp,
                            topEnd = 27.dp,
                            bottomStart = 27.dp,
                            bottomEnd = 10.dp
                        )
                    )
            ) {
                Image(
                    painter = painterResource(id = safeImageRes),
                    contentDescription = card.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .align(Alignment.TopCenter)
                        .alpha(if (isInDeck) 0.42f else 1f),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color(0x3312001F),
                                    Color(0xE008000F)
                                )
                            )
                        )
                )

                // Highlight atas dihapus total agar tidak muncul shape kuning/panjang.


                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(9.dp)
                        .background(
                            rarityColor.copy(alpha = if (isInDeck) 0.38f else 0.94f),
                            RoundedCornerShape(50.dp)
                        )
                        .border(
                            1.dp,
                            Color.White.copy(alpha = if (isInDeck) 0.08f else 0.22f),
                            RoundedCornerShape(50.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = card.rarity.uppercase(),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(9.dp)
                        .size(42.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    rarityColor.copy(alpha = if (isInDeck) 0.52f else 1f),
                                    Color(0xFF190026),
                                    Color(0xFF050008)
                                )
                            ),
                            CircleShape
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = card.mana.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }

                if (isInDeck) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(
                                Color.Black.copy(alpha = 0.76f),
                                RoundedCornerShape(50.dp)
                            )
                            .border(1.dp, Color(0x66FFD66B), RoundedCornerShape(50.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "IN DECK",
                            color = Color(0xFFFFD66B),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(184.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xF0180024),
                                    Color(0xFF08000F)
                                )
                            ),
                            RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
                        )
                        .border(
                            1.dp,
                            Color.White.copy(alpha = 0.07f),
                            RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
                        )
                        .padding(11.dp)
                ) {
                    Text(
                        text = card.name,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (card.title.isNotBlank()) {
                        Text(
                            text = card.title,
                            color = rarityColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "${card.element} • ${card.role}",
                        color = Color(0xFFE8D7FF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(7.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DeckStatBadge("ATK", card.attack, Color(0xFFFF6B6B), Modifier.weight(1f))
                        DeckStatBadge("DEF", card.defense, Color(0xFF82D8FF), Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DeckStatBadge("HP", card.hp, Color(0xFF6CFF9B), Modifier.weight(1f))
                        DeckStatBadge("SPD", card.speed, Color(0xFF26C6DA), Modifier.weight(1f))
                    }

                    // Action pill bawah dihapus TOTAL.
                    // Sebelumnya bagian ini membuat kotak/pill kuning panjang di bawah kartu.
                    // Jangan tambahkan Surface/Button/Text apapun di area bawah agar artifact benar-benar hilang.
                }
            }
        }
    }
}

@Composable
fun DeckStatBadge(
    label: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.16f),
        shape = RoundedCornerShape(50.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.22f))
    ) {
        Text(
            text = "$label $value",
            color = color,
            fontSize = 8.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 3.dp),
            maxLines = 1
        )
    }
}

fun deckRarityColor(rarity: String): Color {
    return when (rarity) {
        "Legendary" -> Color(0xFFFFD700)
        "Epic" -> Color(0xFFB56CFF)
        "Rare" -> Color(0xFF4FC3F7)
        else -> Color(0xFFC9C9C9)
    }
}

fun rarityPower(rarity: String): Int {
    return when (rarity) {
        "Legendary" -> 4
        "Epic" -> 3
        "Rare" -> 2
        else -> 1
    }
}
