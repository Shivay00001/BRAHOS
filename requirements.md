# Bharat Rural AI Health Orchestration System (BRAHOS)
## Requirements Specification Document

### Version 1.0 | Classification: Technical Specification

---

## 1. Problem Statement

India's rural healthcare infrastructure serves approximately 65% of the population (900M+ people) with inadequate medical resources:

**Current Inefficiencies:**
- Doctor-to-patient ratio in rural areas: 1:10,926 (WHO recommended: 1:1,000)
- Average diagnostic delay for non-emergency cases: 7-14 days
- 40% of PHC positions remain vacant
- Patient travel time to nearest qualified doctor: 2-4 hours average
- Diagnostic equipment utilization rate: <30% due to lack of trained operators
- Language barriers: 22 official languages, 19,500+ dialects
- Infrastructure: 65% of PHCs lack reliable electricity; 80% lack stable internet >512 kbps

**Measurable Gaps:**
- 47% of rural patients bypass PHCs for direct hospital visits due to lack of trust in preliminary screening
- 31% of preventable complications arise from delayed triage
- Average time-to-first-medical-opinion: 72+ hours for non-emergency cases

**Core Problem:**
Absence of scalable, offline-capable preliminary health assessment system that can operate at PHC/sub-center level without requiring trained medical practitioners for basic triage.

---

## 2. Target Users

### 2.1 Primary Users

**ASHA Workers (Accredited Social Health Activists)**
- Population: ~1 million nationwide
- Technical proficiency: Basic smartphone literacy
- Role: Community-level health screening and referral
- Requirements: Voice-first interface, minimal training overhead

**ANM Staff (Auxiliary Nurse Midwives)**
- Population: ~250,000 at sub-centers
- Technical proficiency: Moderate digital literacy
- Role: First-line health service delivery
- Requirements: Image-based diagnostic support, patient record management

**PHC Medical Officers**
- Population: ~25,000 at Primary Health Centers
- Technical proficiency: High digital literacy
- Role: Diagnosis and treatment oversight
- Requirements: Escalation queue management, decision support dashboard

### 2.2 Secondary Users

**Telemedicine Coordinators**
- Population: State/district level deployment
- Role: Remote consultation facilitation
- Requirements: Case prioritization engine, bandwidth-aware video conferencing

**Rural Patients**
- Population: Target 50M in pilot phase
- Technical proficiency: Low to none
- Requirements: Zero-learning interface, language flexibility

### 2.3 Administrative Users

**Health Department Officials**
- Role: Population health monitoring, resource allocation
- Requirements: Anonymized analytics dashboard, compliance reporting

---

## 3. Functional Requirements

### 3.1 Multimodal Symptom Intake

**FR-1.1: Voice Input Processing**
- Support for 12 major Indian languages (Hindi, Bengali, Telugu, Marathi, Tamil, Gujarati, Urdu, Kannada, Odia, Malayalam, Punjabi, Assamese)
- On-device speech-to-text conversion using quantized ASR models (<50MB)
- Noise cancellation for rural environment (livestock, traffic, outdoor settings)
- Fallback to prompted questionnaire if transcription confidence <70%

**FR-1.2: Text Input Processing**
- Multi-script support (Devanagari, Bengali, Tamil, Telugu, etc.)
- Transliteration support for Roman script input
- Structured symptom checklist with skip logic
- Free-form text annotation capability

**FR-1.3: Image Input Processing**
- Capture support for: skin conditions, wounds, tongue examination, eye examination
- On-device image quality validation (resolution >640x480, brightness check)
- Privacy-preserving image compression (max 200KB per image)
- EXIF metadata stripping for anonymization

**FR-1.4: Structured Data Collection**
- Vital signs input: Temperature, BP, pulse, SpO2 (via external Bluetooth devices if available)
- Medical history: Previous conditions, medications, allergies
- Demographic data: Age, gender, occupation, location (district-level granularity only)

### 3.2 Triage Reasoning Engine

**FR-2.1: Risk Stratification**
- Three-tier classification: Low-priority (green), Medium-priority (yellow), High-priority (red)
- Red flags detection for emergency referral: chest pain patterns, stroke symptoms, severe allergic reactions, obstetric emergencies
- Confidence scoring for each assessment (display to provider, not patient)

