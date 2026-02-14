package com.brahos.app.domain.usecase

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.brahos.app.util.ImageAnalysisClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class AnalyzeVitalsUseCase @Inject constructor(
    private val context: Context
) {
    private val classifier = ImageAnalysisClassifier(context)

    suspend operator fun invoke(imageUri: String): Map<String, String> = withContext(Dispatchers.IO) {
        try {
            val uri = Uri.parse(imageUri)
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            
            if (bitmap == null) return@withContext emptyMap()

            val rawResults = classifier.analyzeImage(bitmap)
            
            // Apply Thresholds
            val detectedVitals = mutableMapOf<String, String>()
            
            rawResults.forEach { (condition, probability) ->
                if (probability > 0.75f) { // High confidence threshold
                    detectedVitals[condition] = "High Probability detected (${(probability * 100).toInt()}%)"
                }
            }
            
            detectedVitals
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }
}
