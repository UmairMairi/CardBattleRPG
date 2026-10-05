package com.pegasus.cardbattlerpg.ui.profile

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.pegasus.cardbattlerpg.R
import com.pegasus.cardbattlerpg.entity.*
import com.pegasus.cardbattlerpg.repository.GameRepository
import com.pegasus.cardbattlerpg.online.AccountDeletionManager
import com.pegasus.cardbattlerpg.online.FirebaseOnlineManager
import com.pegasus.cardbattlerpg.online.ModerationManager
import com.pegasus.cardbattlerpg.utils.ContentFilter
import com.pegasus.cardbattlerpg.utils.LegalLinks
import kotlinx.coroutines.launch

@Composable
fun profileDrawableIdByName(name: String): Int {
    val context = LocalContext.current
    return remember(name) {
        context.resources.getIdentifier(name, "drawable", context.packageName)
    }
}

@Composable
fun ProfileImageByName(
    name: String,
    modifier: Modifier,
    fallback: Int = R.drawable.hero_unknown,
    contentScale: ContentScale = ContentScale.Fit
) {
    val res = profileDrawableIdByName(name)
    Image(
        painter = painterResource(if (res != 0) res else fallback),
        contentDescription = name,
        modifier = modifier,
        contentScale = contentScale
    )
}

@Composable
fun ProfileAvatarImage(
    avatar: String,
    modifier: Modifier
) {
    val context = LocalContext.current

    val bitmap = remember(avatar) {
        if (avatar.startsWith("content://")) {
            runCatching {
                context.contentResolver.openInputStream(Uri.parse(avatar))?.use {
                    BitmapFactory.decodeStream(it)
                }
            }.getOrNull()
        } else null
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Custom Avatar",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        ProfileImageByName(
            name = avatar,
            modifier = modifier,
            fallback = R.drawable.avatar_1,
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun ProfileScreen(
    repository: GameRepository,
    onBack: () -> Unit,
    onLogout: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val firebaseOnline = remember { FirebaseOnlineManager() }
    val accountDeletion = remember { AccountDeletionManager() }
    val moderation = remember { ModerationManager() }
    val context = LocalContext.current
    var blockedPlayers by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }

    var player by remember { mutableStateOf<PlayerEntity?>(null) }
    var selectedHero by remember { mutableStateOf<HeroEntity?>(null) }
    var ownedCards by remember { mutableStateOf<List<CardEntity>>(emptyList()) }

    var name by remember { mutableStateOf("") }
    var avatar by remember { mutableStateOf("avatar_1") }
    var message by remember { mutableStateOf("") }

    suspend fun reload() {
        player = repository.getPlayer()
        selectedHero = repository.getSelectedHero()
        ownedCards = repository.getOwnedCards()
        name = player?.name ?: ""
        avatar = player?.avatar ?: "avatar_1"
    }

    LaunchedEffect(Unit) {
        reload()
    }

    DisposableEffect(Unit) {
        val stop = moderation.listenBlocked { rows -> blockedPlayers = rows }
        onDispose { stop() }
    }

    if (showDeleteDialog) {
        DeleteAccountDialog(
            isEmailAccount = accountDeletion.isEmailAccount(),
            email = accountDeletion.currentEmail(),
            deleting = deleting,
            onDismiss = { if (!deleting) showDeleteDialog = false },
            onConfirm = { password ->
                scope.launch {
                    deleting = true
                    try {
                        message = accountDeletion.deleteAccount(password)
                        repository.resetSaveData()
                        showDeleteDialog = false
                        onLogout()
                    } catch (e: Exception) {
                        message = "Hapus akun gagal: ${e.localizedMessage ?: "Periksa koneksi / password"}"
                    } finally {
                        deleting = false
                    }
                }
            }
        )
    }

    val avatarPicker =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) avatar = uri.toString()
        }

    val avatars = listOf(
        "avatar_1",
        "avatar_2",
        "avatar_3",
        "avatar_4",
        "avatar_5",
        "avatar_6",
        "avatar_7",
        "avatar_8",
        "avatar_9",
        "avatar_10"
    )

    val power =
        (selectedHero?.let {
            it.hp + it.attack * 10 + it.defense * 8 + it.level * 100
        } ?: 0) + ownedCards.size * 75

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.profile_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        ProfilePremiumBackground()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 28.dp)
        ) {
            item {
                ProfilePremiumHeader(
                    player = player,
                    hero = selectedHero,
                    avatar = avatar,
                    power = power,
                    message = message,
                    onBack = onBack
                )
            }

            item {
                EditProfileCard(
                    name = name,
                    avatar = avatar,
                    avatars = avatars,
                    onNameChange = { name = it },
                    onAvatarChange = { avatar = it },
                    onPickImage = { avatarPicker.launch("image/*") },
                    onSave = {
                        scope.launch {
                            ContentFilter.validateName(name, "Nama player")?.let {
                                message = it
                                return@launch
                            }
                            message = repository.updatePlayerProfile(
                                name = name,
                                avatar = avatar
                            )
                            reload()
                        }
                    }
                )
            }

            item {
                LogoutAccountCard(
                    onLogout = {
                        firebaseOnline.signOut()
                        message = "Akun online berhasil logout"
                        onLogout()
                    }
                )
            }

            item {
                PrivacyAccountCard(
                    blockedPlayers = blockedPlayers,
                    onOpenPrivacy = { LegalLinks.open(context, LegalLinks.PRIVACY_POLICY) },
                    onOpenTerms = { LegalLinks.open(context, LegalLinks.TERMS_OF_USE) },
                    onUnblock = { uid ->
                        scope.launch {
                            message = try {
                                moderation.unblockUser(uid)
                            } catch (e: Exception) {
                                "Gagal membuka blokir: ${e.localizedMessage ?: "Online error"}"
                            }
                        }
                    },
                    onDeleteAccount = { showDeleteDialog = true }
                )
            }

            item {
                PlayerStatsPanel(
                    player = player,
                    hero = selectedHero,
                    cards = ownedCards,
                    power = power
                )
            }

            item {
                OwnedCardsPreview(cards = ownedCards)
            }
        }
    }
}

