# Bharat Rural AI Health Orchestration System (BRAHOS)
## System Design Document

### Version 1.0 | Classification: Technical Architecture

---

## 1. System Architecture Overview

### 1.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client Layer (Android)                    │
│  ┌──────────────┐  ┌──────────────┐  ┌─────────────────────┐   │
│  │ Voice Input  │  │ Image Capture│  │ Offline Storage     │   │
│  │ (ASR Engine) │  │ (Camera API) │  │ (SQLite + AES-256)  │   │
│  └──────────────┘  └──────────────┘  └─────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │         On-Device AI Inference Layer                     │   │
│  │  - Quantized LLM (INT8, 300MB)                          │   │
│  │  - Vision Model (MobileNetV3, 25MB)                     │   │
│  │  - Rule-Based Safety Layer                              │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              ↕ (Opportunistic Sync)
┌─────────────────────────────────────────────────────────────────┐
│                     Backend Layer (AWS India)                    │
│  ┌──────────────┐  ┌──────────────┐  ┌─────────────────────┐   │
│  │ API Gateway  │  │ ECS Fargate  │  │ RDS PostgreSQL      │   │
│  │ (REST/gRPC)  │  │ (Containers) │  │ (Encrypted at rest) │   │
│  └──────────────┘  └──────────────┘  └─────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │         Federated Learning Coordinator                   │   │
│  │  - Gradient Aggregation Server                          │   │
│  │  - Differential Privacy Engine                          │   │
│  └──────────────────────────────────────────────────────────┘   │
│  ┌──────────────┐  ┌──────────────┐  ┌─────────────────────┐   │
│  │ S3 Storage   │  │ CloudWatch   │  │ Lambda Functions    │   │
│  │ (Images/Logs)│  │ (Monitoring) │  │ (Event Processing)  │   │
│  └──────────────┘  └──────────────┘  └─────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              ↕
┌─────────────────────────────────────────────────────────────────┐
│                  Telemedicine Layer (Optional)                   │
│  ┌──────────────┐  ┌──────────────┐  ┌─────────────────────┐   │
│  │ WebRTC Video │  │ Case Queue   │  │ Doctor Dashboard    │   │
│  │ (Jitsi Meet) │  │ Management   │  │ (React Web App)     │   │
│  └──────────────┘  └──────────────┘  └─────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 Design Principles

**Offline-First Architecture**
- All core functionality operates without network connectivity
- Synchronization is opportunistic, not mandatory
- State management uses local-first CRDT principles for conflict-free merging

**Progressive Enhancement**
- Base functionality on low-end devices (2GB RAM, Android 8)
- Enhanced features on higher-spec devices (camera quality, faster inference)
- Cloud features activate only when bandwidth permits

**Privacy by Design**
- Data encrypted at rest and in transit by default
- Federated learning prevents raw data centralization
- Granular consent management with opt-in cloud sync

**Fail-Safe Defaults**
- Conservative triage recommendations when confidence is low
- Automatic escalation for edge cases
- Graceful degradation when AI models fail (fallback to questionnaire mode)

**Regulatory Compliance**
- DISHA-compliant data handling
- Audit trails for all clinical decisions
- Explicit non-diagnostic positioning

---

## 2. Client Layer (Android Application)

### 2.1 Technology Stack

**Core Framework**
- Platform: Android (Kotlin/Java)
- Minimum SDK: API 26 (Android 8.0 Oreo)
- Target SDK: API 33 (Android 13)
- Architecture: MVVM (Model-View-ViewModel) with Clean Architecture

**Key Libraries**
- Jetpack Compose: Modern UI toolkit
- Room Database: Local persistence with encryption extension
- WorkManager: Background sync job scheduling
- CameraX: Unified camera API for image capture
- TensorFlow Lite: On-device ML inference runtime
- SQLCipher: Encrypted SQLite database
- Retrofit: HTTP client for API communication
- Dagger Hilt: Dependency injection

### 2.2 Voice Input Module

**Speech Recognition Pipeline**

```
Audio Capture → Noise Reduction → Voice Activity Detection → ASR Model → Text Output
     ↓              ↓                    ↓                      ↓            ↓
  16kHz PCM    WebRTC NS         Energy-based VAD        Whisper-tiny   Confidence Score
```

**Implementation Details**

**Model Selection**: Whisper-tiny quantized to INT8
- Original size: 75MB → Quantized: 39MB
- Languages: Hindi, Bengali, Tamil, Telugu, Marathi (additional languages via separate model downloads)
- Inference time: 2.8 seconds for 30-second audio on Snapdragon 665
- WER (Word Error Rate): 18-22% on Indian-accented English, 12-16% on native Hindi

**Noise Handling**
- Pre-processing: WebRTC noise suppression library
- Adaptive gain control for outdoor recording
- Echo cancellation for speaker phone usage

**Fallback Mechanism**
- If ASR confidence <70%: Prompt user to repeat
- After 3 failed attempts: Switch to text input or structured questionnaire
- Audio recordings stored locally for manual transcription by ANM staff

**Privacy Considerations**
- Audio files deleted after successful transcription
- Option to disable audio storage entirely (transcription-only mode)
- No cloud upload of raw audio without explicit consent

### 2.3 Image Capture Module

**Image Acquisition Pipeline**

```
Camera Preview → User Guidance → Capture → Quality Check → Compression → Storage
      ↓              ↓              ↓           ↓              ↓            ↓
  CameraX API   Overlay Guide    JPEG Save   Resolution    Progressive   SQLite BLOB
                                              Brightness    JPEG 70%
```

**Supported Image Types**
1. Skin conditions (rashes, lesions, wounds)
2. Tongue examination (coating, color)
3. Eye examination (redness, discharge)
4. Wounds and injuries

**Image Quality Enforcement**
- Minimum resolution: 640x480 (VGA)
- Brightness check: Reject if mean pixel value <30 or >220 (on 0-255 scale)
- Blur detection: Reject if Laplacian variance <50 (indicates motion blur)
- On-screen guidance: Overlay gridlines and distance indicators

