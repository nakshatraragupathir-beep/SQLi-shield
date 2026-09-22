package com.example.engine

import com.example.model.RuleMatch
import com.example.model.RuleSeverity
import com.example.model.SecurityRule
import java.util.concurrent.CopyOnWriteArrayList

class RuleEngine {

    private val rules = CopyOnWriteArrayList<SecurityRule>()

    init {
        loadDefaultRules()
    }

    private fun loadDefaultRules() {
        rules.clear()
        rules.addAll(
            listOf(
                SecurityRule(
                    id = "RULE-UNION-01",
                    name = "UNION-Based Extraction",
                    category = "Data Extraction",
                    regexPattern = "(?i)\\bUNION\\s+(ALL\\s+|DISTINCT\\s+)?SELECT\\b",
                    severity = RuleSeverity.CRITICAL,
                    description = "Detects UNION SELECT extraction vectors intended to exfiltrate unauthorized table columns.",
                    remediation = "Use parameterized queries / PreparedStatements and enforce strict ORM field mapping."
                ),
                SecurityRule(
                    id = "RULE-TAUTOLOGY-02",
                    name = "Boolean Tautology Bypass",
                    category = "Authentication Bypass",
                    regexPattern = "(?i)('\\s*(OR|AND)\\s*('?[0-9a-z]+'?)\\s*=\\s*\\3)|('\\s*OR\\s*1\\s*=\\s*1)|('\\s*OR\\s*'1'\\s*=\\s*'1')|('\\s*OR\\s*true\\b)|(\\bOR\\s+1\\s*=\\s*1\\b)",
                    severity = RuleSeverity.CRITICAL,
                    description = "Identifies classic and quoted boolean tautologies (' OR 1=1) forcing conditional statements to evaluate to true.",
                    remediation = "Never concatenate untrusted input into WHERE clauses; bind user input parameters safely."
                ),
                SecurityRule(
                    id = "RULE-TIME-03",
                    name = "Time-Based Blind Delay",
                    category = "Blind Injection",
                    regexPattern = "(?i)\\b(WAITFOR\\s+DELAY|SLEEP\\s*\\([0-9]+\\)|BENCHMARK\\s*\\([0-9]+|PG_SLEEP\\s*\\()",
                    severity = RuleSeverity.CRITICAL,
                    description = "Detects time-delay functions (WAITFOR DELAY, SLEEP) utilized for inference and blind exfiltration.",
                    remediation = "Disable high-privilege DB diagnostic routines and enforce execution query timeouts at the gateway."
                ),
                SecurityRule(
                    id = "RULE-XPATH-04",
                    name = "Error-Based XPath Leakage",
                    category = "Error Disclosure",
                    regexPattern = "(?i)\\b(EXTRACTVALUE|UPDATEXML|EXP\\s*\\(~|CTXSYS\\.DRVXPATH)",
                    severity = RuleSeverity.CRITICAL,
                    description = "Catches XPath/XML error disclosure functions (EXTRACTVALUE, UPDATEXML) used to trigger syntax error reflections.",
                    remediation = "Suppress database verbose errors in production and sanitize input with strict type casting."
                ),
                SecurityRule(
                    id = "RULE-STACKED-05",
                    name = "Stacked DDL / Command Execution",
                    category = "Data Destruction",
                    regexPattern = "(?i)(;\\s*(DROP|ALTER|TRUNCATE)\\s+TABLE)|(;\\s*EXEC\\s+(xp_cmdshell|sp_executesql))",
                    severity = RuleSeverity.CRITICAL,
                    description = "Identifies dangerous semicolon-stacked DDL commands attempting destructive table drops or system execution.",
                    remediation = "Disable multi-statement queries in database drivers and run the app user with least privilege permissions."
                ),
                SecurityRule(
                    id = "RULE-EVASION-06",
                    name = "URL-Encoded Evasion Markers",
                    category = "Evasion Technique",
                    regexPattern = "(?i)(%27|%22|%2D%2D|%23|%3B|%2F%2A|%5C|%00)",
                    severity = RuleSeverity.HIGH,
                    description = "Detects URL-encoded SQL syntax markers (%27 for quote, %2D%2D for comments, %00 for null byte) used to evade WAFs.",
                    remediation = "Perform recursive URL decoding at the web server layer before applying security evaluation filters."
                ),
                SecurityRule(
                    id = "RULE-COMMENT-07",
                    name = "Trailing SQL Comment Truncation",
                    category = "Syntax Truncation",
                    regexPattern = "(?i)(--\\s*$|--[\\s+]|/\\*.*?\\*/|#\\s*$)",
                    severity = RuleSeverity.MEDIUM,
                    description = "Catches trailing SQL comment markers designed to neutralize legitimate trailing query constraints.",
                    remediation = "Strip or reject unexpected comment delimiters from standard alphanumeric user inputs."
                )
            )
        )
    }

