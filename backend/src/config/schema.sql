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

-- -----------------------------------------------------------------------------
-- ROW LEVEL SECURITY (RLS)
-- Define strict access policies for multi-tenant data isolation
-- -----------------------------------------------------------------------------

-- 1. Enable RLS on the table
ALTER TABLE triage_assessments ENABLE ROW LEVEL SECURITY;

-- 2. Create Roles (Simulated for this implementation)
-- In a real deployment, these would constitute actual DB users or JWT claims
-- CREATE ROLE dashboard_viewer;
-- CREATE ROLE sync_worker;

-- 3. Policy: Dashboard Viewer (Read-Only)
-- Can view all assessments to populate the dashboard stats
CREATE POLICY dashboard_read_policy ON triage_assessments
    FOR SELECT
    USING (true); -- In a real multi-tenant app, this would be: auth.role() = 'dashboard_viewer'

-- 4. Policy: Sync Worker (Insert/Update)
-- Can insert new assessments from the field
CREATE POLICY sync_write_policy ON triage_assessments
    FOR INSERT
    WITH CHECK (true); -- Real world: auth.role() = 'sync_worker'

-- 5. Policy: No Delete
-- Data is immutable audit trail. No delete policy exists, ensuring no one can delete rows.
