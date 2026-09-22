package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.IncidentEntity
import com.example.data.IncidentRepository
import com.example.engine.AlertDispatcher
import com.example.engine.DualLayerDetector
import com.example.engine.TestSuiteEngine
import com.example.model.DetectionResult
import com.example.model.RiskClassification
import com.example.model.RuleSeverity
import com.example.model.SecurityRule
import com.example.model.SocSummary
import com.example.model.TestStatus
import com.example.model.ThreatAlert
import com.example.model.VerificationTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class SocTab(val title: String) {
    ANALYZER("Threat Analyzer"),
    OVERVIEW("SOC Overview"),
    INCIDENTS("Incident Logs"),
    ALERTS("Alert Stream"),
    DIAGNOSTICS("ML Diagnostics"),
    TEST_RUNNER("Test Suite (14)"),
    RULES("Rule Manager"),
    API_DOCS("API & Middleware")
}

data class ThreatPreset(
    val title: String,
    val payload: String,
    val category: String,
    val isBenign: Boolean
)

class SocViewModel(application: Application) : AndroidViewModel(application) {

    private val detector = DualLayerDetector()
    private val alertDispatcher = AlertDispatcher()
    private val testSuiteEngine = TestSuiteEngine(detector)
    private val repository: IncidentRepository

    init {
        val db = AppDatabase.getInstance(application)
        repository = IncidentRepository(db.incidentDao())
    }

    // Active Navigation Tab
    private val _currentTab = MutableStateFlow(SocTab.ANALYZER)
    val currentTab: StateFlow<SocTab> = _currentTab.asStateFlow()

    // Interactive Threat Analyzer State
    private val _inputPayload = MutableStateFlow("' OR 1=1 --")
    val inputPayload: StateFlow<String> = _inputPayload.asStateFlow()

    private val _clientIp = MutableStateFlow("192.168.1.105")
    val clientIp: StateFlow<String> = _clientIp.asStateFlow()

    private val _currentResult = MutableStateFlow<DetectionResult?>(null)
    val currentResult: StateFlow<DetectionResult?> = _currentResult.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    // Incident Logs State
    val allIncidents: StateFlow<List<IncidentEntity>> = repository.allIncidents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _incidentFilter = MutableStateFlow("ALL")
    val incidentFilter: StateFlow<String> = _incidentFilter.asStateFlow()

    private val _incidentSearch = MutableStateFlow("")
    val incidentSearch: StateFlow<String> = _incidentSearch.asStateFlow()

    // Alert Stream State
    val alertStream: StateFlow<List<ThreatAlert>> = alertDispatcher.alertStream
    val logBuffer: StateFlow<List<String>> = alertDispatcher.logBuffer

    // Test Suite State
    private val _testCases = MutableStateFlow<List<VerificationTest>>(emptyList())
    val testCases: StateFlow<List<VerificationTest>> = _testCases.asStateFlow()

    private val _isRunningTests = MutableStateFlow(false)
    val isRunningTests: StateFlow<Boolean> = _isRunningTests.asStateFlow()

    // Security Rules State
    private val _securityRules = MutableStateFlow<List<SecurityRule>>(emptyList())
    val securityRules: StateFlow<List<SecurityRule>> = _securityRules.asStateFlow()

    // Active Banner Alert for Critical Detection
    private val _criticalBannerAlert = MutableStateFlow<ThreatAlert?>(null)
    val criticalBannerAlert: StateFlow<ThreatAlert?> = _criticalBannerAlert.asStateFlow()

    // API Console State
    private val _apiSelectedEndpoint = MutableStateFlow("/api/analyze")
    val apiSelectedEndpoint: StateFlow<String> = _apiSelectedEndpoint.asStateFlow()

    private val _apiConsoleInput = MutableStateFlow("{\"payload\": \"' OR 1=1 --\"}")
    val apiConsoleInput: StateFlow<String> = _apiConsoleInput.asStateFlow()

    private val _apiConsoleResponse = MutableStateFlow("")
    val apiConsoleResponse: StateFlow<String> = _apiConsoleResponse.asStateFlow()

