package com.pegasus.cardbattlerpg.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.pegasus.cardbattlerpg.R
import com.pegasus.cardbattlerpg.repository.GameRepository
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    repository: GameRepository,
    onGoLogin: () -> Unit,
    onGoMenu: () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "splash_premium")

    val logoScale by infinite.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )

    val glowAlpha by infinite.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.90f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val runeRotate by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rune_rotate"
    )

    val reverseRuneRotate by infinite.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(13000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "reverse_rune_rotate"
    )

    val shimmerX by infinite.animateFloat(
        initialValue = -260f,
        targetValue = 260f,
        animationSpec = infiniteRepeatable(
            animation = tween(2100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(2200, easing = EaseOutCubic),
        label = "progress"
    )

    LaunchedEffect(Unit) {
        repository.seedInitialData()
        delay(2300)

        val player = repository.getPlayer()
        if (player == null) onGoLogin() else onGoMenu()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.splash_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        SplashPremiumOverlay()

        SplashMagicParticles()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SplashLogoOrb(
                logoScale = logoScale,
                glowAlpha = glowAlpha,
                runeRotate = runeRotate,
                reverseRuneRotate = reverseRuneRotate
            )

            Spacer(modifier = Modifier.height(34.dp))

            Text(
                text = "SPIRIT CARD",
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Premium Card Battle RPG",
                color = Color(0xFFFFD66B),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(34.dp))

            SplashLoadingBar(
                progress = progress,
                shimmerX = shimmerX
            )

            Spacer(modifier = Modifier.height(16.dp))

            CircularProgressIndicator(
                color = Color(0xFFFFD66B),
                strokeWidth = 3.dp,
                modifier = Modifier.size(34.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Loading adventure...",
                color = Color(0xFFEBD9FF),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
                .background(
                    Color.White.copy(alpha = 0.08f),
                    RoundedCornerShape(50.dp)
                )
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.12f),
                    RoundedCornerShape(50.dp)
                )
                .padding(horizontal = 18.dp, vertical = 8.dp)
        ) {
            Text(
                text = "DAP Studio",
                color = Color.White.copy(alpha = 0.84f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SplashPremiumOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xF7050009),
                        Color(0xCC230039),
                        Color(0xAA451176),
                        Color(0xFA050009)
                    )
                )
            )
    )

    Box(
        modifier = Modifier
            .size(360.dp)
            .offset(x = 170.dp, y = (-110).dp)
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

    Box(
        modifier = Modifier
            .size(320.dp)
            .offset(x = (-120).dp, y = 560.dp)
            .alpha(0.28f)
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
fun SplashLogoOrb(
    logoScale: Float,
    glowAlpha: Float,
    runeRotate: Float,
    reverseRuneRotate: Float
) {
    Box(
        modifier = Modifier
            .size(236.dp)
            .scale(logoScale),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(236.dp)
                .alpha(glowAlpha)
                .blur(18.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFFFFD66B),
                            Color(0x994B1478),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(224.dp)
                .rotate(runeRotate)
                .border(
                    2.dp,
                    Color(0xFFFFD66B).copy(alpha = 0.58f),
                    CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(188.dp)
                .rotate(reverseRuneRotate)
                .border(
                    1.dp,
                    Color(0xFFB56CFF).copy(alpha = 0.62f),
                    CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(180.dp)
                .shadow(
                    elevation = 36.dp,
                    shape = CircleShape,
                    ambientColor = Color(0xFFFFD66B),
                    spotColor = Color(0xFFB56CFF)
                )
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFFFFD66B).copy(alpha = 0.72f),
                            Color(0xFF4A1375).copy(alpha = 0.72f),
                            Color(0xFF090011)
                        )
                    ),
                    CircleShape
                )
                .border(
                    2.dp,
                    Color(0xFFFFD66B),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.splash_logo),
                contentDescription = "Splash Logo",
                modifier = Modifier.size(132.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun SplashLoadingBar(
    progress: Float,
    shimmerX: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(14.dp)
            .background(
                Color.Black.copy(alpha = 0.40f),
                RoundedCornerShape(50.dp)
            )
            .border(
                1.dp,
                Color.White.copy(alpha = 0.14f),
                RoundedCornerShape(50.dp)
            )
            .clip(RoundedCornerShape(50.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(14.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFFFFD66B),
                            Color(0xFFFF8A00),
                            Color(0xFFB56CFF)
                        )
                    ),
                    RoundedCornerShape(50.dp)
                )
        )

        Box(
            modifier = Modifier
                .offset(x = shimmerX.dp)
                .width(80.dp)
                .height(14.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.55f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun SplashMagicParticles() {
    Box(modifier = Modifier.fillMaxSize()) {
        SplashParticle(40.dp, 110.dp, 8.dp, 0.48f)
        SplashParticle(88.dp, 210.dp, 5.dp, 0.35f)
        SplashParticle(280.dp, 150.dp, 7.dp, 0.42f)
        SplashParticle(330.dp, 330.dp, 4.dp, 0.34f)
        SplashParticle(52.dp, 520.dp, 6.dp, 0.36f)
        SplashParticle(300.dp, 610.dp, 9.dp, 0.42f)
        SplashParticle(210.dp, 720.dp, 5.dp, 0.30f)
    }
}

@Composable
fun SplashParticle(
    x: Dp,
    y: Dp,
    size: Dp,
    alpha: Float
) {
    Box(
        modifier = Modifier
            .offset(x = x, y = y)
            .size(size)
            .alpha(alpha)
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
}