**Privacy-Preserving Compression**
- EXIF metadata stripping (GPS, device info, timestamp retained as separate fields)
- Face detection: Blur faces if detected in non-facial examination images
- Progressive JPEG encoding for efficient partial transmission
- Target size: <200KB per image

**Storage Strategy**
- Local storage: Encrypted BLOB in SQLite (references patient record)
- Retention: 90 days local, extended retention only with consent
- Cloud upload: Queued for opportunistic sync when WiFi available

### 2.4 On-Device AI Inference

**Model Architecture**

**Primary: Quantized Language Model**
- Base model: Llama-3-8B → Distilled to 1B parameters → Quantized to INT8
- Final size: 320MB
- Context window: 2048 tokens
- Inference speed: 8 tokens/second on Snapdragon 665
- Quantization: GGML format, 8-bit quantization

**Model Responsibilities**
- Symptom understanding: Parse free-text or voice-transcribed symptoms
- Contextual questioning: Generate follow-up questions based on responses
- Triage reasoning: Generate evidence-based risk assessment
- Explanation generation: Provide reasoning for recommendations

**Prompt Engineering Strategy**
- System prompt hardcoded with medical safety guidelines
- Few-shot examples for common rural health scenarios
- Temperature: 0.3 (low creativity, high consistency)
- Top-k sampling: k=20 to limit output variance

**Example System Prompt (Abbreviated)**
```
You are a medical triage assistant for rural healthcare workers in India. Your role is to:
1. Ask clarifying questions about symptoms
2. Assess urgency level (Green/Yellow/Red)
3. Suggest possible condition categories (NOT definitive diagnoses)
4. Recommend appropriate care level (home care, PHC visit, hospital referral)

CRITICAL SAFETY RULES:
- NEVER provide definitive diagnoses
- NEVER recommend specific medications beyond OTC guidelines
- ALWAYS escalate: chest pain, difficulty breathing, stroke symptoms, pregnancy complications
- ALWAYS express uncertainty when applicable
- NEVER suggest cancer, psychiatric diagnoses, or rare diseases

Output format: JSON with fields {questions: [], risk_level: "", suggestions: [], recommendations: "", reasoning: ""}
```

**Secondary: Vision Model**
- Architecture: MobileNetV3-Small
- Input size: 224x224 RGB
- Model size: 25MB (quantized to INT8)
- Inference time: 180ms on Snapdragon 665

**Model Responsibilities**
- Skin condition classification: 15 common categories (rashes, fungal infections, burns, wounds)
- Image quality assessment: Blur, brightness, occlusion detection
- Anatomical region detection: Verify image matches requested examination type

**Output Format**
```json
{
  "top_predictions": [
    {"category": "fungal_infection", "confidence": 0.68},
    {"category": "contact_dermatitis", "confidence": 0.21},
    {"category": "insect_bite", "confidence": 0.08}
  ],
  "quality_score": 0.85,
  "region_detected": "hand_dorsal"
}
```

**Model Limitations**
- Cannot detect rare conditions (long-tail distribution)
- Requires well-lit images (outdoor daylight or bright indoor lighting)
- Confidence threshold: Predictions <0.5 confidence flagged as uncertain

### 2.5 Rule-Based Safety Layer

**Hard-Coded Safety Rules** (NOT learned from data)

**Emergency Escalation Triggers**
```kotlin
sealed class EmergencyTrigger {
    object ChestPain : EmergencyTrigger()  // Any mention of chest pain
    object DifficultyBreathing : EmergencyTrigger()  // Shortness of breath, can't speak
    object StrokeSymptoms : EmergencyTrigger()  // Face drooping, arm weakness, speech difficulty
    object SevereHeadache : EmergencyTrigger()  // Sudden worst headache ever
    object UncontrolledBleeding : EmergencyTrigger()
    object SevereAllergy : EmergencyTrigger()  // Swelling of face/throat, difficulty swallowing
    object ObstetricEmergency : EmergencyTrigger()  // Pregnancy + bleeding/severe pain
    object PediatricRisk : EmergencyTrigger()  // Age <2 years + fever >102°F
    object AltertedConsciousness : EmergencyTrigger()  // Confusion, unresponsive
}
```

**Auto-Escalation Logic**
```kotlin
fun assessRiskLevel(symptoms: List<String>, vitals: Vitals?, demographics: Demographics): RiskLevel {
    // Rule-based checks override ML predictions
    if (emergencyTriggerDetected(symptoms)) {
        return RiskLevel.RED_EMERGENCY
    }
    
    if (demographics.age < 2 && vitals?.temperature ?: 0f > 39.0f) {
        return RiskLevel.RED_EMERGENCY
    }
    
    if (demographics.isPregnant && symptoms.any { it.contains("bleeding") }) {
        return RiskLevel.RED_EMERGENCY
    }
    
    // Defer to ML model for non-emergency cases
    val mlPrediction = triageModel.predict(symptoms, vitals)
    
    // Conservative adjustment: Upgrade borderline cases
    if (mlPrediction.confidence < 0.6 && mlPrediction.level == RiskLevel.GREEN_LOW) {
        return RiskLevel.YELLOW_MEDIUM
    }
    
    return mlPrediction.level
}
```

**Prohibited Outputs**
- System will NEVER output: "You have cancer", "You have schizophrenia", "This is definitely X"
- Banned phrases: "diagnosis", "confirmed", "definitely", "certainly" (in medical context)
- Mandatory phrasing: "possible", "may indicate", "could be", "requires doctor evaluation"

### 2.6 Local Data Storage

**Database Schema**

