package com.example.engine

import kotlin.math.ln
import kotlin.math.log2
import kotlin.math.max

data class FeatureExtractionResult(
    val features: Map<String, Double>,
    val orderedVector: DoubleArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FeatureExtractionResult
        return orderedVector.contentEquals(other.orderedVector)
    }

    override fun hashCode(): Int {
        return orderedVector.contentHashCode()
    }
}

object FeatureExtractor {

    private val SQL_KEYWORDS = setOf(
        "select", "union", "insert", "update", "delete", "drop", "alter", "exec", "execute",
        "create", "truncate", "declare", "from", "where", "having", "group", "order",
        "by", "join", "inner", "left", "right", "outer", "table", "database", "schema",
        "information_schema", "sysobjects", "pg_tables", "cast", "convert", "char",
        "concat", "load_file", "into", "outfile", "dumpfile", "sleep", "benchmark",
        "waitfor", "delay", "extractvalue", "updatexml", "xp_cmdshell", "sp_executesql"
    )

    private val DDL_KEYWORDS = setOf("drop", "alter", "truncate", "create")
    private val DML_KEYWORDS = setOf("insert", "update", "delete", "merge")
    private val BLIND_KEYWORDS = setOf("sleep", "benchmark", "waitfor", "delay", "pg_sleep")
    private val XPATH_KEYWORDS = setOf("extractvalue", "updatexml", "exp")
    private val SYS_TABLES = setOf("information_schema", "sys.", "sysobjects", "pg_catalog", "all_tables")
    private val BOOLEAN_KEYWORDS = setOf("and", "or", "xor", "not")

