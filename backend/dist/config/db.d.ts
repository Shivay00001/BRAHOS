import pg from 'pg';
/**
 * Database connection pool.
 * Configured for robustness and automatic reconnection.
 */
declare const pool: pg.Pool;
export declare const query: (text: string, params: any[]) => Promise<pg.QueryResult<any>>;
export default pool;
//# sourceMappingURL=db.d.ts.map