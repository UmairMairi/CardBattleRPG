package com.pegasus.cardbattlerpg.ui.equipment

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
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.pegasus.cardbattlerpg.entity.EquipmentEntity
import com.pegasus.cardbattlerpg.entity.PlayerEntity
import com.pegasus.cardbattlerpg.repository.GameRepository
import kotlinx.coroutines.launch

@Composable
fun equipmentDrawableIdByName(name: String): Int {
    val context = LocalContext.current
    return remember(name) {
        context.resources.getIdentifier(name, "drawable", context.packageName)
    }
}

@Composable
fun safeEquipmentImage(equipment: EquipmentEntity): Int {
    val res = equipmentDrawableIdByName(equipment.image)
    return if (res != 0) res else R.drawable.eq_unknown
}

@Composable
fun EquipmentScreen(
    repository: GameRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var player by remember { mutableStateOf<PlayerEntity?>(null) }
    var equipments by remember { mutableStateOf<List<EquipmentEntity>>(emptyList()) }
    var equippedItems by remember { mutableStateOf<List<EquipmentEntity>>(emptyList()) }
    var selectedType by remember { mutableStateOf("All") }
    var selectedEquipment by remember { mutableStateOf<EquipmentEntity?>(null) }
    var message by remember { mutableStateOf("") }

    suspend fun reload() {
        player = repository.getPlayer()
        equipments = repository.getAllEquipments()
        equippedItems = repository.getEquippedItems()
    }

    LaunchedEffect(Unit) { reload() }

    val totalAttack = equippedItems.sumOf { it.attackBonus }
    val totalDefense = equippedItems.sumOf { it.defenseBonus }
    val totalHp = equippedItems.sumOf { it.hpBonus }

    val sortedEquipments = equipments.sortedWith(
        compareByDescending<EquipmentEntity> { it.equipped }
            .thenBy { !it.unlocked }
            .thenByDescending { equipmentRarityPower(it.rarity) }
            .thenBy { it.type }
            .thenBy { it.name }
    )

    val types = listOf("All") + sortedEquipments.map { it.type }.distinct()
    val filteredEquipments = if (selectedType == "All") {
        sortedEquipments
    } else {
        sortedEquipments.filter { it.type == selectedType }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.equipment_background),
            contentDescription = "Equipment Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        PremiumEquipmentBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            EquipmentHeader(
                gold = player?.gold ?: 0,
                equippedCount = equippedItems.size,
                totalAttack = totalAttack,
                totalDefense = totalDefense,
                totalHp = totalHp,
                message = message,
                onBack = onBack
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(types) { type ->
                    PremiumEquipmentChip(
                        text = type,
                        selected = selectedType == type,
                        onClick = { selectedType = type }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredEquipments.isEmpty()) {
                EmptyEquipmentState(selectedType = selectedType)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(15.dp),
                    contentPadding = PaddingValues(bottom = 26.dp)
                ) {
                    items(filteredEquipments, key = { it.id }) { equipment ->
                        PremiumEquipmentCard(
                            equipment = equipment,
                            onClick = { selectedEquipment = equipment },
                            onAction = {
                                scope.launch {
                                    message = when {
                                        !equipment.unlocked -> repository.unlockEquipment(equipment)
                                        equipment.equipped -> repository.unequipItem(equipment)
                                        else -> repository.equipItem(equipment)
                                    }
                                    reload()
                                }
                            }
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = selectedEquipment != null,
            enter = scaleIn(initialScale = 0.88f, animationSpec = tween(360, easing = EaseOutBack)) + fadeIn(tween(240))
        ) {
            selectedEquipment?.let { equipment ->
                EquipmentDetailDialog(
                    equipment = equipment,
                    onDismiss = { selectedEquipment = null },
                    onAction = {
                        scope.launch {
                            message = when {
                                !equipment.unlocked -> repository.unlockEquipment(equipment)
                                equipment.equipped -> repository.unequipItem(equipment)
                                else -> repository.equipItem(equipment)
                            }
                            selectedEquipment = null
                            reload()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun BoxScope.PremiumEquipmentBackground() {
    val infinite = rememberInfiniteTransition(label = "equipment_bg_fx")
    val glow by infinite.animateFloat(
        initialValue = 0.20f,
        targetValue = 0.48f,
        animationSpec = infiniteRepeatable(
            animation = tween(1900, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "equipment_bg_glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFA040007),
                        Color(0xC91F003B),
                        Color(0xEE080010),
                        Color(0xFF030006)
                    )
                )
            )
    )

    Box(
        modifier = Modifier
            .size(370.dp)
            .align(Alignment.TopEnd)
            .offset(x = 140.dp, y = (-135).dp)
            .alpha(glow)
            .blur(32.dp)
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFFFFD66B), Color(0x664B1478), Color.Transparent)
                ),
                CircleShape
            )
    )

    Box(
        modifier = Modifier
            .size(300.dp)
            .align(Alignment.BottomStart)
            .offset(x = (-130).dp, y = 90.dp)
            .alpha(glow * 0.62f)
            .blur(34.dp)
            .background(
                Brush.radialGradient(listOf(Color(0xFFB56CFF), Color.Transparent)),
                CircleShape
            )
    )
}

@Composable
fun EquipmentHeader(
    gold: Int,
    equippedCount: Int,
    totalAttack: Int,
    totalDefense: Int,
    totalHp: Int,
    message: String,
    onBack: () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "equipment_header_fx")
    val pulse by infinite.animateFloat(
        initialValue = 0.38f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1350, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "equipment_header_pulse"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(26.dp, RoundedCornerShape(32.dp), ambientColor = Color(0xFFFFD66B), spotColor = Color(0xFFB56CFF)),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(
                    Color.White.copy(alpha = 0.20f),
                    Color(0xFFFFD66B).copy(alpha = pulse),
                    Color(0xFFB56CFF).copy(alpha = 0.45f),
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
                        listOf(Color(0xFF100019), Color(0xFF4B1478), Color(0xFF190026), Color(0xFF08000F))
                    )
                )
                .padding(13.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 24.dp, y = (-38).dp)
                    .alpha(0.20f)
                    .blur(18.dp)
                    .background(Brush.radialGradient(listOf(Color(0xFFFFD66B), Color.Transparent)), CircleShape)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.height(40.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFD66B).copy(alpha = 0.58f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.18f),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp)
                ) {
                    Text("Back", fontSize = 12.sp, fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Equipment Vault",
                        color = Color.White,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "Gold $gold • Equipped $equippedCount",
                        color = Color(0xFFFFD66B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "ATK +$totalAttack • DEF +$totalDefense • HP +$totalHp",
                        color = Color(0xFFE9D9FF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (message.isNotBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = message,
                            color = Color(0xFFFFF0B0),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
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
fun PremiumEquipmentChip(text: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(text = text, fontSize = 11.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        shape = RoundedCornerShape(50.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFFFFD66B),
            selectedLabelColor = Color(0xFF160021),
            containerColor = Color.Black.copy(alpha = 0.34f),
            labelColor = Color.White
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (selected) Color(0xFFFFD66B) else Color.White.copy(alpha = 0.18f),
            selectedBorderColor = Color(0xFFFFD66B)
        )
    )
}

@Composable
fun EmptyEquipmentState(selectedType: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .shadow(18.dp, RoundedCornerShape(30.dp)),
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xCC100019))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.05f), Color.Transparent)))
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(78.dp)
                    .background(Brush.radialGradient(listOf(Color(0x44FFD66B), Color.Transparent)), CircleShape)
                    .border(1.dp, Color(0x55FFD66B), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("⚔", color = Color(0xFFFFD66B), fontSize = 32.sp, fontWeight = FontWeight.Black)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Equipment kosong", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Text(
                text = "Tidak ada equipment untuk kategori $selectedType.",
                color = Color(0xFFE9D9FF),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PremiumEquipmentCard(
    equipment: EquipmentEntity,
    onClick: () -> Unit,
    onAction: () -> Unit
) {
    val imageRes = safeEquipmentImage(equipment)
    val rarityColor = equipmentRarityColor(equipment.rarity)
    val typeColor = equipmentTypeColor(equipment.type)

    val infinite = rememberInfiniteTransition(label = "equipment_card_${equipment.id}")
    val glow by infinite.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(animation = tween(1700, easing = EaseInOutSine), repeatMode = RepeatMode.Reverse),
        label = "equipment_card_glow"
    )
    val imageFloat by infinite.animateFloat(
        initialValue = 2f,
        targetValue = -3f,
        animationSpec = infiniteRepeatable(animation = tween(1600, easing = EaseInOutSine), repeatMode = RepeatMode.Reverse),
        label = "equipment_image_float"
    )

    val lockedAlpha by animateFloatAsState(
        targetValue = if (equipment.unlocked) 1f else 0.64f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "equipment_locked_alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(214.dp)
            .graphicsLayer { alpha = lockedAlpha }
            .shadow(
                elevation = if (equipment.equipped) 30.dp else 16.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = rarityColor.copy(alpha = glow),
                spotColor = rarityColor.copy(alpha = glow)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(
                    Color.White.copy(alpha = 0.12f),
                    rarityColor.copy(alpha = if (equipment.equipped) 0.95f else 0.62f),
                    Color.White.copy(alpha = 0.07f)
                )
            )
        ),
        colors = CardDefaults.cardColors(containerColor = Color(0xE812001F))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFF06000B), rarityColor.copy(alpha = 0.30f), Color(0xFF170024)))
                )
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(120.dp)
                    .background(
                        Brush.radialGradient(listOf(rarityColor.copy(alpha = 0.82f), typeColor.copy(alpha = 0.38f), Color(0xFF100019))),
                        RoundedCornerShape(30.dp)
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.13f), RoundedCornerShape(30.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = equipment.name,
                    modifier = Modifier
                        .size(94.dp)
                        .graphicsLayer { translationY = imageFloat },
                    contentScale = ContentScale.Fit
                )

                if (!equipment.unlocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.50f), RoundedCornerShape(30.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.72f),
                            shape = RoundedCornerShape(50.dp),
                            border = BorderStroke(1.dp, Color(0x99FFD66B))
                        ) {
                            Text(
                                text = "LOCKED",
                                color = Color(0xFFFFD66B),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 4.dp, top = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                if (equipment.equipped) EquipmentBadge(text = "EQUIPPED")
                if (!equipment.unlocked) EquipmentBadge(text = "LOCKED")
            }

            EquipmentStatusPill(
                equipment = equipment,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 2.dp, end = 2.dp)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 134.dp, end = 86.dp, top = 24.dp, bottom = 12.dp)
            ) {
                Text(
                    text = equipment.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${equipment.rarity} • ${equipment.type}",
                    color = rarityColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    EquipmentMiniBadge("ATK +${equipment.attackBonus}", Color(0xFFFF6B6B))
                    EquipmentMiniBadge("DEF +${equipment.defenseBonus}", Color(0xFF82D8FF))
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    EquipmentMiniBadge("HP +${equipment.hpBonus}", Color(0xFF6CFF9B))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (equipment.unlocked) "Ready to equip" else "Unlock ${equipment.unlockPriceGold} Gold",
                    color = Color(0xFFFFD66B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            PremiumEquipmentActionButton(
                enabled = true,
                text = when {
                    !equipment.unlocked -> "Unlock"
                    equipment.equipped -> "Unequip"
                    else -> "Equip"
                },
                onClick = onAction,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .width(82.dp)
            )
        }
    }
}

