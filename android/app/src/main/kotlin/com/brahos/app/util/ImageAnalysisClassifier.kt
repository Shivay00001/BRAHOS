package com.brahos.app.util

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.nnapi.NnApiDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.Rot90Op

/**
 * Specialized Classifier for Image Analysis (Vitals Detection).
 * Uses TFLite Support Library for Image preprocessing.
 */
class ImageAnalysisClassifier(
    private val context: Context,
    private val modelName: String = "vitals_detection.tflite"
) {

    private var interpreter: Interpreter? = null
    private var gpuDelegate: GpuDelegate? = null
    private var nnApiDelegate: NnApiDelegate? = null
    private val inputImageWidth = 224
    private val inputImageHeight = 224

    init {
        initializeInterpreter()
    }

    private fun initializeInterpreter() {
        try {
            val model = FileUtil.loadMappedFile(context, modelName)
            val options = Interpreter.Options()

            // Hardware Acceleration (NPU/GPU)
            try {
                nnApiDelegate = NnApiDelegate()
                options.addDelegate(nnApiDelegate)
            } catch (e: Exception) {
                try {
                    gpuDelegate = GpuDelegate()
                    options.addDelegate(gpuDelegate)
                } catch (e2: Exception) {
                    Log.w("ImageClassifier", "Hardware acceleration unavailable")
                }
            }

            interpreter = Interpreter(model, options)
        } catch (e: Exception) {
            Log.e("ImageClassifier", "Failed to init interpreter", e)
        }
    }

    fun analyzeImage(bitmap: Bitmap): Map<String, Float> {
        val currentInterpreter = interpreter ?: return emptyMap()

        // 1. Preprocess Image
        val imageProcessor = org.tensorflow.lite.support.image.ImageProcessor.Builder()
            .add(ResizeOp(inputImageHeight, inputImageWidth, ResizeOp.ResizeMethod.BILINEAR))
            .build()

        var tensorImage = TensorImage(org.tensorflow.lite.DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        // 2. Prepare Output
        // Assuming model outputs 2 probabilities: [Anemia, Jaundice]
        val outputBuffer = Array(1) { FloatArray(2) }

        try {
            currentInterpreter.run(tensorImage.buffer, outputBuffer)
            
            // 3. Map Results
            val anemiaProb = outputBuffer[0][0]
            val jaundiceProb = outputBuffer[0][1]

            return mapOf(
                "Anemia (Pallor)" to anemiaProb,
                "Jaundice (Icterus)" to jaundiceProb
            )
        } catch (e: Exception) {
            Log.e("ImageClassifier", "Inference failed", e)
            return emptyMap()
        }
    }

    fun close() {
        interpreter?.close()
        nnApiDelegate?.close()
        gpuDelegate?.close()
    }
}
