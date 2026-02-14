"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = __importDefault(require("express"));
const cors_1 = __importDefault(require("cors"));
const helmet_1 = __importDefault(require("helmet"));
const morgan_1 = __importDefault(require("morgan"));
const triage_js_1 = __importDefault(require("./routes/triage.js"));
const dashboard_js_1 = __importDefault(require("./routes/dashboard.js"));
const app = (0, express_1.default)();
const port = process.env.PORT || 3000;
// Security Middleware
app.use((0, helmet_1.default)());
app.use((0, cors_1.default)());
app.use((0, morgan_1.default)('dev'));
app.use(express_1.default.json());
// Routes
app.use('/api/v1/triage', triage_js_1.default);
app.use('/api/v1/dashboard', dashboard_js_1.default);
// Health Checks
app.get('/health', (req, res) => {
    res.status(200).json({ status: 'OK', message: 'BRAHOS Backend is healthy' });
});
app.get('/', (req, res) => {
    res.send('Bharat Rural AI Health Orchestration System (BRAHOS) API');
});
app.listen(port, () => {
    console.log(`BRAHOS Server running on port ${port}`);
});
