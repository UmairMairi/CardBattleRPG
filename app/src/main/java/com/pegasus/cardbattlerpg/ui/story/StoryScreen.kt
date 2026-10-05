package com.pegasus.cardbattlerpg.ui.story

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.pegasus.cardbattlerpg.entity.StoryStageEntity
import com.pegasus.cardbattlerpg.repository.GameRepository

@Composable
fun drawableIdByName(name: String): Int {
    val context = LocalContext.current
    return remember(name) {
        context.resources.getIdentifier(name, "drawable", context.packageName)
    }
}

@Composable
fun StoryScreen(
    repository: GameRepository,
    onBack: () -> Unit,
    onOpenStage: (Int) -> Unit
) {
    var stages by remember { mutableStateOf<List<StoryStageEntity>>(emptyList()) }
    var showIntro by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        stages = repository.getStoryStages()
    }

    val currentStage = stages.firstOrNull { it.unlocked && !it.completed }
        ?: stages.firstOrNull { it.unlocked }
        ?: stages.firstOrNull()

    if (showIntro) {
        StoryIntroScreen(
            stage = currentStage,
            onBack = onBack,
            onStart = { showIntro = false }
        )
    } else {
        StoryStageListScreen(
            stages = stages,
            onBack = onBack,
            onOpenStage = onOpenStage
        )
    }
}