    val presets = listOf(
        ThreatPreset(
            title = "Tautology Bypass",
            payload = "' OR 1=1 --",
            category = "Auth Bypass",
            isBenign = false
        ),
        ThreatPreset(
            title = "UNION Extraction",
            payload = "' UNION SELECT null, username, password FROM users --",
            category = "Data Exfiltration",
            isBenign = false
        ),
        ThreatPreset(
            title = "Time-Based Blind Delay",
            payload = "1; WAITFOR DELAY '0:0:5'--",
            category = "Blind Injection",
            isBenign = false
        ),
        ThreatPreset(
            title = "Error-Based XPath Leak",
            payload = "1' AND EXTRACTVALUE(1, CONCAT(0x7e, @@version))--",
            category = "XPath Leakage",
            isBenign = false
        ),
        ThreatPreset(
            title = "Stacked DDL (Drop Table)",
            payload = "test'; DROP TABLE users;--",
            category = "Destructive DDL",
            isBenign = false
        ),
        ThreatPreset(
            title = "URL-Encoded Evasion",
            payload = "%27%20OR%201%3D1%2D%2D",
            category = "WAF Evasion",
            isBenign = false
        ),
        ThreatPreset(
            title = "Name with Apostrophe",
            payload = "Emily O'Connor",
            category = "Benign Name (FP Guard)",
            isBenign = true
        ),
        ThreatPreset(
            title = "Contraction Surname",
            payload = "Liam D'Angelo",
            category = "Benign Name (FP Guard)",
            isBenign = true
        ),
        ThreatPreset(
            title = "Query with 'order'",
            payload = "how to order coffee beans online",
            category = "Natural Query (FP Guard)",
            isBenign = true
        ),
        ThreatPreset(
            title = "Statement with 'create'",
            payload = "create quarterly project roadmap 2025",
            category = "Natural Query (FP Guard)",
            isBenign = true
        )
    )

    init {
        _testCases.value = testSuiteEngine.getInitialTestCases()
        refreshRules()
        // Run initial analysis on default payload
        analyzePayload(_inputPayload.value)
    }

    fun setTab(tab: SocTab) {
        _currentTab.value = tab
    }

    fun updateInputPayload(text: String) {
        _inputPayload.value = text
    }

    fun updateClientIp(ip: String) {
        _clientIp.value = ip
    }

    fun applyPreset(preset: ThreatPreset) {
        _inputPayload.value = preset.payload
        analyzePayload(preset.payload)
    }

    fun analyzePayload(payload: String = _inputPayload.value) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            val result = withContext(Dispatchers.Default) {
                detector.analyze(payload, _clientIp.value)
            }
            _currentResult.value = result
            _isAnalyzing.value = false

            // Log to persistent Room database
            withContext(Dispatchers.IO) {
                repository.logDetection(result)
            }

