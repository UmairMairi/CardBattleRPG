package com.pegasus.cardbattlerpg.ui.shop

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.pegasus.cardbattlerpg.entity.PlayerEntity
import com.pegasus.cardbattlerpg.entity.ShopItemEntity
import com.pegasus.cardbattlerpg.repository.GameRepository
import kotlinx.coroutines.launch

@Composable
fun shopDrawableIdByName(name: String): Int {
    val context = LocalContext.current
    return remember(name) {
        context.resources.getIdentifier(name, "drawable", context.packageName)
    }
}

@Composable
fun safeShopItemImage(item: ShopItemEntity): Int {
    val res = shopDrawableIdByName(item.itemImage)
    return if (res != 0) res else R.drawable.item_unknown
}

@Composable
fun ShopScreen(
    repository: GameRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var player by remember { mutableStateOf<PlayerEntity?>(null) }
    var items by remember { mutableStateOf<List<ShopItemEntity>>(emptyList()) }
    var message by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("All") }

    suspend fun reload() {
        player = repository.getPlayer()
        items = repository.getShopItems()
    }

    LaunchedEffect(Unit) {
        reload()
    }

    val activeItems = items.filter { it.active }.sortedBy { it.sortOrder }
    val categories = listOf("All") + activeItems.map { it.itemType }.distinct()

    val filteredItems = if (selectedType == "All") {
        activeItems
    } else {
        activeItems.filter { it.itemType == selectedType }
    }

    val infinite = rememberInfiniteTransition(label = "shop_background_fx")
    val glowPulse by infinite.animateFloat(
        initialValue = 0.24f,
        targetValue = 0.56f,
        animationSpec = infiniteRepeatable(
            animation = tween(1900, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shop_glow_pulse"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.shop_background),
            contentDescription = "Shop Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xF908000F),
                            Color(0xC5250044),
                            Color(0xE012001F),
                            Color(0xFF030006)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .size(340.dp)
                .align(Alignment.TopEnd)
                .offset(x = 130.dp, y = (-130).dp)
                .alpha(glowPulse)
                .blur(28.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFFFFD66B),
                            Color(0x664B1478),
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
                .offset(x = (-150).dp, y = 90.dp)
                .alpha(glowPulse * 0.62f)
                .blur(34.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFF4FC3F7),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            ShopHeader(
                gold = player?.gold ?: 0,
                diamond = player?.diamond ?: 0,
                totalItems = activeItems.size,
                message = message,
                onBack = onBack
            )

            Spacer(modifier = Modifier.height(12.dp))

            PremiumShopCategoryRow(
                categories = categories,
                selectedType = selectedType,
                onSelect = { selectedType = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (filteredItems.isEmpty()) {
                    item {
                        EmptyShopState(selectedType = selectedType)
                    }
                }

                items(filteredItems, key = { it.id }) { item ->
                    PremiumShopItemCard(
                        item = item,
                        onBuy = {
                            scope.launch {
                                message = repository.buyShopItem(item)
                                reload()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ShopHeader(
    gold: Int,
    diamond: Int,
    totalItems: Int,
    message: String,
    onBack: () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "shop_header_fx")
    val borderGlow by infinite.animateFloat(
        initialValue = 0.34f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shop_header_border"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 26.dp,
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
                    Color(0xFFFFD66B).copy(alpha = borderGlow),
                    Color(0xFFB56CFF).copy(alpha = 0.50f),
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
                            Color(0xFF100019),
                            Color(0xFF4B1478),
                            Color(0xFF2A0445),
                            Color(0xFF09000F)
                        )
                    )
                )
                .padding(13.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 22.dp, y = (-42).dp)
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
                        text = "Premium Shop",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Gold $gold",
                            color = Color(0xFFFFD66B),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1
                        )
                        Text(
                            text = "  •  Diamond $diamond  •  Items $totalItems",
                            color = Color(0xFFEBD9FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    AnimatedVisibility(
                        visible = message.isNotBlank(),
                        enter = fadeIn(tween(220)) + scaleIn(initialScale = 0.96f)
                    ) {
                        Text(
                            text = message,
                            color = Color(0xFFFFF1BF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumShopCategoryRow(
    categories: List<String>,
    selectedType: String,
    onSelect: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 1.dp, vertical = 2.dp)
    ) {
        items(categories) { type ->
            val selected = selectedType == type
            val chipColor = shopTypeColor(type)

            FilterChip(
                selected = selected,
                onClick = { onSelect(type) },
                label = {
                    Text(
                        text = type,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                shape = RoundedCornerShape(50.dp),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected,
                    borderColor = if (selected) chipColor.copy(alpha = 0.78f) else Color.White.copy(alpha = 0.18f),
                    selectedBorderColor = chipColor,
                    borderWidth = 1.dp,
                    selectedBorderWidth = 1.dp
                ),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.Black.copy(alpha = 0.24f),
                    labelColor = Color(0xFFEBD9FF),
                    selectedContainerColor = chipColor.copy(alpha = 0.22f),
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
fun PremiumShopItemCard(
    item: ShopItemEntity,
    onBuy: () -> Unit
) {
    val imageRes = safeShopItemImage(item)
    val rarityColor = shopRarityColor(item.rarity)
    val typeColor = shopTypeColor(item.itemType)
    val enabled = item.stock != 0

    val priceText = if (item.isDiamondShop) {
        "${item.priceDiamond} Diamond"
    } else {
        "${item.priceGold} Gold"
    }

    val infinite = rememberInfiniteTransition(label = "shop_item_${item.id}")
    val glow by infinite.animateFloat(
        initialValue = 0.52f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "item_glow"
    )

    val cardAlpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.74f,
        animationSpec = tween(260, easing = FastOutSlowInEasing),
        label = "shop_item_alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(218.dp)
            .graphicsLayer { alpha = cardAlpha }
            .shadow(
                elevation = if (item.featured) 28.dp else 16.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = if (enabled) rarityColor.copy(alpha = glow) else Color.Black,
                spotColor = if (enabled) rarityColor.copy(alpha = glow) else Color.Black
            ),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(
                    Color.White.copy(alpha = 0.14f),
                    rarityColor.copy(alpha = if (enabled) 0.80f else 0.26f),
                    typeColor.copy(alpha = if (enabled) 0.44f else 0.18f),
                    Color.White.copy(alpha = 0.08f)
                )
            )
        ),
        colors = CardDefaults.cardColors(containerColor = Color(0xE812001F))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF07000D),
                            rarityColor.copy(alpha = if (enabled) 0.26f else 0.10f),
                            Color(0xFF1B0430),
                            Color(0xFF07000D)
                        )
                    )
                )
                .padding(12.dp)
        ) {
            if (item.featured) {
                FeaturedRibbon(modifier = Modifier.align(Alignment.TopEnd))
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(116.dp)
                    .shadow(18.dp, RoundedCornerShape(28.dp), ambientColor = rarityColor, spotColor = typeColor)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                rarityColor.copy(alpha = if (enabled) 0.92f else 0.28f),
                                typeColor.copy(alpha = if (enabled) 0.36f else 0.14f),
                                Color(0xFF100019)
                            )
                        ),
                        RoundedCornerShape(28.dp)
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(Color.Black.copy(alpha = 0.20f), CircleShape)
                        .blur(1.dp)
                )

                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = item.itemName,
                    modifier = Modifier
                        .size(92.dp)
                        .graphicsLayer {
                            scaleX = if (enabled) 1f else 0.94f
                            scaleY = if (enabled) 1f else 0.94f
                        },
                    contentScale = ContentScale.Fit
                )
            }

            if (item.badge.isNotBlank()) {
                ShopBadgeRibbon(
                    text = item.badge,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 7.dp, y = 4.dp)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 132.dp, end = 6.dp)
                    .fillMaxHeight()
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.itemName,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (item.stock == 0) {
                        StockPill("SOLD", Color(0xFFFF5D78))
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${item.rarity} • ${item.itemType}",
                    color = rarityColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(7.dp))

                Text(
                    text = item.description.ifBlank { "Premium RPG shop item untuk memperkuat perjalananmu." },
                    color = Color(0xFFE9D9FF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ShopMiniBadge(
                        text = "Qty ${item.quantity}",
                        color = typeColor,
                        modifier = Modifier.weight(1f)
                    )

                    if (item.stock >= 0) {
                        ShopMiniBadge(
                            text = "Stock ${item.stock}",
                            color = Color(0xFFFF7043),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (item.discountPercent > 0) {
                    ShopMiniBadge(
                        text = "DISCOUNT -${item.discountPercent}%",
                        color = Color(0xFFFFD66B),
                        modifier = Modifier.widthIn(min = 116.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PricePill(
                        text = priceText,
                        isDiamond = item.isDiamondShop,
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = onBuy,
                        enabled = enabled,
                        modifier = Modifier
                            .width(82.dp)
                            .height(42.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (item.isDiamondShop) Color(0xFF7DE7FF) else Color(0xFFFFD66B),
                            contentColor = Color(0xFF160021),
                            disabledContainerColor = Color.White.copy(alpha = 0.12f),
                            disabledContentColor = Color.White.copy(alpha = 0.42f)
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            text = if (enabled) "BUY" else "SOLD",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun FeaturedRibbon(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color(0x22FFD66B),
        shape = RoundedCornerShape(50.dp),
        border = BorderStroke(1.dp, Color(0x66FFD66B))
    ) {
        Text(
            text = "FEATURED",
            color = Color(0xFFFFD66B),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            maxLines = 1
        )
    }
}

@Composable
fun ShopBadgeRibbon(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color(0xFFFFD66B),
        shape = RoundedCornerShape(50.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.30f))
    ) {
        Text(
            text = text.uppercase(),
            color = Color(0xFF21000A),
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun StockPill(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.18f),
        shape = RoundedCornerShape(50.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.46f))
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            maxLines = 1
        )
    }
}

@Composable
fun ShopMiniBadge(
    text: String,
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
            text = text,
            color = color,
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun PricePill(
    text: String,
    isDiamond: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (isDiamond) Color(0xFF7DE7FF) else Color(0xFFFFD66B)

    Surface(
        modifier = modifier,
        color = Color.Black.copy(alpha = 0.32f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.42f))
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)
        )
    }
}

@Composable
fun EmptyShopState(selectedType: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .shadow(18.dp, RoundedCornerShape(30.dp)),
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xD90B0014))
    ) {
        Column(
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
                .padding(18.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .background(Color(0x22FFD66B), CircleShape)
                    .border(1.dp, Color(0x66FFD66B), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("✦", color = Color(0xFFFFD66B), fontSize = 34.sp, fontWeight = FontWeight.Black)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Item tidak tersedia",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "Kategori $selectedType belum memiliki item aktif.",
                color = Color(0xFFEBD9FF),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

fun shopTypeColor(type: String): Color {
    return when (type) {
        "All" -> Color(0xFFFFD66B)
        "Consumable" -> Color(0xFF6CFF9B)
        "Ticket" -> Color(0xFFFFD66B)
        "Material" -> Color(0xFF4FC3F7)
        "Upgrade" -> Color(0xFFB56CFF)
        "Chest" -> Color(0xFFFF8A50)
        "Diamond" -> Color(0xFF7DE7FF)
        else -> Color(0xFFC9C9C9)
    }
}

fun shopRarityColor(rarity: String): Color {
    return when (rarity) {
        "Mythic" -> Color(0xFFFF4D4D)
        "Legendary" -> Color(0xFFFFD700)
        "Epic" -> Color(0xFFB56CFF)
        "Rare" -> Color(0xFF4FC3F7)
        else -> Color(0xFFC9C9C9)
    }
}
