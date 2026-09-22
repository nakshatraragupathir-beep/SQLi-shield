package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusSafe
import com.example.ui.theme.StatusSuspicious
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

data class FeatureDisplayGroup(
    val title: String,
    val description: String,
    val features: List<Pair<String, Double>>
)

@Composable
fun FeatureForensicsDialog(
    features: Map<String, Double>,
    onDismiss: () -> Unit
) {
    val entropyFeatures = listOf(
        "shannon_entropy" to (features["shannon_entropy"] ?: 0.0),
        "normalized_entropy" to (features["normalized_entropy"] ?: 0.0)
    )

    val lexicalFeatures = listOf(
        "string_length" to (features["string_length"] ?: 0.0),
        "alphanumeric_ratio" to (features["alphanumeric_ratio"] ?: 0.0),
        "digit_ratio" to (features["digit_ratio"] ?: 0.0),
        "whitespace_ratio" to (features["whitespace_ratio"] ?: 0.0),
        "punctuation_ratio" to (features["punctuation_ratio"] ?: 0.0),
        "uppercase_ratio" to (features["uppercase_ratio"] ?: 0.0),
        "special_symbol_ratio" to (features["special_symbol_ratio"] ?: 0.0),
        "non_ascii_ratio" to (features["non_ascii_ratio"] ?: 0.0)
    )

    val quotationFeatures = listOf(
        "single_quotes_count" to (features["single_quotes_count"] ?: 0.0),
        "double_quotes_count" to (features["double_quotes_count"] ?: 0.0),
        "backticks_count" to (features["backticks_count"] ?: 0.0),
        "quote_imbalance" to (features["quote_imbalance"] ?: 0.0),
        "legitimate_apostrophe" to (features["legitimate_apostrophe"] ?: 0.0),
        "quote_density" to (features["quote_density"] ?: 0.0),
        "escaped_quote_count" to (features["escaped_quote_count"] ?: 0.0)
    )

    val sqlFeatures = listOf(
        "sql_keyword_count" to (features["sql_keyword_count"] ?: 0.0),
        "distinct_sql_keywords" to (features["distinct_sql_keywords"] ?: 0.0),
        "sql_keyword_density" to (features["sql_keyword_density"] ?: 0.0),
        "ddl_keyword_count" to (features["ddl_keyword_count"] ?: 0.0),
        "dml_keyword_count" to (features["dml_keyword_count"] ?: 0.0),
        "union_statement_flag" to (features["union_statement_flag"] ?: 0.0),
        "sys_table_count" to (features["sys_table_count"] ?: 0.0),
        "blind_function_count" to (features["blind_function_count"] ?: 0.0),
        "xpath_function_count" to (features["xpath_function_count"] ?: 0.0),
        "boolean_operator_count" to (features["boolean_operator_count"] ?: 0.0)
    )

    val syntaxFeatures = listOf(
        "comment_marker_count" to (features["comment_marker_count"] ?: 0.0),
        "semicolon_count" to (features["semicolon_count"] ?: 0.0),
        "equal_sign_count" to (features["equal_sign_count"] ?: 0.0),
        "comparison_operator_count" to (features["comparison_operator_count"] ?: 0.0),
        "parentheses_imbalance" to (features["parentheses_imbalance"] ?: 0.0),
        "hex_literal_count" to (features["hex_literal_count"] ?: 0.0),
        "url_encoded_count" to (features["url_encoded_count"] ?: 0.0),
        "consecutive_space_ratio" to (features["consecutive_space_ratio"] ?: 0.0),
        "operator_density" to (features["operator_density"] ?: 0.0),
        "null_byte_count" to (features["null_byte_count"] ?: 0.0),
        "string_concat_count" to (features["string_concat_count"] ?: 0.0),
        "syntactic_anomaly_ratio" to (features["syntactic_anomaly_ratio"] ?: 0.0)
    )

    val groups = listOf(
        FeatureDisplayGroup("1. Shannon String Entropy", "Character distribution randomness and complexity", entropyFeatures),
        FeatureDisplayGroup("2. Lexical Distributions", "Character frequencies, alphanumeric & symbol density", lexicalFeatures),
        FeatureDisplayGroup("3. Quotation Imbalance", "Single/double quotes, balance parity & contraction filter", quotationFeatures),
        FeatureDisplayGroup("4. SQL Keyword Density", "SQL DDL/DML grammar, blind delays & UNION markers", sqlFeatures),
        FeatureDisplayGroup("5. Syntactic Anomalies", "Comment delimiters, semicolon stacking, hex/URL encodings", syntaxFeatures)
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1322)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
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
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "39-D Feature Space Forensics",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Extracted feature vector fed to 100-Tree Random Forest",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = CyberCardBorder)
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(groups) { group ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CyberCardBg)
                                .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = group.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberCyan
                            )
                            Text(
                                text = group.description,
                                fontSize = 11.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            group.features.forEach { (name, value) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = name.replace("_", " "),
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )

                                    val isAnomalous = when {
                                        name == "legitimate_apostrophe" && value > 0.5 -> false
                                        name.contains("quote_imbalance") && value > 0.5 -> true
                                        name.contains("union") && value > 0.0 -> true
                                        name.contains("comment") && value > 0.0 -> true
                                        name.contains("blind") && value > 0.0 -> true
                                        name.contains("xpath") && value > 0.0 -> true
                                        name.contains("ddl") && value > 0.0 -> true
                                        value > 0.5 && (name.contains("density") || name.contains("ratio")) -> true
                                        else -> false
                                    }

                                    val valColor = when {
                                        name == "legitimate_apostrophe" && value > 0.5 -> StatusSafe
                                        isAnomalous -> StatusCritical
                                        value > 0.0 -> CyberCyan
                                        else -> TextMuted
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isAnomalous) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(StatusCritical)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Text(
                                            text = String.format(Locale.US, "%.3f", value),
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = valColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
