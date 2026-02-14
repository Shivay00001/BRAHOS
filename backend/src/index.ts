import triageRouter from './routes/triage';

const app = express();
const port = process.env.PORT || 3000;

// Security Middleware
app.use(helmet());
app.use(cors());
app.use(morgan('dev'));
app.use(express.json());

// Routes
app.use('/api/v1/triage', triageRouter);

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
