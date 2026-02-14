import express from 'express';
import { query } from '../config/db.js';
const router = express.Router();
/**
 * GET /summary
 * Returns an overview of risk levels for the dashboard.
 */
router.get('/summary', async (req, res) => {
    try {
        const result = await query(`SELECT risk_level, COUNT(*) as count 
             FROM triage_assessments 
             GROUP BY risk_level`, []);
        const summary = result.rows.reduce((acc, row) => {
            acc[row.risk_level] = parseInt(row.count);
            return acc;
        }, { GREEN_STABLE: 0, YELLOW_OBSERVE: 0, RED_EMERGENCY: 0 });
        res.json({
            status: 'success',
            data: summary
        });
    }
    catch (error) {
        console.error('[DashboardAPI] Summary failed:', error);
        res.status(500).json({ status: 'error', message: 'Internal Server Error' });
    }
});
/**
 * GET /latest
 * Returns the most recent 50 assessments for the feed.
 */
router.get('/latest', async (req, res) => {
    try {
        const result = await query('SELECT * FROM triage_assessments ORDER BY created_at DESC LIMIT 50', []);
        res.json({
            status: 'success',
            data: result.rows
        });
    }
    catch (error) {
        console.error('[DashboardAPI] Latest feed failed:', error);
        res.status(500).json({ status: 'error', message: 'Internal Server Error' });
    }
});
export default router;
//# sourceMappingURL=dashboard.js.map