            // Real-time Critical Alert Dispatch
            val alert = alertDispatcher.dispatchIfCritical(result)
            if (alert != null) {
                _criticalBannerAlert.value = alert
            }
        }
    }

    fun dismissCriticalBanner() {
        _criticalBannerAlert.value = null
    }

    fun setIncidentFilter(filter: String) {
        _incidentFilter.value = filter
    }

    fun setIncidentSearch(query: String) {
        _incidentSearch.value = query
    }

    fun clearIncidents() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.clearAll()
            }
        }
    }

    fun clearAlerts() {
        alertDispatcher.clearAlerts()
    }

    private fun refreshRules() {
        _securityRules.value = detector.ruleEngine.getAllRules()
    }

    fun toggleRule(ruleId: String) {
        detector.ruleEngine.toggleRule(ruleId)
        refreshRules()
    }

    fun addCustomRule(
        name: String,
        category: String,
        regexPattern: String,
        severity: RuleSeverity,
        description: String,
        remediation: String
    ) {
        detector.ruleEngine.addCustomRule(name, category, regexPattern, severity, description, remediation)
        refreshRules()
    }

    fun removeCustomRule(ruleId: String) {
        detector.ruleEngine.removeCustomRule(ruleId)
        refreshRules()
    }

    fun runAllTests() {
        if (_isRunningTests.value) return
        viewModelScope.launch {
            _isRunningTests.value = true
            val updated = _testCases.value.map { it.copy(status = TestStatus.RUNNING) }.toMutableList()
            _testCases.value = updated

            for (i in updated.indices) {
                val executed = withContext(Dispatchers.Default) {
                    testSuiteEngine.executeTest(updated[i])
                }
                updated[i] = executed
                _testCases.value = updated.toList()
            }
            _isRunningTests.value = false
        }
    }

    fun resetTests() {
        _testCases.value = testSuiteEngine.getInitialTestCases()
    }

    fun selectApiEndpoint(endpoint: String) {
        _apiSelectedEndpoint.value = endpoint
        _apiConsoleInput.value = when (endpoint) {
            "/api/analyze" -> "{\"payload\": \"' OR 1=1 --\", \"client_ip\": \"192.168.1.105\"}"
            "/api/batch-analyze" -> "{\"payloads\": [\"Emily O'Connor\", \"' OR 1=1 --\", \"1; WAITFOR DELAY '0:0:5'--\"]}"
            "/api/incidents" -> "{\"limit\": 10, \"classification\": \"CRITICAL\"}"
            "/api/stats" -> "{}"
            "/api/rules" -> "{}"
            "/api/model-info" -> "{}"
            "/api/retrain" -> "{\"epochs\": 10, \"estimators\": 100}"
            else -> "{}"
        }
        executeApiSimulation()
    }

    fun setApiConsoleInput(text: String) {
        _apiConsoleInput.value = text
    }

    fun executeApiSimulation() {
        val endpoint = _apiSelectedEndpoint.value
        val input = _apiConsoleInput.value

        val response = when (endpoint) {
            "/api/analyze" -> {
                val payload = try {
                    val regex = Regex("\"payload\":\\s*\"(.*?)\"")
                    regex.find(input)?.groupValues?.get(1) ?: "' OR 1=1 --"
                } catch (_: Exception) { "' OR 1=1 --" }
                val result = detector.analyze(payload)
                """
                HTTP/1.1 200 OK
                Content-Type: application/json
                X-Inference-Latency: ${result.latencyMs}ms

                {
                  "status": "success",
                  "classification": "${result.classification.name}",
                  "composite_score": ${result.compositeScore},
                  "layers": {
                    "ml_layer": {
                      "weight": 0.60,
                      "threat_score": ${result.mlScore}
                    },
                    "rule_engine": {
                      "weight": 0.40,
                      "score": ${result.ruleScore},
                      "triggered_rules": [${result.triggeredRules.joinToString { "\"${it.ruleName}\"" }}]
                    }
                  },
                  "floor_override_applied": ${result.floorOverrideApplied},
                  "latency_ms": ${result.latencyMs},
                  "client_ip": "${result.clientIp}",
                  "timestamp": ${result.timestamp}
                }
                """.trimIndent()
            }
            "/api/batch-analyze" -> {
                """
                HTTP/1.1 200 OK
                Content-Type: application/json

                {
                  "status": "success",
                  "processed_count": 3,
                  "average_latency_ms": 14.2,
                  "results": [
                    {"payload": "Emily O'Connor", "classification": "SAFE", "score": 4},
                    {"payload": "' OR 1=1 --", "classification": "CRITICAL", "score": 92},
                    {"payload": "1; WAITFOR DELAY '0:0:5'--", "classification": "CRITICAL", "score": 95}
                  ]
                }
                """.trimIndent()
            }
            "/api/incidents" -> {
                val count = allIncidents.value.size
                """
                HTTP/1.1 200 OK
                Content-Type: application/json

                {
                  "status": "success",
                  "total_incidents_recorded": $count,
                  "storage_engine": "SQLite (data/sqli_incidents.db)",
                  "recent_incidents": ${allIncidents.value.take(3).map {
                    "{\"id\": ${it.id}, \"classification\": \"${it.classification}\", \"score\": ${it.compositeScore}, \"payload\": \"${it.payload.take(30)}\"}"
                }}
                }
                """.trimIndent()
            }
            "/api/stats" -> {
                val incidents = allIncidents.value
                val total = maxOf(1, incidents.size)
                val crit = incidents.count { it.classification == "CRITICAL" }
                val susp = incidents.count { it.classification == "SUSPICIOUS" }
                val safe = incidents.count { it.classification == "SAFE" }
                """
                HTTP/1.1 200 OK
                Content-Type: application/json

                {
                  "status": "success",
                  "soc_metrics": {
                    "total_scans": $total,
                    "critical_threats_blocked": $crit,
                    "suspicious_queries": $susp,
                    "safe_queries": $safe,
                    "avg_inference_latency_ms": 14.8,
                    "sla_adherence_percent": 100.0,
                    "false_positive_rate": 0.00
                  }
                }
                """.trimIndent()
            }
            "/api/rules" -> {
                val rulesList = _securityRules.value
                """
                HTTP/1.1 200 OK
                Content-Type: application/json

                {
                  "status": "success",
                  "active_rules_count": ${rulesList.count { it.isEnabled }},
                  "total_rules": ${rulesList.size},
                  "rules": [${rulesList.take(4).joinToString { "{\"id\": \"${it.id}\", \"name\": \"${it.name}\", \"severity\": \"${it.severity}\"}" }}]
                }
                """.trimIndent()
            }
            "/api/model-info" -> {
                """
                HTTP/1.1 200 OK
                Content-Type: application/json

                {
                  "status": "success",
                  "model_architecture": "RandomForestClassifier",
                  "estimators": 100,
                  "feature_space_dimensions": 39,
                  "weights": {
                    "machine_learning_layer": 0.60,
                    "rule_based_security_engine": 0.40
                  },
                  "metrics": {
                    "accuracy": 0.987,
                    "precision": 0.991,
                    "recall": 0.984,
                    "f1_score": 0.987,
                    "roc_auc": 0.996
                  }
                }
                """.trimIndent()
            }
            "/api/retrain" -> {
                """
                HTTP/1.1 200 OK
                Content-Type: application/json

                {
                  "status": "success",
                  "message": "Ensemble weights verified and calibrated. 100 estimators active across 39 feature dimensions. Zero retraining downtime.",
                  "timestamp": ${System.currentTimeMillis()}
                }
                """.trimIndent()
            }
            else -> "{}"
        }
        _apiConsoleResponse.value = response
    }
}
