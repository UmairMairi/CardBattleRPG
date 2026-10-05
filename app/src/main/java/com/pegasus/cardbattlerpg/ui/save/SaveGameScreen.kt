package com.pegasus.cardbattlerpg.ui.save

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.pegasus.cardbattlerpg.R
import com.pegasus.cardbattlerpg.model.SaveGameSummary
import com.pegasus.cardbattlerpg.repository.GameRepository
import com.pegasus.cardbattlerpg.online.FirebaseSyncRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SaveGameScreen(
    repository: GameRepository,
    onBack: () -> Unit,
    onResetFinished: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var summary by remember { mutableStateOf<SaveGameSummary?>(null) }
    var message by remember { mutableStateOf("") }
    val firebaseSync = remember { FirebaseSyncRepository(repository) }

    suspend fun reload() {
        summary = repository.getSaveSummary()
    }

    LaunchedEffect(Unit) { reload() }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.save_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        SavePremiumBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 14.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            SaveHeader(
                message = message,
                onBack = onBack
            )

            Spacer(modifier = Modifier.height(18.dp))

            summary?.let {
                SaveSummaryCard(it)
            } ?: EmptySaveCard()

            Spacer(modifier = Modifier.height(18.dp))

            PremiumSaveButton(
                text = "Manual Save",
                color = Color(0xFFFFD66B),
                onClick = {
                    scope.launch {
                        repository.touchSave()
                        message = firebaseSync.syncFullSave()
                        reload()
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            PremiumSaveButton(
                text = "Sync Realtime Save",
                color = Color(0xFF7DE7FF),
                onClick = {
                    scope.launch {
                        message = firebaseSync.syncFullSave()
                        reload()
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            PremiumSaveButton(
                text = "Reset Save Data",
                color = Color(0xFFFF4D4D),
                onClick = {
                    scope.launch {
                        repository.resetSaveData()
                        onResetFinished()
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SavePremiumBackground() {
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
            .background(
                Brush.radialGradient(
                    listOf(
                        Color(0xFFFFD66B),
                        Color(0x774B1478),
                        Color.Transparent
                    )
                ),
                CircleShape
            )
    )
}

@Composable
fun SaveHeader(
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
                    text = "Save Crystal",
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "Local save status",
                    color = Color(0xFFFFD66B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                if (message.isNotBlank()) {
                    Text(
                        text = message,
                        color = Color(0xFFE9D9FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SaveSummaryCard(summary: SaveGameSummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(28.dp, RoundedCornerShape(34.dp)),
        shape = RoundedCornerShape(34.dp),
        border = BorderStroke(1.dp, Color(0x66FFD66B)),
        colors = CardDefaults.cardColors(containerColor = Color(0xE812001F))
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF180026),
                            Color(0xFF100019),
                            Color(0xFF06000B)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Text(
                text = summary.playerName,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp
            )

            Text(
                text = "Last Saved • ${formatTime(summary.lastSavedAt)}",
                color = Color(0xFFFFD66B),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            SaveStatGrid(summary)
        }
    }
}

@Composable
fun SaveStatGrid(summary: SaveGameSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SaveStatBox("Level", summary.level.toString(), R.drawable.ic_save_card, Modifier.weight(1f), Color(0xFFFFD66B))
            SaveStatBox("Gold", summary.gold.toString(), R.drawable.ic_save_gold, Modifier.weight(1f), Color(0xFFFFD66B))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SaveStatBox("Diamond", summary.diamond.toString(), R.drawable.ic_save_diamond, Modifier.weight(1f), Color(0xFF7DE7FF))
            SaveStatBox("Cards", summary.ownedCards.toString(), R.drawable.ic_save_card, Modifier.weight(1f), Color(0xFFB56CFF))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SaveStatBox("Heroes", summary.heroes.toString(), R.drawable.ic_save_hero, Modifier.weight(1f), Color(0xFF6CFF9B))
            SaveStatBox("Equip", summary.equipments.toString(), R.drawable.ic_save_equipment, Modifier.weight(1f), Color(0xFFFF7043))
        }

        SaveStatBox(
            label = "Completed Stages",
            value = summary.completedStages.toString(),
            icon = R.drawable.ic_save_stage,
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFFFD66B)
        )
    }
}

@Composable
fun SaveStatBox(
    label: String,
    value: String,
    icon: Int,
    modifier: Modifier = Modifier,
    color: Color
) {
    Box(
        modifier = modifier
            .height(82.dp)
            .background(
                Brush.radialGradient(
                    listOf(
                        color.copy(alpha = 0.22f),
                        Color(0xFF100019)
                    )
                ),
                RoundedCornerShape(24.dp)
            )
            .border(1.dp, color.copy(alpha = 0.30f), RoundedCornerShape(24.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color.copy(alpha = 0.16f),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = label,
                    modifier = Modifier.size(28.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = label,
                    color = Color(0xFFE9D9FF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = value,
                    color = color,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun EmptySaveCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(34.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xCC100019))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("✦", color = Color(0xFFFFD66B), fontSize = 42.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(10.dp))
            Text("Belum ada data save", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Text("Mulai game terlebih dahulu untuk membuat data.", color = Color(0xFFE9D9FF), fontSize = 12.sp)
        }
    }
}

@Composable
fun PremiumSaveButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(14.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color(0xFF160021)
        )
    ) {
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.Black)
    }
}

fun formatTime(time: Long): String {
    return SimpleDateFormat(
        "dd MMM yyyy HH:mm",
        Locale.getDefault()
    ).format(Date(time))
}