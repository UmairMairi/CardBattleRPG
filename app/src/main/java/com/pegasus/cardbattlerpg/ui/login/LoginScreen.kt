package com.pegasus.cardbattlerpg.ui.login

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pegasus.cardbattlerpg.R
import com.pegasus.cardbattlerpg.online.FirebaseOnlineManager
import com.pegasus.cardbattlerpg.repository.GameRepository
import com.pegasus.cardbattlerpg.utils.ContentFilter
import com.pegasus.cardbattlerpg.utils.LegalLinks
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    repository: GameRepository,
    onLoginSuccess: () -> Unit
) {
    var playerName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var mode by remember { mutableStateOf("register") }
    val scope = rememberCoroutineScope()
    val firebase = remember { FirebaseOnlineManager() }
    val context = LocalContext.current
    var acceptedTerms by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "login_anim")
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.22f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    suspend fun finishLocalPlayer(name: String) {
        repository.createGuestPlayer(name.ifBlank { "Guest Player" })
        onLoginSuccess()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.login_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xE8050009), Color(0xAA17002B), Color(0xEF06000C))
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopEnd)
                .offset(x = 120.dp, y = (-110).dp)
                .alpha(0.22f)
                .background(Brush.radialGradient(listOf(Color(0xFFFFD66B), Color.Transparent)), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(380.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-160).dp, y = 150.dp)
                .alpha(0.24f)
                .background(Brush.radialGradient(listOf(Color(0xFFB56CFF), Color.Transparent)), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .size(132.dp)
                    .scale(logoScale)
                    .shadow(18.dp, CircleShape, ambientColor = Color(0x55FFD66B), spotColor = Color(0x44B56CFF))
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFFFD66B).copy(alpha = glowAlpha), Color(0x884B1478), Color(0xFF12001F))
                        ),
                        CircleShape
                    )
                    .border(1.dp, Color(0xCCFFD66B), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.splash_logo),
                    contentDescription = null,
                    modifier = Modifier.size(102.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("SPIRIT CARD", color = Color.White, fontSize = 35.sp, fontWeight = FontWeight.Black)
            Text("Online Battle RPG Adventure", color = Color(0xFFFFD66B), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(18.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(18.dp, RoundedCornerShape(36.dp), ambientColor = Color(0x44B56CFF), spotColor = Color(0x33FFD66B)),
                shape = RoundedCornerShape(36.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.18f), Color(0xFFFFD66B), Color(0xFFB56CFF)))
                ),
                colors = CardDefaults.cardColors(containerColor = Color(0xDD12001F))
            ) {
                Column(
                    modifier = Modifier
                        .background(Brush.verticalGradient(listOf(Color(0xEE2A063F), Color(0xD90A0012))))
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (mode == "login") "Login Summoner" else "Create Online Account",
                        fontSize = 24.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Akun tersimpan online • Ranking, Guild, Arena realtime",
                        color = Color(0xFFE9D9FF),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        ModeChip("REGISTER", mode == "register", Modifier.weight(1f)) { mode = "register"; message = "" }
                        ModeChip("LOGIN", mode == "login", Modifier.weight(1f)) { mode = "login"; message = "" }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (mode == "register") {
                        PremiumLoginField(
                            value = playerName,
                            onValueChange = { playerName = it; message = "" },
                            label = "Nama Player",
                            iconRes = R.drawable.ic_profile
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    PremiumLoginField(
                        value = email,
                        onValueChange = { email = it; message = "" },
                        label = "Email",
                        iconRes = R.drawable.ic_profile
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    PremiumLoginField(
                        value = password,
                        onValueChange = { password = it; message = "" },
                        label = "Password",
                        iconRes = R.drawable.ic_profile,
                        isPassword = true
                    )

                    if (message.isNotBlank()) {
                        Spacer(modifier = Modifier.height(9.dp))
                        Text(
                            text = message,
                            color = if (message.contains("berhasil", true)) Color(0xFF6CFF9B) else Color(0xFFFF7777),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TermsConsentRow(
                        accepted = acceptedTerms,
                        onAcceptedChange = { acceptedTerms = it; message = "" },
                        onOpenTerms = { LegalLinks.open(context, LegalLinks.TERMS_OF_USE) },
                        onOpenPrivacy = { LegalLinks.open(context, LegalLinks.PRIVACY_POLICY) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (loading) return@Button
                            scope.launch {
                                loading = true
                                try {
                                    if (mode == "register") {
                                        val nameError = ContentFilter.validateName(playerName, "Nama player")
                                        if (nameError != null) {
                                            message = nameError
                                        } else if (!acceptedTerms) {
                                            message = "Setujui Terms of Use & Privacy Policy terlebih dahulu"
                                        } else {
                                            firebase.registerWithEmail(playerName, email, password)
                                            finishLocalPlayer(playerName)
                                        }
                                    } else {
                                        firebase.loginWithEmail(email, password)
                                        finishLocalPlayer(email.substringBefore("@").ifBlank { "Online Player" })
                                    }
                                } catch (e: Exception) {
                                    message = e.localizedMessage ?: "Login online gagal"
                                } finally {
                                    loading = false
                                }
                            }
                        },
                        enabled = !loading,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD66B), contentColor = Color(0xFF160021))
                    ) {
                        Text(
                            text = when {
                                loading -> "PLEASE WAIT..."
                                mode == "login" -> "LOGIN ONLINE"
                                else -> "REGISTER ONLINE"
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            if (loading) return@OutlinedButton
                            scope.launch {
                                loading = true
                                try {
                                    val name = playerName.ifBlank { "Guest Player" }
                                    val nameError = ContentFilter.validateName(name, "Nama player")
                                    if (nameError != null) {
                                        message = nameError
                                        return@launch
                                    }
                                    if (!acceptedTerms) {
                                        message = "Setujui Terms of Use & Privacy Policy terlebih dahulu"
                                        return@launch
                                    }
                                    firebase.loginAsGuest(name)
                                    finishLocalPlayer(name)
                                } catch (e: Exception) {
                                    message = e.localizedMessage ?: "Guest online gagal"
                                } finally {
                                    loading = false
                                }
                            }
                        },
                        enabled = !loading,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(18.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD66B).copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("PLAY AS GUEST ONLINE", fontSize = 13.sp, fontWeight = FontWeight.Black)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Cloud Save • Realtime Arena • Realtime Guild • Online Ranking",
                        color = Color(0xFFFFD66B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun ModeChip(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFFFFD66B) else Color.White.copy(alpha = 0.10f),
            contentColor = if (selected) Color(0xFF160021) else Color.White
        ),
        contentPadding = PaddingValues(horizontal = 6.dp)
    ) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun PremiumLoginField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    iconRes: Int,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        leadingIcon = {
            Box(modifier = Modifier.size(38.dp), contentAlignment = Alignment.Center) {
                Image(painter = painterResource(iconRes), contentDescription = null, modifier = Modifier.size(24.dp), contentScale = ContentScale.Fit)
            }
        },
        label = { Text(label, color = Color.White.copy(alpha = 0.85f)) },
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        textStyle = TextStyle(color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold),
        modifier = Modifier.fillMaxWidth().height(62.dp),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = Color(0xFFFFD66B),
            unfocusedLabelColor = Color.White.copy(alpha = 0.75f),
            cursorColor = Color(0xFFFFD66B),
            focusedBorderColor = Color(0xFFFFD66B),
            unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
            focusedContainerColor = Color.Black.copy(alpha = 0.22f),
            unfocusedContainerColor = Color.Black.copy(alpha = 0.18f)
        )
    )
}

@Composable
fun TermsConsentRow(
    accepted: Boolean,
    onAcceptedChange: (Boolean) -> Unit,
    onOpenTerms: () -> Unit,
    onOpenPrivacy: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = accepted,
                onCheckedChange = onAcceptedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFFFFD66B),
                    uncheckedColor = Color.White.copy(alpha = 0.7f),
                    checkmarkColor = Color(0xFF160021)
                )
            )
            Text(
                text = "Saya berusia 13+ dan menyetujui Terms of Use (termasuk aturan chat: tanpa kata kasar, pelecehan, spam) serta Privacy Policy.",
                color = Color(0xFFE9D9FF),
                fontSize = 11.sp
            )
        }
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onOpenTerms) {
                Text("Terms of Use", color = Color(0xFFFFD66B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onOpenPrivacy) {
                Text("Privacy Policy", color = Color(0xFFFFD66B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
