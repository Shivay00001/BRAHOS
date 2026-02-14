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
    private val performTriageUseCase: PerformTriageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TriageUiState())
    val uiState: StateFlow<TriageUiState> = _uiState.asStateFlow()

    fun onSymptomChange(symptoms: String) {
        _uiState.update { it.copy(symptoms = symptoms) }
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
                    temperature = _uiState.value.temperature
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
    val isLoading: Boolean = false,
    val error: String? = null,
    val assessmentResult: TriageAssessment? = null
)