```sql
-- Patients table
CREATE TABLE patients (
    patient_id TEXT PRIMARY KEY,  -- UUID v4
    abha_id TEXT UNIQUE,  -- Ayushman Bharat Health Account ID (if available)
    name_encrypted BLOB,  -- AES-256 encrypted
    age INTEGER,
    gender TEXT,
    phone_hash TEXT,  -- SHA-256 hash for deduplication, not reversible
    household_id TEXT,  -- Links family members
    consent_local_storage BOOLEAN,
    consent_cloud_sync BOOLEAN,
    consent_research BOOLEAN,
    created_at INTEGER,  -- Unix timestamp
    updated_at INTEGER
);

-- Consultations table
CREATE TABLE consultations (
    consultation_id TEXT PRIMARY KEY,
    patient_id TEXT REFERENCES patients(patient_id),
    provider_id TEXT,  -- ASHA/ANM worker ID
    timestamp INTEGER,
    symptoms_text TEXT,
    symptoms_audio_path TEXT,  -- Local file path, nullable
    vital_signs_json TEXT,  -- JSON blob
    risk_level TEXT,  -- GREEN/YELLOW/RED
    ai_suggestions_json TEXT,  -- JSON array of possible conditions
    ai_reasoning TEXT,
    doctor_assigned_id TEXT,  -- Nullable, populated on escalation
    doctor_notes TEXT,  -- Nullable
    outcome TEXT,  -- home_care/phc_visit/hospital_referral/followup
    sync_status TEXT,  -- pending/synced/failed
    created_at INTEGER,
    updated_at INTEGER
);

-- Images table
CREATE TABLE images (
    image_id TEXT PRIMARY KEY,
    consultation_id TEXT REFERENCES consultations(consultation_id),
    image_type TEXT,  -- skin/tongue/eye/wound
    image_blob BLOB,  -- Encrypted JPEG
    vision_predictions_json TEXT,  -- Model output
    quality_score REAL,
    created_at INTEGER
);

-- Audit log
CREATE TABLE audit_log (
    log_id TEXT PRIMARY KEY,
    user_id TEXT,
    action TEXT,  -- view/create/update/export
    resource_type TEXT,  -- patient/consultation
    resource_id TEXT,
    timestamp INTEGER,
    details_json TEXT
);
```

**Encryption Strategy**
- Database: Full database encryption using SQLCipher with AES-256
- Key derivation: PBKDF2 with device-specific salt
- Key storage: Android Keystore (hardware-backed if available)
- Per-field encryption: Additional encryption for PII fields (name, contact)

**Data Retention Policy**
- Local device: 90 days for consultations, 30 days for images
- Automatic purge: Background job runs weekly
- User-triggered deletion: Immediate with overwrite (not just delete flag)

---

## 3. AI Layer

### 3.1 Model Training Pipeline

**Offline Training Infrastructure** (Not on-device)

```
Training Data Collection → Data Annotation → Model Training → Validation → Quantization → Deployment
         ↓                        ↓                 ↓              ↓             ↓              ↓
   Federated Learning       Medical Experts    GPU Cluster    Holdout Set    INT8 GGML    OTA Update
   (Gradient Aggregation)   (Indian cases)     (A100 GPUs)    (Indian data)  Conversion   (APK Bundle)
```

**Data Sources** (All anonymized)
1. Public datasets: MIMIC-III (anonymized EHR), PubMed case reports
2. Indian-specific: Collaborations with AIIMS, ICMR for regional disease patterns
3. Federated learning: On-device gradient updates from deployed devices (opt-in)
4. Synthetic data: Rule-based symptom generation for rare conditions

**Training Process**
- Base model: Pre-trained Llama-3-8B
- Fine-tuning: LoRA (Low-Rank Adaptation) on medical Q&A datasets
- Indian-specific fine-tuning: Regional disease prevalence, local language symptoms
- Distillation: Teacher-student approach to compress to 1B parameters
- Quantization: Post-training quantization to INT8 using GGML

**Validation Methodology**
- Clinical validation set: 5,000 Indian rural health cases (retrospective)
- Metrics: Top-3 accuracy, false negative rate for emergencies, calibration error
- Human evaluation: Board-certified doctors rate explanation quality
- Bias testing: Performance stratified by age, gender, geography

### 3.2 Federated Learning Architecture

**Motivation**: Train on real-world deployment data without centralizing sensitive patient information.

**Architecture**

```
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│  Device 1   │  │  Device 2   │  │  Device N   │
│             │  │             │  │             │
│ Local Model │  │ Local Model │  │ Local Model │
│     ↓       │  │     ↓       │  │     ↓       │
│ Compute     │  │ Compute     │  │ Compute     │
│ Gradients   │  │ Gradients   │  │ Gradients   │
└──────┬──────┘  └──────┬──────┘  └──────┬──────┘
       │                │                │
       └────────────────┼────────────────┘
                        ↓
                ┌───────────────┐
                │  Aggregation  │
                │    Server     │
                │  (AWS India)  │
                └───────┬───────┘
                        ↓
                ┌───────────────┐
                │ Global Model  │
                │    Update     │
                └───────┬───────┘
                        ↓
              ┌─────────────────────┐
              │  Push to Devices    │
              │  (OTA Update)       │
              └─────────────────────┘
```

**Implementation Details**

**Federated Averaging (FedAvg)**
- Rounds: Monthly model updates
- Participation: 10% of devices per round (random sampling)
- Local epochs: 3 epochs on local data before gradient upload
- Gradient clipping: L2 norm clipping at 1.0 to prevent outliers

**Differential Privacy**
- Mechanism: Gaussian noise addition to gradients
- Privacy budget: ε=8, δ=10^-5 (per round)
- Trade-off: Slight accuracy degradation for strong privacy guarantees

**Secure Aggregation**
- Protocol: Bonawitz et al. (2017) secure aggregation
- Encryption: Each device encrypts gradients; server learns only aggregate
- Dropout resilience: System tolerates 20% device dropout during aggregation

**Device Selection Criteria**
- Minimum: 100 consultations completed
- Data quality: >80% consultations with doctor feedback
- Consent: Explicit opt-in for federated learning participation

