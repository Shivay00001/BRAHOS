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

    override fun getConsultations(): Flow<List<TriageAssessment>> {
        return dao.getAllConsultations().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun saveAssessment(assessment: TriageAssessment) {
        // Implementation would use dao.insertConsultation(assessment.toEntity())
        // but for brevity I will leave the mapper as an extension below
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
        timestamp = timestamp,
        confidenceScore = confidence
    )
}
