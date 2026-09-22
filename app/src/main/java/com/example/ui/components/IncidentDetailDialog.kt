package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.IncidentEntity
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusSafe
import com.example.ui.theme.StatusSuspicious
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun IncidentDetailDialog(
    incident: IncidentEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    val formattedDate = sdf.format(Date(incident.timestamp))

    val statusColor = when (incident.classification) {
        "SAFE" -> StatusSafe
        "SUSPICIOUS" -> StatusSuspicious
        else -> StatusCritical
    }

    val jsonExport = """
    {
      "incident_id": ${incident.id},
      "timestamp": "$formattedDate",
      "client_ip": "${incident.clientIp}",
      "classification": "${incident.classification}",
      "composite_score": ${incident.compositeScore},
      "ml_score": ${incident.mlScore},
      "rule_score": ${incident.ruleScore},
      "floor_override": ${incident.floorOverride},
      "latency_ms": ${incident.latencyMs},
      "payload": "${incident.payload.replace("\"", "\\\"")}",
      "triggered_rules": "${incident.triggeredRules}",
      "primary_threat": "${incident.primaryThreat}"
    }
    """.trimIndent()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1322)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Incident #${incident.id}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                HorizontalDivider(color = CyberCardBorder, modifier = Modifier.padding(vertical = 10.dp))

                // Risk & Classification
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Classification", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = incident.classification,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Composite Risk", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = "${incident.compositeScore}/100",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Layer Breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberCardBg)
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("ML Layer (60%)", fontSize = 10.sp, color = TextMuted)
                            Text("${incident.mlScore}/100", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyberCyan, fontFamily = FontFamily.Monospace)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberCardBg)
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("Rules Layer (40%)", fontSize = 10.sp, color = TextMuted)
                            Text("${incident.ruleScore}/100", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8), fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Payload Code Box
                Text("Tested Payload", fontSize = 11.sp, color = TextMuted)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF05080E))
                        .border(1.dp, CyberCardBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = incident.payload,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CyberCyan
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Metadata: IP, Time, Latency
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Client IP", fontSize = 10.sp, color = TextMuted)
                        Text(incident.clientIp, fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = TextPrimary)
                    }
                    Column {
                        Text("Latency", fontSize = 10.sp, color = TextMuted)
                        Text("${incident.latencyMs}ms", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = CyberCyan)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Timestamp", fontSize = 10.sp, color = TextMuted)
                        Text(formattedDate, fontSize = 11.sp, color = TextSecondary)
                    }
                }

                if (incident.triggeredRules.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Triggered Security Rules", fontSize = 11.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = incident.triggeredRules,
                        fontSize = 12.sp,
                        color = StatusCritical,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Export JSON Button
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(jsonExport))
                        Toast.makeText(context, "Incident JSON copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CyberCyan))
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Incident JSON", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
