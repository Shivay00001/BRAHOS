package com.brahos.app.domain.usecase

import com.brahos.app.domain.model.RiskLevel
import com.brahos.app.domain.model.TriageAssessment
import com.brahos.app.domain.repository.ConsultationRepository
import com.brahos.app.util.SafetyGuardrail
import javax.inject.Inject

/**
 * Orchestrates the triage process:
 * 1. AI Analysis (Placeholder for quantization engine)
 * 2. Deterministic Safety Override
 * 3. Final Persistence
 */
class PerformTriageUseCase @Inject constructor(
    private val repository: ConsultationRepository
) {
    suspend operator fun invoke(
        patientId: String,
        symptoms: String,
        age: Int,
        temperature: Float,
        imageUri: String? = null
    ): TriageAssessment {
        
        // Step 1: Simulated AI Prediction (to be replaced by TFLite/ExecuTorch)
        // In a real scenario, this would call an AI model service/engine.
        val aiPredictedLevel = simulateAiPrediction(symptoms)
        val initialConfidence = 0.85f

        // Step 2: Apply Safety Guardrails (Deterministic Overrides)
        // This is the core "Bug prevention" and "Safety" layer requested by the user.
        val finalRiskLevel = SafetyGuardrail.getSafeRiskLevel(
            aiPredictedLevel, 
            symptoms, 
            age, 
            temperature
        )

        val isEmergency = finalRiskLevel == RiskLevel.RED_EMERGENCY

        val assessment = TriageAssessment(
            patientId = patientId,
            riskLevel = finalRiskLevel,
            primaryObservation = symptoms,
            suggestions = generateSuggestions(finalRiskLevel),
            requiresImmediateEscalation = isEmergency,
            imageUri = imageUri,
            confidenceScore = if (isEmergency && aiPredictedLevel != RiskLevel.RED_EMERGENCY) 1.0f else initialConfidence
        )

        // Step 3: Persist in Encrypted Storage
        repository.saveAssessment(assessment)

        return assessment
    }

    private fun simulateAiPrediction(symptoms: String): RiskLevel {
        // Mock logic for initial integration
        return when {
            symptoms.lowercase().contains("fever") -> RiskLevel.YELLOW_OBSERVE
            else -> RiskLevel.GREEN_STABLE
        }
    }

    private fun generateSuggestions(level: RiskLevel): List<String> {
        return when (level) {
            RiskLevel.GREEN_STABLE -> listOf("Increase fluid intake", "Monitor temperature", "Rest at home")
            RiskLevel.YELLOW_OBSERVE -> listOf("Visit PHC within 24 hours", "Isolation if contagious", "Paracetamol as prescribed")
            RiskLevel.RED_EMERGENCY -> listOf("IMMEDIATE AMBULANCE", "Oxygen support if available", "Notify nearest District Hospital")
        }
    }
}