**Model Update Distribution**
- Delivery: Android APK update via Play Store or direct download
- Frequency: Quarterly major updates, monthly minor updates
- Rollback: Previous model version retained for 30 days

### 3.3 Continuous Learning Feedback Loop

**Doctor Feedback Collection**
- Annotation interface: Doctor marks AI suggestion as correct/incorrect/partially_correct
- Corrected labels: Doctor provides correct triage level and condition categories
- Explanation feedback: Doctor rates quality of AI reasoning (1-5 scale)

**Feedback Storage**
```json
{
  "consultation_id": "uuid",
  "ai_suggestion": ["condition_a", "condition_b", "condition_c"],
  "ai_risk_level": "YELLOW",
  "doctor_correction": {
    "correct_conditions": ["condition_b", "condition_d"],
    "correct_risk_level": "RED",
    "reasoning": "Patient also reported chest pain, which was missed",
    "rating": 2
  },
  "timestamp": "2026-01-15T14:30:00Z"
}
```

**Retraining Trigger**
- Quarterly retraining cycles
- Early trigger if false negative rate >8% in monitoring dashboard
- Manual trigger for critical bug fixes

---

## 4. Backend Layer

### 4.1 Cloud Infrastructure (AWS ap-south-1)

**Service Architecture**

**Compute**
- ECS Fargate: Containerized microservices (serverless containers)
- Lambda: Event-driven functions (sync processing, image resizing)
- Auto-scaling: CPU utilization >70% triggers scale-up

**Storage**
- RDS PostgreSQL: Structured patient data, consultation records
  - Instance type: db.r5.large (16GB RAM, 2 vCPU) for pilot; scale to db.r5.4xlarge for state-level
  - Encryption: AES-256 at rest, TLS 1.3 in transit
  - Backups: Automated daily backups, 30-day retention
- S3: Images, audio files, model artifacts
  - Bucket: Versioning enabled, lifecycle policy (30-day transition to IA, 90-day deletion)
  - Encryption: SSE-S3 (AES-256)
- ElastiCache Redis: Session management, sync queue caching

**Networking**
- VPC: Isolated network, private subnets for databases
- NAT Gateway: Outbound internet access for containers
- Application Load Balancer: HTTPS termination, request routing

**Security**
- KMS: Customer-managed encryption keys (CMK) for data encryption
- IAM: Least-privilege access policies, role-based access
- Secrets Manager: Database credentials, API keys rotation every 90 days
- WAF: Web Application Firewall for DDoS protection, SQL injection prevention

### 4.2 API Design

**RESTful Endpoints**

```
POST /api/v1/sync/consultations
  - Bulk upload of pending consultations
  - Request: Array of consultation objects
  - Response: Sync confirmation with server-assigned IDs

GET /api/v1/sync/consultations?since=<timestamp>
  - Download updates from server (doctor notes, corrections)
  - Response: Array of updated consultations

POST /api/v1/images/upload
  - Upload images with metadata
  - Request: Multipart form-data
  - Response: S3 URL (pre-signed for 7-day access)

POST /api/v1/escalation/create
  - Create escalation case for doctor review
  - Request: consultation_id, priority, reason
  - Response: Case queue position, estimated response time

GET /api/v1/models/latest
  - Check for model updates
  - Response: Version number, download URL, checksum

POST /api/v1/federated/gradients
  - Upload encrypted gradients for federated learning
  - Request: Encrypted gradient blob, device_id
  - Response: Acknowledgment
```

**gRPC Endpoints** (for low-latency doctor dashboard)

```protobuf
service DoctorDashboard {
  rpc GetCaseQueue(QueueRequest) returns (stream CaseUpdate);
  rpc UpdateConsultation(ConsultationUpdate) returns (Acknowledgment);
  rpc SendMessage(Message) returns (Acknowledgment);
}
```

**Rate Limiting**
- Anonymous: 10 requests/minute
- Authenticated healthcare worker: 100 requests/minute
- Doctor dashboard: 1000 requests/minute

**Authentication**
- OAuth 2.0 + JWT tokens
- Token expiry: 1 hour (refresh token valid for 30 days)
- Multi-factor authentication: SMS OTP for initial login

### 4.3 Synchronization Strategy

**Conflict-Free Sync Protocol**

**CRDT-Based Merging**
- Consultations: Last-write-wins for immutable fields (timestamp, symptoms)
- Doctor notes: Append-only, never overwrite
- Patient demographics: Last-write-wins with version vector

**Sync Queue Management**
```kotlin
data class SyncQueueItem(
    val id: String,
    val type: SyncType,  // CONSULTATION/IMAGE/PATIENT
    val priority: Int,  // 0=Emergency, 1=High, 2=Normal
    val payload: ByteArray,
    val retryCount: Int,
    val createdAt: Long
)

// Priority-based processing
fun processSyncQueue() {
    val queue = localDb.getSyncQueue().sortedByDescending { it.priority }
    for (item in queue) {
        if (networkAvailable && batteryLevel > 20) {
            try {
                apiClient.sync(item)
                localDb.markSynced(item.id)
            } catch (e: NetworkException) {
                incrementRetryCount(item.id)
                exponentialBackoff(item.retryCount)
            }
        }
    }
}
```

**Bandwidth Adaptation**
```kotlin
fun selectSyncStrategy(bandwidth: Int): SyncStrategy {
    return when {
        bandwidth > 1_000_000 -> SyncStrategy.FULL  // >1 Mbps: Sync all data
        bandwidth > 256_000 -> SyncStrategy.TEXT_ONLY  // 256 kbps - 1 Mbps: Skip images
        bandwidth > 64_000 -> SyncStrategy.EMERGENCY_ONLY  // 64-256 kbps: Only red-flag cases
        else -> SyncStrategy.OFFLINE  // <64 kbps: Stay offline
    }
}
```

**Compression**
- JSON payloads: GZIP compression (typical 70% size reduction)
- Images: Already compressed as JPEG, no additional compression
- Batch uploads: Combine multiple consultations into single HTTP request

