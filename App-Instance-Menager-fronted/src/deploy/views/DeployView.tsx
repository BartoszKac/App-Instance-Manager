// src/deploy/views/DeployView.tsx
import React from 'react';
import { useDeployViewModel } from '@/deploy/viewmodel';
import { FilesPanel, CenterPanel, ServersPanel } from '@/deploy/components';
import { Topbar } from '@/editor/components';

interface DeployViewProps {
  onBack?: () => void;
  onExecution?: () => void;
}

export const DeployView: React.FC<DeployViewProps> = ({ onBack, onExecution }) => {
  const vm = useDeployViewModel();

  return (
    <div className="screen-deploy">
      <Topbar
        left={
          <div className="nav-header" style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
            <div className="nav-tabs">
              <button className="nav-tab" onClick={onBack}>Projekty</button>
              <button className="nav-tab active">Deploy</button>
              <button className="nav-tab" onClick={onExecution}>Instancje</button>
            </div>
            <div className="breadcrumb">
              <span className="breadcrumb-sep">/</span>
              <span className="breadcrumb-cur">deploy</span>
            </div>
          </div>
        }
        right={
          <div style={{ display: 'flex', alignItems: 'center', gap: 15 }}>
            {vm.loading ? (
              <span style={{ fontSize: 10, color: 'var(--text3)', fontFamily: 'var(--mono)' }}>
                <span className="deploy-spinner" style={{ marginRight: 4 }} />
                Ładowanie…
              </span>
            ) : (
              <span style={{ fontSize: 10, color: 'var(--text3)', fontFamily: 'var(--mono)' }}>
                {vm.selectedFilesList.length} pliki · {vm.selectedServersList.length} serwery
              </span>
            )}
            <div className="status-item" style={{ fontSize: 11, fontFamily: 'var(--mono)' }}>
              <div
                className="status-dot"
                style={{ background: vm.loadError ? '#ef4444' : '#22c55e' }}
              />
              <span>{vm.loadError ? 'Błąd' : 'Ready'}</span>
            </div>
          </div>
        }
      />

      {/* 3-column layout */}
      <div className="deploy-layout">
        <FilesPanel   vm={vm} />
        <CenterPanel  vm={vm} />
        <ServersPanel vm={vm} />
      </div>

      {/* statusbar */}
      <div className="statusbar">
        <div className="status-item">
          <div className="status-dot" />
          {vm.deploying ? 'Deploying…' : vm.deployDone ? 'Gotowe' : 'Gotowy'}
        </div>
        <span>SSH · Deploy Mode</span>
        <div className="status-spacer" />
        <span>{vm.allServers.length} serwerów</span>
        <span>· {vm.files.length} plików</span>
        <span>· UTF-8</span>
      </div>
    </div>
  );
};