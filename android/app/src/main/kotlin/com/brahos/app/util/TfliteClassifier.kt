package com.brahos.app.util

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.nnapi.NnApiDelegate
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Robust Wrapper for TFLite Inference.
 * Implements hardware acceleration with graceful fallback and error boundaries.
 */
class TfliteClassifier(private val context: Context, private val modelName: String) {

    private var interpreter: Interpreter? = null
    private var gpuDelegate: GpuDelegate? = null
    private var nnApiDelegate: NnApiDelegate? = null

    init {
        initializeInterpreter()
    }

    private fun initializeInterpreter() {
        try {
            val model = FileUtil.loadMappedFile(context, modelName)
            val options = Interpreter.Options()

            // Try NPU/GPU acceleration
            try {
                nnApiDelegate = NnApiDelegate()
                options.addDelegate(nnApiDelegate)
                Log.d("TfliteClassifier", "NnApiDelegate enabled")
            } catch (e: Exception) {
                Log.w("TfliteClassifier", "NnApi unavailable, falling back to CPU")
            }

            interpreter = Interpreter(model, options)
            Log.d("TfliteClassifier", "Interpreter initialized for $modelName")
        } catch (e: Exception) {
            Log.e("TfliteClassifier", "Initialization failed for $modelName", e)
        }
    }

    /**
     * Runs inference with a safety boundary.
     * Returns a Result to prevent logic breaks on model failure.
     */
    fun runInference(input: FloatArray, outputSize: Int): Result<FloatArray> {
        val currentInterpreter = interpreter ?: return Result.failure(Exception("Interpreter not initialized"))
        
        return try {
            val output = FloatArray(outputSize)
            currentInterpreter.run(input, output)
            Result.success(output)
        } catch (e: Exception) {
            Log.e("TfliteClassifier", "Inference failed", e)
            Result.failure(e)
        }
    }

    fun close() {
        interpreter?.close()
        nnApiDelegate?.close()
        gpuDelegate?.close()
        interpreter = null
    }
}