**FR-2.2: Differential Suggestion**
- Generate top 3-5 possible condition categories (not specific diagnoses)
- Evidence presentation: Map symptoms to suggested conditions
- Exclusion reasoning: Explicitly state why certain conditions are unlikely
- Uncertainty acknowledgment: Flag ambiguous cases for mandatory doctor review

**FR-2.3: Safety Guardrails**
- Hard-coded exclusion list: Never suggest cancer diagnoses, psychiatric conditions, or rare diseases
- Automatic escalation triggers: Pediatric cases <2 years, pregnancy-related symptoms, neurological symptoms
- Response templates: Standardized language avoiding diagnostic certainty

**FR-2.4: Recommendation Engine**
- Home care guidance for low-priority cases (hydration, rest, OTC medications within protocol)
- PHC visit recommendations with urgency timeline
- Hospital referral triggers with rationale

### 3.3 Low-Bandwidth Synchronization

**FR-3.1: Offline-First Architecture**
- Full functionality without network connectivity
- Local SQLite database for patient records
- Queue-based sync: Store-and-forward mechanism

**FR-3.2: Adaptive Sync Protocol**
- Bandwidth detection: Adjust payload size based on available throughput
- Differential sync: Only transmit deltas, not full records
- Compression: GZIP compression for JSON payloads
- Retry logic: Exponential backoff for failed sync attempts
- Priority queue: Emergency cases sync first

**FR-3.3: Sync Conflict Resolution**
- Last-write-wins for demographic data
- Append-only for clinical observations
- Manual review queue for conflicting diagnoses

### 3.4 Doctor Escalation Workflow

**FR-4.1: Case Queue Management**
- Automatic routing to on-duty doctor based on availability roster
- SLA tracking: Flag cases exceeding 4-hour response time
- Case transfer mechanism between providers

**FR-4.2: Consultation Interface**
- Asynchronous text messaging (WhatsApp-like interface)
- Optional voice note recording (when bandwidth permits)
- Real-time video consultation (fall back to voice if bandwidth <128 kbps)

**FR-4.3: Decision Support**
- Display AI triage output alongside case details
- Provider override mechanism with mandatory reason capture
- Prescription template library (government essential medicines list)

**FR-4.4: Feedback Loop**
- Doctor annotations on AI suggestions (correct/incorrect/partially correct)
- Outcome tracking: Follow-up status, condition resolution
- Federated learning input: Anonymized feedback for model improvement

### 3.5 Health Record Management

**FR-5.1: Patient Registry**
- Unique health ID generation (compatible with ABHA - Ayushman Bharat Health Account)
- Household-level clustering for family health tracking
- Deduplication logic for repeat visits

**FR-5.2: Longitudinal Records**
- Visit history with timestamps
- Medication adherence tracking
- Vaccination records integration
- Chronic condition monitoring (diabetes, hypertension)

**FR-5.3: Data Export**
- PDF generation for patient copy (printable at PHC)
- FHIR-compatible export for interoperability
- Anonymized bulk export for research (with ethics approval)

### 3.6 Language Translation Layer

**FR-6.1: User Interface Localization**
- Complete UI translation for 12 supported languages
- Dynamic language switching without app restart
- Right-to-left script support (Urdu)

**FR-6.2: Medical Term Translation**
- Layman term mapping: Medical jargon to common language
- Reverse translation validation: Ensure patient understanding
- Audio playback of recommendations (text-to-speech)

**FR-6.3: Provider Communication**
- Auto-translate patient inputs to English for doctor review
- Preserve original language text for verification
- Translation quality indicator (machine vs human verified)

---

## 4. Non-Functional Requirements

### 4.1 Performance Requirements

**NFR-1.1: Device Compatibility**
- Minimum: Android 8.0 (API level 26), 2GB RAM, 4GB storage available
- Target: Android 10+, 3GB RAM, 8GB storage
- APK size: <150MB with all language models
- Background memory footprint: <200MB

**NFR-1.2: Response Latency**
- Voice transcription: <3 seconds for 30-second audio clip
- Triage inference: <5 seconds on-device
- Image analysis: <10 seconds for skin condition assessment
- UI interactions: <100ms response time

