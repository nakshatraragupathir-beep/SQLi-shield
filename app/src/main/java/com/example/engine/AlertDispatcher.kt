package com.example.engine

import com.example.model.DetectionResult
import com.example.model.RiskClassification
import com.example.model.ThreatAlert
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AlertDispatcher {

    private val _alertStream = MutableStateFlow<List<ThreatAlert>>(emptyList())
    val alertStream: StateFlow<List<ThreatAlert>> = _alertStream.asStateFlow()

    private val _logFileBuffer = MutableStateFlow<List<String>>(emptyList())
    val logFileBuffer: StateFlow<List<String>> = _logFileBuffer.asStateFlow()
    val logBuffer: StateFlow<List<String>> = _logFileBuffer.asStateFlow()

    fun dispatchIfCritical(result: DetectionResult): ThreatAlert? {
        if (result.classification != RiskClassification.CRITICAL) {
            return null
        }

        val alertId = "ALT-${UUID.randomUUID().toString().take(8).uppercase()}"
        val primaryRule = result.triggeredRules.firstOrNull()?.ruleName
            ?: "Composite ML Heuristic Anomaly (Score ${result.compositeScore}/100)"

        val advisory = when {
            result.payload.contains("union", ignoreCase = true) ->
                "CRITICAL: Immediate UNION injection vector. Audit database privileges, verify that ORM queries use bound parameters, and check application server logs for column structure enumeration."
            result.payload.contains("drop", ignoreCase = true) || result.payload.contains("exec", ignoreCase = true) ->
                "SEVERE ALARM: Destructive stacked query attempt detected. Verify database user privileges do not grant DDL DROP/ALTER permissions to web applications. Restrict multi-statement queries in JDBC/DB connection strings."
            result.payload.contains("sleep", ignoreCase = true) || result.payload.contains("waitfor", ignoreCase = true) ->
                "HIGH PRIORITY: Time-based blind SQL injection attempt detected. Enforce strict web application firewall (WAF) rule sets, monitor database connection thread pools, and verify input sanitization."
            result.payload.contains("extractvalue", ignoreCase = true) ->
                "ERROR-BASED LEAK: XPath injection attempt detected. Ensure custom generic error pages are shown to users and disable database driver debug tracing in production."
            else ->
                "AUTHENTICATION BYPASS: Boolean logic manipulation detected. Review login authentication routines and ensure parameterized PreparedStatement or ORM query builders are enforced."
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        val isoDate = sdf.format(Date(result.timestamp))

        val escapedPayload = result.payload.replace("\"", "\\\"").replace("\n", "\\n")
        val triggeredNames = result.triggeredRules.map { "\"${it.ruleName}\"" }

        val structuredJson = """
        {
          "alert_id": "$alertId",
          "timestamp": "$isoDate",
          "severity": "CRITICAL",
          "client_ip": "${result.clientIp}",
          "composite_score": ${result.compositeScore},
          "ml_layer_score": ${result.mlScore},
          "rule_layer_score": ${result.ruleScore},
          "floor_override": ${result.floorOverrideApplied},
          "latency_ms": ${result.latencyMs},
          "payload": "$escapedPayload",
          "triggered_rules": [${triggeredNames.joinToString(", ")}],
          "advisory": "$advisory"
        }
        """.trimIndent()

        val alert = ThreatAlert(
            id = alertId,
            timestamp = result.timestamp,
            payload = result.payload,
            clientIp = result.clientIp,
            compositeScore = result.compositeScore,
            classification = result.classification,
            primaryThreat = primaryRule,
            advisory = advisory,
            structuredJson = structuredJson
        )

        // Prepend to in-memory alert stream (keep last 50)
        _alertStream.value = (listOf(alert) + _alertStream.value).take(50)

        // Log entry to simulated logs/threat_alerts.log
        val logLine = "[$isoDate] CRITICAL ALARM $alertId from ${result.clientIp} - Score ${result.compositeScore}/100: $primaryRule"
        _logFileBuffer.value = (listOf(logLine) + _logFileBuffer.value).take(100)

        return alert
    }

    fun clearAlerts() {
        _alertStream.value = emptyList()
        _logFileBuffer.value = emptyList()
    }
}