@Composable
fun StoryIntroScreen(
    stage: StoryStageEntity?,
    onBack: () -> Unit,
    onStart: () -> Unit
) {
    val bgName = stage?.backgroundImage ?: "story_intro_bg"
    val bgRes = drawableIdByName(bgName)
    val safeBgRes = if (bgRes != 0) bgRes else R.drawable.story_intro_bg

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = safeBgRes),
            contentDescription = "Story Intro Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        PremiumStoryOverlay()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            StoryTopSmallBar(
                title = "Story Campaign",
                subtitle = "Chapter ${stage?.chapter ?: 1} • ${stage?.title ?: "Awakening Forest"}",
                onBack = onBack
            )

            Spacer(modifier = Modifier.weight(1f))

            IntroChapterCard(
                stage = stage,
                onStart = onStart
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun BoxScope.PremiumStoryOverlay() {
    val infinite = rememberInfiniteTransition(label = "story_overlay")
    val pulse by infinite.animateFloat(
        initialValue = 0.30f,
        targetValue = 0.62f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "overlay_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xE6030006),
                        Color(0x9B160026),
                        Color(0xF8050009)
                    )
                )
            )
    )

    Box(
        modifier = Modifier
            .size(320.dp)
            .align(Alignment.TopEnd)
            .offset(x = 125.dp, y = (-105).dp)
            .alpha(0.18f * pulse)
            .blur(34.dp)
            .background(
                Brush.radialGradient(
                    listOf(
                        Color(0xFFFFD66B),
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
            .offset(x = (-130).dp, y = 80.dp)
            .alpha(0.20f * pulse)
            .blur(38.dp)
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
}

@Composable
fun StoryTopSmallBar(
    title: String,
    subtitle: String,
    onBack: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color(0xFFB56CFF),
                spotColor = Color(0xFFFFD66B)
            ),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(
                    Color.White.copy(alpha = 0.18f),
                    Color(0xFFFFD66B).copy(alpha = 0.52f),
                    Color.White.copy(alpha = 0.10f)
                )
            )
        ),
        colors = CardDefaults.cardColors(containerColor = Color(0xDD12001F))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF13001D),
                            Color(0xFF4A1478),
                            Color(0xFF100018)
                        )
                    )
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.height(40.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFFFD66B).copy(alpha = 0.58f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White,
                    containerColor = Color.Black.copy(alpha = 0.16f)
                ),
                contentPadding = PaddingValues(horizontal = 14.dp)
            ) {
                Text("Back", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = subtitle,
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

@Composable
fun IntroChapterCard(
    stage: StoryStageEntity?,
    onStart: () -> Unit
) {
    val difficulty = stage?.difficulty ?: "NORMAL"
    val typeColor = storyDifficultyColor(difficulty = difficulty, isBoss = stage?.isBoss == true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 28.dp,
                shape = RoundedCornerShape(36.dp),
                ambientColor = typeColor,
                spotColor = Color(0xFFFFD66B)
            ),
        shape = RoundedCornerShape(36.dp),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(
                    Color.White.copy(alpha = 0.18f),
                    typeColor.copy(alpha = 0.72f),
                    Color(0xFFFFD66B).copy(alpha = 0.32f)
                )
            )
        ),
        colors = CardDefaults.cardColors(containerColor = Color(0xEA160021))
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x88240042),
                            Color(0xEE100018),
                            Color(0xF8050009)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            StoryPill(
                text = "CHAPTER ${stage?.chapter ?: 1}",
                color = Color(0xFFFFD66B)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stage?.title ?: "Awakening Forest",
                color = Color.White,
                fontSize = 31.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 35.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StoryInfoChip("DIFFICULTY", difficulty, typeColor)
                StoryInfoChip("ENERGY", "${stage?.energyCost ?: 5}", Color(0xFF4FC3F7))
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stage?.storyText
                    ?: "Di hutan kuno yang tertutup kabut ungu, sebuah kartu roh terbangun dari tidur panjangnya.",
                color = Color(0xFFE9D9FF),
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(18.dp))

            StoryPowerBox(
                recommendedPower = stage?.recommendedPower ?: 0,
                rewardGold = stage?.rewardGold ?: 0,
                rewardExp = stage?.rewardExp ?: 0,
                rewardDiamond = stage?.rewardDiamond ?: 0
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD66B),
                    contentColor = Color(0xFF160021)
                )
            ) {
                Text(
                    text = "Mulai Story",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun StoryPowerBox(
    recommendedPower: Int,
    rewardGold: Int,
    rewardExp: Int,
    rewardDiamond: Int
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, Color(0xFFFFD66B).copy(alpha = 0.24f)),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.24f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Recommended Power: $recommendedPower",
                color = Color(0xFFFFD66B),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Reward: $rewardGold Gold • $rewardExp EXP • $rewardDiamond Diamond",
                color = Color(0xFFE9D9FF),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun StoryStageListScreen(
    stages: List<StoryStageEntity>,
    onBack: () -> Unit,
    onOpenStage: (Int) -> Unit
) {
    val selectedStage = stages.firstOrNull { it.unlocked && !it.completed }
        ?: stages.firstOrNull { it.unlocked }
        ?: stages.firstOrNull()

    val bgName = selectedStage?.backgroundImage ?: "story_chapter_1"
    val bgRes = drawableIdByName(bgName)
    val safeBgRes = if (bgRes != 0) bgRes else R.drawable.story_chapter_1

    val completedCount = stages.count { it.completed }
    val unlockedCount = stages.count { it.unlocked }
    val progress = if (stages.isEmpty()) 0f else completedCount.toFloat() / stages.size.toFloat()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = safeBgRes),
            contentDescription = "Story Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        PremiumStoryOverlay()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            StoryCampaignHeader(
                selectedStage = selectedStage,
                completedCount = completedCount,
                unlockedCount = unlockedCount,
                totalCount = stages.size,
                progress = progress,
                onBack = onBack
            )

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 22.dp)
            ) {
                items(stages, key = { it.id }) { stage ->
                    EpicStoryStageCard(
                        stage = stage,
                        onClick = {
                            if (stage.unlocked) {
                                onOpenStage(stage.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StoryCampaignHeader(
    selectedStage: StoryStageEntity?,
    completedCount: Int,
    unlockedCount: Int,
    totalCount: Int,
    progress: Float,
    onBack: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(30.dp),
                ambientColor = Color(0xFFB56CFF),
                spotColor = Color(0xFFFFD66B)
            ),
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(
                    Color.White.copy(alpha = 0.18f),
                    Color(0xFFFFD66B).copy(alpha = 0.52f),
                    Color.White.copy(alpha = 0.10f)
                )
            )
        ),
        colors = CardDefaults.cardColors(containerColor = Color(0xE812001F))
    ) {
        Column(
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
                    border = BorderStroke(1.dp, Color(0xFFFFD66B).copy(alpha = 0.58f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White,
                        containerColor = Color.Black.copy(alpha = 0.16f)
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp)
                ) {
                    Text("Back", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Story Campaign",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "Chapter ${selectedStage?.chapter ?: 1} • ${selectedStage?.title ?: "Awakening Forest"}",
                        color = Color(0xFFFFD66B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StoryInfoChip("DONE", "$completedCount/$totalCount", Color(0xFF6CFF9B))
                StoryInfoChip("OPEN", "$unlockedCount", Color(0xFFFFD66B))
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(9.dp)
                    .clip(RoundedCornerShape(50.dp)),
                color = Color(0xFFFFD66B),
                trackColor = Color.White.copy(alpha = 0.16f)
            )
        }
    }
}

@Composable
fun EpicStoryStageCard(
    stage: StoryStageEntity,
    onClick: () -> Unit
) {
    val statusText = when {
        stage.completed -> "COMPLETED"
        stage.unlocked -> "UNLOCKED"
        else -> "LOCKED"
    }

    val statusColor = when {
        stage.completed -> Color(0xFF6CFF9B)
        stage.unlocked -> Color(0xFFFFD66B)
        else -> Color(0xFF9CA3AF)
    }

    val typeText = when {
        stage.isBoss -> "BOSS"
        stage.isElite -> "ELITE"
        stage.isSecretStage -> "SECRET"
        else -> stage.difficulty.uppercase()
    }

    val typeColor = storyDifficultyColor(stage.difficulty, stage.isBoss, stage.isElite, stage.isSecretStage)

    val imageName = when {
        stage.isBoss && stage.bossImage.isNotBlank() -> stage.bossImage
        else -> stage.stageImage.ifBlank { "story_stage_1" }
    }

    val imageRes = drawableIdByName(imageName)
    val safeImageRes = if (imageRes != 0) imageRes else R.drawable.story_stage_1

    val infinite = rememberInfiniteTransition(label = "stage_card_${stage.id}")
    val glow by infinite.animateFloat(
        initialValue = 0.42f,
        targetValue = 0.90f,
        animationSpec = infiniteRepeatable(
            animation = tween(1700, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "stage_glow"
    )

    val lockedAlpha by animateFloatAsState(
        targetValue = if (stage.unlocked) 1f else 0.58f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "locked_alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (stage.isBoss) 232.dp else 214.dp)
            .graphicsLayer {
                alpha = lockedAlpha
            }
            .shadow(
                elevation = when {
                    !stage.unlocked -> 6.dp
                    stage.isBoss -> 28.dp
                    else -> 20.dp
                },
                shape = RoundedCornerShape(32.dp),
                ambientColor = if (stage.unlocked) typeColor.copy(alpha = glow) else Color.Black,
                spotColor = if (stage.unlocked) typeColor.copy(alpha = glow) else Color.Black
            )
            .clickable {
                if (stage.unlocked) onClick()
            },
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(
                    Color.White.copy(alpha = if (stage.unlocked) 0.16f else 0.06f),
                    statusColor.copy(alpha = if (stage.unlocked) 0.64f else 0.22f),
                    Color.White.copy(alpha = if (stage.unlocked) 0.10f else 0.04f)
                )
            )
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (stage.unlocked) Color(0xEE180026) else Color(0xEE09090B)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = safeImageRes),
                contentDescription = stage.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                if (stage.unlocked) Color(0xF608000F) else Color(0xF2070707),
                                if (stage.unlocked) Color(0xCC160026) else Color(0xDD101010),
                                Color.Transparent
                            )
                        )
                    )
            )

            if (!stage.unlocked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.34f))
                )
            }

            if (stage.unlocked) {
                Box(
                    modifier = Modifier
                        .size(if (stage.isBoss) 190.dp else 155.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 64.dp, y = (-62).dp)
                        .alpha(if (stage.completed) 0.16f else 0.24f)
                        .blur(18.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(typeColor, Color.Transparent)
                            ),
                            CircleShape
                        )
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StoryStatusBadge(
                    text = statusText,
                    color = statusColor,
                    locked = !stage.unlocked
                )

                StoryStatusBadge(
                    text = typeText,
                    color = typeColor,
                    locked = !stage.unlocked
                )
            }

            if (!stage.unlocked) {
                LockedStamp(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                )
            } else if (stage.completed) {
                CompletedStamp(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, top = 58.dp, end = 14.dp, bottom = 14.dp)
            ) {
                Text(
                    text = "Chapter ${stage.chapter}-${stage.stage}",
                    color = Color(0xFFD7C3F2),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stage.title,
                    color = Color.White,
                    fontSize = if (stage.isBoss) 21.sp else 20.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    lineHeight = 24.sp,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(7.dp))

                Text(
                    text = "Enemy: ${stage.enemyName}",
                    color = Color(0xFFE9D9FF),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    MiniStoryBadge("POWER", "${stage.recommendedPower}", Color(0xFFFFD66B), Modifier.weight(1f))
                    MiniStoryBadge("ENERGY", "${stage.energyCost}", Color(0xFF4FC3F7), Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(7.dp))

                Text(
                    text = "Reward: ${stage.rewardGold} Gold • ${stage.rewardExp} EXP • ${stage.rewardDiamond} Diamond",
                    color = if (stage.unlocked) Color(0xFFFFD66B) else Color(0xFFBDBDBD),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    lineHeight = 15.sp,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.weight(1f))

                StageActionStrip(
                    unlocked = stage.unlocked,
                    completed = stage.completed,
                    isBoss = stage.isBoss
                )
            }
        }
    }
}

