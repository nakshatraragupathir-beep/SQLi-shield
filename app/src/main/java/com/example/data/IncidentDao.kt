package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidentDao {
    @Query("SELECT * FROM sqli_incidents ORDER BY timestamp DESC")
    fun getAllIncidents(): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM sqli_incidents WHERE classification = :classification ORDER BY timestamp DESC")
    fun getIncidentsByClassification(classification: String): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM sqli_incidents WHERE payload LIKE '%' || :query || '%' OR primaryThreat LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchIncidents(query: String): Flow<List<IncidentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: IncidentEntity): Long

    @Query("DELETE FROM sqli_incidents")
    suspend fun clearAll()

    @Query("DELETE FROM sqli_incidents WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM sqli_incidents")
    suspend fun getIncidentCount(): Int
}
