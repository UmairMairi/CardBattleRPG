package com.pegasus.cardbattlerpg.ui.ranking

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.pegasus.cardbattlerpg.R
import com.pegasus.cardbattlerpg.model.RankingPlayer
import com.pegasus.cardbattlerpg.repository.GameRepository
import com.pegasus.cardbattlerpg.repository.RankingRepository
import kotlinx.coroutines.launch

@Composable
fun RankingScreen(
    gameRepository: GameRepository,
    rankingRepository: RankingRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var rankings by remember { mutableStateOf<List<RankingPlayer>>(emptyList()) }
    var message by remember { mutableStateOf("") }

    suspend fun reload() {
        rankings = rankingRepository.getTopRanking()
    }

    DisposableEffect(Unit) {
        val stop = rankingRepository.listenTopRanking { realtimeRows ->
            rankings = realtimeRows
        }
        onDispose { stop() }
    }

    LaunchedEffect(Unit) {
        reload()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.ranking_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        RankingPremiumBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            RankingHeader(
                message = message,
                onBack = onBack
            )

            Spacer(modifier = Modifier.height(14.dp))

            PremiumRankingButton(
                onClick = {
                    scope.launch {
                        val ranking = gameRepository.buildRankingPlayer()

                        if (ranking != null) {
                            message = rankingRepository.uploadRanking(
                                playerId = ranking.playerName.lowercase().replace(" ", "_"),
                                ranking = ranking
                            )
                            reload()
                        } else {
                            message = "Player belum tersedia"
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (rankings.isEmpty()) {
                EmptyRankingCard()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 28.dp)
                ) {
                    item {
                        RankingPodium(
                            rankings = rankings.take(3)
                        )
                    }

                    itemsIndexed(rankings) { index, player ->
                        PremiumRankingCard(
                            rank = index + 1,
                            player = player
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RankingPremiumBackground() {
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
fun RankingHeader(
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
                ),
                contentPadding = PaddingValues(horizontal = 14.dp)
            ) {
                Text("Back", fontSize = 12.sp, fontWeight = FontWeight.Black)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hall of Champions",
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Online Ranking • Power Leaderboard",
                    color = Color(0xFFFFD66B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                if (message.isNotBlank()) {
                    Text(
                        text = message,
                        color = Color(0xFFE9D9FF),
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
fun PremiumRankingButton(
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
            containerColor = Color(0xFFFFD66B),
            contentColor = Color(0xFF160021)
        )
    ) {
        Text(
            text = "Upload My Ranking",
            fontSize = 15.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun RankingPodium(
    rankings: List<RankingPlayer>
) {
    if (rankings.isEmpty()) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .shadow(24.dp, RoundedCornerShape(34.dp)),
        shape = RoundedCornerShape(34.dp),
        border = BorderStroke(1.dp, Color(0x66FFD66B)),
        colors = CardDefaults.cardColors(containerColor = Color(0xE812001F))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF1A002B),
                            Color(0xFF09000F)
                        )
                    )
                )
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            rankings.getOrNull(1)?.let {
                PodiumPlayer(
                    rank = 2,
                    player = it,
                    height = 116.dp,
                    color = Color(0xFFC0C0C0),
                    modifier = Modifier.weight(1f)
                )
            }

            rankings.getOrNull(0)?.let {
                PodiumPlayer(
                    rank = 1,
                    player = it,
                    height = 150.dp,
                    color = Color(0xFFFFD66B),
                    modifier = Modifier.weight(1f)
                )
            }

            rankings.getOrNull(2)?.let {
                PodiumPlayer(
                    rank = 3,
                    player = it,
                    height = 100.dp,
                    color = Color(0xFFCD7F32),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun PodiumPlayer(
    rank: Int,
    player: RankingPlayer,
    height: Dp,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = when (rank) {
                1 -> "♛"
                2 -> "◆"
                else -> "✦"
            },
            color = color,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black
        )

        Text(
            text = player.playerName,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "PWR ${player.power}",
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .height(height)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            color.copy(alpha = 0.90f),
                            color.copy(alpha = 0.18f),
                            Color(0xFF12001F)
                        )
                    ),
                    RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
                )
                .border(
                    1.dp,
                    color.copy(alpha = 0.60f),
                    RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#$rank",
                color = Color(0xFF160021),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}


@Composable
fun rankingDrawableIdByName(name: String): Int {
    val context = LocalContext.current
    return remember(name) {
        context.resources.getIdentifier(name, "drawable", context.packageName)
    }
}

@Composable
fun safeRankingHeroImage(player: RankingPlayer): Int {
    val res = rankingDrawableIdByName(player.heroImage)
    return if (res != 0) res else R.drawable.hero_unknown
}

@Composable
fun PremiumRankingCard(
    rank: Int,
    player: RankingPlayer
) {
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD66B)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> Color(0xFFB56CFF)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .shadow(
                elevation = if (rank <= 3) 22.dp else 12.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = rankColor,
                spotColor = rankColor
            ),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, rankColor.copy(alpha = 0.45f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xE812001F))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF07000D),
                            rankColor.copy(alpha = 0.25f),
                            Color(0xFF170024)
                        )
                    )
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(66.dp)
                    .shadow(14.dp, RoundedCornerShape(20.dp), ambientColor = rankColor, spotColor = rankColor)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                rankColor.copy(alpha = 0.55f),
                                Color(0xFF12001F)
                            )
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .border(1.dp, rankColor.copy(alpha = 0.55f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = safeRankingHeroImage(player)),
                    contentDescription = player.heroName,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp),
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = "#$rank",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.62f), RoundedCornerShape(50.dp))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.playerName,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${player.heroName} • Lv ${player.level} • Cards ${player.ownedCards}",
                    color = Color(0xFFE9D9FF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                RankingMiniStats(player)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "POWER",
                    color = rankColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = player.power.toString(),
                    color = Color(0xFFFFD66B),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun RankingMiniStats(
    player: RankingPlayer
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        RankingMiniBadge(
            text = "Gold ${player.gold}",
            color = Color(0xFFFFD66B)
        )

        RankingMiniBadge(
            text = "Dia ${player.diamond}",
            color = Color(0xFF7DE7FF)
        )
    }
}

@Composable
fun RankingMiniBadge(
    text: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.14f),
        shape = RoundedCornerShape(50.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.22f))
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun EmptyRankingCard() {
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
            Text("♛", color = Color(0xFFFFD66B), fontSize = 44.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(10.dp))
            Text("Belum ada ranking", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Text("Upload ranking Anda untuk masuk leaderboard.", color = Color(0xFFE9D9FF), fontSize = 12.sp)
        }
    }
}