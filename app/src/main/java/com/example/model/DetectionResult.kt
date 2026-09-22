package com.example.model

enum class RiskClassification(val displayName: String) {
    SAFE("Safe"),
    SUSPICIOUS("Suspicious"),
    CRITICAL("Critical")
}

enum class RuleSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

data class RuleMatch(
    val ruleId: String,
    val ruleName: String,
    val category: String,
    val severity: RuleSeverity,
    val description: String,
    val matchedSubstring: String = ""
)

data class DetectionResult(
    val id: Long = 0,
    val payload: String,
    val compositeScore: Int, // 0 - 100
    val mlScore: Int,        // 0 - 100 (60% weight)
    val ruleScore: Int,      // 0 - 100 (40% weight)
    val classification: RiskClassification,
    val floorOverrideApplied: Boolean,
    val latencyMs: Long,
    val clientIp: String,
    val timestamp: Long,
    val triggeredRules: List<RuleMatch>,
    val features: Map<String, Double>
)

data class SecurityRule(
    val id: String,
    val name: String,
    val category: String,
    val regexPattern: String,
    val severity: RuleSeverity,
    val description: String,
    val remediation: String,
    val isCustom: Boolean = false,
    val isEnabled: Boolean = true
)

data class ThreatAlert(
    val id: String,
    val timestamp: Long,
    val payload: String,
    val clientIp: String,
    val compositeScore: Int,
    val classification: RiskClassification,
    val primaryThreat: String,
    val advisory: String,
    val structuredJson: String
)

data class SocSummary(
    val totalScans: Int,
    val safeCount: Int,
    val suspiciousCount: Int,
    val criticalCount: Int,
    val avgLatencyMs: Double,
    val slaAdherencePercent: Double,
    val falsePositiveRatePercent: Double = 0.0
)

data class VerificationTest(
    val id: Int,
    val testName: String,
    val payload: String,
    val expectedClassification: RiskClassification?,
    val requireFloorOverride: Boolean = false,
    val maxAllowedLatencyMs: Long = 200,
    val description: String,
    var status: TestStatus = TestStatus.NOT_RUN,
    var actualClassification: RiskClassification? = null,
    var actualScore: Int? = null,
    var actualLatencyMs: Long? = null,
    var failureReason: String? = null
)

enum class TestStatus {
    NOT_RUN, RUNNING, PASSED, FAILED
}
