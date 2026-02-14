import React, { useState, useEffect } from 'react';
import { Activity, AlertTriangle, CheckCircle, Clock, Shield, Search } from 'lucide-react';

const BASE_URL = 'http://localhost:3000/api/v1/dashboard';

const App = () => {
  const [summary, setSummary] = useState({
    GREEN_STABLE: 0,
    YELLOW_OBSERVE: 0,
    RED_EMERGENCY: 0
  });

  const [latestTriage, setLatestTriage] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedPatient, setSelectedPatient] = useState(null);
  const [isPanelOpen, setIsPanelOpen] = useState(false);

  const handleInspect = (patient) => {
    setSelectedPatient(patient);
    setIsPanelOpen(true);
  };

  const fetchData = async () => {
    try {
      const [sumRes, lateRes] = await Promise.all([
        fetch(`${BASE_URL}/summary`),
        fetch(`${BASE_URL}/latest`)
      ]);

      if (!sumRes.ok || !lateRes.ok) throw new Error('Failed to fetch from server');

      const sumData = await sumRes.json();
      const lateData = await lateRes.json();

      if (sumData.status === 'success') setSummary(sumData.data);
      if (lateData.status === 'success') setLatestTriage(lateData.data);

      setError(null);
    } catch (err) {
      console.error('Fetch failed:', err);
      setError('Connection to BRAHOS Backend failed. Check server status.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, 30000); // Polling every 30s
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="dashboard-container">
      <header>
        <div className="logo-section">
          <h1>BRAHOS <span style={{ fontWeight: 400, opacity: 0.5 }}>Clinical Dashboard</span></h1>
        </div>
        <div className="status-badge" style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#10b981' }}>
          <div style={{ width: 8, height: 8, borderRadius: '50%', background: '#10b981', boxShadow: '0 0 10px #10b981' }}></div>
          Live Connection
        </div>
      </header>

      {error && (
        <div style={{ background: 'rgba(239, 68, 68, 0.1)', border: '1px solid var(--red)', padding: '1rem', borderRadius: '0.5rem', marginBottom: '2rem', color: 'var(--red)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <AlertTriangle size={20} />
            <span>{error}</span>
          </div>
        </div>
      )}

      <div className="risk-overview">
        <div className="stat-card risk-red">
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <div>
              <p style={{ color: '#94a3b8', fontSize: '0.875rem' }}>Critical (Red)</p>
              <h2 style={{ fontSize: '2rem', marginTop: '0.5rem' }}>{summary.RED_EMERGENCY}</h2>
            </div>
            <AlertTriangle color="#ef4444" size={32} />
          </div>
          <p style={{ fontSize: '0.75rem', marginTop: '1rem', color: '#ef4444', opacity: 0.8 }}>Requires Immediate Review</p>
        </div>

        <div className="stat-card risk-yellow">
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <div>
              <p style={{ color: '#94a3b8', fontSize: '0.875rem' }}>Observation (Yellow)</p>
              <h2 style={{ fontSize: '2rem', marginTop: '0.5rem' }}>{summary.YELLOW_OBSERVE}</h2>
            </div>
            <Clock color="#f59e0b" size={32} />
          </div>
          <p style={{ fontSize: '0.75rem', marginTop: '1rem', color: '#f59e0b', opacity: 0.8 }}>Monitor Progress</p>
        </div>

        <div className="stat-card risk-green">
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <div>
              <p style={{ color: '#94a3b8', fontSize: '0.875rem' }}>Stable (Green)</p>
              <h2 style={{ fontSize: '2rem', marginTop: '0.5rem' }}>{summary.GREEN_STABLE}</h2>
            </div>
            <CheckCircle color="#10b981" size={32} />
          </div>
          <p style={{ fontSize: '0.75rem', marginTop: '1rem', color: '#10b981', opacity: 0.8 }}>Outpatient care</p>
        </div>
      </div>

      <div className="triage-feed">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
          <h3>Recent Triage Events</h3>
          <div style={{ position: 'relative' }}>
            <Search size={16} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', opacity: 0.5 }} />
            <input
              type="text"
              placeholder="Search Patient ID..."
              style={{ padding: '0.5rem 1rem 0.5rem 2.5rem', background: 'rgba(255,255,255,0.05)', border: 'none', borderRadius: '0.5rem', color: 'white', width: '300px' }}
            />
          </div>
        </div>

        {loading && latestTriage.length === 0 ? (
          <div style={{ padding: '4rem', textAlign: 'center', color: '#94a3b8' }}>Loading triage data...</div>
        ) : (
          <table className="triage-table">
            <thead>
              <tr>
                <th>Patient</th>
                <th>Status</th>
                <th>Symptoms</th>
                <th>Confidence</th>
                <th>Time</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {latestTriage.map(triage => (
                <tr key={triage.id}>
                  <td>
                    <div style={{ fontWeight: 600 }}>{triage.patient_id}</div>
                    <div style={{ fontSize: '0.75rem', color: '#94a3b8' }}>ID: {triage.id.substring(0, 8)}</div>
                  </td>
                  <td>
                    <span className={`badge badge-${triage.risk_level === 'RED_EMERGENCY' ? 'red' : triage.risk_level === 'YELLOW_OBSERVE' ? 'yellow' : 'green'}`}>
                      {triage.risk_level.replace('RiskLevel.', '').split('_')[1] || triage.risk_level.split('_')[1] || triage.risk_level}
                    </span>
                  </td>
                  <td style={{ maxWidth: '400px' }}>
                    <p style={{ fontSize: '0.9rem' }}>{triage.primary_observation}</p>
                  </td>
                  <td>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <div style={{ flex: 1, height: 4, background: 'rgba(255,255,255,0.1)', borderRadius: 2 }}>
                        <div style={{ width: `${triage.confidence_score * 100}%`, height: '100%', background: '#2563eb', borderRadius: 2 }}></div>
                      </div>
                      <span style={{ fontSize: '0.75rem', opacity: 0.7 }}>{(triage.confidence_score * 100).toFixed(0)}%</span>
                    </div>
                  </td>
                  <td>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '4px', opacity: 0.6, fontSize: '0.8rem' }}>
                      <Clock size={12} />
                      {new Date(triage.created_at).toLocaleTimeString()}
                    </div>
                  </td>
                  <td>
                    <button
                      onClick={() => handleInspect(triage)}
                      style={{ background: 'var(--primary)', border: 'none', padding: '0.4rem 0.8rem', borderRadius: '0.4rem', color: 'white', fontSize: '0.8rem', cursor: 'pointer' }}>
                      Inspect
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <div className={`side-panel ${isPanelOpen ? 'open' : ''}`}>
        {selectedPatient && (
          <>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
              <h2 style={{ fontSize: '1.25rem' }}>Assessment Details</h2>
              <button
                onClick={() => setIsPanelOpen(false)}
                style={{ background: 'none', border: 'none', color: '#94a3b8', cursor: 'pointer', fontSize: '1.25rem' }}>×</button>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '2rem' }}>
              <div style={{ width: 48, height: 48, borderRadius: '50%', background: 'rgba(37, 99, 235, 0.1)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <Activity size={24} color="var(--primary)" />
              </div>
              <div>
                <p style={{ fontSize: '0.8rem', color: '#94a3b8' }}>Patient ID</p>
                <p style={{ fontWeight: 600, fontSize: '1.1rem' }}>{selectedPatient.patient_id}</p>
              </div>
            </div>

            <div className={`badge badge-${selectedPatient.risk_level === 'RED_EMERGENCY' ? 'red' : selectedPatient.risk_level === 'YELLOW_OBSERVE' ? 'yellow' : 'green'}`} style={{ marginBottom: '1.5rem', display: 'inline-block' }}>
              {selectedPatient.risk_level.replace('_', ' ')}
            </div>

            <h4 style={{ color: '#94a3b8', fontSize: '0.8rem', textTransform: 'uppercase', marginBottom: '1rem', marginTop: '1rem' }}>Clinical Observations</h4>
            <div style={{ background: 'rgba(0,0,0,0.2)', padding: '1.25rem', borderRadius: '0.75rem', border: '1px solid rgba(255,255,255,0.05)', marginBottom: '1.5rem' }}>
              <p style={{ fontSize: '0.95rem', lineHeight: 1.6, color: '#e2e8f0' }}>{selectedPatient.primary_observation}</p>
            </div>

            {selectedPatient.image_uri && (
              <>
                <h4 style={{ color: '#94a3b8', fontSize: '0.8rem', textTransform: 'uppercase', marginBottom: '1rem' }}>Patient Record Photo</h4>
                <img src={selectedPatient.image_uri} alt="Patient Record" className="patient-image" />
              </>
            )}

            <h4 style={{ color: '#94a3b8', fontSize: '0.8rem', textTransform: 'uppercase', marginBottom: '1rem' }}>AI Recommendations</h4>
            <ul style={{ listStyle: 'none', padding: 0 }}>
              {selectedPatient.suggestions?.map((s, i) => (
                <li key={i} style={{ display: 'flex', gap: '0.75rem', marginBottom: '1rem', fontSize: '0.9rem', color: '#cbd5e1' }}>
                  <Shield size={16} color="var(--primary)" style={{ flexShrink: 0, marginTop: '2px' }} />
                  {s}
                </li>
              ))}
            </ul>

            <div style={{ marginTop: '3rem', padding: '1.25rem', background: 'rgba(37, 99, 235, 0.1)', borderRadius: '0.75rem', border: '1px solid rgba(37, 99, 235, 0.2)' }}>
              <p style={{ fontSize: '0.875rem', color: '#60a5fa', marginBottom: '0.5rem', fontWeight: 600 }}>Action Required</p>
              <p style={{ fontSize: '0.8rem', color: '#94a3b8', lineHeight: 1.5 }}>
                {selectedPatient.risk_level === 'RED_EMERGENCY'
                  ? "Coordinate with nearest District Hospital for immediate transfer. Notify ambulance services."
                  : selectedPatient.risk_level === 'YELLOW_OBSERVE'
                    ? "Monitor patient remotely and follow up within 24-48 hours. Ensure patient is isolating if symptoms persist."
                    : "Patient is stable. Continue basic care and monitor for any changes."}
              </p>
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default App;
