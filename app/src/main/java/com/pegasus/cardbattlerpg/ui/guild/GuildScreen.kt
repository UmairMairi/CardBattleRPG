package com.pegasus.cardbattlerpg.ui.guild

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.pegasus.cardbattlerpg.R
import com.pegasus.cardbattlerpg.entity.GuildChatEntity
import com.pegasus.cardbattlerpg.entity.GuildEntity
import com.pegasus.cardbattlerpg.entity.GuildMemberEntity
import com.pegasus.cardbattlerpg.repository.GameRepository
import com.pegasus.cardbattlerpg.online.FirebaseOnlineManager
import com.pegasus.cardbattlerpg.online.ModerationManager
import com.pegasus.cardbattlerpg.online.OnlineGuildManager
import com.pegasus.cardbattlerpg.ui.common.ReportDialog
import com.pegasus.cardbattlerpg.ui.common.ReportTarget
import kotlinx.coroutines.launch

@Composable
fun GuildScreen(
    repository: GameRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var guilds by remember { mutableStateOf<List<GuildEntity>>(emptyList()) }
    var myGuild by remember { mutableStateOf<GuildEntity?>(null) }
    var members by remember { mutableStateOf<List<GuildMemberEntity>>(emptyList()) }
    var chats by remember { mutableStateOf<List<GuildChatEntity>>(emptyList()) }

    var guildName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var chatText by remember { mutableStateOf("") }
    var donationText by remember { mutableStateOf("100") }
    var message by remember { mutableStateOf("") }
    var onlineGuilds by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var onlineMyGuildId by remember { mutableStateOf<String?>(null) }
    var onlineMembers by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var onlineChats by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var onlineChatText by remember { mutableStateOf("") }
    var onlineDonationText by remember { mutableStateOf("100") }
    val onlineGuildManager = remember { OnlineGuildManager() }
    val moderation = remember { ModerationManager() }
    val myUid = remember { FirebaseOnlineManager().currentUid() }
    var blockedUids by remember { mutableStateOf<Set<String>>(emptySet()) }
    var reportTarget by remember { mutableStateOf<ReportTarget?>(null) }

    fun blockUser(uid: String, name: String) {
        scope.launch {
            message = try {
                moderation.blockUser(uid, name)
            } catch (e: Exception) {
                "Blokir gagal: ${e.localizedMessage ?: "Koneksi online belum siap"}"
            }
        }
    }

    suspend fun refreshOnlineGuild() {
        onlineMyGuildId = onlineGuildManager.getMyGuildId()
    }

    suspend fun reload() {
        guilds = repository.getGuilds()
        myGuild = repository.getMyGuild()

        if (myGuild != null) {
            members = repository.getGuildMembers(myGuild!!.id)
            chats = repository.getGuildChats(myGuild!!.id)
        } else {
            members = emptyList()
            chats = emptyList()
        }
    }

    LaunchedEffect(Unit) {
        reload()
        refreshOnlineGuild()
    }

    DisposableEffect(Unit) {
        val stop = onlineGuildManager.listenGuilds { rows ->
            onlineGuilds = rows
        }
        onDispose { stop() }
    }

    DisposableEffect(Unit) {
        val stop = moderation.listenBlocked { rows ->
            blockedUids = rows.mapNotNull { it["uid"]?.toString() }.toSet()
        }
        onDispose { stop() }
    }

    reportTarget?.let { target ->
        ReportDialog(
            target = target,
            onDismiss = { reportTarget = null },
            onSubmit = { reason, alsoBlock ->
                reportTarget = null
                scope.launch {
                    message = try {
                        moderation.report(
                            type = target.type,
                            reason = reason,
                            reportedUid = target.reportedUid,
                            reportedName = target.reportedName,
                            content = target.content,
                            guildId = target.guildId,
                            messageId = target.messageId
                        )
                    } catch (e: Exception) {
                        "Laporan gagal: ${e.localizedMessage ?: "Koneksi online belum siap"}"
                    }
                    if (alsoBlock) blockUser(target.reportedUid, target.reportedName)
                }
            }
        )
    }

    DisposableEffect(onlineMyGuildId) {
        val guildId = onlineMyGuildId
        if (guildId.isNullOrBlank()) {
            onlineMembers = emptyList()
            onlineChats = emptyList()
            onDispose { }
        } else {
            val stopMembers = onlineGuildManager.listenMembers(guildId) { rows -> onlineMembers = rows }
            val stopChats = onlineGuildManager.listenGuildChat(guildId) { rows -> onlineChats = rows }
            onDispose {
                stopMembers()
                stopChats()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.guild_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        GuildPremiumBackground()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 28.dp)
        ) {
            item {
                GuildPremiumHeader(
                    guild = myGuild,
                    message = message,
                    onBack = onBack
                )
            }

            item {
                RealtimeGuildOnlineCard(
                    onlineGuilds = onlineGuilds,
                    myGuildId = onlineMyGuildId,
                    members = onlineMembers,
                    chats = onlineChats.filter { it["uid"]?.toString() !in blockedUids },
                    myUid = myUid,
                    onReportChat = { chat ->
                        reportTarget = ReportTarget(
                            type = "chat_message",
                            reportedUid = chat["uid"]?.toString().orEmpty(),
                            reportedName = chat["username"]?.toString().orEmpty(),
                            content = chat["message"]?.toString().orEmpty(),
                            guildId = onlineMyGuildId.orEmpty(),
                            messageId = chat["id"]?.toString().orEmpty()
                        )
                    },
                    onBlockChatUser = { chat ->
                        blockUser(chat["uid"]?.toString().orEmpty(), chat["username"]?.toString().orEmpty().ifBlank { "Player" })
                    },
                    onReportGuild = { guild ->
                        val guildId = guild["guildId"]?.toString().orEmpty().ifBlank { guild["docId"]?.toString().orEmpty() }
                        reportTarget = ReportTarget(
                            type = "guild",
                            reportedUid = guild["leaderUid"]?.toString().orEmpty(),
                            reportedName = guild["name"]?.toString().orEmpty().ifBlank { "Guild" },
                            content = "${guild["name"] ?: ""} — ${guild["description"] ?: ""}",
                            guildId = guildId
                        )
                    },
                    chatText = onlineChatText,
                    donationText = onlineDonationText,
                    newGuildName = guildName,
                    newGuildDescription = description,
                    onNewGuildNameChange = { guildName = it },
                    onNewGuildDescriptionChange = { description = it },
                    onChatChange = { onlineChatText = it },
                    onDonationChange = { onlineDonationText = it },
                    onRefresh = {
                        scope.launch {
                            refreshOnlineGuild()
                            message = "Daftar guild online direfresh"
                        }
                    },
                    onCreateOnlineGuild = {
                        scope.launch {
                            val player = repository.getPlayer()
                            val hero = repository.getSelectedHero()
                            if (player == null) {
                                message = "Player tidak ditemukan"
                                return@launch
                            }
                            message = try {
                                onlineGuildManager.createGuild(
                                    name = guildName,
                                    description = description,
                                    player = player,
                                    power = repository.calculatePlayerPower(),
                                    heroImage = hero?.image ?: "hero_unknown"
                                )
                            } catch (e: Exception) {
                                "Buat guild online gagal: ${e.localizedMessage ?: "Koneksi online belum siap"}"
                            }
                            guildName = ""
                            description = ""
                            refreshOnlineGuild()
                        }
                    },
                    onJoinOnlineGuild = { guildId ->
                        scope.launch {
                            val player = repository.getPlayer()
                            val hero = repository.getSelectedHero()
                            if (player != null) {
                                message = try {
                                    onlineGuildManager.joinGuild(
                                        guildId = guildId,
                                        player = player,
                                        power = repository.calculatePlayerPower(),
                                        heroImage = hero?.image ?: "hero_unknown"
                                    )
                                } catch (e: Exception) {
                                    "Join guild online gagal: ${e.localizedMessage ?: "Koneksi online belum siap"}"
                                }
                                if (!message.contains("gagal", ignoreCase = true) && !message.contains("Keluar guild dulu", ignoreCase = true)) {
                                    onlineMyGuildId = guildId
                                }
                                refreshOnlineGuild()
                            }
                        }
                    },
                    onLeaveOnlineGuild = { guildId ->
                        scope.launch {
                            message = try {
                                onlineGuildManager.leaveGuild(guildId)
                            } catch (e: Exception) {
                                "Keluar guild online gagal: ${e.localizedMessage ?: "Koneksi online belum siap"}"
                            }
                            onlineMyGuildId = null
                            onlineMembers = emptyList()
                            onlineChats = emptyList()
                            refreshOnlineGuild()
                        }
                    },
                    onDonateOnlineGuild = { guildId ->
                        scope.launch {
                            val player = repository.getPlayer()
                            message = try {
                                onlineGuildManager.donate(
                                    guildId = guildId,
                                    amount = onlineDonationText.toIntOrNull() ?: 0,
                                    username = player?.name ?: "Player"
                                )
                            } catch (e: Exception) {
                                "Donasi guild online gagal: ${e.localizedMessage ?: "Koneksi online belum siap"}"
                            }
                            if (!message.contains("gagal", ignoreCase = true) && !message.contains("tidak cukup", ignoreCase = true)) onlineDonationText = "100"
                        }
                    },
                    onSendOnlineChat = { guildId ->
                        scope.launch {
                            val player = repository.getPlayer()
                            message = try {
                                onlineGuildManager.sendChat(guildId, player?.name ?: "Player", onlineChatText)
                            } catch (e: Exception) {
                                "Chat guild online gagal: ${e.localizedMessage ?: "Koneksi online belum siap"}"
                            }
                            if (!message.contains("gagal", ignoreCase = true)) onlineChatText = ""
                        }
                    }
                )
            }

            // Local/offline guild section must NOT open when only an ONLINE guild exists.
            // Previous condition used `else { myGuild!! }`, so after Create/Join Online Guild
            // the app could force close because myGuild was still null while onlineMyGuildId was not blank.
            if (myGuild == null && onlineMyGuildId.isNullOrBlank()) {
                item {
                    CreateGuildCard(
                        guildName = guildName,
                        description = description,
                        onNameChange = { guildName = it },
                        onDescriptionChange = { description = it },
                        onCreate = {
                            scope.launch {
                                message = repository.createGuild(guildName, description)
                                reload()
                            }
                        }
                    )
                }

                item {
                    SectionTitle("Available Guilds", "${guilds.size} guilds")
                }

                items(guilds) { guild ->
                    GuildListCard(
                        guild = guild,
                        onJoin = {
                            scope.launch {
                                message = repository.joinGuild(guild)
                                reload()
                            }
                        }
                    )
                }
            }

            if (myGuild != null) {
                item {
                    MyGuildCard(
                        guild = myGuild!!,
                        donationText = donationText,
                        onDonationChange = { donationText = it },
                        onDonate = {
                            scope.launch {
                                message = repository.donateGuildGold(donationText.toIntOrNull() ?: 0)
                                reload()
                            }
                        },
                        onLeave = {
                            scope.launch {
                                message = repository.leaveGuild()
                                reload()
                            }
                        }
                    )
                }

                item {
                    SectionTitle("Members", "${members.size} players")
                }

                items(members) { member ->
                    GuildMemberCard(member)
                }

                item {
                    GuildChatBox(
                        chatText = chatText,
                        onChatChange = { chatText = it },
                        onSend = {
                            scope.launch {
                                message = repository.sendGuildChat(chatText)
                                chatText = ""
                                reload()
                            }
                        }
                    )
                }

                item {
                    GuildChatPanel(chats = chats)
                }
            }
        }
    }
}

