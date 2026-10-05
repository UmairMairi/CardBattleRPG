package com.pegasus.cardbattlerpg.ui.achievement

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.pegasus.cardbattlerpg.R
import com.pegasus.cardbattlerpg.entity.*
import com.pegasus.cardbattlerpg.repository.GameRepository
import kotlinx.coroutines.launch

@Composable
fun achievementDrawableIdByName(name: String): Int {
    val context = LocalContext.current
    return remember(name) {
        context.resources.getIdentifier(name, "drawable", context.packageName)
    }
}

@Composable
fun safeAchievementIcon(achievement: AchievementEntity): Int {
    val res = achievementDrawableIdByName(achievement.icon)
    return if (res != 0) res else R.drawable.achievement_default
}

@Composable
fun AchievementScreen(
    repository: GameRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var achievements by remember { mutableStateOf<List<AchievementEntity>>(emptyList()) }
    var player by remember { mutableStateOf<PlayerEntity?>(null) }
    var selectedFilter by remember { mutableStateOf("All") }
    var message by remember { mutableStateOf("") }

    suspend fun reload() {
        achievements = repository.getAchievements()
        player = repository.getPlayer()
    }

    LaunchedEffect(Unit) { reload() }

    val visibleAchievements = achievements.filter { !it.hidden || it.unlocked }
    val filters = listOf("All", "Unlocked", "Claimed") +
            visibleAchievements.map { it.category }.distinct()

    val filteredAchievements = when (selectedFilter) {
        "Unlocked" -> visibleAchievements.filter { it.unlocked && !it.claimed }
        "Claimed" -> visibleAchievements.filter { it.claimed }
        "All" -> visibleAchievements
        else -> visibleAchievements.filter { it.category == selectedFilter }
    }.sortedWith(
        compareBy<AchievementEntity> { it.claimed }
            .thenByDescending { it.unlocked }
            .thenByDescending { achievementRarityPower(it.rarity) }
            .thenBy { it.id }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.achievement_background),
            contentDescription = "Achievement Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        AchievementPremiumBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            AchievementHeader(
                diamond = player?.diamond ?: 0,
                unlocked = achievements.count { it.unlocked },
                claimed = achievements.count { it.claimed },
                total = achievements.size,
                message = message,
                onBack = onBack
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filters) { filter ->
                    AchievementFilterChip(
                        text = filter,
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(15.dp),
                contentPadding = PaddingValues(bottom = 28.dp)
            ) {
                items(filteredAchievements, key = { it.id }) { achievement ->
                    PremiumAchievementCard(
                        achievement = achievement,
                        onClaim = {
                            scope.launch {
                                message = repository.claimAchievement(achievement)
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
fun AchievementPremiumBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xF7050009),
                        Color(0xC126004A),
                        Color(0xFA08000F),
                        Color(0xFF030006)
                    )
                )
            )
    )

    Box(
        modifier = Modifier
            .size(390.dp)
            .offset(x = 170.dp, y = (-130).dp)
            .alpha(0.34f)
            .blur(24.dp)
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFFFFD66B), Color(0x774B1478), Color.Transparent)
                ),
                CircleShape
            )
    )
}

