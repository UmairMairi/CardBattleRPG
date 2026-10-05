package com.pegasus.cardbattlerpg.ui.mission

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pegasus.cardbattlerpg.R
import com.pegasus.cardbattlerpg.entity.MissionEntity
import com.pegasus.cardbattlerpg.entity.PlayerEntity
import com.pegasus.cardbattlerpg.repository.GameRepository
import kotlinx.coroutines.launch

@Composable
fun MissionScreen(
    repository: GameRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var missions by remember { mutableStateOf<List<MissionEntity>>(emptyList()) }
    var player by remember { mutableStateOf<PlayerEntity?>(null) }
    var selectedType by remember { mutableStateOf("All") }
    var message by remember { mutableStateOf("") }

    suspend fun reload() {
        missions = repository.getMissions()
        player = repository.getPlayer()
    }

    LaunchedEffect(Unit) {
        reload()
    }

    val sortedMissions = missions.sortedWith(
        compareBy<MissionEntity> { it.claimed }
            .thenByDescending { it.completed }
            .thenBy { missionTypeOrder(it.type) }
            .thenBy { it.id }
    )

    val types = listOf("All") + sortedMissions.map { it.type }.distinct()

    val filteredMissions =
        if (selectedType == "All") sortedMissions
        else sortedMissions.filter { it.type == selectedType }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.mission_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        MissionPremiumBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            MissionHeader(
                gold = player?.gold ?: 0,
                diamond = player?.diamond ?: 0,
                claimableCount = missions.count { it.completed && !it.claimed },
                completedCount = missions.count { it.completed },
                totalCount = missions.size,
                message = message,
                onBack = onBack
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(types) { type ->
                    MissionTypeChip(
                        text = type,
                        selected = selectedType == type,
                        onClick = { selectedType = type }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(15.dp),
                contentPadding = PaddingValues(bottom = 28.dp)
            ) {
                items(filteredMissions, key = { it.id }) { mission ->
                    PremiumMissionCard(
                        mission = mission,
                        onClaim = {
                            scope.launch {
                                message = repository.claimMission(mission)
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
fun MissionPremiumBackground() {
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
            .size(380.dp)
            .offset(x = 180.dp, y = (-130).dp)
            .alpha(0.32f)
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
}

@Composable
fun MissionHeader(
    gold: Int,
    diamond: Int,
    claimableCount: Int,
    completedCount: Int,
    totalCount: Int,
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
                    text = "Mission Board",
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "Gold $gold • Diamond $diamond",
                    color = Color(0xFFFFD66B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "Claim $claimableCount • Complete $completedCount/$totalCount",
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
fun MissionTypeChip(
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
fun PremiumMissionCard(
    mission: MissionEntity,
    onClaim: () -> Unit
) {
    val progressValue =
        if (mission.target <= 0) 0f
        else (mission.progress.toFloat() / mission.target.toFloat()).coerceIn(0f, 1f)

    val typeColor = missionTypeColor(mission.type)

    val statusText = when {
        mission.claimed -> "CLAIMED"
        mission.completed -> "READY"
        else -> "PROGRESS"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (mission.completed && !mission.claimed) 26.dp else 16.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = typeColor,
                spotColor = typeColor
            ),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.dp, typeColor.copy(alpha = 0.55f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xE812001F))
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF07000D),
                            typeColor.copy(alpha = 0.28f),
                            Color(0xFF170024)
                        )
                    )
                )
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MissionIcon(mission.type, typeColor)

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mission.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = mission.type,
                        color = typeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                MissionStatusBadge(
                    text = statusText,
                    color = when {
                        mission.claimed -> Color(0xFF6CFF9B)
                        mission.completed -> Color(0xFFFFD66B)
                        else -> Color(0xFFD7C3F2)
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = mission.description,
                color = Color(0xFFE9D9FF),
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            MissionProgressBar(progressValue, typeColor)

            Spacer(modifier = Modifier.height(7.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${mission.progress.coerceAtMost(mission.target)}/${mission.target}",
                    color = Color(0xFFD7C3F2),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${(progressValue * 100).toInt()}%",
                    color = typeColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                MissionRewardBadge("${mission.rewardGold} Gold", Color(0xFFFFD66B))

                Spacer(modifier = Modifier.width(8.dp))

                MissionRewardBadge("${mission.rewardDiamond} Diamond", Color(0xFF7DE7FF))

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onClaim,
                    enabled = mission.completed && !mission.claimed,
                    modifier = Modifier.height(42.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (mission.claimed) "Claimed" else "Claim",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun MissionIcon(type: String, color: Color) {
    Box(
        modifier = Modifier
            .size(58.dp)
            .background(
                Brush.radialGradient(
                    listOf(color.copy(alpha = 0.90f), Color(0xFF100019))
                ),
                RoundedCornerShape(18.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (type) {
                "Daily" -> "D"
                "Weekly" -> "W"
                "Main" -> "M"
                "Event" -> "E"
                else -> "Q"
            },
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun MissionProgressBar(progress: Float, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(50.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(12.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(color, Color(0xFFFFD66B))
                    ),
                    RoundedCornerShape(50.dp)
                )
        )
    }
}

@Composable
fun MissionStatusBadge(text: String, color: Color) {
    Surface(
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
fun MissionRewardBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(50.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

fun missionTypeColor(type: String): Color {
    return when (type) {
        "Daily" -> Color(0xFFFFD66B)
        "Weekly" -> Color(0xFF4FC3F7)
        "Main" -> Color(0xFFB56CFF)
        "Event" -> Color(0xFFFF7043)
        else -> Color(0xFFC9C9C9)
    }
}

fun missionTypeOrder(type: String): Int {
    return when (type) {
        "Daily" -> 1
        "Weekly" -> 2
        "Main" -> 3
        "Event" -> 4
        else -> 9
    }
}