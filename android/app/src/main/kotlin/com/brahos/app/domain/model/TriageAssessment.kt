package com.brahos.app.domain.model

import java.util.UUID

/**
 * Robust Triage Assessment result with strict risk levels.
 * Logic is decoupled from AI models to ensure fail-safe operation.
 */
data class TriageAssessment(
    val id: String = UUID.randomUUID().toString(),
    val patientId: String,
    val riskLevel: RiskLevel,
    val primaryObservation: String,
    val suggestions: List<String>,
    val requiresImmediateEscalation: Boolean,
    val imageUri: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val confidenceScore: Float
)

enum class RiskLevel {
    GREEN_STABLE,    // Home care recommended
    YELLOW_OBSERVE,  // PHC visit within 24-48h
    RED_EMERGENCY    // Immediate hospital referral
}
