package com.brahos.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brahos.app.domain.model.TriageAssessment
import com.brahos.app.domain.usecase.PerformTriageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TriageViewModel @Inject constructor(
    private val performTriageUseCase: com.brahos.app.domain.usecase.PerformTriageUseCase,
    private val processVoiceIntakeUseCase: com.brahos.app.domain.usecase.ProcessVoiceIntakeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TriageUiState())
    val uiState: StateFlow<TriageUiState> = _uiState.asStateFlow()

    private var audioRecorder: com.brahos.app.util.AudioRecorder? = null
    private var audioFile: java.io.File? = null

    fun onSymptomChange(symptoms: String) {
        _uiState.update { it.copy(symptoms = symptoms) }
    }

    fun startRecording(context: android.content.Context) {
        val file = java.io.File(context.cacheDir, "temp_rec.pcm")
        audioFile = file
        audioRecorder = com.brahos.app.util.AudioRecorder(file)
        _uiState.update { it.copy(isRecording = true) }
        viewModelScope.launch {
            try {
                audioRecorder?.startRecording()
            } catch (e: Exception) {
                _uiState.update { it.copy(isRecording = false, error = "Recording Error: ${e.localizedMessage}") }
            }
        }
    }

    fun stopRecording() {
        audioRecorder?.stopRecording()
        _uiState.update { it.copy(isRecording = false, isLoadingTranscription = true) }
        
        viewModelScope.launch {
            audioFile?.let { file ->
                val result = processVoiceIntakeUseCase(file)
                result.onSuccess { transcript ->
                    _uiState.update { it.copy(
                        symptoms = it.symptoms + (if (it.symptoms.isBlank()) "" else "\n") + transcript,
                        isLoadingTranscription = false
                    ) }
                }.onFailure { e ->
                    _uiState.update { it.copy(
                        isLoadingTranscription = false,
                        error = "Transcription failed: ${e.localizedMessage}"
                    ) }
                }
            }
        }
    }

    fun onPatientDetailsChange(patientId: String, age: Int, temperature: Float) {
        _uiState.update { it.copy(patientId = patientId, age = age, temperature = temperature) }
    }

    fun onImageCaptured(uri: android.net.Uri) {
        _uiState.update { it.copy(capturedImageUri = uri) }
    }

    fun submitTriage() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = performTriageUseCase(
                    patientId = _uiState.value.patientId,
                    symptoms = _uiState.value.symptoms,
                    age = _uiState.value.age,
                    temperature = _uiState.value.temperature,
                    imageUri = _uiState.value.capturedImageUri?.toString()
                )
                _uiState.update { it.copy(isLoading = false, assessmentResult = result) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Triage Failed: ${e.localizedMessage}") }
            }
        }
    }
}

data class TriageUiState(
    val patientId: String = "",
    val symptoms: String = "",
    val age: Int = 0,
    val temperature: Float = 37.0f,
    val capturedImageUri: android.net.Uri? = null,
    val isRecording: Boolean = false,
    val isLoadingTranscription: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val assessmentResult: TriageAssessment? = null
)
