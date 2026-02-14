"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = __importDefault(require("express"));
const express_validator_1 = require("express-validator");
const db_js_1 = require("../config/db.js");
const router = express_1.default.Router();
/**
 * Endpoint for syncing triage assessments from mobile devices.
 */
router.post('/sync', [
    (0, express_validator_1.body)('*.id').isUUID(),
    (0, express_validator_1.body)('*.patientId').notEmpty(),
    (0, express_validator_1.body)('*.riskLevel').isIn(['GREEN_STABLE', 'YELLOW_OBSERVE', 'RED_EMERGENCY']),
], (req, res) => {
    const errors = (0, express_validator_1.validationResult)(req);
    if (!errors.isEmpty()) {
        return res.status(400).json({ status: 'VALIDATION_ERROR', errors: errors.array() });
    }
    const triageData = req.body;
    // 2. Persist to PostgreSQL with UPSERT logic to prevent duplicates
    (async () => {
        try {
            const insertPromises = triageData.map(async (a) => {
                return (0, db_js_1.query)(`INSERT INTO triage_assessments 
                        (id, patient_id, risk_level, primary_observation, suggestions, is_emergency, image_uri, confidence_score, created_at)
                        VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
                        ON CONFLICT (id) DO NOTHING`, [a.id, a.patientId, a.riskLevel, a.primaryObservation, a.suggestions, a.requiresImmediateEscalation, a.imageUri, a.confidenceScore, a.timestamp]);
            });
            await Promise.all(insertPromises);
            res.status(200).json({
                status: 'SUCCESS',
                syncedAt: new Date().toISOString(),
                count: triageData.length
            });
        }
        catch (error) {
            console.error('[SyncAPI] Persistence failed:', error);
            res.status(500).json({ status: 'ERROR', message: 'Failed to persist triage data' });
        }
    })();
});
/**
 * Critical Escalation Endpoint.
 */
router.post('/escalate', [
    (0, express_validator_1.body)('patientId').notEmpty(),
    (0, express_validator_1.body)('riskLevel').equals('RED_EMERGENCY'),
], (req, res) => {
    const { patientId, riskLevel, reason, location } = req.body;
    console.log(`[ALERT] HIGH PRIORITY ESCALATION DETECTED`);
    console.log(`Patient: ${patientId} | Reason: ${reason} | Location: ${location}`);
    res.status(200).json({
        status: 'ESCALATED',
        escalationId: `ESC-${Date.now()}`,
        instruction: 'Notify nearest District Hospital immediately'
    });
});
exports.default = router;
