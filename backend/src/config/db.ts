import pg from 'pg';
import dotenv from 'dotenv';

dotenv.config();

const { Pool } = pg;

/**
 * Database connection pool.
 * Configured for robustness and automatic reconnection.
 */
const pool = new Pool({
    connectionString: process.env.DATABASE_URL,
    ssl: process.env.NODE_ENV === 'production' ? { rejectUnauthorized: false } : false,
});

pool.on('error', (err) => {
    console.error('[DB] Unexpected error on idle client:', err);
});

export const query = (text: string, params: any[]) => pool.query(text, params);

export default pool;