### 4.4 Telemedicine Integration

**Video Conferencing** (Optional, bandwidth-permitting)

**Technology Stack**
- WebRTC: Peer-to-peer video/audio
- Jitsi Meet: Self-hosted video conferencing server
- Fallback: Voice-only call if video bandwidth insufficient

**Bandwidth Requirements**
- Video call: 512 kbps minimum (240p resolution)
- Voice call: 64 kbps minimum
- Text chat: 10 kbps (always available)

**Adaptive Streaming**
```javascript
// JavaScript pseudo-code for Jitsi integration
const constraints = {
  video: {
    width: { ideal: 640 },
    height: { ideal: 480 },
    frameRate: { ideal: 15, max: 24 }
  },
  audio: {
    echoCancellation: true,
    noiseSuppression: true
  }
};

// Downgrade video quality if bandwidth drops
connection.addEventListener('connectionstatsreceived', (stats) => {
  if (stats.bandwidth < 512000) {
    setVideoQuality('low');  // 240p, 10 fps
  } else if (stats.bandwidth < 256000) {
    disableVideo();  // Voice only
  }
});
```

**Doctor Dashboard** (Web Application)

**Tech Stack**
- Frontend: React + TypeScript
- State management: Redux Toolkit
- Real-time updates: WebSocket (Socket.io)
- UI framework: Material-UI

**Key Features**
- Case queue: Sortable by priority, wait time
- Consultation history: Patient timeline view
- Prescription templates: Pre-filled forms for common medications
- AI suggestion review: Side-by-side view of AI output and patient data
- Feedback mechanism: One-click correct/incorrect for AI suggestions

---

## 5. Security Design

### 5.1 Threat Model

**Threat Actors**
1. External attackers: Unauthorized access to patient data
2. Malicious insiders: Healthcare workers accessing data without clinical need
3. Device theft: Physical access to unlocked or stolen devices
4. Network eavesdropping: Man-in-the-middle attacks on public networks

**Assets to Protect**
- Patient PII: Name, age, contact, location
- Health data: Symptoms, diagnoses, medical history
- Images: Clinical photographs
- Provider credentials: Login credentials, digital signatures

### 5.2 Defense-in-Depth Strategy

**Layer 1: Device Security**
- Full-disk encryption: Android File-Based Encryption (FBE)
- App-level encryption: SQLCipher for database, additional PII field encryption
- Biometric authentication: Fingerprint unlock (fallback to PIN)
- Screen timeout: 5 minutes inactivity auto-lock
- Root detection: App refuses to run on rooted devices

**Layer 2: Network Security**
- TLS 1.3: All network communication encrypted
- Certificate pinning: Prevent MITM with fake certificates
- VPN requirement: Optional but recommended for telemedicine
- Firewall rules: Whitelist only required endpoints

**Layer 3: Application Security**
- Input validation: Sanitize all user inputs to prevent injection attacks
- Secure coding: OWASP Mobile Top 10 compliance
- Code obfuscation: ProGuard/R8 to hinder reverse engineering
- Tamper detection: Checksum validation on app startup

**Layer 4: Data Security**
- Encryption at rest: AES-256 for all stored data
- Encryption in transit: TLS 1.3 for all network traffic
- Key management: Android Keystore (hardware-backed on supported devices)
- Data minimization: Collect only necessary data, purge after retention period

**Layer 5: Access Control**
- RBAC: Role-based access control (ASHA, ANM, Doctor, Admin)
- Least privilege: Users access only what their role requires
- Audit logging: All data access logged with user ID, timestamp, action
- Anomaly detection: Alert on unusual access patterns (e.g., 100 patient records in 1 hour)

### 5.3 Compliance Framework

**DISHA Compliance** (Digital Information Security in Healthcare Act - Draft)
- Data localization: All patient data stored in India (AWS ap-south-1 region)
- Consent management: Explicit, informed, granular consent
- Data portability: Patients can export their data in FHIR format
- Breach notification: Notify affected patients within 72 hours of breach discovery

**Medical Device Regulations**
- Classification: Class A (lowest risk) as decision support tool
- Clinical validation: Prospective study showing non-inferiority to standard triage
- Adverse event reporting: Mechanism to report AI errors leading to patient harm

**Data Protection Best Practices**
- Privacy by design: Default to most privacy-preserving settings
- Anonymization: K-anonymity (k≥5) for research datasets
- Regular audits: Quarterly security audits by independent third party

### 5.4 Incident Response Plan

**Detection**
- Automated monitoring: CloudWatch alarms for unusual API activity
- User reporting: In-app mechanism to report suspected security issues
- Penetration testing: Annual third-party security assessment

**Response**
1. Containment: Isolate affected systems, revoke compromised credentials
2. Investigation: Determine scope of breach, affected users
3. Notification: Inform affected patients, health authorities within 72 hours
4. Remediation: Patch vulnerabilities, enhance monitoring
5. Post-mortem: Document lessons learned, update security protocols

---

## 6. Scalability Model

### 6.1 Scalability Dimensions

**Horizontal Scaling** (Add more instances)
- Application servers: ECS Fargate auto-scaling (target CPU 70%)
- Database: Read replicas for doctor dashboard queries
- Cache: Redis cluster for session management

**Vertical Scaling** (Increase instance size)
- Database: Upgrade RDS instance type as data volume grows
- Not applicable to Fargate (serverless containers scale horizontally)

**Geographic Scaling** (Multi-region deployment)
- Phase 3: Replicate infrastructure to additional AWS regions for disaster recovery
- Data residency: All patient data remains in ap-south-1; only anonymized analytics in other regions

### 6.2 Capacity Planning

**Pilot Phase (2 districts, 500 healthcare workers, 50,000 patients)**

