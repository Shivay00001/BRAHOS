import express from 'express';
import type { Request, Response } from 'express';
import { body, validationResult } from 'express-validator';
import { query } from '../config/db.js';

const router = express.Router();

interface TriageSyncData {
    id: string;
    patientId: string;
    riskLevel: string;
}

/**
 * Endpoint for syncing triage assessments from mobile devices.
 */
router.post(
    '/sync',
    [
        body('*.id').isUUID(),
        body('*.patientId').notEmpty(),
        body('*.riskLevel').isIn(['GREEN_STABLE', 'YELLOW_OBSERVE', 'RED_EMERGENCY']),
    ],
    (req: Request, res: Response) => {
        const errors = validationResult(req);
        if (!errors.isEmpty()) {
            return res.status(400).json({ status: 'VALIDATION_ERROR', errors: errors.array() });
        }

        const triageData = req.body as any[];

        // 2. Persist to PostgreSQL with UPSERT logic to prevent duplicates
        (async () => {
            try {
                const insertPromises = triageData.map(async (a: any) => {
                    return query(
                        `INSERT INTO triage_assessments 
                        (id, patient_id, risk_level, primary_observation, suggestions, is_emergency, image_uri, confidence_score, created_at)
                        VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
                        ON CONFLICT (id) DO NOTHING`,
                        [a.id, a.patientId, a.riskLevel, a.primaryObservation, a.suggestions, a.requiresImmediateEscalation, a.imageUri, a.confidenceScore, a.timestamp]
                    );
                });

                await Promise.all(insertPromises);

                res.status(200).json({
                    status: 'SUCCESS',
                    syncedAt: new Date().toISOString(),
                    count: triageData.length
                });
            } catch (error) {
                console.error('[SyncAPI] Persistence failed:', error);
                res.status(500).json({ status: 'ERROR', message: 'Failed to persist triage data' });
            }
        })();
    }
);

/**
 * Critical Escalation Endpoint.
 */
router.post(
    '/escalate',
    [
        body('patientId').notEmpty(),
        body('riskLevel').equals('RED_EMERGENCY'),
    ],
    (req: Request, res: Response) => {
        const { patientId, riskLevel, reason, location } = req.body;

        console.log(`[ALERT] HIGH PRIORITY ESCALATION DETECTED`);
        console.log(`Patient: ${patientId} | Reason: ${reason} | Location: ${location}`);

        res.status(200).json({
            status: 'ESCALATED',
            escalationId: `ESC-${Date.now()}`,
            instruction: 'Notify nearest District Hospital immediately'
        });
    }
);

export default router;