    /**
     * Extracts exactly 39 numerical features representing the input payload.
     */
    fun extractFeatures(input: String): FeatureExtractionResult {
        val len = input.length.toDouble()
        val safeLen = max(1.0, len)
        val lower = input.lowercase()

        // 1. Shannon String Entropy
        val entropy = calculateShannonEntropy(input)

        // 2. Normalized Entropy (Entropy / log2(len + 1))
        val maxEntropy = if (input.isEmpty()) 1.0 else log2(len + 1.0)
        val normEntropy = (entropy / max(1.0, maxEntropy)).coerceIn(0.0, 1.0)

        // 3. String Length
        val lengthFeature = len

        // 4. Alphanumeric Ratio
        val alphaNumCount = input.count { it.isLetterOrDigit() }.toDouble()
        val alphaNumRatio = alphaNumCount / safeLen

        // 5. Digit Ratio
        val digitCount = input.count { it.isDigit() }.toDouble()
        val digitRatio = digitCount / safeLen

        // 6. Whitespace Ratio
        val spaceCount = input.count { it.isWhitespace() }.toDouble()
        val spaceRatio = spaceCount / safeLen

        // 7. Punctuation Ratio
        val punctCount = input.count { !it.isLetterOrDigit() && !it.isWhitespace() }.toDouble()
        val punctRatio = punctCount / safeLen

        // 8. Uppercase Ratio
        val upperCount = input.count { it.isUpperCase() }.toDouble()
        val upperRatio = upperCount / safeLen

        // 9. Special Symbol Ratio
        val specialSymbols = setOf('\'', '"', ';', '-', '/', '*', '#', '=', '<', '>', '(', ')', '%', '$', '@', '&', '|', '^')
        val specialCount = input.count { it in specialSymbols }.toDouble()
        val specialRatio = specialCount / safeLen

        // 10. Non-ASCII Ratio
        val nonAsciiCount = input.count { it.code > 127 }.toDouble()
        val nonAsciiRatio = nonAsciiCount / safeLen

        // 11. Single Quotes Count
        val singleQuoteCount = input.count { it == '\'' }.toDouble()

        // 12. Double Quotes Count
        val doubleQuoteCount = input.count { it == '"' }.toDouble()

        // 13. Backtick Count
        val backtickCount = input.count { it == '`' }.toDouble()

        // 14. Quotation Imbalance (1.0 if odd single quotes or odd double quotes)
        val hasOddSingle = (singleQuoteCount.toInt() % 2 != 0)
        val hasOddDouble = (doubleQuoteCount.toInt() % 2 != 0)
        val quoteImbalance = if (hasOddSingle || hasOddDouble) 1.0 else 0.0

        // 15. Legitimate Apostrophe Indicator
        // Detects natural names like O'Connor, D'Angelo, or contractions like don't, it's
        val legitApostrophe = isLegitimateApostropheUsage(input)
        val legitApostropheScore = if (legitApostrophe) 1.0 else 0.0

        // 16. Quotation Density
        val totalQuotes = singleQuoteCount + doubleQuoteCount + backtickCount
        val quoteDensity = totalQuotes / safeLen

        // 17. Escaped Quotation Count (\', '', \")
        val escapedQuoteCount = (
            countOccurrences(input, "\\'") +
            countOccurrences(input, "''") +
            countOccurrences(input, "\\\"")
        ).toDouble()

        // Tokenize for lexical keyword inspection
        val words = lower.split(Regex("[^a-z0-9_]+")).filter { it.isNotBlank() }
        val wordCount = max(1.0, words.size.toDouble())

        // 18. SQL Keyword Count
        val sqlKeywordCount = words.count { it in SQL_KEYWORDS }.toDouble()

        // 19. Distinct SQL Keywords
        val distinctSqlKeywords = words.filter { it in SQL_KEYWORDS }.distinct().size.toDouble()

        // 20. SQL Keyword Density
        val sqlKeywordDensity = sqlKeywordCount / wordCount

        // 21. DDL Keyword Count
        val ddlCount = words.count { it in DDL_KEYWORDS }.toDouble()

        // 22. DML Keyword Count
        val dmlCount = words.count { it in DML_KEYWORDS }.toDouble()

        // 23. UNION Statement Indicator
        val hasUnion = if (lower.contains("union") && lower.contains("select")) 1.0 else if (lower.contains("union")) 0.5 else 0.0

        // 24. System / Info Schema Tables
        val sysTableCount = SYS_TABLES.count { lower.contains(it) }.toDouble()

        // 25. Time Delay / Blind Function Count
        val blindCount = BLIND_KEYWORDS.count { lower.contains(it) }.toDouble()

        // 26. XPath / Error-based Function Count
        val xpathCount = XPATH_KEYWORDS.count { lower.contains(it) }.toDouble()

        // 27. Boolean Logic Operator Count
        val booleanOpCount = words.count { it in BOOLEAN_KEYWORDS }.toDouble()

        // 28. Comment Indicators Count (--, /*, */, #)
        val commentMarkerCount = (
            countOccurrences(input, "--") +
            countOccurrences(input, "/*") +
            countOccurrences(input, "*/") +
            countOccurrences(input, "#")
        ).toDouble()

        // 29. Semicolon Count (stacked queries indicator)
        val semicolonCount = input.count { it == ';' }.toDouble()

        // 30. Equal Sign Count
        val equalSignCount = input.count { it == '=' }.toDouble()

        // 31. Comparison Operator Count (<, >, <=, >=, !=, <>, LIKE)
        val comparisonCount = (
            countOccurrences(input, "<=") +
            countOccurrences(input, ">=") +
            countOccurrences(input, "!=") +
            countOccurrences(input, "<>") +
            input.count { it == '<' || it == '>' } +
            words.count { it == "like" || it == "ilike" }
        ).toDouble()

        // 32. Parentheses Imbalance
        val openParen = input.count { it == '(' }
        val closeParen = input.count { it == ')' }
        val parenImbalance = kotlin.math.abs(openParen - closeParen).toDouble()

        // 33. Hexadecimal Literal Count (0x...)
        val hexCount = Regex("(?i)0x[0-9a-f]{2,}").findAll(input).count().toDouble()

        // 34. URL-Encoded Sequence Count (%20, %27, %3d, etc.)
        val urlEncodedCount = Regex("(?i)%[0-9a-f]{2}").findAll(input).count().toDouble()

        // 35. Consecutive Whitespace Ratio
        val consecutiveSpaces = Regex("\\s{2,}").findAll(input).sumOf { it.value.length }.toDouble()
        val consecutiveSpaceRatio = consecutiveSpaces / safeLen

        // 36. Operator Density
        val operators = setOf('+', '-', '*', '/', '%', '&', '|', '^', '~', '=')
        val operatorCount = input.count { it in operators }.toDouble()
        val operatorDensity = operatorCount / safeLen

        // 37. Null Byte Count (\0, %00, \x00)
        val nullByteCount = (
            countOccurrences(lower, "\\0") +
            countOccurrences(lower, "%00") +
            countOccurrences(lower, "\\x00")
        ).toDouble()

        // 38. String Concat Operator Count (||, concat, +)
        val concatCount = (
            countOccurrences(input, "||") +
            if (lower.contains("concat(")) 1 else 0
        ).toDouble()

        // 39. Syntactic Anomaly Ratio
        val syntaxAnomalyRatio = (commentMarkerCount * 2 + semicolonCount * 2 + specialCount) / safeLen

        val featureMap = linkedMapOf<String, Double>(
            "shannon_entropy" to entropy,
            "normalized_entropy" to normEntropy,
            "string_length" to lengthFeature,
            "alphanumeric_ratio" to alphaNumRatio,
            "digit_ratio" to digitRatio,
            "whitespace_ratio" to spaceRatio,
            "punctuation_ratio" to punctRatio,
            "uppercase_ratio" to upperRatio,
            "special_symbol_ratio" to specialRatio,
            "non_ascii_ratio" to nonAsciiRatio,
            "single_quotes_count" to singleQuoteCount,
            "double_quotes_count" to doubleQuoteCount,
            "backticks_count" to backtickCount,
            "quote_imbalance" to quoteImbalance,
            "legitimate_apostrophe" to legitApostropheScore,
            "quote_density" to quoteDensity,
            "escaped_quote_count" to escapedQuoteCount,
            "sql_keyword_count" to sqlKeywordCount,
            "distinct_sql_keywords" to distinctSqlKeywords,
            "sql_keyword_density" to sqlKeywordDensity,
            "ddl_keyword_count" to ddlCount,
            "dml_keyword_count" to dmlCount,
            "union_statement_flag" to hasUnion,
            "sys_table_count" to sysTableCount,
            "blind_function_count" to blindCount,
            "xpath_function_count" to xpathCount,
            "boolean_operator_count" to booleanOpCount,
            "comment_marker_count" to commentMarkerCount,
            "semicolon_count" to semicolonCount,
            "equal_sign_count" to equalSignCount,
            "comparison_operator_count" to comparisonCount,
            "parentheses_imbalance" to parenImbalance,
            "hex_literal_count" to hexCount,
            "url_encoded_count" to urlEncodedCount,
            "consecutive_space_ratio" to consecutiveSpaceRatio,
            "operator_density" to operatorDensity,
            "null_byte_count" to nullByteCount,
            "string_concat_count" to concatCount,
            "syntactic_anomaly_ratio" to syntaxAnomalyRatio
        )

        val orderedVector = DoubleArray(39) { i ->
            featureMap.values.elementAt(i)
        }

        return FeatureExtractionResult(featureMap, orderedVector)
    }