@Composable
fun GuildChatPanel(chats: List<GuildChatEntity>) {
    PremiumGuildCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Guild Chat",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "${chats.size} messages",
                color = Color(0xFFFFD66B),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color(0xFF12001F).copy(alpha = 0.80f)
                        )
                    ),
                    RoundedCornerShape(24.dp)
                )
                .border(
                    1.dp,
                    Color(0x55FFD66B),
                    RoundedCornerShape(24.dp)
                )
                .padding(10.dp)
        ) {
            if (chats.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada pesan guild.",
                        color = Color(0xFFEBD9FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    reverseLayout = true,
                    contentPadding = PaddingValues(bottom = 4.dp)
                ) {
                    items(chats.reversed()) { chat ->
                        GuildChatCard(chat)
                    }
                }
            }
        }
    }
}


@Composable
fun RealtimeGuildOnlineCard(
    onlineGuilds: List<Map<String, Any>>,
    myGuildId: String?,
    members: List<Map<String, Any>>,
    chats: List<Map<String, Any>>,
    chatText: String,
    donationText: String,
    newGuildName: String,
    newGuildDescription: String,
    onNewGuildNameChange: (String) -> Unit,
    onNewGuildDescriptionChange: (String) -> Unit,
    onChatChange: (String) -> Unit,
    onDonationChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onCreateOnlineGuild: () -> Unit,
    onJoinOnlineGuild: (String) -> Unit,
    onLeaveOnlineGuild: (String) -> Unit,
    onDonateOnlineGuild: (String) -> Unit,
    onSendOnlineChat: (String) -> Unit,
    myUid: String? = null,
    onReportChat: (Map<String, Any>) -> Unit = {},
    onBlockChatUser: (Map<String, Any>) -> Unit = {},
    onReportGuild: (Map<String, Any>) -> Unit = {}
) {
    val joinedGuild = onlineGuilds.firstOrNull { guild ->
        val guildId = guild["guildId"]?.toString().orEmpty().ifBlank { guild["docId"]?.toString().orEmpty() }
        guildId == myGuildId
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(22.dp, RoundedCornerShape(30.dp), ambientColor = Color(0xFF7DE7FF), spotColor = Color(0xFFB56CFF)),
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(1.dp, Color(0x887DE7FF)),
        colors = CardDefaults.cardColors(containerColor = Color(0xE812001F))
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF05000B), Color(0xFF12345A), Color(0xFF170024))
                    )
                )
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (myGuildId.isNullOrBlank()) "Guild Online" else "My Online Guild",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = if (myGuildId.isNullOrBlank()) "Refresh list • join 1 guild only" else "Guild aktif • member/chat realtime",
                        color = Color(0xFF7DE7FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onRefresh,
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD66B),
                        contentColor = Color(0xFF08000F)
                    )
                ) {
                    Text("Refresh", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!myGuildId.isNullOrBlank()) {
                // Show guild information immediately after create/join, even before the guild list listener refreshes.
                val guildName = joinedGuild?.get("name")?.toString().orEmpty()
                    .ifBlank { joinedGuild?.get("guildName")?.toString().orEmpty() }
                    .ifBlank { "Guild Aktif" }
                val leader = joinedGuild?.get("leaderName")?.toString().orEmpty().ifBlank { "-" }
                val memberCount = joinedGuild?.get("memberCount")?.toString().orEmpty().ifBlank { members.size.coerceAtLeast(1).toString() }
                val guildLevel = joinedGuild?.get("level")?.toString().orEmpty().ifBlank { "1" }
                val guildExp = joinedGuild?.get("exp")?.toString().orEmpty().ifBlank { "0" }
                val guildFund = joinedGuild?.get("goldFund")?.toString().orEmpty().ifBlank { joinedGuild?.get("fund")?.toString().orEmpty().ifBlank { "0" } }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0x667DE7FF)),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(guildName, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black)
                        Text("Leader $leader • Member $memberCount", color = Color(0xFFFFD66B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Level $guildLevel • Guild EXP $guildExp/1000 • Fund $guildFund", color = Color(0xFF7DE7FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = donationText,
                                onValueChange = onDonationChange,
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                label = { Text("Gold Donate") },
                                textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.Bold),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFFFFD66B),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                                    focusedLabelColor = Color(0xFFFFD66B),
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                                    cursorColor = Color(0xFFFFD66B)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            Button(
                                onClick = { onDonateOnlineGuild(myGuildId) },
                                modifier = Modifier.height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD66B), contentColor = Color(0xFF08000F))
                            ) { Text("Donate", fontWeight = FontWeight.Black) }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Members", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(6.dp))
                        if (members.isEmpty()) {
                            Text(
                                text = "Data member sedang dimuat...",
                                color = Color(0xFFEBD9FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
                                    .padding(8.dp)
                            )
                        }
                        members.take(6).forEach { member ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(member["username"]?.toString().orEmpty().ifBlank { "Player" }, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                                Text("${member["role"] ?: "Member"} • ${member["power"] ?: 0}", color = Color(0xFF7DE7FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = chatText,
                                onValueChange = onChatChange,
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                label = { Text("Guild Chat") },
                                textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.Bold),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF7DE7FF),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                                    focusedLabelColor = Color(0xFF7DE7FF),
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                                    cursorColor = Color(0xFF7DE7FF)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            Button(
                                onClick = { onSendOnlineChat(myGuildId) },
                                modifier = Modifier.height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7DE7FF), contentColor = Color(0xFF08000F))
                            ) { Text("Send", fontWeight = FontWeight.Black) }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        if (chats.isEmpty()) {
                            Text(
                                text = "Belum ada chat guild. Kirim pesan pertama.",
                                color = Color(0xFFEBD9FF),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        chats.take(8).forEach { chat ->
                            OnlineGuildChatRow(
                                chat = chat,
                                isMine = chat["uid"]?.toString() == myUid,
                                onReport = { onReportChat(chat) },
                                onBlock = { onBlockChatUser(chat) }
                            )
                        }
                        if (chats.isNotEmpty()) {
                            Text(
                                text = "Tap ⋮ pada pesan untuk melaporkan atau memblokir player.",
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = 9.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (joinedGuild != null && joinedGuild["leaderUid"]?.toString() != myUid) {
                            TextButton(
                                onClick = { onReportGuild(joinedGuild) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Laporkan Guild", color = Color(0xFFFF7777), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { onLeaveOnlineGuild(myGuildId) },
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4D4D), contentColor = Color.White)
                        ) {
                            Text("Keluar Guild", fontWeight = FontWeight.Black)
                        }
                    }
                }
            } else {
                Text(
                    text = "Buat guild atau pilih salah satu guild untuk join. Setelah join, tombol guild lain terkunci sampai kamu keluar guild.",
                    color = Color(0xFFE9D9FF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.dp, Color(0x55FFD66B)),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.24f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Buat Guild Online", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(9.dp))
                        OutlinedTextField(
                            value = newGuildName,
                            onValueChange = onNewGuildNameChange,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text("Guild Name") },
                            textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.Bold),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFFD66B),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                                focusedLabelColor = Color(0xFFFFD66B),
                                unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                                cursorColor = Color(0xFFFFD66B)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newGuildDescription,
                            onValueChange = onNewGuildDescriptionChange,
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 3,
                            label = { Text("Description") },
                            textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.Bold),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF7DE7FF),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                                focusedLabelColor = Color(0xFF7DE7FF),
                                unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                                cursorColor = Color(0xFF7DE7FF)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onCreateOnlineGuild,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD66B), contentColor = Color(0xFF08000F))
                        ) {
                            Text("Buat & Masuk Guild", fontWeight = FontWeight.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Daftar Guild Online", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(8.dp))

                onlineGuilds.take(8).forEach { guild ->
                    val guildId = guild["guildId"]?.toString().orEmpty().ifBlank { guild["docId"]?.toString().orEmpty() }
                    val name = guild["name"]?.toString().orEmpty()
                    val leader = guild["leaderName"]?.toString().orEmpty()
                    val memberCount = guild["memberCount"]?.toString().orEmpty()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .background(Color.Black.copy(alpha = 0.22f), RoundedCornerShape(16.dp))
                            .padding(9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(name.ifBlank { "Realtime Guild" }, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("Leader $leader • Member $memberCount", color = Color(0xFFE9D9FF), fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }

                        TextButton(
                            onClick = { onReportGuild(guild) },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                        ) {
                            Text("Lapor", color = Color(0xFFFF7777), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { if (guildId.isNotBlank()) onJoinOnlineGuild(guildId) },
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7DE7FF),
                                contentColor = Color(0xFF08000F)
                            )
                        ) {
                            Text("Join", fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnlineGuildChatRow(
    chat: Map<String, Any>,
    isMine: Boolean,
    onReport: () -> Unit,
    onBlock: () -> Unit
) {
    val username = chat["username"]?.toString().orEmpty().ifBlank { "Player" }
    val canModerate = !isMine && username != "System" && chat["uid"]?.toString().orEmpty().isNotBlank()
    var menuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$username: ${chat["message"] ?: ""}",
            color = Color(0xFFEBD9FF),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (canModerate) {
            Box {
                Text(
                    text = "⋮",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .clickable { menuOpen = true }
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                )
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("Laporkan pesan") },
                        onClick = { menuOpen = false; onReport() }
                    )
                    DropdownMenuItem(
                        text = { Text("Blokir $username") },
                        onClick = { menuOpen = false; onBlock() }
                    )
                }
            }
        }
    }
}

@Composable
fun GuildPremiumBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xEE030006),
                        Color(0xCC24003E),
                        Color(0xF2050009),
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
fun GuildPremiumHeader(
    guild: GuildEntity?,
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

            val context = LocalContext.current
            val emblemRes = remember { context.resources.getIdentifier("guild_emblem", "drawable", context.packageName) }
            if (emblemRes != 0) {
                Image(
                    painter = painterResource(id = emblemRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.Black.copy(alpha = 0.22f), CircleShape)
                        .padding(8.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.Black.copy(alpha = 0.22f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("G", color = Color(0xFFFFD66B), fontSize = 28.sp, fontWeight = FontWeight.Black)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = guild?.guildName ?: "Guild Hall",
                    color = Color.White,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = guild?.let { "Lv ${it.level} • ${it.memberCount} Members" }
                        ?: "Buat atau join guild legendaris",
                    color = Color(0xFFFFD66B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                if (message.isNotBlank()) {
                    Text(
                        text = message,
                        color = Color(0xFFEBD9FF),
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
fun CreateGuildCard(
    guildName: String,
    description: String,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCreate: () -> Unit
) {
    PremiumGuildCard {
        Text("Create Guild", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(12.dp))

        GuildTextField(guildName, onNameChange, "Guild Name")

        Spacer(modifier = Modifier.height(10.dp))

        GuildTextField(description, onDescriptionChange, "Description")

        Spacer(modifier = Modifier.height(14.dp))

        GuildGoldButton("Create Guild", onCreate)
    }
}

@Composable
fun GuildListCard(
    guild: GuildEntity,
    onJoin: () -> Unit
) {
    PremiumGuildCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.guild_emblem),
                contentDescription = null,
                modifier = Modifier
                    .size(70.dp)
                    .background(Color.Black.copy(alpha = 0.24f), RoundedCornerShape(20.dp))
                    .padding(10.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = guild.guildName,
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Leader: ${guild.leaderName}",
                    color = Color(0xFFEBD9FF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Lv ${guild.level} • ${guild.memberCount} Members",
                    color = Color(0xFFFFD66B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = guild.description,
            color = Color(0xFFEBD9FF),
            fontSize = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(12.dp))

        GuildGoldButton("Join Guild", onJoin)
    }
}

@Composable
fun MyGuildCard(
    guild: GuildEntity,
    donationText: String,
    onDonationChange: (String) -> Unit,
    onDonate: () -> Unit,
    onLeave: () -> Unit
) {
    PremiumGuildCard {
        Text(
            text = guild.guildName,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )

        Text(
            text = "Leader: ${guild.leaderName}",
            color = Color(0xFFFFD66B),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = guild.description,
            color = Color(0xFFEBD9FF),
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        GuildExpBar(exp = guild.exp)

        Spacer(modifier = Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GuildStatBox("Level", guild.level.toString(), Color(0xFF7DE7FF), Modifier.weight(1f))
            GuildStatBox("Members", guild.memberCount.toString(), Color(0xFF6CFF9B), Modifier.weight(1f))
            GuildStatBox("Fund", guild.goldFund.toString(), Color(0xFFFFD66B), Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(14.dp))

        GuildTextField(donationText, onDonationChange, "Donate Gold")

        Spacer(modifier = Modifier.height(10.dp))

        GuildGoldButton("Donate Gold", onDonate)

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onLeave,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF4D4D),
                contentColor = Color.White
            )
        ) {
            Text("Leave Guild", fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun GuildMemberCard(member: GuildMemberEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xDD12001F))
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF100019),
                            Color(0xFF260042),
                            Color(0xFF08000F)
                        )
                    )
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.avatar_1),
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color(0xFFFFD66B), CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.playerName,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                GuildRoleBadge(member.role)
            }

            Text(
                text = "⚔ ${member.power}",
                color = Color(0xFF7DE7FF),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun GuildChatBox(
    chatText: String,
    onChatChange: (String) -> Unit,
    onSend: () -> Unit
) {
    PremiumGuildCard {
        Text("Send Message", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)

        Spacer(modifier = Modifier.height(10.dp))

        GuildTextField(chatText, onChatChange, "Message")

        Spacer(modifier = Modifier.height(10.dp))

        GuildGoldButton("Send Message", onSend)
    }
}

@Composable
fun GuildChatCard(chat: GuildChatEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = 24.dp,
            topEnd = 24.dp,
            bottomEnd = 24.dp,
            bottomStart = 8.dp
        ),
        colors = CardDefaults.cardColors(containerColor = Color(0xDD2B0A45)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = chat.senderName,
                color = Color(0xFFFFD66B),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = chat.message,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun PremiumGuildCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(20.dp, RoundedCornerShape(30.dp)),
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
                .padding(14.dp),
            content = content
        )
    }
}

@Composable
fun GuildTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.85f)
            )
        },
        textStyle = TextStyle(
            color = Color.White,
            fontSize = 14.sp,
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
}

@Composable
fun GuildGoldButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(12.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFFD66B),
            contentColor = Color(0xFF160021)
        )
    ) {
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun GuildExpBar(exp: Int) {
    val progress = (exp.toFloat() / 1000f).coerceIn(0f, 1f)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Guild EXP", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("$exp/1000", color = Color(0xFFFFD66B), fontSize = 11.sp, fontWeight = FontWeight.Black)
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(Color.Black.copy(alpha = 0.38f), RoundedCornerShape(50.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(12.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFFD66B), Color(0xFFFF8A00))
                        ),
                        RoundedCornerShape(50.dp)
                    )
            )
        }
    }
}

@Composable
fun GuildStatBox(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(74.dp)
            .background(
                Brush.radialGradient(
                    listOf(
                        color.copy(alpha = 0.20f),
                        Color.Black.copy(alpha = 0.25f)
                    )
                ),
                RoundedCornerShape(22.dp)
            )
            .border(1.dp, color.copy(alpha = 0.28f), RoundedCornerShape(22.dp))
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Color(0xFFEBD9FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(value, color = color, fontSize = 16.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun GuildRoleBadge(role: String) {
    Box(
        modifier = Modifier
            .background(
                if (role == "Leader") Color(0xFFFFD66B) else Color(0xFFB56CFF),
                RoundedCornerShape(50.dp)
            )
            .padding(horizontal = 9.dp, vertical = 3.dp)
    ) {
        Text(
            text = role,
            color = if (role == "Leader") Color(0xFF160021) else Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun SectionTitle(
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black
        )

        Text(
            text = subtitle,
            color = Color(0xFFFFD66B),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}