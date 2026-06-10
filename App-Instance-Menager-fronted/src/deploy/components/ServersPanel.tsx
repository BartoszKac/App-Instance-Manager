// src/deploy/components/ServersPanel.tsx
import React from 'react';
import { getServerId } from '../model/DeployState';
import { DeployViewModel } from '../viewmodel/useDeployViewModel';
import { Checkbox, StatusDot } from './DeployAtoms';

interface ServersPanelProps {
  vm: DeployViewModel;
}

export const ServersPanel: React.FC<ServersPanelProps> = ({ vm }) => {
  const {
    allServers,
    selectedServers,
    toggleServer,
    deleteServer,
    loading,
    showAddServer,
    setShowAddServer,
    newServer,
    setNewServer,
    addServer,
    addingServer,
    addServerError,
  } = vm;

  return (
    <aside className="sidebar" style={{ width: 280, borderLeft: '1px solid var(--border)' }}>
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

      {loading && (
        <div style={{ padding: 16, color: 'var(--text3)', fontSize: 12 }}>
          <span className="deploy-spinner" style={{ marginRight: 8 }} />
          Ładowanie serwerów…
        </div>
      )}

      {!loading && (
        <div className="server-list">
          {allServers.map((srv) => {
            const id      = getServerId(srv);
            const checked = selectedServers.has(id);

            return (
              <div
                key={id}
                className={`server-card ${checked ? 'selected' : ''}`}
                onClick={() => toggleServer(id)}
              >
                <Checkbox checked={checked} onChange={() => toggleServer(id)} />
                <StatusDot status="online" />
                <div className="server-info">
                  <div className="server-name">{srv.name}</div>
                  <div className="server-address">{srv.user}@{srv.ip}</div>
                </div>
                {srv.operationSystem && (
                  <span style={{
                    fontSize: 9, color: 'var(--text3)', fontFamily: 'var(--mono)',
                    background: 'var(--bg3)', padding: '1px 5px', borderRadius: 3,
                  }}>
                    {srv.operationSystem}
                  </span>
                )}
                <button
                  onClick={(e) => { e.stopPropagation(); deleteServer(id); }}
                  style={{
                    background: 'transparent', border: 'none', cursor: 'pointer',
                    color: '#ef444460', padding: '2px 4px', borderRadius: 3,
                    display: 'flex', alignItems: 'center', transition: 'color .12s',
                  }}
                  onMouseEnter={(e) => (e.currentTarget.style.color = '#ef4444')}
                  onMouseLeave={(e) => (e.currentTarget.style.color = '#ef444460')}
                  title="Usuń serwer"
                >
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <polyline points="3 6 5 6 21 6" />
                    <path d="M19 6l-1 14H6L5 6" />
                    <path d="M10 11v6M14 11v6" />
                    <path d="M9 6V4h6v2" />
                  </svg>
                </button>
              </div>
            );
          })}

          {allServers.length === 0 && (
            <div style={{ padding: 16, color: 'var(--text3)', fontSize: 12, textAlign: 'center' }}>
              Brak serwerów. Dodaj pierwszy.
            </div>
          )}
        </div>
      )}

      {showAddServer && (
        <div className="modal-backdrop open">
          <div className="modal">
            <div className="modal-title">Nowy serwer</div>
            <div className="modal-sub">Konfiguracja SSH</div>

            {([
              { label: 'Nazwa',      key: 'name' as const, ph: 'Production', type: 'text'     },
              { label: 'Host / IP',  key: 'host' as const, ph: '192.168.1.1',type: 'text'     },
              { label: 'Użytkownik', key: 'user' as const, ph: 'deploy',     type: 'text'     },
              { label: 'Hasło SSH',  key: 'pass' as const, ph: '••••••••',   type: 'password' },
            ] as const).map((f) => (
              <div key={f.key} className="form-group">
                <label className="form-label">{f.label}</label>
                <input
                  type={f.type}
                  value={newServer[f.key]}
                  onChange={(e) => setNewServer((s) => ({ ...s, [f.key]: e.target.value }))}
                  placeholder={f.ph}
                  onKeyDown={(e) => { if (e.key === 'Enter') addServer(); }}
                  className="form-input"
                />
              </div>
            ))}

            {addServerError && (
              <div style={{
                fontSize: 11, color: '#ef4444', fontFamily: 'var(--mono)',
                padding: '6px 8px', background: '#ef444415',
                borderRadius: 4, marginBottom: 4,
              }}>
                ✗ {addServerError}
              </div>
            )}

            <div className="modal-footer">
              <button
                onClick={() => setShowAddServer(false)}
                className="btn-cancel"
                disabled={addingServer}
              >
                Anuluj
              </button>
              <button
                onClick={addServer}
                className="btn-confirm"
                disabled={addingServer || !newServer.name.trim() || !newServer.host.trim()}
                style={{ opacity: addingServer ? 0.7 : 1 }}
              >
                {addingServer ? <><span className="deploy-spinner" />Dodawanie…</> : 'Dodaj'}
              </button>
            </div>
          </div>
        </div>
      )}
    </aside>
  );
};