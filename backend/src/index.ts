import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import morgan from 'morgan';

const app = express();
const port = process.env.PORT || 3000;

// Security Middleware
app.use(helmet());
app.use(cors());
app.use(morgan('dev'));
app.use(express.json());

// Models (Simplified for Skeleton)
interface TriageSyncData {
    patientId: string;
    riskLevel: string;
    timestamp: number;
    deviceId: string;
}

// Routes
app.post('/api/v1/sync', (req, res) => {
    const data: TriageSyncData[] = req.body;
    console.log(`Received sync request for ${data.length} records`);
    // Logic: Save to PostgreSQL
    res.status(200).json({ status: 'SUCCESS', syncedCount: data.length });
});

app.post('/api/v1/escalate', (req, res) => {
    const { patientId, riskLevel, reason } = req.body;
    console.log(`EMERGENCY: Escalation requested for Patient ${patientId}`);
    // Logic: Trigger SMS/Notification to Doctor
    res.status(200).json({ status: 'ESCALATED', contactDoctor: true });
});

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
