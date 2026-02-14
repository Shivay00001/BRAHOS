package com.brahos.app.domain.usecase

import com.brahos.app.domain.model.RiskLevel
import com.brahos.app.util.TfliteClassifier
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Encapsulates symptom-to-risk-level AI classification logic.
 * Ensures the AI output is mapped back to domain models safely.
 */
@Singleton
class ClassifySymptomsUseCase @Inject constructor(
    private val classifier: TfliteClassifier
) {
    suspend operator fun invoke(symptoms: String): RiskLevel {
        // Step 1: Pre-processing (Tokenization logic would go here)
        val dummyInput = FloatArray(128) { i -\u003e if (i \u003c symptoms.length) symptoms[i].code.toFloat() else 0f }

        // Step 2: Inference
        val result = classifier.runInference(dummyInput, 3)
        
        return result.fold(
            onSuccess = { scores -\u003e
                // Simple argmax mapping
                val maxIdx = scores.indices.maxByOrNull { scores[it] } ?: 0
                when (maxIdx) {
                    0 -\u003e RiskLevel.GREEN_STABLE
                    1 -\u003e RiskLevel.YELLOW_OBSERVE
                    2 -\u003e RiskLevel.RED_EMERGENCY
                    else -\u003e RiskLevel.GREEN_STABLE
                }
            },
            onFailure = { 
                // Default to conservative observation on AI failure
                RiskLevel.YELLOW_OBSERVE 
            }
        )
    }
}