| Resource | Specification | Rationale |
|----------|---------------|-----------|
| RDS PostgreSQL | db.r5.large (2 vCPU, 16GB RAM) | 50,000 patients × 10 consultations/patient/year = 500K records |
| ECS Fargate Tasks | 10 tasks (1 vCPU, 2GB RAM each) | 500 workers × 10 consultations/day = 5,000 daily syncs |
| S3 Storage | 500 GB | 50,000 patients × 2 images/consultation × 200KB/image |
| ElastiCache Redis | cache.r5.large (13GB RAM) | Session storage for 500 concurrent users |
| API Gateway | 10,000 requests/day | 5,000 consultations + 5,000 sync checks |

**State Phase (1 state, 10,000 healthcare workers, 5M patients)**

| Resource | Specification | Scale Factor |
|----------|---------------|--------------|
| RDS PostgreSQL | db.r5.2xlarge (8 vCPU, 64GB RAM) | 100x data volume |
| ECS Fargate Tasks | 100 tasks (auto-scaling) | 20x worker count |
| S3 Storage | 50 TB | 100x patient count |
| ElastiCache Redis | cache.r5.xlarge (26GB RAM) | 20x concurrent users |
| API Gateway | 1M requests/day | 100x request volume |

**National Phase (10 states, 200,000 healthcare workers, 100M patients)**

| Resource | Specification | Scale Factor |
|----------|---------------|--------------|
| RDS PostgreSQL | Aurora Serverless v2 (auto-scaling) | 2,000x data volume |
| ECS Fargate Tasks | 1,000+ tasks (auto-scaling) | 400x worker count |
| S3 Storage | 1 PB | 2,000x patient count |
| ElastiCache Redis | Multi-AZ cluster (100GB+ RAM) | 200x concurrent users |
| API Gateway | 20M requests/day | 2,000x request volume |
| CDN | CloudFront for model distribution | Reduce download latency |

### 6.3 Cost Modeling

**Pilot Phase (Monthly)**
- ECS Fargate: 10 tasks × $30/task = $300
- RDS: db.r5.large × $150 = $150
- S3: 500GB × $0.023/GB = $11.50
- Data transfer: 100GB × $0.09/GB = $9
- **Total: ~$500/month** (~₹42,000)
- **Cost per consultation: ₹1** (assuming 40,000 consultations/month)

**State Phase (Monthly)**
- ECS Fargate: 100 tasks × $30/task = $3,000
- RDS: db.r5.2xlarge × $600 = $600
- S3: 50TB × $0.023/GB = $1,150
- Data transfer: 10TB × $0.09/GB = $900
- **Total: ~$6,000/month** (~₹5,00,000)
- **Cost per consultation: ₹1.25** (assuming 4M consultations/month)

**National Phase (Monthly)**
- Aurora Serverless: ~$50,000 (auto-scaling based on demand)
- ECS Fargate: ~$30,000
- S3: ~$24,000
- Data transfer: ~$20,000
- **Total: ~$125,000/month** (~₹1.05 crore)
- **Cost per consultation: ₹1.3** (assuming 80M consultations/month)

**Cost Optimization Strategies**
- Reserved instances: 30-50% savings on RDS
- S3 Intelligent-Tiering: Automatic archival of old data
- Spot instances: Use for non-critical batch processing (federated learning)
- Data compression: Reduce storage and transfer costs

### 6.4 Partnership Model for Sustainability

**Government Funding**
- National Health Mission (NHM) budget allocation
- State health department operational funding
- Digital India initiative grants

**Corporate Social Responsibility (CSR)**
- Pharmaceutical companies: Fund in disease-endemic areas
- Tech companies: Donate cloud credits (AWS Activate, GCP credits)
- Telecom operators: Subsidized data plans for healthcare workers

**International Grants**
- WHO, UNICEF, Gates Foundation: Pilot funding
- World Bank: Scale-up financing

**Revenue Model** (Optional, if not fully government-funded)
- Freemium: Free for government facilities, paid tier for private clinics (₹500/month per provider)
- Data insights: Anonymized population health analytics sold to pharmaceutical companies for drug development (requires ethics approval)

---

## 7. Deployment Roadmap

### 7.1 Phase 1: Pilot Deployment (Months 1-12)

**Objective**: Validate clinical efficacy and technical feasibility in controlled environment

**Timeline**: 12 months

**Scope**
- Geography: 2 districts in 1 state (preferably high disease burden, poor doctor-to-patient ratio)
- Users: 500 healthcare workers (250 ASHA, 200 ANM, 50 PHC doctors)
- Patients: 50,000 expected consultations

**Month 1-3: Infrastructure Setup**
- AWS account setup, VPC configuration
- RDS database provisioning
- ECS cluster setup
- CI/CD pipeline (GitHub Actions → ECR → ECS)
- Monitoring: CloudWatch dashboards, PagerDuty alerts

**Month 2-4: Application Development**
- Android app development (iterative sprints)
- Backend API development
- Doctor dashboard web app
- AI model integration and testing
- Security audit by external firm

**Month 4-6: User Training and Onboarding**
- Train-the-trainer: 50 master trainers (state health dept staff)
- Field training: 2-day workshops for ASHA/ANM workers
- Doctor onboarding: 4-hour orientation sessions
- Feedback collection: Weekly feedback sessions

**Month 6-9: Active Deployment**
- Phased rollout: 100 workers → 250 workers → 500 workers
- Real-time monitoring: Daily dashboards for usage, errors, escalations
- Helpdesk: Dedicated support team (phone + WhatsApp)

**Month 9-12: Evaluation and Iteration**
- Clinical validation study: Compare AI triage vs standard care (RCT design)
- Usability testing: SUS (System Usability Scale) scores
- Performance metrics: Accuracy, false negative rate, user satisfaction
- Model refinement: Retrain models based on real-world data
- Prepare for scale-up: Document lessons learned, SOPs

**Success Criteria for Phase 1**
- Clinical safety: Zero serious adverse events
- Adoption: 70% of devices active (≥4 consultations/week)
- Accuracy: 75% top-3 agreement with doctor assessment
- False negatives: <5% for high-priority conditions
- User satisfaction: >65%

