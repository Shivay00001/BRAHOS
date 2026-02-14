package com.brahos.app.util

import com.brahos.app.domain.model.RiskLevel

/**
 * Hard-coded safety rules that override any AI prediction.
 * This is the "Safety Guardrail" layer to prevent diagnostic errors.
 */
object SafetyGuardrail {

    private val EMERGENCY_KEYWORDS = setOf(
        "chest pain", "difficulty breathing", "shortness of breath",
        "stroke", "paralysis", "heavy bleeding", "unconscious",
        "seizure", "severe burn"
    )

    /**
     * Validates if a triage result must be elevated to RED regardless of AI confidence.
     */
    fun mustEscalate(symptoms: String, age: Int, temperature: Float): Boolean {
        // Rule 1: Key emergency phrases
        if (EMERGENCY_KEYWORDS.any { symptoms.lowercase().contains(it) }) return true

        // Rule 2: Pediatric fever (>39°C for infants)
        if (age < 2 && temperature > 39.0f) return true

        // Rule 3: Severe hypertension symptoms (generalized logic)
        // Note: Real vitals logic would be more complex
        
        return false
    }

    /**
     * Force-corrects a RiskLevel based on deterministic safety rules.
     */
    fun getSafeRiskLevel(aiPredictedLevel: RiskLevel, symptoms: String, age: Int, temperature: Float): RiskLevel {
        return if (mustEscalate(symptoms, age, temperature)) {
            RiskLevel.RED_EMERGENCY
        } else {
            aiPredictedLevel
        }
    }
}