@Composable
fun PremiumEquipmentActionButton(
    enabled: Boolean,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFFD66B),
            contentColor = Color(0xFF160021),
            disabledContainerColor = Color.White.copy(alpha = 0.12f),
            disabledContentColor = Color.White.copy(alpha = 0.54f)
        ),
        contentPadding = PaddingValues(horizontal = 7.dp)
    ) {
        Text(text = text, fontSize = 10.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun EquipmentStatusPill(equipment: EquipmentEntity, modifier: Modifier = Modifier) {
    val color = when {
        !equipment.unlocked -> Color(0xFFFF6B6B)
        equipment.equipped -> Color(0xFF6CFF9B)
        else -> Color(0xFFFFD66B)
    }
    val text = when {
        !equipment.unlocked -> "LOCKED"
        equipment.equipped -> "ACTIVE"
        else -> "READY"
    }

    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.16f),
        shape = RoundedCornerShape(50.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.58f))
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun EquipmentDetailDialog(
    equipment: EquipmentEntity,
    onDismiss: () -> Unit,
    onAction: () -> Unit
) {
    val imageRes = safeEquipmentImage(equipment)
    val rarityColor = equipmentRarityColor(equipment.rarity)
    val typeColor = equipmentTypeColor(equipment.type)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xF20B0010),
        shape = RoundedCornerShape(32.dp),
        confirmButton = {
            PremiumEquipmentActionButton(
                enabled = true,
                text = when {
                    !equipment.unlocked -> "Unlock"
                    equipment.equipped -> "Unequip"
                    else -> "Equip"
                },
                onClick = onAction,
                modifier = Modifier.width(112.dp)
            )
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Tutup", fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
        },
        title = {
            Column {
                Text(
                    text = equipment.name,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${equipment.rarity} • ${equipment.type}",
                    color = rarityColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 610.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .shadow(24.dp, RoundedCornerShape(30.dp), ambientColor = rarityColor, spotColor = typeColor),
                    shape = RoundedCornerShape(30.dp),
                    border = BorderStroke(
                        1.dp,
                        Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.16f), rarityColor.copy(alpha = 0.72f), Color.White.copy(alpha = 0.08f)))
                    ),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF12001F))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(listOf(rarityColor.copy(alpha = 0.38f), Color(0xFF150020), Color(0xFF050009)))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = imageRes),
                            contentDescription = equipment.name,
                            modifier = Modifier.size(156.dp),
                            contentScale = ContentScale.Fit
                        )

                        Surface(
                            color = when {
                                !equipment.unlocked -> Color.Black.copy(alpha = 0.76f)
                                equipment.equipped -> Color(0xFF6CFF9B).copy(alpha = 0.18f)
                                else -> Color(0xFFFFD66B).copy(alpha = 0.16f)
                            },
                            shape = RoundedCornerShape(50.dp),
                            border = BorderStroke(1.dp, rarityColor.copy(alpha = 0.55f)),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = when {
                                    !equipment.unlocked -> "LOCKED ITEM"
                                    equipment.equipped -> "EQUIPPED"
                                    else -> "UNLOCKED"
                                },
                                color = if (!equipment.unlocked) Color(0xFFFFD66B) else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                EquipmentDetailSection(
                    title = "Equipment Detail",
                    color = rarityColor,
                    lines = listOf(
                        "Type: ${equipment.type}",
                        "Rarity: ${equipment.rarity}",
                        "Attack: +${equipment.attackBonus}",
                        "Defense: +${equipment.defenseBonus}",
                        "HP: +${equipment.hpBonus}",
                        "Status: ${equipmentStatusText(equipment)}"
                    )
                )

                if (!equipment.unlocked) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        border = BorderStroke(1.dp, Color(0xFFFFD66B).copy(alpha = 0.30f)),
                        colors = CardDefaults.cardColors(containerColor = Color(0x22FFD66B))
                    ) {
                        Text(
                            text = "Butuh ${equipment.unlockPriceGold} Gold untuk membuka equipment ini.",
                            color = Color(0xFFFFD66B),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun EquipmentDetailSection(title: String, color: Color, lines: List<String>) {
    Card(
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.28f)),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, color = color, fontSize = 14.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(8.dp))

            lines.chunked(2).forEach { rowLines ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowLines.forEach { line ->
                        Text(
                            text = line,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.Black.copy(alpha = 0.24f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 7.dp)
                        )
                    }
                    if (rowLines.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(7.dp))
            }
        }
    }
}

