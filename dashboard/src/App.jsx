import React, { useState, useEffect } from 'react';
import { Activity, AlertTriangle, CheckCircle, Clock, Shield, Search } from 'lucide-react';

const App = () => {
  const [summary, setSummary] = useState({
    GREEN_STABLE: 12,
    YELLOW_OBSERVE: 5,
    RED_EMERGENCY: 2
  });

  const [latestTriage, setLatestTriage] = useState([
    {
      id: "1",
      patient_id: "PB-9921",
      risk_level: "RED_EMERGENCY",
      primary_observation: "Persistent high fever, respiratory distress",
      created_at: new Date().toISOString(),
      confidence_score: 0.98
    },
    {
      id: "2",
      patient_id: "PB-9922",
      risk_level: "YELLOW_OBSERVE",
      primary_observation: "Fatigue, body ache, mild cough",
      created_at: new Date(Date.now() - 3600000).toISOString(),
      confidence_score: 0.85
    }
  ]);

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
                    {triage.risk_level.split('_')[1]}
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
                  <button style={{ background: 'var(--primary)', border: 'none', padding: '0.4rem 0.8rem', borderRadius: '0.4rem', color: 'white', fontSize: '0.8rem', cursor: 'pointer' }}>
                    Inspect
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default App;
