package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import com.example.model.RiskClassification
import com.example.model.RuleSeverity
import com.example.ui.SocViewModel
import com.example.ui.components.FeatureForensicsDialog
import com.example.ui.components.ScoreGaugeCard
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusSafe
import com.example.ui.theme.StatusSuspicious
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ThreatAnalyzerScreen(
    viewModel: SocViewModel,
    modifier: Modifier = Modifier
) {
    val inputPayload by viewModel.inputPayload.collectAsStateWithLifecycle()
    val clientIp by viewModel.clientIp.collectAsStateWithLifecycle()
    val currentResult by viewModel.currentResult.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()

    var showForensicsDialog by remember { mutableStateOf(false) }

    if (showForensicsDialog && currentResult != null) {
        FeatureForensicsDialog(
            features = currentResult!!.features,
            onDismiss = { showForensicsDialog = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Section Header
        Text(
            text = "Interactive Threat Analyzer",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Live inspection playground evaluating the 39-D ML Layer & Signature Engine",
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // One-Click Presets Bar
        Text(
            text = "ONE-CLICK TEST PRESETS",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = CyberCyan,
            letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            viewModel.presets.forEach { preset ->
                val pillBorder = if (preset.isBenign) StatusSafe.copy(alpha = 0.5f) else StatusCritical.copy(alpha = 0.5f)
                val pillText = if (preset.isBenign) StatusSafe else Color(0xFFFF7B72)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberCardBg)
                        .border(1.dp, pillBorder, RoundedCornerShape(8.dp))
                        .clickable { viewModel.applyPreset(preset) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("preset_${preset.title.replace(" ", "_")}")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (preset.isBenign) StatusSafe else StatusCritical)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = preset.title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = pillText
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Input Payload Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CyberCardBg)
                .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Query / Parameter Payload",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )

                    // Client IP field
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Client IP: ", fontSize = 10.sp, color = TextMuted)
                        Text(clientIp, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = CyberCyan)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = inputPayload,
                    onValueChange = { viewModel.updateInputPayload(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .testTag("payload_input"),
                    placeholder = { Text("Enter SQL query, parameter, or search phrase...", fontSize = 12.sp, color = TextMuted) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF05080E),
                        unfocusedContainerColor = Color(0xFF05080E),
                        focusedTextColor = CyberCyan,
                        unfocusedTextColor = TextPrimary,
                        focusedIndicatorColor = CyberCyan,
                        unfocusedIndicatorColor = CyberCardBorder
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.analyzePayload() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("analyze_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan,
                            contentColor = Color(0xFF00363D)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color(0xFF00363D),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Analyze Threat Vector", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = { showForensicsDialog = true },
                        modifier = Modifier.testTag("forensics_button"),
                        enabled = currentResult != null,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(CyberCyan)
                        )
                    ) {
                        Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("39-D Forensics", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Result Score Gauge Card
        currentResult?.let { result ->
            ScoreGaugeCard(
                compositeScore = result.compositeScore,
                mlScore = result.mlScore,
                ruleScore = result.ruleScore,
                classification = result.classification,
                floorOverrideApplied = result.floorOverrideApplied,
                latencyMs = result.latencyMs
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Triggered Security Signatures
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberCardBg)
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Triggered Rule Signatures",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${result.triggeredRules.size} matched",
                        fontSize = 11.sp,
                        color = if (result.triggeredRules.isEmpty()) StatusSafe else StatusCritical,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (result.triggeredRules.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(StatusSafe.copy(alpha = 0.1f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = StatusSafe,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "No malicious SQL signatures triggered. Syntax pattern is benign.",
                            fontSize = 12.sp,
                            color = StatusSafe
                        )
                    }
                } else {
                    result.triggeredRules.forEach { match ->
                        val sevColor = when (match.severity) {
                            RuleSeverity.CRITICAL -> StatusCritical
                            RuleSeverity.HIGH -> StatusSuspicious
                            RuleSeverity.MEDIUM -> CyberCyan
                            RuleSeverity.LOW -> StatusSafe
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0C1322))
                                .border(1.dp, sevColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = match.ruleName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = match.severity.name,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = sevColor
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = match.description,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            if (match.matchedSubstring.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Matched token: \"${match.matchedSubstring}\"",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
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
