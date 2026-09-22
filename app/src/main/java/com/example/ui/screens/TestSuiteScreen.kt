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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.model.TestStatus
import com.example.ui.SocViewModel
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
fun TestSuiteScreen(
    viewModel: SocViewModel,
    modifier: Modifier = Modifier
) {
    val testCases by viewModel.testCases.collectAsStateWithLifecycle()
    val isRunning by viewModel.isRunningTests.collectAsStateWithLifecycle()

    val passedCount = testCases.count { it.status == TestStatus.PASSED }
    val failedCount = testCases.count { it.status == TestStatus.FAILED }
    val totalCount = testCases.size
    val progress = if (totalCount > 0) (passedCount + failedCount).toFloat() / totalCount else 0f

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("test_suite_screen")
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
                        imageVector = Icons.Default.Rule,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Automated Test Suite",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Text(
                    text = "pytest -v tests/test_detector.py (14 Verification Cases)",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary
                )
            }

            IconButton(
                onClick = { viewModel.resetTests() },
                enabled = !isRunning,
                modifier = Modifier.testTag("reset_tests_btn")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Run Suite CTA Button
        Button(
            onClick = { viewModel.runAllTests() },
            enabled = !isRunning,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("run_test_suite_btn"),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF00363D))
        ) {
            if (isRunning) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF00363D), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Executing Verification Tests...", fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Run 14 Automated Verification Tests", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Progress & Tally Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(CyberCardBg)
                .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Execution Status", fontSize = 11.sp, color = TextMuted)
                    Text(
                        text = "$passedCount Passed • $failedCount Failed • $totalCount Total",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (failedCount > 0) StatusCritical else StatusSafe
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = if (failedCount > 0) StatusCritical else StatusSafe,
                    trackColor = Color(0xFF1E293B)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Test Cases List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(testCases) { test ->
                val (statusIcon, statusColor, statusLabel) = when (test.status) {
                    TestStatus.PASSED -> Triple(Icons.Default.CheckCircle, StatusSafe, "PASSED")
                    TestStatus.FAILED -> Triple(Icons.Default.Error, StatusCritical, "FAILED")
                    TestStatus.RUNNING -> Triple(Icons.Default.HourglassEmpty, CyberCyan, "RUNNING")
                    TestStatus.NOT_RUN -> Triple(Icons.Default.HourglassEmpty, TextMuted, "QUEUED")
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberCardBg)
                        .border(1.dp, statusColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = statusIcon,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = test.testName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = statusLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = test.description,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Payload snippet
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF05080E))
                                .padding(6.dp)
                        ) {
                            Text(
                                text = "payload: ${test.payload}",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = CyberCyan
                            )
                        }

                        if (test.actualLatencyMs != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Latency: ${test.actualLatencyMs}ms (SLA <200ms)",
                                    fontSize = 10.sp,
                                    color = StatusSafe,
                                    fontFamily = FontFamily.Monospace
                                )
                                if (test.actualScore != null) {
                                    Text(
                                        text = "Risk Score: ${test.actualScore}/100 [${test.actualClassification}]",
                                        fontSize = 10.sp,
                                        color = TextSecondary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        if (test.failureReason != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Error: ${test.failureReason}",
                                fontSize = 10.sp,
                                color = StatusCritical
                            )
                        }
                    }
                }
            }
        }
    }
}
