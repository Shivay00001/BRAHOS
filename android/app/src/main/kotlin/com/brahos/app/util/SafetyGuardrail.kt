package com.brahos.app.util

import com.brahos.app.domain.model.RiskLevel

/**
 * Hard-coded safety rules that override any AI prediction.
 * EXTREME ROBUSTNESS: These rules are deterministic and audited.
 */
object SafetyGuardrail {

    private val EMERGENCY_KEYWORDS = setOf(
        "chest pain", "difficulty breathing", "shortness of breath",
        "stroke", "paralysis", "heavy bleeding", "unconscious",
        "seizure", "severe burn", "cyanosis", "low oxygen"
    )

    /**
     * DETAILED EMERGENCY VALIDATION
     */
    fun mustEscalate(symptoms: String, age: Int, temperature: Float): Boolean {
        val sympLower = symptoms.lowercase()
        
        // 1. Keyword match (Highly Robust)
        if (EMERGENCY_KEYWORDS.any { sympLower.contains(it) }) return true

        // 2. Physiological Guardrails
        if (temperature > 40.0f) return true // Severe hyperpyrexia
        if (temperature < 35.0f) return true // Hypothermia
        
        // 3. Vulnerable Population Rules
        if (age < 1 && temperature > 38.5f) return true // Infant fever
        if (age > 70 && temperature > 39.0f) return true // Elderly severe fever

        return false
    }

    /**
     * RED-ZONE DETERMINISM: Ensures no "Logic Break" can suppress an emergency.
     */
    fun getSafeRiskLevel(aiPredictedLevel: RiskLevel, symptoms: String, age: Int, temperature: Float): RiskLevel {
        val needsEscalation = mustEscalate(symptoms, age, temperature)
        
        // Safety logic: You can only be UPGRADED to RED by safety rules, never downgraded.
        return if (needsEscalation) {
            RiskLevel.RED_EMERGENCY
        } else {
            aiPredictedLevel
        }
    }
}
