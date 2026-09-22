package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sqli_incidents")
data class IncidentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val payload: String,
    val clientIp: String,
    val timestamp: Long,
    val compositeScore: Int,
    val mlScore: Int,
    val ruleScore: Int,
    val classification: String,
    val floorOverride: Boolean,
    val latencyMs: Long,
    val triggeredRules: String, // JSON or comma-delimited
    val primaryThreat: String
)