@Composable
fun EquipmentBadge(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Brush.horizontalGradient(listOf(Color(0xFFFFD66B), Color(0xFFFF8A00))), RoundedCornerShape(50.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(text = text, color = Color(0xFF21000A), fontSize = 9.sp, fontWeight = FontWeight.Black, maxLines = 1)
    }
}

@Composable
fun EquipmentMiniBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.16f),
        shape = RoundedCornerShape(50.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.24f))
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}

fun equipmentStatusText(equipment: EquipmentEntity): String {
    return when {
        !equipment.unlocked -> "Locked"
        equipment.equipped -> "Equipped"
        else -> "Unlocked"
    }
}

fun equipmentTypeColor(type: String): Color {
    return when (type) {
        "Weapon" -> Color(0xFFFF6B6B)
        "Armor" -> Color(0xFF82D8FF)
        "Ring" -> Color(0xFFFFD66B)
        "Boots" -> Color(0xFF6CFF9B)
        "Amulet" -> Color(0xFFB56CFF)
        else -> Color(0xFFC9C9C9)
    }
}

fun equipmentRarityColor(rarity: String): Color {
    return when (rarity) {
        "Mythic" -> Color(0xFFFF4D4D)
        "Legendary" -> Color(0xFFFFD700)
        "Epic" -> Color(0xFFB56CFF)
        "Rare" -> Color(0xFF4FC3F7)
        else -> Color(0xFFC9C9C9)
    }
}

fun equipmentRarityPower(rarity: String): Int {
    return when (rarity) {
        "Mythic" -> 5
        "Legendary" -> 4
        "Epic" -> 3
        "Rare" -> 2
        else -> 1
    }
}
