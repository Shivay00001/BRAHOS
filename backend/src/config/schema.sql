-- BRAHOS Database Schema Initialization
-- Focus: Robustness, Clinical Traceability, and Searchability

CREATE TABLE IF NOT EXISTS triage_assessments (
    id UUID PRIMARY KEY,
    patient_id VARCHAR(50) NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    primary_observation TEXT,
    suggestions TEXT[],
    is_emergency BOOLEAN DEFAULT FALSE,
    image_uri TEXT,
    confidence_score REAL,
    sync_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Index for fast risk-level aggregation (Dashboard)
CREATE INDEX idx_risk_level ON triage_assessments(risk_level);

-- Index for patient history retrieval
CREATE INDEX idx_patient_id ON triage_assessments(patient_id);

-- Index for time-series analysis
CREATE INDEX idx_created_at ON triage_assessments(created_at);