@Composable
fun AchievementHeader(
    diamond: Int,
    unlocked: Int,
    claimed: Int,
    total: Int,
    message: String,
    onBack: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(26.dp, RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.dp, Color(0x66FFD66B)),
        colors = CardDefaults.cardColors(containerColor = Color(0xE812001F))
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF100019),
                            Color(0xFF4B1478),
                            Color(0xFF190026),
                            Color(0xFF08000F)
                        )
                    )
                )
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.height(40.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFFFD66B).copy(alpha = 0.58f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.18f),
                    contentColor = Color.White
                )
            ) {
                Text("Back", fontSize = 12.sp, fontWeight = FontWeight.Black)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Achievement Hall",
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Diamond $diamond",
                    color = Color(0xFFFFD66B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "Unlocked $unlocked/$total • Claimed $claimed",
                    color = Color(0xFFE9D9FF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                if (message.isNotBlank()) {
                    Text(
                        text = message,
                        color = Color(0xFFFFD66B),
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

@Composable
fun AchievementFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(text, fontSize = 11.sp, fontWeight = FontWeight.Black)
        },
        shape = RoundedCornerShape(50.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFFFFD66B),
            selectedLabelColor = Color(0xFF160021),
            containerColor = Color.Black.copy(alpha = 0.34f),
            labelColor = Color.White
        )
    )
}

@Composable
fun PremiumAchievementCard(
    achievement: AchievementEntity,
    onClaim: () -> Unit
) {
    val progressValue =
        if (achievement.target <= 0) 0f
        else (achievement.progress.toFloat() / achievement.target.toFloat()).coerceIn(0f, 1f)

    val rarityColor = achievementRarityColor(achievement.rarity)
    val iconRes = safeAchievementIcon(achievement)

    val statusText = when {
        achievement.claimed -> "CLAIMED"
        achievement.unlocked -> "READY"
        else -> "LOCKED"
    }

    val statusColor = when {
        achievement.claimed -> Color(0xFF6CFF9B)
        achievement.unlocked -> Color(0xFFFFD66B)
        else -> Color(0xFF888888)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(205.dp)
            .shadow(
                elevation = if (achievement.unlocked && !achievement.claimed) 28.dp else 16.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = rarityColor,
                spotColor = rarityColor
            ),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.dp, rarityColor.copy(alpha = 0.55f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xE812001F))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF07000D),
                            rarityColor.copy(alpha = 0.30f),
                            Color(0xFF170024)
                        )
                    )
                )
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(104.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                rarityColor.copy(alpha = 0.85f),
                                Color(0xFF100019)
                            )
                        ),
                        RoundedCornerShape(28.dp)
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = achievement.title,
                    modifier = Modifier.size(78.dp),
                    contentScale = ContentScale.Fit
                )

                if (!achievement.unlocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(28.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("LOCK", color = Color(0xFFFFD66B), fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            if (achievement.badge.isNotBlank()) {
                AchievementBadge(
                    text = achievement.badge,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 4.dp, y = 2.dp)
                )
            }

            AchievementStatusBadge(
                text = statusText,
                color = statusColor,
                modifier = Modifier.align(Alignment.TopEnd)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 118.dp, end = 8.dp, top = 24.dp)
            ) {
                Text(
                    text = achievement.title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${achievement.rarity} • ${achievement.category}",
                    color = rarityColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = achievement.description,
                    color = Color(0xFFE9D9FF),
                    fontSize = 10.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(9.dp))

                AchievementProgressBar(progressValue, rarityColor)

                Spacer(modifier = Modifier.height(5.dp))

                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${achievement.progress.coerceAtMost(achievement.target)}/${achievement.target}",
                        color = Color(0xFFE9D9FF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${(progressValue * 100).toInt()}%",
                        color = rarityColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (achievement.rewardGold > 0) {
                        AchievementRewardBadge("${achievement.rewardGold} Gold", Color(0xFFFFD66B))
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    if (achievement.rewardDiamond > 0) {
                        AchievementRewardBadge("${achievement.rewardDiamond} Diamond", Color(0xFF7DE7FF))
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    if (achievement.rewardItemName.isNotBlank()) {
                        AchievementRewardBadge(
                            "${achievement.rewardItemName} x${achievement.rewardItemAmount}",
                            Color(0xFFB56CFF)
                        )
                    }
                }
            }

            Button(
                onClick = onClaim,
                enabled = achievement.unlocked && !achievement.claimed,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .height(38.dp),
                shape = RoundedCornerShape(15.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text(
                    text = if (achievement.claimed) "Claimed" else "Claim",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun AchievementProgressBar(progress: Float, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .background(Color.Black.copy(alpha = 0.36f), RoundedCornerShape(50.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(10.dp)
                .background(
                    Brush.horizontalGradient(listOf(color, Color(0xFFFFD66B))),
                    RoundedCornerShape(50.dp)
                )
        )
    }
}

@Composable
fun AchievementBadge(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                Brush.horizontalGradient(listOf(Color(0xFFFFD66B), Color(0xFFFF8A00))),
                RoundedCornerShape(50.dp)
            )
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(text, color = Color(0xFF21000A), fontSize = 9.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun AchievementStatusBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.16f),
        shape = RoundedCornerShape(50.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.55f))
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun AchievementRewardBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.16f),
        shape = RoundedCornerShape(50.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
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

fun achievementRarityColor(rarity: String): Color {
    return when (rarity) {
        "Mythic" -> Color(0xFFFF4D4D)
        "Legendary" -> Color(0xFFFFD700)
        "Epic" -> Color(0xFFB56CFF)
        "Rare" -> Color(0xFF4FC3F7)
        else -> Color(0xFFC9C9C9)
    }
}

fun achievementRarityPower(rarity: String): Int {
    return when (rarity) {
        "Mythic" -> 5
        "Legendary" -> 4
        "Epic" -> 3
        "Rare" -> 2
        else -> 1
    }
}