### 7.2 Phase 2: State Expansion (Months 13-24)

**Objective**: Scale to entire state, optimize operations, achieve financial sustainability

**Timeline**: 12 months

**Scope**
- Geography: Entire state (e.g., Uttar Pradesh with 75 districts)
- Users: 10,000 healthcare workers
- Patients: 5M consultations expected

**Month 13-15: Infrastructure Scaling**
- RDS upgrade: db.r5.2xlarge
- ECS auto-scaling: 100+ tasks
- CDN setup: CloudFront for model downloads
- Multi-AZ deployment: High availability

**Month 15-18: Statewide Rollout**
- District-wise rollout: 5 districts/month
- Master trainer program: 500 trainers across state
- Government partnership: MoU with State Health Department
- Integration: Link with state HMIS (Health Management Information System)

**Month 18-21: Optimization**
- Model updates: Incorporate 12 months of feedback
- Feature additions: Chronic disease tracking, medication adherence
- UI/UX improvements: Based on user feedback
- Multilingual expansion: Add 2 more regional languages

**Month 21-24: Sustainability Planning**
- Government budget integration: Secure recurring funding
- CSR partnerships: Onboard 3-5 corporate partners
- Training institutionalization: Integrate into ANM curriculum
- Prepare for national scale: Policy advocacy, regulatory approvals

**Success Criteria for Phase 2**
- State coverage: 80% of districts using system
- Cost efficiency: <₹5 per consultation
- Clinical impact: 30% reduction in diagnostic delay
- Provider confidence: >60% report increased confidence

### 7.3 Phase 3: National Rollout (Months 25-48)

**Objective**: Deploy across 10 states, establish as national standard, enable research ecosystem

**Timeline**: 24 months

**Scope**
- Geography: 10 states (prioritize high disease burden: UP, Bihar, MP, Rajasthan, Odisha, etc.)
- Users: 200,000 healthcare workers
- Patients: 100M consultations expected

**Month 25-30: Infrastructure Preparation**
- Aurora Serverless: Auto-scaling database
- Multi-region replication: Disaster recovery in ap-south-2
- Federated learning at scale: Coordinate across 10 states
- Data lake: S3 + Athena for population health analytics

**Month 30-42: State-by-State Rollout**
- 2 states every 3 months
- State-specific customization: Local languages, disease priorities
- Government advocacy: MoU with National Health Mission
- Regulatory compliance: Medical device registration if required

**Month 42-48: Ecosystem Development**
- Research collaborations: Partner with ICMR, medical colleges
- Open-source components: Release anonymized datasets, model APIs
- Third-party integrations: ABHA, CoWIN (vaccination), e-Sanjeevani (telemedicine)
- International expansion: Pilot in Bangladesh, Nepal (similar healthcare challenges)

**Success Criteria for Phase 3**
- National coverage: 100M consultations logged
- Clinical validation: Published peer-reviewed studies
- Policy impact: Included in National Digital Health Mission
- Cost sustainability: Self-sustaining through government funding

### 7.4 Risk Mitigation

**Technical Risks**

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Model accuracy below threshold | Medium | High | Extensive validation, conservative thresholds, doctor oversight |
| Device compatibility issues | Medium | Medium | Broad device testing, fallback to basic features |
| Network outages affecting sync | High | Low | Offline-first design, queued sync |
| Security breach | Low | High | Penetration testing, encryption, incident response plan |

**Operational Risks**

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Low user adoption | Medium | High | Extensive training, user-friendly design, incentives |
| Healthcare worker turnover | High | Medium | Simple onboarding, video tutorials, ongoing support |
| Government funding discontinuation | Low | High | CSR partnerships, demonstrate ROI, policy advocacy |
| Regulatory rejection | Low | High | Early engagement with regulators, clinical trials |

**Clinical Risks**

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Missed emergency case | Low | Critical | Hard-coded safety rules, mandatory doctor review for ambiguous cases |
| Over-reliance on AI leading to deskilling | Medium | Medium | Position as decision support, not replacement; continuous training |
| Algorithmic bias (gender, caste, geography) | Medium | High | Bias testing, stratified validation, diverse training data |

---

## 8. Monitoring and Evaluation

### 8.1 Technical Metrics

**System Performance**
- API latency: p50, p95, p99 response times
- Error rate: HTTP 5xx errors per 1000 requests
- Sync success rate: % of consultations successfully synced
- App crash rate: Crashes per 1000 sessions
- Battery drain: % per hour of active use

**AI Performance**
- Inference latency: On-device prediction time
- Model accuracy: Top-1, top-3 agreement with doctor
- Calibration: Expected calibration error (ECE)
- False negative rate: Emergencies classified as low-priority
- Confidence distribution: Histogram of model confidence scores

**Infrastructure**
- Database query time: p95 latency for patient search
- Storage utilization: % of provisioned capacity used
- Auto-scaling events: Frequency of scale-up/down
- Cost per consultation: Monthly cloud spend / consultations

### 8.2 Clinical Metrics

**Outcome Metrics**
- Time to first medical opinion: Days from symptom onset
- Referral appropriateness: % of PHC referrals that result in admission
- Diagnostic concordance: AI vs doctor vs final diagnosis
- Adverse events: Patient harm attributable to system

**Process Metrics**
- Consultation completion rate: % of started consultations completed
- Doctor response time: Hours from escalation to doctor review
- Follow-up adherence: % of patients who return for follow-up

**Equity Metrics**
- Geographic distribution: Consultations per capita by district
- Gender distribution: % female patients (should match population)
- Socioeconomic distribution: Consultation rates by income quintile

### 8.3 User Experience Metrics

**Healthcare Worker**
- SUS score: System Usability Scale (target >68)
- NPS: Net Promoter Score (target >40)
- Training time: Hours to proficiency
- Daily active users: % of provisioned devices used daily

**Patient**
- Satisfaction: 5-point Likert scale post-consultation
- Wait time: Minutes from arrival to consultation start
- Understanding: % of patients who can explain their triage result

