// src/deploy/components/ExecutionPanel.tsx
import React, { useState } from 'react';
import { DeployViewModel } from '../../deploy/viewmodel/useDeployViewModel';
import { StatusDot, LangBadge } from '../../deploy/components/DeployAtoms';

interface ExecutionPanelProps {
  vm: DeployViewModel;
}

export const ExecutionPanel: React.FC<ExecutionPanelProps> = ({ vm }) => {
  const [commands, setCommands] = useState<Record<string, string>>({});

  // Symulacja komend dla Javy
  const JAVA_PRESETS = [
    { label: 'javac', cmd: 'javac {file}' },
    { label: 'java Run', cmd: 'java {name}' },
    { label: 'Clean', cmd: 'rm *.class' },
  ];

  const handleCommandChange = (id: string, val: string) => {
    setCommands(prev => ({ ...prev, [id]: val }));
  };

  const runCommand = (id: string, preset?: string) => {
    const cmd = preset || commands[id];
    if (!cmd) return;
    console.log(`Executing on instance ${id}: ${cmd}`);
    // Tutaj docelowo vm.executeCommand(id, cmd)
  };

  return (
    <aside className="sidebar" style={{ width: 320, borderLeft: '1px solid var(--border)' }}>
      <div className="sidebar-header">
        <span className="sidebar-section-title">Aktywne Instancje</span>
        <span className="sidebar-badge">Live</span>
      </div>

      <div className="server-list">
        {/* Mapujemy po "wysłanych" plikach. 
            Jeśli vm.deployedFiles jest pusty w Twoim modelu, 
            możesz użyć vm.selectedFilesList jako podgląd */}
        {vm.selectedFilesList.length === 0 && (
          <div className="empty-state">Brak wysłanych plików do uruchomienia.</div>
        )}

        {vm.selectedFilesList.map(({ project, file }, idx) => {
          const instanceId = `${project.id}-${file.name}`;
          return (
            <div key={instanceId} className="instance-card">
              <div className="instance-info">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span className="server-name" style={{ fontSize: 13 }}>{file.name}</span>
                  <LangBadge lang={project.lang} />
                </div>
                <span className="instance-path">
                  Server: <span style={{ color: 'var(--text2)' }}>Production-1</span><br />
                  Path: /home/deploy/apps/{project.name}/
                </span>
              </div>

              <div className="command-input-group">
                <input 
                  className="form-input" 
                  style={{ fontSize: 11, height: 28 }}
                  placeholder="Wpisz komendę..."
                  value={commands[instanceId] || ''}
                  onChange={(e) => handleCommandChange(instanceId, e.target.value)}
                />
                <button className="nav-btn primary small" onClick={() => runCommand(instanceId)}>Run</button>
              </div>

              {project.lang === 'ts' || project.lang === 'js' ? (
                <div className="command-presets">
                  {JAVA_PRESETS.map(p => (
                    <button key={p.label} className="preset-btn" onClick={() => runCommand(instanceId, p.cmd)}>{p.label}</button>
                  ))}
                </div>
              ) : null}
            </div>
          );
        })}
      </div>
    </aside>
  );
};