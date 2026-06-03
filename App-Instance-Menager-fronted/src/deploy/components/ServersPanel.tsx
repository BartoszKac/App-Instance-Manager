// src/deploy/components/ServersPanel.tsx
import React from 'react';
import { DeployViewModel } from '@/deploy/viewmodel';
import { Checkbox, StatusDot } from '@/deploy/components';

interface ServersPanelProps {
  vm: DeployViewModel;
}

export const ServersPanel: React.FC<ServersPanelProps> = ({ vm }) => {
  const {
    allServers,
    selectedServers,
    toggleServer,
    showAddServer,
    setShowAddServer,
    newServer,
    setNewServer,
    addServer,
  } = vm;

  return (
    <aside className="sidebar" style={{ width: 280, borderLeft: '1px solid var(--border)' }}>
      {/* header */}
      <div className="sidebar-header">
        <div>
          <div className="sidebar-section-title">Serwery docelowe</div>
          <div className="sidebar-badge">{selectedServers.size} zaznaczonych</div>
        </div>
        <button onClick={() => setShowAddServer(true)} className="nav-btn secondary small">
          <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
            <path d="M12 5v14M5 12h14" />
          </svg>
          Dodaj
        </button>
      </div>

      {/* server list */}
      <div className="server-list">
        {allServers.map((srv) => {
          const checked = selectedServers.has(srv.id);
          const offline = srv.status === 'offline';
          return (
            <div
              key={srv.id}
              onClick={() => !offline && toggleServer(srv.id)}
              className={`server-card ${checked ? 'selected' : ''} ${offline ? 'offline' : ''}`}
            >
              <Checkbox checked={checked} onChange={() => !offline && toggleServer(srv.id)} />
              <StatusDot status={srv.status} />
              <div className="server-info">
                <div className="server-name">{srv.name}</div>
                <div className="server-address">
                  {srv.user}@{srv.host}:{srv.port}
                </div>
              </div>
              {offline && (
                <span style={{ fontSize: 10, color: '#ef4444', fontFamily: 'var(--mono)' }}>offline</span>
              )}
            </div>
          );
        })}
      </div>

      {/* add server modal */}
      {showAddServer && (
        <div className="modal-backdrop open">
          <div className="modal">
            <div className="modal-title">Nowy serwer</div>
            <div className="modal-sub">Konfiguracja SSH</div>

            {([
              { label: 'Nazwa',     key: 'name' as const, ph: 'Production'  },
              { label: 'Host / IP', key: 'host' as const, ph: '192.168.1.1' },
              { label: 'Port SSH',  key: 'port' as const, ph: '22'          },
              { label: 'Użytkownik', key: 'user' as const, ph: 'deploy'     },
            ] as const).map((f) => (
              <div key={f.key} className="form-group">
                <label className="form-label">{f.label}</label>
                <input
                  value={newServer[f.key]}
                  onChange={(e) => setNewServer((s) => ({ ...s, [f.key]: e.target.value }))}
                  placeholder={f.ph}
                  onKeyDown={(e) => e.key === 'Enter' && addServer()}
                  className="form-input"
                />
              </div>
            ))}

            <div className="modal-footer">
              <button onClick={() => setShowAddServer(false)} className="btn-cancel">Anuluj</button>
              <button onClick={addServer} className="btn-confirm">Dodaj</button>
            </div>
          </div>
        </div>
      )}
    </aside>
  );
};
