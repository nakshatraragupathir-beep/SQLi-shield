package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.RuleSeverity
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
fun AddRuleDialog(
    onDismiss: () -> Unit,
    onAddRule: (name: String, category: String, regex: String, severity: RuleSeverity, desc: String, rem: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Custom Signature") }
    var regex by remember { mutableStateOf("(?i)\\b(CUSTOM_VECTOR|PAYLOAD)\\b") }
    var severity by remember { mutableStateOf(RuleSeverity.HIGH) }
    var description by remember { mutableStateOf("") }
    var remediation by remember { mutableStateOf("Apply strict input sanitization and parameterized bindings.") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                    Text(
                        text = "Register Dynamic Rule",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Text(
                    text = "Dynamic signature injected into Rule Engine without retraining downtime.",
                    fontSize = 11.sp,
                    color = TextMuted
                )

                HorizontalDivider(color = CyberCardBorder, modifier = Modifier.padding(vertical = 10.dp))

                // Rule Name
                Text("Rule Name", fontSize = 11.sp, color = TextSecondary)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("e.g., Polyglot Injection Signature", fontSize = 12.sp, color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
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

                // Regex Pattern
                Text("Regular Expression (Regex)", fontSize = 11.sp, color = TextSecondary)
                OutlinedTextField(
                    value = regex,
                    onValueChange = { regex = it },
                    placeholder = { Text("(?i)\\bPATTERN\\b", fontSize = 12.sp, color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = CyberCardBg,
                        unfocusedContainerColor = CyberCardBg,
                        focusedTextColor = CyberCyan,
                        unfocusedTextColor = CyberCyan,
                        focusedIndicatorColor = CyberCyan,
                        unfocusedIndicatorColor = CyberCardBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Severity Selection
                Text("Severity Level", fontSize = 11.sp, color = TextSecondary)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RuleSeverity.values().forEach { sev ->
                        val isSelected = severity == sev
                        val tint = when (sev) {
                            RuleSeverity.CRITICAL -> StatusCritical
                            RuleSeverity.HIGH -> StatusSuspicious
                            RuleSeverity.MEDIUM -> CyberCyan
                            RuleSeverity.LOW -> StatusSafe
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { severity = sev },
                            label = { Text(sev.name, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = tint.copy(alpha = 0.25f),
                                selectedLabelColor = tint,
                                containerColor = CyberCardBg,
                                labelColor = TextMuted
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Description
                Text("Description / Vector Context", fontSize = 11.sp, color = TextSecondary)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Detects specific injection pattern", fontSize = 12.sp, color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = CyberCardBg,
                        unfocusedContainerColor = CyberCardBg,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedIndicatorColor = CyberCyan,
                        unfocusedIndicatorColor = CyberCardBorder
                    )
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(errorMessage!!, color = StatusCritical, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (name.isBlank()) {
                            errorMessage = "Rule name cannot be empty"
                            return@Button
                        }
                        if (regex.isBlank()) {
                            errorMessage = "Regex pattern cannot be empty"
                            return@Button
                        }
                        try {
                            Regex(regex) // validate regex
                        } catch (e: Exception) {
                            errorMessage = "Invalid regular expression: ${e.message}"
                            return@Button
                        }
                        onAddRule(name, category, regex, severity, description.ifBlank { name }, remediation)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF00363D))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Register Rule Dynamically", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
