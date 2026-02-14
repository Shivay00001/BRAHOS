"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = __importDefault(require("express"));
const db_js_1 = require("../config/db.js");
const router = express_1.default.Router();
/**
 * GET /summary
 * Returns an overview of risk levels for the dashboard.
 */
router.get('/summary', async (req, res) => {
    try {
        const result = await (0, db_js_1.query)(`SELECT risk_level, COUNT(*) as count 
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
        const result = await (0, db_js_1.query)('SELECT * FROM triage_assessments ORDER BY created_at DESC LIMIT 50', []);
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
exports.default = router;
