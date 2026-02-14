import express from 'express';
import type { Request, Response } from 'express';
import { body, validationResult } from 'express-validator';

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

        const triageData = req.body as TriageSyncData[];
        console.log(`[Sync] Storing ${triageData.length} records...`);

        res.status(200).json({
            status: 'SUCCESS',
            syncedAt: new Date().toISOString(),
            count: triageData.length
        });
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
