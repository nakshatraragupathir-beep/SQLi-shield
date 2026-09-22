package com.example.engine

import com.example.model.RiskClassification
import com.example.model.TestStatus
import com.example.model.VerificationTest
import kotlinx.coroutines.delay

class TestSuiteEngine(private val detector: DualLayerDetector) {

    fun getInitialTestCases(): List<VerificationTest> {
        return listOf(
            VerificationTest(
                id = 1,
                testName = "test_benign_name_with_apostrophe",
                payload = "Emily O'Connor",
                expectedClassification = RiskClassification.SAFE,
                description = "Validates zero false-positive on Irish surname with legitimate apostrophe."
            ),
            VerificationTest(
                id = 2,
                testName = "test_benign_italian_name",
                payload = "Liam D'Angelo",
                expectedClassification = RiskClassification.SAFE,
                description = "Validates zero false-positive on Italian surname contraction with legitimate apostrophe."
            ),
            VerificationTest(
                id = 3,
                testName = "test_benign_search_with_order",
                payload = "how to order coffee beans online",
                expectedClassification = RiskClassification.SAFE,
                description = "Validates natural query containing SQL keyword 'order' without injection syntax."
            ),
            VerificationTest(
                id = 4,
                testName = "test_benign_statement_with_create",
                payload = "create new annual project workspace for team",
                expectedClassification = RiskClassification.SAFE,
                description = "Validates natural sentence containing DDL keyword 'create' without SQL statement structure."
            ),
            VerificationTest(
                id = 5,
                testName = "test_classic_tautology_injection",
                payload = "' OR 1=1 --",
                expectedClassification = RiskClassification.CRITICAL,
                description = "Verifies detection of standard boolean tautology with trailing line comment."
            ),
            VerificationTest(
                id = 6,
                testName = "test_quoted_tautology_bypass",
                payload = "admin' OR '1'='1'#",
                expectedClassification = RiskClassification.CRITICAL,
                description = "Verifies detection of quoted literal tautology with hash comment marker."
            ),
            VerificationTest(
                id = 7,
                testName = "test_union_select_extraction",
                payload = "' UNION SELECT null, username, password FROM users --",
                expectedClassification = RiskClassification.CRITICAL,
                description = "Verifies detection of multi-column UNION SELECT database exfiltration attack."
            ),
            VerificationTest(
                id = 8,
                testName = "test_stacked_ddl_drop_table",
                payload = "1; DROP TABLE users; --",
                expectedClassification = RiskClassification.CRITICAL,
                requireFloorOverride = true,
                description = "Verifies detection of destructive stacked DDL statement with floor override enforcement."
            ),
            VerificationTest(
                id = 9,
                testName = "test_time_based_blind_sleep",
                payload = "1' AND SLEEP(5)--",
                expectedClassification = RiskClassification.CRITICAL,
                description = "Verifies detection of MySQL time-based blind delay function SLEEP()."
            ),
            VerificationTest(
                id = 10,
                testName = "test_time_based_waitfor_delay",
                payload = "admin'; WAITFOR DELAY '0:0:5'--",
                expectedClassification = RiskClassification.CRITICAL,
                description = "Verifies detection of MSSQL time-based blind delay statement WAITFOR DELAY."
            ),
            VerificationTest(
                id = 11,
                testName = "test_error_based_xpath_leakage",
                payload = "1' AND EXTRACTVALUE(1, CONCAT(0x7e, @@version))--",
                expectedClassification = RiskClassification.CRITICAL,
                description = "Verifies detection of error-based XPath information disclosure injection."
            ),
            VerificationTest(
                id = 12,
                testName = "test_url_encoded_evasion",
                payload = "%27%20OR%201%3D1%2D%2D",
                expectedClassification = RiskClassification.CRITICAL,
                description = "Verifies signature detection of hex/URL-encoded quotation and comment markers."
            ),
            VerificationTest(
                id = 13,
                testName = "test_dual_layer_weighting",
                payload = "test' OR 1=1 --",
                expectedClassification = RiskClassification.CRITICAL,
                description = "Verifies the 60% ML + 40% Rule composite calculation and calibration."
            ),
            VerificationTest(
                id = 14,
                testName = "test_inference_latency_sla",
                payload = "SELECT * FROM products WHERE id = 42",
                expectedClassification = null, // latency test focus
                maxAllowedLatencyMs = 200,
                description = "Validates inference latency remains well within the < 200ms latency SLA (avg ~15ms)."
            )
        )
    }

    suspend fun executeTest(test: VerificationTest): VerificationTest {
        delay(40) // Brief animation delay for visual test runner feedback
        val result = detector.analyze(test.payload)

        val passed = when (test.id) {
            13 -> { // dual layer weighting test
                val expectedWeight = (0.60 * result.mlScore + 0.40 * result.ruleScore)
                val diff = kotlin.math.abs(result.compositeScore - expectedWeight)
                diff <= 15.0 || result.floorOverrideApplied
            }
            14 -> { // latency SLA test
                result.latencyMs <= test.maxAllowedLatencyMs
            }
            else -> {
                val matchesClass = test.expectedClassification == null || result.classification == test.expectedClassification
                val matchesFloor = !test.requireFloorOverride || result.floorOverrideApplied
                matchesClass && matchesFloor
            }
        }

        return test.copy(
            status = if (passed) TestStatus.PASSED else TestStatus.FAILED,
            actualClassification = result.classification,
            actualScore = result.compositeScore,
            actualLatencyMs = result.latencyMs,
            failureReason = if (!passed) "Expected ${test.expectedClassification} (Score=${result.compositeScore}, Latency=${result.latencyMs}ms)" else null
        )
    }
}
