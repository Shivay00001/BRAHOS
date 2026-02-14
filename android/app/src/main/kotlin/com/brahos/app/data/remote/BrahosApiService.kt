package com.brahos.app.data.remote

import com.brahos.app.domain.model.TriageAssessment
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface BrahosApiService {
    @POST("api/v1/triage/sync")
    suspend fun syncAssessments(@Body assessments: List<TriageAssessment>): Response<SyncResponse>

    @POST("api/v1/triage/escalate")
    suspend fun escalateEmergency(@Body escalationData: EscalationRequest): Response<EscalationResponse>
}

data class SyncResponse(val status: String, val count: Int)
data class EscalationRequest(val patientId: String, val riskLevel: String, val reason: String, val location: String = "Rural Clinic A")
data class EscalationResponse(val status: String, val escalationId: String)
