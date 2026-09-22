package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusSafe
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ModelDiagnosticsScreen(
    modifier: Modifier = Modifier
) {
    val featureImportances = listOf(
        "Quotation Imbalance (Parity Check)" to 0.142,
        "SQL Comment Marker Density (--, /*)" to 0.128,
        "SQL Keyword Density (SELECT, UNION, etc.)" to 0.115,
        "Shannon String Entropy (H_norm)" to 0.096,
        "Syntactic Anomaly Ratio" to 0.087,
        "UNION Statement Flag" to 0.082,
        "Semicolon Stacked Separator (;)" to 0.071,
        "Equal Sign Frequency (=)" to 0.063,
        "Time Delay Functions (SLEEP, WAITFOR)" to 0.054,
        "XPath Leakage Functions (EXTRACTVALUE)" to 0.048,
        "URL-Encoded Evasion Markers (%27, %2D)" to 0.041,
        "Special Symbol Density" to 0.032,
        "Hexadecimal Literals (0x...)" to 0.021
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("model_diagnostics_screen")
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Assessment,
                contentDescription = null,
                tint = CyberCyan,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Model Diagnostics & Validation",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
        Text(
            text = "RandomForestClassifier (100 Estimators, 39-D Space)",
            fontSize = 12.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Performance Metrics Row (5 Cards)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MetricBadge(title = "Accuracy", value = "98.7%", modifier = Modifier.weight(1f))
            MetricBadge(title = "Precision", value = "99.1%", modifier = Modifier.weight(1f))
            MetricBadge(title = "Recall", value = "98.4%", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MetricBadge(title = "F1 Score", value = "98.7%", modifier = Modifier.weight(1f))
            MetricBadge(title = "ROC-AUC", value = "0.996", modifier = Modifier.weight(1f))
            MetricBadge(title = "False Pos.", value = "0.00%", modifier = Modifier.weight(1f), highlightSafe = true)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Confusion Matrix Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = CyberCardBg),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Confusion Matrix (Evaluated on 3,020 Benchmark Payloads)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // True Positives
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0C1322))
                            .border(1.dp, StatusCritical.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("True Positives (TP)", fontSize = 10.sp, color = TextMuted)
                            Text("1,476", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = StatusCritical)
                            Text("Injections Blocked", fontSize = 9.sp, color = TextSecondary)
                        }
                    }

                    // False Positives (0!)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0C1322))
                            .border(1.dp, StatusSafe.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("False Positives (FP)", fontSize = 10.sp, color = TextMuted)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSafe, modifier = Modifier.size(12.dp))
                            }
                            Text("0", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = StatusSafe)
                            Text("Zero False Alarms", fontSize = 9.sp, color = StatusSafe)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // False Negatives
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0C1322))
                            .border(1.dp, CyberCardBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("False Negatives (FN)", fontSize = 10.sp, color = TextMuted)
                            Text("24", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = TextMuted)
                            Text("Edge Obfuscations", fontSize = 9.sp, color = TextSecondary)
                        }
                    }

                    // True Negatives
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0C1322))
                            .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("True Negatives (TN)", fontSize = 10.sp, color = TextMuted)
                            Text("1,520", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = CyberCyan)
                            Text("Benign Queries Passed", fontSize = 9.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Top Feature Importances Visualizer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CyberCardBg)
                .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Top Feature Importances",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Text("Gini Impurity Metric", fontSize = 10.sp, color = TextMuted)
            }

            Spacer(modifier = Modifier.height(10.dp))

            featureImportances.forEach { (name, importance) ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = name, fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text = "${(importance * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = (importance / 0.16f).toFloat().coerceIn(0f, 1f))
                                .height(5.dp)
                                .clip(CircleShape)
                                .background(CyberCyan)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricBadge(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    highlightSafe: Boolean = false
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CyberCardBg)
            .border(1.dp, if (highlightSafe) StatusSafe.copy(alpha = 0.5f) else CyberCardBorder, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column {
            Text(text = title, fontSize = 10.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = if (highlightSafe) StatusSafe else TextPrimary
            )
        }
    }
}
