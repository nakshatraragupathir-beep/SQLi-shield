package com.example.data

import com.example.model.DetectionResult
import kotlinx.coroutines.flow.Flow

class IncidentRepository(private val incidentDao: IncidentDao) {

    val allIncidents: Flow<List<IncidentEntity>> = incidentDao.getAllIncidents()

    fun getIncidentsByClassification(classification: String): Flow<List<IncidentEntity>> {
        return if (classification == "ALL") {
            incidentDao.getAllIncidents()
        } else {
            incidentDao.getIncidentsByClassification(classification)
        }
    }

    fun searchIncidents(query: String): Flow<List<IncidentEntity>> {
        return if (query.isBlank()) {
            incidentDao.getAllIncidents()
        } else {
            incidentDao.searchIncidents(query)
        }
    }

    suspend fun logDetection(result: DetectionResult): Long {
        val triggeredStr = result.triggeredRules.joinToString(separator = "; ") {
            "${it.ruleName} [${it.severity}]"
        }
        val primaryThreat = result.triggeredRules.firstOrNull()?.ruleName
            ?: if (result.compositeScore >= 71) "High Risk Anomaly (ML Detection)"
            else if (result.compositeScore >= 31) "Suspicious Query Syntax"
            else "Clean Query"

        val entity = IncidentEntity(
            payload = result.payload,
            clientIp = result.clientIp,
            timestamp = result.timestamp,
            compositeScore = result.compositeScore,
            mlScore = result.mlScore,
            ruleScore = result.ruleScore,
            classification = result.classification.name,
            floorOverride = result.floorOverrideApplied,
            latencyMs = result.latencyMs,
            triggeredRules = triggeredStr,
            primaryThreat = primaryThreat
        )
        return incidentDao.insertIncident(entity)
    }

    suspend fun clearAll() = incidentDao.clearAll()

    suspend fun deleteById(id: Long) = incidentDao.deleteById(id)

    suspend fun getCount(): Int = incidentDao.getIncidentCount()
}