    fun getAllRules(): List<SecurityRule> = rules.toList()

    fun addCustomRule(
        name: String,
        category: String,
        regexPattern: String,
        severity: RuleSeverity,
        description: String,
        remediation: String
    ): SecurityRule {
        val newRule = SecurityRule(
            id = "RULE-CUSTOM-${System.currentTimeMillis() % 10000}",
            name = name,
            category = category,
            regexPattern = regexPattern,
            severity = severity,
            description = description,
            remediation = remediation,
            isCustom = true,
            isEnabled = true
        )
        rules.add(newRule)
        return newRule
    }

    fun toggleRule(ruleId: String): Boolean {
        val idx = rules.indexOfFirst { it.id == ruleId }
        if (idx != -1) {
            val current = rules[idx]
            val updated = current.copy(isEnabled = !current.isEnabled)
            rules[idx] = updated
            return updated.isEnabled
        }
        return false
    }

    fun removeCustomRule(ruleId: String): Boolean {
        return rules.removeIf { it.id == ruleId && it.isCustom }
    }

    /**
     * Evaluates all active rules against the input.
     * Returns matching rules, rule score (0 - 100), and whether a critical floor override was triggered.
     */
    fun evaluateRules(input: String): RuleEvaluation {
        val matches = ArrayList<RuleMatch>()
        var maxSeverityScore = 0
        var hasCriticalFloorOverride = false

        for (rule in rules) {
            if (!rule.isEnabled) continue
            try {
                val pattern = Regex(rule.regexPattern)
                val matchResult = pattern.find(input)
                if (matchResult != null) {
                    matches.add(
                        RuleMatch(
                            ruleId = rule.id,
                            ruleName = rule.name,
                            category = rule.category,
                            severity = rule.severity,
                            description = rule.description,
                            matchedSubstring = matchResult.value
                        )
                    )

                    val severityWeight = when (rule.severity) {
                        RuleSeverity.CRITICAL -> 95
                        RuleSeverity.HIGH -> 75
                        RuleSeverity.MEDIUM -> 50
                        RuleSeverity.LOW -> 25
                    }

                    if (severityWeight > maxSeverityScore) {
                        maxSeverityScore = severityWeight
                    }

                    if (rule.severity == RuleSeverity.CRITICAL) {
                        hasCriticalFloorOverride = true
                    }
                }
            } catch (_: Exception) {
                // Ignore invalid regex in user-added rules
            }
        }

        // Aggregate rule score
        val calculatedRuleScore = if (matches.isEmpty()) {
            0
        } else {
            // Base on max triggered severity + additive count bonus
            (maxSeverityScore + (matches.size - 1) * 3).coerceIn(0, 100)
        }

        return RuleEvaluation(
            ruleScore = calculatedRuleScore,
            matches = matches,
            hasCriticalFloorOverride = hasCriticalFloorOverride
        )
    }

    data class RuleEvaluation(
        val ruleScore: Int,
        val matches: List<RuleMatch>,
        val hasCriticalFloorOverride: Boolean
    )
}
