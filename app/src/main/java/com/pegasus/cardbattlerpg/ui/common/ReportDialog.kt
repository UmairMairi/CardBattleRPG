package com.pegasus.cardbattlerpg.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pegasus.cardbattlerpg.online.ModerationManager

// What the player is reporting. `type` is stored with the report: "chat_message", "guild", "player".
data class ReportTarget(
    val type: String,
    val reportedUid: String,
    val reportedName: String,
    val content: String,
    val guildId: String = "",
    val messageId: String = ""
)

@Composable
fun ReportDialog(
    target: ReportTarget,
    onDismiss: () -> Unit,
    onSubmit: (reason: String, alsoBlock: Boolean) -> Unit
) {
    var reason by remember { mutableStateOf(ModerationManager.REPORT_REASONS.first()) }
    var alsoBlock by remember { mutableStateOf(target.type == "chat_message") }
    val canBlock = target.reportedUid.isNotBlank() && target.type != "guild"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A0029),
        titleContentColor = Color.White,
        textContentColor = Color(0xFFEBD9FF),
        title = { Text("Laporkan ${target.reportedName.ifBlank { "konten" }}", fontWeight = FontWeight.Black) },
        text = {
            Column {
                if (target.content.isNotBlank()) {
                    Text(
                        text = "\"${target.content}\"",
                        fontSize = 12.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text("Alasan:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                ModerationManager.REPORT_REASONS.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { reason = option }
                    ) {
                        RadioButton(
                            selected = reason == option,
                            onClick = { reason = option },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFFD66B))
                        )
                        Text(option, fontSize = 13.sp)
                    }
                }
                if (canBlock) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { alsoBlock = !alsoBlock }
                    ) {
                        Checkbox(
                            checked = alsoBlock,
                            onCheckedChange = { alsoBlock = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFFD66B))
                        )
                        Text("Blokir player ini juga", fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(reason, canBlock && alsoBlock) }) {
                Text("Kirim Laporan", color = Color(0xFFFF7777), fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = Color.White) }
        }
    )
}
