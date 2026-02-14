package com.brahos.app.data.repository

import com.brahos.app.data.local.ConsultationDao
import com.brahos.app.data.local.ConsultationEntity
import com.brahos.app.domain.model.TriageAssessment
import com.brahos.app.domain.repository.ConsultationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ConsultationRepositoryImpl @Inject constructor(
    private val dao: ConsultationDao
) : ConsultationRepository {

    override suspend fun saveAssessment(assessment: TriageAssessment) {
        dao.insertConsultation(assessment.toEntity())
    }

    override suspend fun getAssessment(id: String): TriageAssessment? {
        return dao.getConsultationById(id)?.toDomain()
    }

    private fun ConsultationEntity.toDomain() = TriageAssessment(
        id = id,
        patientId = patientId,
        riskLevel = riskLevel,
        primaryObservation = symptoms,
        suggestions = suggestions.split(", "),
        requiresImmediateEscalation = isEmergency,
        imageUri = imagePath,
        detectedVitals = detectedVitals?.split("|")?.associate {
            val (k, v) = it.split(":")
            k to v
        } ?: emptyMap(),
        timestamp = timestamp,
        confidenceScore = confidence
    )

    private fun TriageAssessment.toEntity() = ConsultationEntity(
        id = id,
        patientId = patientId,
        riskLevel = riskLevel,
        symptoms = primaryObservation,
        suggestions = suggestions.joinToString(", "),
        isEmergency = requiresImmediateEscalation,
        imagePath = imageUri,
        detectedVitals = if (detectedVitals.isNotEmpty()) detectedVitals.entries.joinToString("|") { "${it.key}:${it.value}" } else null,
        timestamp = timestamp,
        confidence = confidenceScore
    )
}