**NFR-1.3: Battery Efficiency**
- Standby drain: <2% per hour
- Active usage: Support 8-hour shift on single charge (3000mAh battery)
- Background sync: Maximum 5% battery consumption per day

**NFR-1.4: Network Efficiency**
- Sync payload: <50KB per consultation (excluding images)
- Image upload: Progressive JPEG, max 200KB per image
- Offline operation: 100% feature parity except cloud sync and telemedicine

### 4.2 Security Requirements

**NFR-2.1: Data Encryption**
- At-rest: AES-256 encryption for local SQLite database
- In-transit: TLS 1.3 for all network communication
- Key management: Per-device encryption keys, rotated every 90 days

**NFR-2.2: Authentication**
- Multi-factor authentication for healthcare providers (OTP via SMS)
- Biometric authentication support (fingerprint) for device unlock
- Session timeout: 15 minutes of inactivity
- Role-based access control: ASHA, ANM, Doctor, Admin roles

**NFR-2.3: Audit Logging**
- Immutable append-only audit trail
- Log retention: Minimum 3 years
- Logged events: Data access, modifications, exports, user logins

**NFR-2.4: Consent Management**
- Explicit patient consent for data storage (captured digitally or thumbprint)
- Granular consent: Separate for local storage, cloud sync, research use
- Consent withdrawal mechanism with data deletion within 30 days

**NFR-2.5: Anonymization**
- PII stripping for federated learning datasets
- K-anonymity (k≥5) for any exported analytics
- Differential privacy for aggregate statistics

### 4.3 Reliability Requirements

**NFR-3.1: Availability**
- Offline mode: 100% uptime for local operations
- Cloud services: 99.5% uptime (excluding scheduled maintenance)
- Graceful degradation: Fallback to basic features if AI models fail

**NFR-3.2: Data Integrity**
- Checksums for all sync operations
- Database integrity checks on app startup
- Automated backup: Daily local backup, weekly cloud backup (if online)

**NFR-3.3: Fault Tolerance**
- Crash recovery: Restore last known good state
- Partial sync recovery: Resume from last successful checkpoint
- Corrupt data quarantine: Isolate and flag corrupted records

### 4.4 Scalability Requirements

**NFR-4.1: User Scalability**
- Phase 1 (Pilot): 500 healthcare workers, 50,000 patients across 2 districts
- Phase 2 (State): 10,000 healthcare workers, 5M patients across 1 state
- Phase 3 (National): 200,000 healthcare workers, 100M patients across 10 states

**NFR-4.2: Data Scalability**
- Local storage: Support 10,000 patient records per device
- Cloud storage: Horizontal scaling to 100M patient records
- Query performance: <2 seconds for patient search across 1M records

**NFR-4.3: Concurrent Users**
- Backend API: Support 10,000 concurrent sync requests
- Telemedicine server: Support 500 concurrent video sessions per region

### 4.5 Usability Requirements

**NFR-5.1: Training Requirements**
- ASHA workers: Maximum 2-day training program
- ANM staff: Maximum 1-day training program
- PHC doctors: Maximum 4-hour orientation

**NFR-5.2: Accessibility**
- Font size adjustability (100% to 200%)
- High-contrast mode for low-light environments
- Voice guidance for illiterate users

**NFR-5.3: Error Handling**
- User-friendly error messages in local language
- Automatic error reporting with user consent
- Offline help documentation

### 4.6 Compliance Requirements

**NFR-6.1: Regulatory Compliance**
- Digital Information Security in Healthcare Act (DISHA) compliance
- Medical Device Rules 2017 (if applicable for diagnostic support)
- IT Act 2000 compliance for data protection

**NFR-6.2: Clinical Standards**
- Not a medical device: Explicitly positioned as triage support tool
- Disclaimers: Clear communication that AI output requires professional validation
- Clinical validation: Prospective study required before deployment showing non-inferiority to standard triage

**NFR-6.3: Ethical Standards**
- Institutional Ethics Committee approval for deployment
- Adverse event reporting mechanism
- Algorithm bias monitoring across demographic groups

---

## 5. Impact Metrics

### 5.1 Primary Outcomes

