import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import morgan from 'morgan';
import { rateLimit } from 'express-rate-limit';
import triageRouter from './routes/triage.js';
import dashboardRouter from './routes/dashboard.js';

const app = express();
const port = process.env.PORT || 3000;

// Security Middleware
app.use(helmet());
app.use(cors());
app.use(morgan('dev'));
app.use(express.json());

// Rate Limiting: Prevent abundance of requests (DDoS protection)
const limiter = rateLimit({
    windowMs: 15 * 60 * 1000, // 15 minutes
    limit: 100, // Limit each IP to 100 requests per windowMs
    standardHeaders: 'draft-7',
    legacyHeaders: false,
});
app.use(limiter);

// Routes
app.use('/api/v1/triage', triageRouter);
app.use('/api/v1/dashboard', dashboardRouter);

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
