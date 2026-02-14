package com.brahos.app.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brahos.app.presentation.viewmodel.TriageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriageScreen(
    viewModel: TriageViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("New Triage Assessment") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = uiState.patientId,
                onValueChange = { viewModel.onPatientDetailsChange(it, uiState.age, uiState.temperature) },
                label = { Text("Patient ID") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = if (uiState.age == 0) "" else uiState.age.toString(),
                    onValueChange = { viewModel.onPatientDetailsChange(uiState.patientId, it.toIntOrNull() ?: 0, uiState.temperature) },
                    label = { Text("Age") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = uiState.temperature.toString(),
                    onValueChange = { viewModel.onPatientDetailsChange(uiState.patientId, uiState.age, it.toFloatOrNull() ?: 37f) },
                    label = { Text("Temp (°C)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            OutlinedTextField(
                value = uiState.symptoms,
                onValueChange = { viewModel.onSymptomChange(it) },
                label = { Text("Describe Symptoms") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 5
            )

            Button(
                onClick = { viewModel.submitTriage() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && uiState.patientId.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Analyze Safety & Triage")
                }
            }

            uiState.assessmentResult?.let { result ->
                TriageResultCard(result)
            }

            uiState.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun TriageResultCard(result: com.brahos.app.domain.model.TriageAssessment) {
    val color = when (result.riskLevel) {
        com.brahos.app.domain.model.RiskLevel.GREEN_STABLE -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
        com.brahos.app.domain.model.RiskLevel.YELLOW_OBSERVE -> androidx.compose.ui.graphics.Color(0xFFFFC107)
        com.brahos.app.domain.model.RiskLevel.RED_EMERGENCY -> androidx.compose.ui.graphics.Color(0xFFF44336)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Assessment: ${result.riskLevel}", style = MaterialTheme.typography.headlineSmall, color = color)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Suggestions:", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            result.suggestions.forEach {
                Text("• $it")
            }
            if (result.requiresImmediateEscalation) {
                Text("\n⚠️ ALERT: Immediate Medical Referral Required!", color = androidx.compose.ui.graphics.Color.Red, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold)
            }
        }
    }
}
