package com.brahos.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.brahos.app.domain.model.RiskLevel

@Entity(tableName = "consultations")
data class ConsultationEntity(
    @PrimaryKey val id: String,
    val patientId: String,
    val riskLevel: RiskLevel,
    val symptoms: String,
    val suggestions: String, // Stored as comma-separated or JSON
    val isEmergency: Boolean,
    val imagePath: String? = null,
    val detectedVitals: String? = null, // Stored as JSON or Pipe-separated
    val timestamp: Long,
    val confidence: Float,
    val syncStatus: SyncStatus = SyncStatus.PENDING
)

enum class SyncStatus {
    PENDING, SYNCED, FAILED
}