@Composable
fun ProfilePremiumBackground() {
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

    Box(
        modifier = Modifier
            .size(300.dp)
            .offset(x = (-120).dp, y = 520.dp)
            .alpha(0.26f)
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
fun ProfilePremiumHeader(
    player: PlayerEntity?,
    hero: HeroEntity?,
    avatar: String,
    power: Int,
    message: String,
    onBack: () -> Unit
) {
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

            Box(
                modifier = Modifier
                    .size(82.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFFFFD66B),
                                Color(0xFF4B1478),
                                Color(0xFF100019)
                            )
                        ),
                        CircleShape
                    )
                    .border(2.dp, Color(0xFFFFD66B), CircleShape)
                    .padding(4.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                ProfileAvatarImage(
                    avatar = avatar,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player?.name ?: "Guest Player",
                    color = Color.White,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Level ${player?.level ?: 1} • Power $power",
                    color = Color(0xFFFFD66B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Active Hero: ${hero?.name ?: "None"}",
                    color = Color(0xFFEBD9FF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (message.isNotBlank()) {
                    Text(
                        text = message,
                        color = Color(0xFFFFD66B),
                        fontSize = 10.sp,
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
fun EditProfileCard(
    name: String,
    avatar: String,
    avatars: List<String>,
    onNameChange: (String) -> Unit,
    onAvatarChange: (String) -> Unit,
    onPickImage: () -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(22.dp, RoundedCornerShape(30.dp)),
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(1.dp, Color(0x55FFD66B)),
        colors = CardDefaults.cardColors(containerColor = Color(0xE812001F))
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF210034),
                            Color(0xFF09000F)
                        )
                    )
                )
                .padding(14.dp)
        ) {
            Text(
                text = "Edit Profile",
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = {
                    Text(
                        text = "Player Name",
                        color = Color.White.copy(alpha = 0.85f)
                    )
                },
                singleLine = true,
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = Color(0xFFFFD66B),
                    unfocusedLabelColor = Color.White.copy(alpha = 0.75f),
                    cursorColor = Color(0xFFFFD66B),
                    focusedBorderColor = Color(0xFFFFD66B),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.28f),
                    focusedContainerColor = Color.Black.copy(alpha = 0.20f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.18f)
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Choose Avatar",
                color = Color(0xFFFFD66B),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(avatars) { avatarName ->
                    PremiumAvatarOption(
                        avatarName = avatarName,
                        selected = avatar == avatarName,
                        onClick = { onAvatarChange(avatarName) }
                    )
                }

                item {
                    CustomAvatarButton(onClick = onPickImage)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(12.dp, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD66B),
                    contentColor = Color(0xFF150020)
                )
            ) {
                Text(
                    text = "Save Profile",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun PremiumAvatarOption(
    avatarName: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(66.dp)
            .clip(CircleShape)
            .background(
                if (selected)
                    Brush.radialGradient(
                        listOf(
                            Color(0xFFFFD66B),
                            Color(0xFFB56CFF),
                            Color(0xFF160021)
                        )
                    )
                else
                    Brush.radialGradient(
                        listOf(
                            Color(0xFF4B1478),
                            Color(0xFF12001F)
                        )
                    )
            )
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) Color(0xFFFFD66B) else Color.White.copy(alpha = 0.18f),
                shape = CircleShape
            )
            .clickable { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        ProfileImageByName(
            name = avatarName,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape),
            fallback = R.drawable.avatar_1,
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun CustomAvatarButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(66.dp)
            .clip(CircleShape)
            .background(Color(0xFF12001F))
            .border(1.dp, Color(0xFFFFD66B), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+\nIMG",
            color = Color(0xFFFFD66B),
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LogoutAccountCard(
    onLogout: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(18.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, Color(0x55FF4D4D)),
        colors = CardDefaults.cardColors(containerColor = Color(0xE812001F))
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF180026), Color(0xFF3A0718), Color(0xFF08000F))
                    )
                )
                .padding(14.dp)
        ) {
            Text(
                text = "Online Account",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Logout hanya keluar dari akun online. Save lokal tetap aman.",
                color = Color(0xFFEBD9FF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF4D4D),
                    contentColor = Color.White
                )
            ) {
                Text("Logout Akun Online", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun PrivacyAccountCard(
    blockedPlayers: List<Map<String, Any>>,
    onOpenPrivacy: () -> Unit,
    onOpenTerms: () -> Unit,
    onUnblock: (String) -> Unit,
    onDeleteAccount: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(18.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, Color(0x557DE7FF)),
        colors = CardDefaults.cardColors(containerColor = Color(0xE812001F))
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF180026), Color(0xFF0E2340), Color(0xFF08000F))
                    )
                )
                .padding(14.dp)
        ) {
            Text("Privacy & Account", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onOpenPrivacy,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF7DE7FF).copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) { Text("Privacy Policy", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                OutlinedButton(
                    onClick = onOpenTerms,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF7DE7FF).copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) { Text("Terms of Use", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Player diblokir (${blockedPlayers.size})",
                color = Color(0xFF7DE7FF),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black
            )
            if (blockedPlayers.isEmpty()) {
                Text("Belum ada player yang diblokir.", color = Color(0xFFEBD9FF), fontSize = 11.sp)
            }
            blockedPlayers.forEach { blocked ->
                val uid = blocked["uid"]?.toString().orEmpty()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = blocked["username"]?.toString().orEmpty().ifBlank { "Player" },
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { onUnblock(uid) }) {
                        Text("Buka blokir", color = Color(0xFFFFD66B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Hapus akun akan menghapus permanen akun online, cloud save, ranking, guild dan chat kamu, serta save lokal di perangkat ini.",
                color = Color(0xFFEBD9FF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onDeleteAccount,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B0000), contentColor = Color.White)
            ) {
                Text("Hapus Akun (Delete Account)", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun DeleteAccountDialog(
    isEmailAccount: Boolean,
    email: String,
    deleting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (password: String?) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmText by remember { mutableStateOf("") }
    val canConfirm = !deleting && confirmText.trim().equals("HAPUS", ignoreCase = true) &&
        (!isEmailAccount || password.isNotBlank())

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A0029),
        titleContentColor = Color.White,
        textContentColor = Color(0xFFEBD9FF),
        title = { Text("Hapus akun permanen?", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Semua progress, kartu, hero, ranking, keanggotaan guild dan pesan chat kamu akan dihapus dan tidak bisa dikembalikan.",
                    fontSize = 12.sp
                )
                if (isEmailAccount) {
                    Text("Akun: $email", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        singleLine = true,
                        label = { Text("Password") },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
                OutlinedTextField(
                    value = confirmText,
                    onValueChange = { confirmText = it },
                    singleLine = true,
                    label = { Text("Ketik HAPUS untuk konfirmasi") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                if (deleting) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Color(0xFFFF4D4D))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(if (isEmailAccount) password else null) },
                enabled = canConfirm
            ) {
                Text(
                    if (deleting) "Menghapus..." else "Hapus Permanen",
                    color = if (canConfirm) Color(0xFFFF4D4D) else Color.Gray,
                    fontWeight = FontWeight.Black
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !deleting) { Text("Batal", color = Color.White) }
        }
    )
}

@Composable
fun PlayerStatsPanel(
    player: PlayerEntity?,
    hero: HeroEntity?,
    cards: List<CardEntity>,
    power: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(20.dp, RoundedCornerShape(30.dp)),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xD80B0010)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF09000F),
                            Color(0xFF210034),
                            Color(0xFF08000F)
                        )
                    )
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFFB56CFF).copy(alpha = 0.35f),
                                Color.Black.copy(alpha = 0.30f)
                            )
                        )
                    )
                    .border(1.dp, Color(0x55FFD66B), RoundedCornerShape(26.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (hero != null) {
                    ProfileImageByName(
                        name = hero.image,
                        modifier = Modifier.fillMaxSize(),
                        fallback = R.drawable.hero_unknown,
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text("NO HERO", color = Color.White, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Player Stats",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )

                StatLine("Gold", "${player?.gold ?: 0}", Color(0xFFFFD66B))
                StatLine("Diamond", "${player?.diamond ?: 0}", Color(0xFF7DE7FF))
                StatLine("Owned Cards", "${cards.size}", Color(0xFFB56CFF))
                StatLine("Active Hero", hero?.name ?: "None", Color(0xFF6CFF9B))
                StatLine("Power", "$power", Color(0xFFFF6B6B))
            }
        }
    }
}

@Composable
fun StatLine(
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color(0xFFCDB8E6),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = value,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun OwnedCardsPreview(cards: List<CardEntity>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(20.dp, RoundedCornerShape(30.dp)),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xD80B0010)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF160021),
                            Color(0xFF07000D)
                        )
                    )
                )
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Owned Cards",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "${cards.size} Cards",
                    color = Color(0xFFFFD66B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (cards.isEmpty()) {
                Text(
                    text = "Belum ada kartu dimiliki.",
                    color = Color(0xFFEBD9FF),
                    fontSize = 12.sp
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 4.dp)
                ) {
                    items(cards.take(20), key = { it.id }) { card ->
                        PremiumOwnedCard(card)
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumOwnedCard(card: CardEntity) {
    val color = when (card.rarity) {
        "Legendary" -> Color(0xFFFFD700)
        "Epic" -> Color(0xFFB56CFF)
        "Rare" -> Color(0xFF4FC3F7)
        else -> Color(0xFFC9C9C9)
    }

    Card(
        modifier = Modifier
            .width(118.dp)
            .height(174.dp)
            .shadow(12.dp, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.85f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF07000D))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            ProfileImageByName(
                name = card.image,
                modifier = Modifier.fillMaxSize(),
                fallback = R.drawable.card_unknown,
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Transparent,
                                Color(0xCC08000F),
                                Color(0xFF050009)
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(7.dp)
                    .background(
                        Color.Black.copy(alpha = 0.62f),
                        RoundedCornerShape(50.dp)
                    )
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "${card.mana} MP",
                    color = Color(0xFFFFD66B),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = card.name,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = card.rarity,
                    color = color,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ProfileCardBadge("ATK ${card.attack}", Color(0xFFFF6B6B))
                    ProfileCardBadge("HP ${card.hp}", Color(0xFF6CFF9B))
                }
            }
        }
    }
}

@Composable
fun ProfileCardBadge(
    text: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .background(
                color.copy(alpha = 0.20f),
                RoundedCornerShape(50.dp)
            )
            .border(
                1.dp,
                color.copy(alpha = 0.24f),
                RoundedCornerShape(50.dp)
            )
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 7.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1
        )
    }
}