    private fun calculateShannonEntropy(str: String): Double {
        if (str.isEmpty()) return 0.0
        val freqMap = HashMap<Char, Int>()
        for (ch in str) {
            freqMap[ch] = (freqMap[ch] ?: 0) + 1
        }
        val len = str.length.toDouble()
        var entropy = 0.0
        for (count in freqMap.values) {
            val p = count / len
            entropy -= p * (ln(p) / ln(2.0))
        }
        return entropy
    }

    private fun countOccurrences(src: String, target: String): Int {
        if (target.isEmpty() || src.isEmpty()) return 0
        var count = 0
        var idx = 0
        while (true) {
            val nextIdx = src.indexOf(target, idx, ignoreCase = true)
            if (nextIdx == -1) break
            count++
            idx = nextIdx + target.length
        }
        return count
    }

    /**
     * Checks if all single quotes in the string are legitimate English contractions or names:
     * e.g., O'Connor, D'Angelo, McDonald's, can't, it's, etc.
     */
    fun isLegitimateApostropheUsage(input: String): Boolean {
        if (!input.contains('\'')) return false
        val singleQuotes = input.count { it == '\'' }
        if (singleQuotes == 0) return false

        // Regex for legitimate name/word apostrophe: letter before apostrophe, letter after apostrophe
        // e.g., O'Connor, D'Angelo, don't, user's
        val legitApostropheRegex = Regex("(?i)[a-z]'[a-z]")
        val legitMatches = legitApostropheRegex.findAll(input).count()

        // Also check if there are no SQL delimiters or syntax markers
        val hasSqlSyntax = input.contains("--") || input.contains(";") || input.contains("/*") ||
                input.contains("=") || input.contains("union", ignoreCase = true)
        return legitMatches == singleQuotes && !hasSqlSyntax
    }
}