@Composable
fun StoryStatusBadge(
    text: String,
    color: Color,
    locked: Boolean
) {
    Surface(
        color = if (locked) Color.Black.copy(alpha = 0.56f) else color.copy(alpha = 0.20f),
        shape = RoundedCornerShape(50.dp),
        border = BorderStroke(1.dp, color.copy(alpha = if (locked) 0.30f else 0.66f))
    ) {
        Text(
            text = text,
            color = if (locked) Color(0xFFE5E7EB) else color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun LockedStamp(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color.Black.copy(alpha = 0.62f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.24f))
    ) {
        Text(
            text = "🔒",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
        )
    }
}

@Composable
fun CompletedStamp(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color(0x226CFF9B),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0x666CFF9B))
    ) {
        Text(
            text = "✓",
            color = Color(0xFF6CFF9B),
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
        )
    }
}

@Composable
fun MiniStoryBadge(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.13f),
        shape = RoundedCornerShape(50.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.28f))
    ) {
        Text(
            text = "$label $value",
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun StageActionStrip(
    unlocked: Boolean,
    completed: Boolean,
    isBoss: Boolean
) {
    val text = when {
        !unlocked -> "LOCKED • Selesaikan stage sebelumnya"
        completed -> "COMPLETED • Tap untuk bermain lagi"
        isBoss -> "BOSS READY • Tap untuk battle"
        else -> "UNLOCKED • Tap untuk mulai"
    }

    val color = when {
        !unlocked -> Color(0xFF9CA3AF)
        completed -> Color(0xFF6CFF9B)
        isBoss -> Color(0xFFFF5252)
        else -> Color(0xFFFFD66B)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = color.copy(alpha = if (unlocked) 0.16f else 0.10f),
        shape = RoundedCornerShape(50.dp),
        border = BorderStroke(1.dp, color.copy(alpha = if (unlocked) 0.42f else 0.22f))
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
        )
    }
}

@Composable
fun StoryInfoChip(
    label: String,
    value: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.14f),
        shape = RoundedCornerShape(50.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.34f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.76f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(5.dp))

            Text(
                text = value,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun StoryPill(
    text: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.14f),
        shape = RoundedCornerShape(50.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.42f))
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

fun storyDifficultyColor(
    difficulty: String,
    isBoss: Boolean = false,
    isElite: Boolean = false,
    isSecretStage: Boolean = false
): Color {
    return when {
        isBoss -> Color(0xFFFF5252)
        isElite -> Color(0xFFFFD66B)
        isSecretStage -> Color(0xFFB56CFF)
        difficulty.equals("HARD", ignoreCase = true) -> Color(0xFFFF8A50)
        difficulty.equals("NORMAL", ignoreCase = true) -> Color(0xFF4FC3F7)
        difficulty.equals("EASY", ignoreCase = true) -> Color(0xFF6CFF9B)
        else -> Color(0xFF82D8FF)
    }
}