### 8.4 Dashboards

**Operational Dashboard** (For program managers)
- Daily active devices map (geographic heatmap)
- Consultation volume trends (time series)
- Escalation queue depth (real-time)
- Top 10 symptoms reported (bar chart)

**Clinical Dashboard** (For medical oversight)
- AI accuracy trends (weekly rolling average)
- False negative alerts (real-time)
- Condition category distribution (pie chart)
- Doctor feedback summary (sentiment analysis)

**Technical Dashboard** (For engineering team)
- System health: All green/yellow/red indicators
- Error logs: Grouped by severity
- Performance metrics: Latency, throughput graphs
- Cost tracking: Burn rate vs budget

---

## 9. Ethical Considerations

### 9.1 Beneficence and Non-Maleficence

**Do No Harm**
- Conservative triage: Err on side of caution, upgrade risk level if uncertain
- Mandatory human review: Doctor must validate all AI outputs before final recommendation
- Adverse event reporting: Transparent mechanism for reporting AI errors

**Maximize Benefit**
- Prioritize underserved: Deploy first in areas with worst doctor-to-patient ratios
- Free access: No cost to patients or government healthcare workers
- Continuous improvement: Feedback loop to enhance accuracy over time

### 9.2 Autonomy and Informed Consent

**Patient Autonomy**
- Voluntary participation: Patients can decline AI triage, request direct doctor consultation
- Transparent AI role: Patients informed that initial triage uses AI, final decision by doctor
- Consent for data use: Separate consent for local storage, cloud sync, research use

**Provider Autonomy**
- Override mechanism: Doctors can always override AI suggestions
- Not punitive: Disagreements with AI not used for performance evaluation
- Clinical judgment primacy: System positioned as decision support, not directive

### 9.3 Justice and Equity

**Fair Access**
- Geographic equity: Deploy across high and low disease burden areas
- Gender equity: Monitor for bias in triage accuracy across genders
- Socioeconomic equity: Free for all patients, no premium tiers

**Algorithmic Fairness**
- Bias testing: Stratified validation by age, gender, geography, caste
- Disparate impact analysis: Monitor for differential accuracy across subgroups
- Mitigation: Reweight training data if bias detected

### 9.4 Transparency and Explainability

**Explainable AI**
- Reasoning display: Show why AI suggested specific condition categories
- Uncertainty quantification: Display confidence scores to providers
- Feature importance: Highlight which symptoms drove the decision

**Algorithmic Transparency**
- Public documentation: Open-source model architecture (not weights)
- Clinical validation: Publish accuracy metrics in peer-reviewed journals
- Audit trail: Immutable logs for all AI decisions

### 9.5 Accountability

**Liability Framework**
- Developer responsibility: System must meet safety and accuracy standards
- Provider responsibility: Doctor responsible for final clinical decisions
- Government responsibility: Oversight and quality assurance

**Governance**
- Clinical advisory board: Quarterly review of AI performance and safety
- Ethics committee: Annual review of system deployment and impact
- Patient advocacy: Include patient representatives in governance

---

## 10. Future Enhancements (Post-MVP)

### 10.1 Technical Roadmap

**Multimodal AI Expansion**
- Auscultation: Integrate with digital stethoscopes for heart/lung sound analysis
- ECG interpretation: Basic arrhythmia detection from smartphone-based ECG
- Retinal imaging: Diabetic retinopathy screening via smartphone fundus camera

**Personalization**
- Patient risk profiles: Chronic disease history informs triage
- Medication interaction checking: Alert if new symptoms may be drug side effects
- Predictive analytics: Forecast patient deterioration risk

**Integration**
- Laboratory systems: Pull lab results for holistic assessment
- Pharmacy: E-prescription fulfillment tracking
- Ambulance dispatch: Automatic emergency vehicle routing for red-flag cases

### 10.2 Research Opportunities

**Clinical Research**
- Prospective RCT: AI-assisted triage vs standard care
- Cost-effectiveness analysis: QALYs gained per rupee spent
- Implementation science: Barriers and facilitators to adoption

**AI Research**
- Multilingual medical NLP: Improve symptom understanding in Indic languages
- Few-shot learning: Rapid adaptation to emerging diseases (e.g., new pandemic)
- Causal inference: Identify true risk factors vs spurious correlations

### 10.3 Policy Advocacy

**Regulatory Recognition**
- Establish AI triage as standard of care in resource-constrained settings
- Advocate for reimbursement codes for AI-assisted consultations
- Influence medical device classification for clinical decision support

**Data Governance**
- Contribute to National Digital Health Mission standards
- Participate in FHIR implementation for India
- Advocate for federated learning in national AI policy

---

## 11. Conclusion

BRAHOS represents a pragmatic, technically rigorous approach to augmenting India's rural healthcare system. By prioritizing offline functionality, privacy preservation, and clinical safety, the system addresses real-world constraints while delivering measurable impact.

**Key Success Factors**
1. Clinical validation: Prospective studies demonstrating non-inferiority to standard triage
2. User-centered design: Extensive field testing with ASHA workers and patients
3. Government partnership: Integration with existing health programs
4. Ethical foundation: Transparent AI, patient autonomy, algorithmic fairness
5. Sustainable economics: <₹5 per consultation operational cost

**Non-Negotiable Principles**
- Patient safety above all else
- Human clinician remains decision-maker
- Privacy by design, not as an afterthought
- Explainable AI, not black-box predictions
- Continuous improvement through feedback loops

This system is not a silver bullet for India's healthcare challenges, but a practical tool to extend the reach of existing healthcare workers, reduce diagnostic delays, and improve health outcomes for millions of underserved citizens.

---

**Document Control**

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-02-14 | AI Systems Architecture Team | Initial design specification |

**Approval**

This document requires review and approval from:
- Lead System Architect
- Chief Medical Officer
- Head of Data Science
- Chief Information Security Officer

---

*End of Design Document*
