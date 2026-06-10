// src/deploy/components/CenterPanel.tsx
import React from 'react';
import { LANG_COLOR, LANG_EXT, STRATEGY_LABEL } from '../model/DeployState';
import { getServerId } from '../model/DeployState';
import { DeployViewModel } from '../viewmodel/useDeployViewModel';
import { StatusDot } from './DeployAtoms';

interface CenterPanelProps {
  vm: DeployViewModel;
}

const STRATEGY_COLOR = {
  SOURCE_CODE:       '#4f7ef7',
  COMPILED_CODE:     '#f59e0b',
  REMOTE_EXECUTABLE: '#22c55e',
} as const;

export const CenterPanel: React.FC<CenterPanelProps> = ({ vm }) => {
  const {
    selectedFilesList,
    selectedServersList,
    canDeploy,
    deploy,
    deploying,
    deployLog,
    deployDone,
  } = vm;

  return (
    <div className="deploy-center">

      {/* summary bar */}
      <div className="deploy-summary-bar">
        <div className={`summary-item ${selectedFilesList.length ? 'active-files' : ''}`}>
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="var(--text3)" strokeWidth="2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
            <polyline points="14 2 14 8 20 8" />
          </svg>
          <span className="summary-stat-count">{selectedFilesList.length}</span>
          <span style={{ color: 'var(--text3)' }}>pliki</span>
        </div>

        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--text3)" strokeWidth="2">
          <path d="M5 12h14M12 5l7 7-7 7" />
        </svg>

        <div className={`summary-item ${selectedServersList.length ? 'active-servers' : ''}`}>
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="var(--text3)" strokeWidth="2">
            <rect x="2" y="2" width="20" height="8" rx="2" />
            <rect x="2" y="14" width="20" height="8" rx="2" />
            <line x1="6" y1="6" x2="6.01" y2="6" />
            <line x1="6" y1="18" x2="6.01" y2="18" />
          </svg>
          <span className="summary-stat-count">{selectedServersList.length}</span>
          <span style={{ color: 'var(--text3)' }}>serwery</span>
        </div>

        <div style={{ flex: 1 }} />

        <button
          onClick={deploy}
          disabled={!canDeploy}
          className="btn-deploy-main"
          style={{ opacity: deploying ? 0.8 : 1 }}
        >
          {deploying ? (
            <><span className="deploy-spinner" />Deploying…</>
          ) : deployDone ? (
            <>
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                <path d="M20 6 9 17l-5-5" />
              </svg>
              Gotowe
            </>
          ) : (
            <>
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                <path d="M5 12h14M12 5l7 7-7 7" />
              </svg>
              Deploy
            </>
          )}
        </button>
      </div>

      {/* deploy plan */}
      {selectedFilesList.length > 0 && selectedServersList.length > 0 && !deployLog.length && (
        <div className="deploy-section" style={{ borderBottom: '1px solid var(--border)' }}>
          <div className="deploy-section-title">Plan deploymentu</div>
          <div className="deploy-plan-list">
            {selectedServersList.map((srv) => (
              <div key={getServerId(srv)} className="deploy-card">
                <StatusDot status="online" />
                <div style={{ fontSize: 12, fontWeight: 600, minWidth: 90, paddingTop: 1 }}>
                  {srv.name}
                  <div style={{ fontSize: 10, color: 'var(--text3)', fontFamily: 'var(--mono)', fontWeight: 400 }}>
                    {srv.user}@{srv.ip}
                  </div>
                </div>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 5 }}>
                  {selectedFilesList.map((file) => {
                    const color      = LANG_COLOR[file.language] ?? '#94a3b8';
                    const ext        = LANG_EXT[file.language]   ?? file.language.toLowerCase();
                    const stratColor = STRATEGY_COLOR[file.strategy] ?? '#94a3b8';
                    return (
                      <span
                        key={file.key}
                        style={{
                          fontSize: 11, fontFamily: 'var(--mono)',
                          background: `${color}20`,
                          padding: '2px 7px', borderRadius: 4,
                          border: `1px solid ${color}40`,
                          display: 'flex', alignItems: 'center', gap: 4,
                        }}
                      >
                        <span style={{ color: 'var(--text3)' }}>.{ext}/</span>
                        <span style={{ color }}>{file.name}</span>
                        <span style={{
                          fontSize: 9, color: stratColor,
                          background: stratColor + '20',
                          padding: '0px 4px', borderRadius: 2,
                          border: `1px solid ${stratColor}40`,
                        }}>
                          {STRATEGY_LABEL[file.strategy]}
                        </span>
                      </span>
                    );
                  })}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* empty state */}
      {!deployLog.length && (selectedFilesList.length === 0 || selectedServersList.length === 0) && (
        <div className="deploy-empty">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.2" opacity=".4">
            <path d="M5 12h14M12 5l7 7-7 7" />
          </svg>
          <div style={{ fontSize: 13, textAlign: 'center', lineHeight: 1.7 }}>
            Zaznacz pliki po lewej<br />i serwery po prawej<br />
            <span style={{ fontSize: 11 }}>Następnie kliknij Deploy</span>
          </div>
        </div>
      )}

      {/* deploy log */}
      {deployLog.length > 0 && (
        <div className="deploy-log">
          <div className="deploy-section-title">Log deploymentu</div>
          <div className="deploy-terminal">
            {deployLog.map((line, i) => {
              const color =
                line.includes('✅') ? '#22c55e' :
                line.includes('✗')  ? '#ef4444' :
                line.includes('→')  ? '#4f7ef7' :
                line.includes('⚡') ? '#f59e0b' :
                'var(--text2)';
              return (
                <div key={i} style={{ color, whiteSpace: 'pre-wrap' }}>{line}</div>
              );
            })}
            {deploying && <span className="terminal-cursor" />}
          </div>
        </div>
      )}
    </div>
  );
};