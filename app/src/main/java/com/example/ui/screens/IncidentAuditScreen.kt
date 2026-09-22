package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.IncidentEntity
import com.example.ui.SocViewModel
import com.example.ui.components.IncidentDetailDialog
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
fun IncidentAuditScreen(
    viewModel: SocViewModel,
    modifier: Modifier = Modifier
) {
    val incidents by viewModel.allIncidents.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.incidentFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.incidentSearch.collectAsStateWithLifecycle()

    var selectedIncident by remember { mutableStateOf<IncidentEntity?>(null) }

    if (selectedIncident != null) {
        IncidentDetailDialog(
            incident = selectedIncident!!,
            onDismiss = { selectedIncident = null }
        )
    }

    val filteredIncidents = incidents.filter { entity ->
        val matchesFilter = when (selectedFilter) {
            "CRITICAL" -> entity.classification == "CRITICAL"
            "SUSPICIOUS" -> entity.classification == "SUSPICIOUS"
            "SAFE" -> entity.classification == "SAFE"
            else -> true
        }

        val matchesSearch = searchQuery.isBlank() ||
                entity.payload.contains(searchQuery, ignoreCase = true) ||
                entity.clientIp.contains(searchQuery, ignoreCase = true) ||
                entity.primaryThreat.contains(searchQuery, ignoreCase = true)

        matchesFilter && matchesSearch
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("incident_audit_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Incident Audit Logs",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Text(
                    text = "Persistent SQLite DB (data/sqli_incidents.db)",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary
                )
            }

            IconButton(
                onClick = { viewModel.clearIncidents() },
                modifier = Modifier.testTag("clear_incidents_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear logs",
                    tint = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setIncidentSearch(it) },
            placeholder = { Text("Search by payload, IP, or threat signature...", fontSize = 12.sp, color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_incidents_input"),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CyberCardBg,
                unfocusedContainerColor = CyberCardBg,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedIndicatorColor = CyberCyan,
                unfocusedIndicatorColor = CyberCardBorder
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Classification Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("ALL", "CRITICAL", "SUSPICIOUS", "SAFE").forEach { filter ->
                val isSelected = selectedFilter == filter
                val chipColor = when (filter) {
                    "CRITICAL" -> StatusCritical
                    "SUSPICIOUS" -> StatusSuspicious
                    "SAFE" -> StatusSafe
                    else -> CyberCyan
                }

                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setIncidentFilter(filter) },
                    label = { Text(filter, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = chipColor.copy(alpha = 0.25f),
                        selectedLabelColor = chipColor,
                        containerColor = CyberCardBg,
                        labelColor = TextMuted
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Incident Count & Results
        Text(
            text = "${filteredIncidents.size} incidents recorded",
            fontSize = 11.sp,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (filteredIncidents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberCardBg)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No recorded incidents match current filters.",
                    fontSize = 13.sp,
                    color = TextMuted
                )
            }
        } else {
            val sdf = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.US)

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredIncidents) { incident ->
                    val statusColor = when (incident.classification) {
                        "SAFE" -> StatusSafe
                        "SUSPICIOUS" -> StatusSuspicious
                        else -> StatusCritical
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberCardBg)
                            .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
                            .clickable { selectedIncident = incident }
                            .padding(12.dp)
                            .testTag("incident_item_${incident.id}")
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(statusColor)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = incident.classification,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Score ${incident.compositeScore}/100",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = TextPrimary
                                    )
                                }

                                Text(
                                    text = sdf.format(Date(incident.timestamp)),
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = incident.payload,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = CyberCyan,
                                maxLines = 2
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "IP: ${incident.clientIp} • ${incident.latencyMs}ms",
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Tap to inspect JSON >",
                                    fontSize = 10.sp,
                                    color = CyberCyan
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