**M-1.1: Reduction in Diagnostic Delay**
- Baseline: Average 7 days for first medical opinion
- Target: 50% reduction to 3.5 days within 12 months of deployment
- Measurement: Timestamp from symptom onset to first provider assessment

**M-1.2: Reduction in Unnecessary Hospital Referrals**
- Baseline: 47% of rural patients bypass PHCs
- Target: 20% reduction in direct hospital visits for low-acuity cases
- Measurement: Hospital admission records for conditions manageable at PHC level

**M-1.3: Early-Stage Detection Rate**
- Baseline: 31% of preventable complications from delayed triage
- Target: 40% improvement in early detection of high-priority conditions
- Measurement: Condition severity at first provider contact vs historical cohort

### 5.2 Secondary Outcomes

**M-2.1: Healthcare Worker Productivity**
- Target: 30% increase in patients screened per day per ASHA worker
- Measurement: Consultations logged per worker per shift

**M-2.2: Patient Satisfaction**
- Target: >70% patient satisfaction score
- Measurement: Post-consultation survey (5-point Likert scale)

**M-2.3: Provider Confidence**
- Target: >60% of providers report increased confidence in triage decisions
- Measurement: Quarterly provider survey

**M-2.4: System Utilization**
- Target: >80% of deployed devices active (≥1 consultation per week)
- Measurement: Device telemetry logs

### 5.3 Learning Metrics

**M-3.1: Model Accuracy**
- Target: 75% agreement between AI triage and doctor assessment (top-3 suggestions)
- Measurement: Cohen's kappa on blinded retrospective validation set

**M-3.2: False Negative Rate**
- Target: <5% false negatives for high-priority conditions
- Measurement: Cases flagged as low-priority but subsequently hospitalized

**M-3.3: Calibration**
- Target: Confidence scores within 10% of actual accuracy
- Measurement: Expected calibration error (ECE) on validation set

---

## 6. Constraints

### 6.1 Infrastructure Constraints

**C-1.1: Network Connectivity**
- Assumption: 70% of deployment sites have intermittent 2G/3G (avg 64-256 kbps)
- Assumption: 30% of sites have no reliable cellular connectivity
- Requirement: Zero dependency on real-time internet access for core triage functionality

**C-1.2: Power Availability**
- Assumption: 35% of PHCs experience 4+ hour daily power outages
- Requirement: System must operate on battery-powered Android devices
- Mitigation: Solar charging recommendations, power-efficient architecture

**C-1.3: Device Availability**
- Assumption: Budget allocation of ₹8,000-12,000 per device (USD 100-150)
- Constraint: Target low-end Android devices, not premium hardware
- Mitigation: Quantized models, efficient runtime

### 6.2 User Constraints

**C-2.1: Medical Expertise**
- Assumption: ASHA workers have no formal medical training
- Assumption: ANM staff have basic nursing training (18-month diploma)
- Requirement: System must not require medical knowledge to operate
- Mitigation: Guided workflows, plain language interfaces

**C-2.2: Digital Literacy**
- Assumption: 40% of ASHA workers are first-time smartphone users
- Requirement: Voice-first interaction, minimal text input
- Mitigation: Audio instructions, visual cues, simplified navigation

**C-2.3: Language Diversity**
- Constraint: 22 official languages, 19,500+ dialects
- Requirement: Support for 12 major languages covering 90% of rural population
- Mitigation: Community language mapping, dialect handling via base language models

### 6.3 Regulatory Constraints

**C-3.1: Medical Device Classification**
- Constraint: Cannot be classified as diagnostic medical device
- Requirement: Triage support tool, not diagnostic system
- Mitigation: Explicit disclaimers, mandatory human review

**C-3.2: Data Localization**
- Constraint: Personal health data must be stored within India
- Requirement: Cloud infrastructure on Indian AWS regions (ap-south-1)
- Mitigation: Geo-restricted data residency policies

**C-3.3: Liability**
- Constraint: No liability acceptance for misdiagnosis
- Requirement: Clear scope definition as decision support, not autonomous diagnosis
- Mitigation: Terms of use, provider acknowledgment during onboarding

### 6.4 Technical Constraints

