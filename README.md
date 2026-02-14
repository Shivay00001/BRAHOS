# Bharat Rural AI Health Orchestration System (BRAHOS)

[![License: BSL 1.1](https://img.shields.io/badge/License-BSL%201.1-blue.svg)](https://mariadb.com/bsl11/)

BRAHOS is a state-of-the-art health triage system designed to bridge the healthcare gap in rural India. It empowers community health workers (ASHA/ANM) with offline-capable AI tools to perform preliminary medical screening and triage.

## 🚀 Key Features

- **Offline-First AI**: On-device quantized LLM (Llama-3) and Vision (MobileNet) models for triage reasoning without internet.
- **Multimodal Intake**: Support for voice-to-text (Whisper) in 12+ Indian languages and clinical image capture (skin, tongue, wounds).
- **Advanced Diagnostics**: Cloud-free analysis of patient photos for Anemia (pallor) and Jaundice (icterus).
- **Intelligent Triage**: Three-tier risk stratification (Green/Yellow/Red) with hard-coded safety guardrails.
- **Offline Identity**: Instant patient lookup via encrypted QR codes, eliminating dependence on connectivity.
- **Clinical Dashboard**: Real-time web interface for doctors to monitor high-risk cases and inspect patient data remotely.
- **Privacy Core**: AES-256 encryption (SQLCipher) and Federated Learning to protect patient data.

## 📂 Documentation

- [Requirements Specification](requirements.md): Detailed problem statement, user personas, and functional requirements.
- [System Design](design.md): Deep dive into the technical architecture, AI pipeline, and security framework.

## 🛠️ Tech Stack

- **Mobile**: Android (Kotlin, Jetpack Compose, Room, TensorFlow Lite, ML Kit)
- **Backend**: Node.js, Express, PostgreSQL, AWS (ECS Fargate, S3)
- **Web**: React, Vite, CSS Modules
- **AI**: Quantized Whisper-tiny, Distilled Llama-3-1B, MobileNetV3
- **Security**: SQLCipher, TLS 1.3, Federated Learning

## ⚖️ License

This project is licensed under the [Business Source License 1.1 (BSL 1.1)](https://mariadb.com/bsl11/).
