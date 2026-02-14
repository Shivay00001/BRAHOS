package com.brahos.app.domain.usecase

import com.brahos.app.util.AudioRecorder
import com.brahos.app.util.WhisperEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * Handles the voice intake workflow:
 * 1. Post-processes recorded audio.
 * 2. Runs local Whisper inference.
 * 3. Returns a text transcript.
 */
class ProcessVoiceIntakeUseCase @Inject constructor(
    private val whisperEngine: WhisperEngine
) {
    suspend operator fun invoke(audioFile: File): Result<String> = withContext(Dispatchers.IO) {
        try {
            val audioData = audioFile.readBytes()
            val transcript = whisperEngine.transcribe(audioData)
            Result.success(transcript)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
