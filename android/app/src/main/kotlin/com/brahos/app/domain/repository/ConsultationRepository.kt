package com.brahos.app.domain.repository

import com.brahos.app.domain.model.TriageAssessment
import kotlinx.coroutines.flow.Flow

interface ConsultationRepository {
    fun getConsultations(): Flow<List<TriageAssessment>>
    suspend fun saveAssessment(assessment: TriageAssessment)
    suspend fun getAssessment(id: String): TriageAssessment?
}
