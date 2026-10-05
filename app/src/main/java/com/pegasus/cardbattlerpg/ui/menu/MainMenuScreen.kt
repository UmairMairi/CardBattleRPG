package com.pegasus.cardbattlerpg.ui.menu

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.pegasus.cardbattlerpg.R
import com.pegasus.cardbattlerpg.entity.PlayerEntity
import com.pegasus.cardbattlerpg.repository.GameRepository
import kotlinx.coroutines.delay
import kotlin.math.abs

data class GameMenuItem(
    val title: String,
    val imageRes: Int
)

data class HomeBannerItem(
    val imageRes: Int,
    val url: String
)

@Composable
fun MainMenuScreen(
    repository: GameRepository,
    onOpenCollection: () -> Unit,
    onOpenDeck: () -> Unit,
    onOpenHero: () -> Unit,
    onOpenEquipment: () -> Unit,
    onOpenBattle: () -> Unit,
    onOpenStory: () -> Unit,
    onOpenSummon: () -> Unit,
    onOpenInventory: () -> Unit,
    onOpenMission: () -> Unit,
    onOpenAchievement: () -> Unit,
    onOpenSaveGame: () -> Unit,
    onOpenRanking: () -> Unit,
    onOpenArena: () -> Unit,
    onOpenShop: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenGuild: () -> Unit
) {
    var player by remember { mutableStateOf<PlayerEntity?>(null) }

    LaunchedEffect(Unit) {
        player = repository.getPlayer()
    }

    val menus = listOf(
        GameMenuItem("Story", R.drawable.menu_story),
        GameMenuItem("Deck", R.drawable.menu_deck),
        GameMenuItem("Collection", R.drawable.menu_collection),
        GameMenuItem("Battle", R.drawable.menu_battle),
        GameMenuItem("Summon", R.drawable.menu_summon),
        GameMenuItem("Shop", R.drawable.menu_shop),
        GameMenuItem("Inventory", R.drawable.menu_inventory),
        GameMenuItem("Hero", R.drawable.menu_hero),
        GameMenuItem("Equipment", R.drawable.menu_equipment),
        GameMenuItem("Mission", R.drawable.menu_mission),
        GameMenuItem("Achievement", R.drawable.menu_achievement),
        GameMenuItem("Save", R.drawable.menu_save),
        GameMenuItem("Ranking", R.drawable.menu_ranking),
        GameMenuItem("Arena", R.drawable.menu_arena),
        GameMenuItem("Profile", R.drawable.menu_profile),
        GameMenuItem("Guild", R.drawable.menu_guild)
    )

    val banners = listOf(
        HomeBannerItem(R.drawable.banner_event_1, "https://cardbattlerpg.online/"),
        HomeBannerItem(R.drawable.banner_event_2, "https://cardbattlerpg.online/"),
        HomeBannerItem(R.drawable.banner_event_3, "https://cardbattlerpg.online/"),
        HomeBannerItem(R.drawable.banner_event_4, "https://cardbattlerpg.online/")
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.menu_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        MenuPremiumOverlay()

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 18.dp, bottom = 28.dp)
        ) {
            item(span = { GridItemSpan(2) }) {
                PremiumHeaderCard(player = player)
            }

            item(span = { GridItemSpan(2) }) {
                HomeBannerSlider(banners = banners)
            }

            items(menus) { menu ->
                PremiumMenuCard(
                    item = menu,
                    onClick = {
                        when (menu.title) {
                            "Story" -> onOpenStory()
                            "Collection" -> onOpenCollection()
                            "Deck" -> onOpenDeck()
                            "Hero" -> onOpenHero()
                            "Equipment" -> onOpenEquipment()
                            "Battle" -> onOpenBattle()
                            "Summon" -> onOpenSummon()
                            "Inventory" -> onOpenInventory()
                            "Mission" -> onOpenMission()
                            "Achievement" -> onOpenAchievement()
                            "Save" -> onOpenSaveGame()
                            "Ranking" -> onOpenRanking()
                            "Arena" -> onOpenArena()
                            "Shop" -> onOpenShop()
                            "Profile" -> onOpenProfile()
                            "Guild" -> onOpenGuild()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun HomeBannerSlider(
    banners: List<HomeBannerItem>
) {
    val uriHandler = LocalUriHandler.current
    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(banners.size) {
        while (banners.isNotEmpty()) {
            delay(3500)
            currentIndex = (currentIndex + 1) % banners.size
        }
    }

    val animatedIndex by animateFloatAsState(
        targetValue = currentIndex.toFloat(),
        animationSpec = tween(
            durationMillis = 650,
            easing = FastOutSlowInEasing
        ),
        label = "banner_slide"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(108.dp)
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color(0x44FFD66B),
                spotColor = Color(0x44B56CFF)
            )
            .clickable {
                banners.getOrNull(currentIndex)?.let {
                    uriHandler.openUri(it.url)
                }
            },
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color(0x66FFD66B)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF050009))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF050009))
        ) {
            banners.forEachIndexed { index, banner ->
                val offsetFraction = index - animatedIndex
                val distance = abs(offsetFraction)
                val alpha = (1f - distance * 0.28f).coerceIn(0f, 1f)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX = offsetFraction * size.width
                            this.alpha = alpha
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = banner.imageRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(26.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.35f)
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 7.dp)
                    .background(
                        Color.Black.copy(alpha = 0.35f),
                        RoundedCornerShape(50.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                banners.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .width(if (index == currentIndex) 22.dp else 8.dp)
                            .height(6.dp)
                            .background(
                                if (index == currentIndex) Color(0xFFFFD66B)
                                else Color.White.copy(alpha = 0.38f),
                                RoundedCornerShape(50.dp)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun MenuPremiumOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xE8050009),
                        Color(0xAA1B0030),
                        Color(0xE8050009)
                    )
                )
            )
    )

    Box(
        modifier = Modifier
            .size(330.dp)
            .offset(x = 180.dp, y = (-120).dp)
            .alpha(0.28f)
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFFFFD66B), Color.Transparent)
                ),
                CircleShape
            )
    )

    Box(
        modifier = Modifier
            .size(360.dp)
            .offset(x = (-150).dp, y = 520.dp)
            .alpha(0.30f)
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFFB56CFF), Color.Transparent)
                ),
                CircleShape
            )
    )
}