**C-4.1: Model Size**
- Constraint: Total on-device AI models must fit in <500MB
- Requirement: Quantized models (INT8 or lower precision)
- Mitigation: Model compression, knowledge distillation, pruning

**C-4.2: Explainability**
- Constraint: AI outputs must be interpretable by non-technical healthcare workers
- Requirement: Rule-based reasoning paths, not black-box predictions
- Mitigation: Hybrid architecture with explainable components

**C-4.3: Privacy Preservation**
- Constraint: Cannot centralize raw patient data without consent
- Requirement: Federated learning architecture for model updates
- Mitigation: On-device learning, gradient aggregation, differential privacy

### 6.5 Deployment Constraints

**C-5.1: Organizational Buy-In**
- Constraint: Requires approval from State Health Departments
- Requirement: Pilot validation with measurable outcomes
- Mitigation: Partnership with NITI Aayog, ICMR for endorsement

**C-5.2: Training Infrastructure**
- Constraint: Limited capacity for in-person training at scale
- Requirement: Train-the-trainer model, video-based learning modules
- Mitigation: State-level master trainers, asynchronous learning materials

**C-5.3: Sustainability**
- Constraint: Dependency on government/CSR funding beyond pilot phase
- Requirement: Cost-per-consultation <₹5 (USD 0.06) for operational viability
- Mitigation: Open-source core components, cloud cost optimization

---

## 7. Out of Scope

The following are explicitly excluded from the current system scope:

**OS-1: Definitive Diagnosis**
- System provides triage suggestions, not confirmed diagnoses
- Final diagnosis remains the responsibility of licensed medical practitioners

**OS-2: Treatment Planning**
- System may suggest general care guidelines but not specific treatment regimens
- Prescription authority remains with registered doctors

**OS-3: Chronic Disease Management**
- Long-term condition monitoring (diabetes, hypertension) is limited to tracking, not management
- Insulin dosing, medication adjustments require doctor supervision

**OS-4: Mental Health Support**
- Psychiatric conditions are automatically escalated
- No counseling or therapeutic interventions

**OS-5: Laboratory Integration**
- No direct integration with lab equipment or results processing
- Lab reports can be uploaded as images for doctor review but not analyzed by AI

**OS-6: Pharmacy Integration**
- No automated prescription fulfillment
- Doctors manually issue prescriptions through existing channels

**OS-7: Payment Processing**
- No billing or payment gateway integration
- System is provided free of cost to patients under government schemes

---

## 8. Success Criteria

The system deployment is considered successful if:

1. **Clinical Safety**: Zero serious adverse events attributable to system use over 12-month pilot period
2. **Adoption Rate**: 70% of deployed devices actively used (≥4 consultations/week) after 6 months
3. **Accuracy Threshold**: 75% top-3 agreement between AI triage and doctor assessment
4. **False Negative Control**: <5% critical conditions missed (false negatives) as low-priority
5. **User Satisfaction**: >65% combined satisfaction from healthcare workers and patients
6. **Efficiency Gain**: 30% reduction in time-to-first-medical-opinion for participating patients
7. **Cost Viability**: Operational cost <₹10 per consultation including cloud, support, and updates

---

## 9. Appendix

### 9.1 Acronyms

- ASHA: Accredited Social Health Activist
- ANM: Auxiliary Nurse Midwife
- PHC: Primary Health Center
- ABHA: Ayushman Bharat Health Account
- DISHA: Digital Information Security in Healthcare Act
- FHIR: Fast Healthcare Interoperability Resources
- ICMR: Indian Council of Medical Research
- NITI: National Institution for Transforming India
- CSR: Corporate Social Responsibility

### 9.2 References

- National Health Profile 2023, Ministry of Health and Family Welfare
- Rural Health Statistics 2022-23, Health Management Information System
- WHO Guidelines on Digital Health Interventions, 2019
- Digital Information Security in Healthcare Act (Draft), 2018

---

**Document Control**

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-02-14 | AI Systems Architecture Team | Initial requirements specification |

**Approval**

This document requires review and approval from:
- Chief Medical Officer, State Health Department
- Chief Technology Officer, Implementation Partner
- Data Protection Officer
- Clinical Advisory Board

---

*End of Requirements Specification*
