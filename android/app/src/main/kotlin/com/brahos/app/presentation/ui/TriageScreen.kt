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
import com.brahos.app.presentation.qr.QrScannerScreen
import com.brahos.app.presentation.qr.QrDisplayDialog

enum class TriageScreenMode { FORM, CAMERA, QR_SCANNER }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriageScreen(
    viewModel: TriageViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var screenMode by remember { mutableStateOf(TriageScreenMode.FORM) }
    var showQrDialog by remember { mutableStateOf(false) }

    if (showQrDialog && uiState.patientId.isNotBlank()) {
        QrDisplayDialog(content = uiState.patientId, onDismiss = { showQrDialog = false })
    }

    when (screenMode) {
        TriageScreenMode.CAMERA -> {
            CameraView(
                onImageCaptured = {
                    viewModel.onImageCaptured(it)
                    screenMode = TriageScreenMode.FORM
                },
                onError = { screenMode = TriageScreenMode.FORM }
            )
        }
        TriageScreenMode.QR_SCANNER -> {
            QrScannerScreen(
                onQrScanned = {
                    viewModel.onPatientDetailsChange(it, uiState.age, uiState.temperature)
                    screenMode = TriageScreenMode.FORM
                }
            )
        }
        TriageScreenMode.FORM -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("New Triage Assessment") },
                        actions = {
                            if (uiState.patientId.isNotBlank()) {
                                TextButton(onClick = { showQrDialog = true }) {
                                    Text("Show QR")
                                }
                            }
                            val context = LocalContext.current
                            IconButton(onClick = {
                                val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                    data = android.net.Uri.parse("mailto:")
                                    putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf("support@brahos.org"))
                                    putExtra(android.content.Intent.EXTRA_SUBJECT, "BRAHOS Bug Report")
                                }
                                context.startActivity(intent)
                            }) {
                                Text("🐞") // Report Issue Icon
                            }
                        }
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ... (Existing fields)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.patientId,
                            onValueChange = { viewModel.onPatientDetailsChange(it, uiState.age, uiState.temperature) },
                            label = { Text("Patient ID") },
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = { screenMode = TriageScreenMode.QR_SCANNER },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.width(48.dp)
                        ) {
                            Text("📷") // QR Icon placeholder
                        }
                    }

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

                    // Camera & Voice Intake Buttons
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { screenMode = TriageScreenMode.CAMERA },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Capture Photo")
                        }

                        val context = LocalContext.current
                        Button(
                            onClick = { 
                                if (uiState.isRecording) viewModel.stopRecording() 
                                else viewModel.startRecording(context) 
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Text(if (uiState.isRecording) "Stop Recording" else "Voice Intake")
                        }
                    }

                    if (uiState.isLoadingTranscription) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text("Processing speech offline...", style = MaterialTheme.typography.bodySmall)
                    }

                    uiState.capturedImageUri?.let { uri ->
                        Text("Image Attached: ${uri.lastPathSegment}", style = MaterialTheme.typography.bodySmall)
                    }
                    
                    var isConsentGiven by remember { mutableStateOf(false) }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isConsentGiven, onCheckedChange = { isConsentGiven = it })
                        Text("Patient consents to data collection", style = MaterialTheme.typography.bodyMedium)
                    }

                    Button(
                        onClick = { viewModel.submitTriage() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading && uiState.patientId.isNotBlank() && isConsentGiven
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