@Composable
fun PremiumHeaderCard(player: PlayerEntity?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(118.dp)
            .shadow(
                elevation = 18.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = Color(0x55FFD66B),
                spotColor = Color(0x44B56CFF)
            ),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.dp, Color(0x66FFD66B)),
        colors = CardDefaults.cardColors(containerColor = Color(0xDD210536))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF2A063F),
                            Color(0xFF4A1575),
                            Color(0xFF170024)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .shadow(16.dp, CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFFFFD66B),
                                    Color(0xFF7B2BBF),
                                    Color(0xFF140020)
                                )
                            ),
                            CircleShape
                        )
                        .border(1.dp, Color(0xFFFFD66B), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.splash_logo),
                        contentDescription = null,
                        modifier = Modifier.size(58.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Spirit Card RPG",
                        fontSize = 24.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "Welcome, ${player?.name ?: "Player"}",
                        color = Color(0xFFFFD66B),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row {
                        Text(
                            text = "Gold ${player?.gold ?: 0}",
                            color = Color(0xFFFFE08A),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "  •  ",
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 11.sp
                        )

                        Text(
                            text = "Diamond ${player?.diamond ?: 0}",
                            color = Color(0xFF82D8FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumMenuCard(
    item: GameMenuItem,
    onClick: () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "menu_glow")
    val shineAlpha by infinite.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.42f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shine_alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(176.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color(0x44FFD66B),
                spotColor = Color(0x44B56CFF)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, Color(0x55FFD66B)),
        colors = CardDefaults.cardColors(containerColor = Color(0xEE1A052D))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = item.title,
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
                                Color(0x3312001F),
                                Color(0xF008000F)
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .size(110.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 34.dp, y = (-34).dp)
                    .alpha(shineAlpha)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFFFD66B), Color.Transparent)
                        ),
                        CircleShape
                    )
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(36.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFFFFD66B),
                                Color(0xFF7B2BBF)
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✦",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(78.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0xDD12001F),
                                Color(0xFF050009)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 14.dp, end = 14.dp, bottom = 14.dp)
            ) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Tap to open",
                    color = Color(0xFFFFD66B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 14.dp, bottom = 8.dp)
                    .width(76.dp)
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFFD66B), Color.Transparent)
                        ),
                        RoundedCornerShape(20.dp)
                    )
            )
        }
    }
}