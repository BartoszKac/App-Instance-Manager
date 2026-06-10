// src/execution/views/ExecutionView.tsx
import React, { useState, useRef, useEffect } from 'react';
import { Topbar } from '@/editor/components';
import { useExecutionViewModel } from '@/execution/viewmodels/useExecutionViewModel';
import { DeployedInstance } from '@/execution/model/ExecutionModel';
import { LangBadge, StatusDot } from '@/deploy/components';

interface ExecutionViewProps {
  onProjects: () => void;
  onDeploy: () => void;
}

interface Preset {
  label: string;
  cmd: (inst: DeployedInstance) => string;
}

const PRESETS: Record<string, Preset[]> = {
  java: [
    { label: 'javac',     cmd: (i) => `javac ${i.fileName}` },
    { label: 'java run',  cmd: (i) => `java ${i.fileName.replace('.java', '')}` },
    { label: 'java -jar', cmd: (i) => `java -jar ${i.fileName.replace('.java', '.jar')}` },
    { label: 'ls',        cmd: () => 'ls -la' },
    { label: 'rm .class', cmd: (i) => `rm -f ${i.fileName.replace('.java', '.class')}` },
  ],
  py: [
    { label: 'python3',    cmd: (i) => `python3 ${i.fileName}` },
    { label: 'pip install',cmd: () => 'pip3 install -r requirements.txt' },
    { label: 'ls',         cmd: () => 'ls -la' },
    { label: 'rm .pyc',    cmd: (i) => `rm -f ${i.fileName.replace('.py', '.pyc')}` },
  ],
  ts: [
    { label: 'ts-node',  cmd: (i) => `npx ts-node ${i.fileName}` },
    { label: 'tsc',      cmd: (i) => `npx tsc ${i.fileName}` },
    { label: 'node run', cmd: (i) => `node ${i.fileName.replace('.ts', '.js')}` },
    { label: 'ls',       cmd: () => 'ls -la' },
  ],
  sh: [
    { label: 'ls',  cmd: () => 'ls -la' },
    { label: 'pwd', cmd: () => 'pwd' },
    { label: 'df',  cmd: () => 'df -h' },
  ],
};

export const ExecutionView: React.FC<ExecutionViewProps> = ({ onProjects, onDeploy }) => {
  const vm = useExecutionViewModel();
  const [customCommands, setCustomCommands] = useState<Record<string, string>>({});
  const [openPresets, setOpenPresets]       = useState<Record<string, boolean>>({});
  const terminalEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    terminalEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [vm.terminalLines]);

  const handleCmdChange = (id: string, val: string) => {
    setCustomCommands(prev => ({ ...prev, [id]: val }));
  };

  const handleRun = (inst: DeployedInstance) => {
    const cmd = customCommands[inst.id] ?? '';
    if (!cmd.trim()) return;
    vm.executeCommand(inst.idConfiguration, cmd);
  };

  const handlePreset = (inst: DeployedInstance, cmd: string) => {
    vm.executeCommand(inst.idConfiguration, cmd);
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>, inst: DeployedInstance) => {
    if (e.key === 'Enter') handleRun(inst);
  };

  const togglePresets = (id: string) => {
    setOpenPresets(prev => ({ ...prev, [id]: !prev[id] }));
  };

  const lineColor = (type: string) => {
    switch (type) {
      case 'command': return '#eee';
      case 'success': return 'var(--green, #4caf50)';
      case 'error':   return 'var(--red, #f44336)';
      default:        return 'var(--text3, #666)';
    }
  };

  return (
    <div className="screen-execution">
      <Topbar
        left={
          <div className="nav-header" style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
            <div className="nav-tabs">
              <button className="nav-tab" onClick={onProjects}>Projekty</button>
              <button className="nav-tab" onClick={onDeploy}>Deploy</button>
              <button className="nav-tab active">Instancje</button>
            </div>
            <div className="breadcrumb">
              <span className="breadcrumb-sep">/</span>
              <span className="breadcrumb-cur">aktywne-instancje</span>
            </div>
          </div>
        }
      />

      <div className="execution-layout">
        <main className="execution-main" style={{ flex: 1, overflowY: 'auto', padding: '20px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '20px' }}>
            <h2 style={{ fontSize: '18px', margin: 0 }}>Zdalne Instancje</h2>
            <button
              className="nav-btn"
              onClick={vm.fetchInstances}
              disabled={vm.loading}
              style={{ fontSize: '12px', padding: '4px 10px' }}
            >
              {vm.loading ? 'Ładowanie...' : '⟳ Odśwież'}
            </button>
          </div>

          {vm.error && (
            <div style={{
              background: 'rgba(244,67,54,0.1)',
              border: '1px solid var(--red, #f44336)',
              borderRadius: '6px',
              padding: '10px 14px',
              marginBottom: '16px',
              fontSize: '13px',
              color: 'var(--red, #f44336)',
            }}>
              ⚠ {vm.error}
            </div>
          )}

          {vm.loading && vm.instances.length === 0 ? (
            <div style={{ color: 'var(--text3)', fontSize: '13px' }}>Pobieranie instancji...</div>
          ) : vm.instances.length === 0 && !vm.loading ? (
            <div style={{ color: 'var(--text3)', fontSize: '13px' }}>Brak skonfigurowanych instancji.</div>
          ) : (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(400px, 1fr))', gap: '20px' }}>
              {vm.instances.map((inst: DeployedInstance) => {
                const presets = PRESETS[inst.lang] ?? PRESETS.sh;
                const isOpen  = openPresets[inst.id] ?? false;

                return (
                  <div key={inst.id} className="instance-card">
                    {/* Header */}
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                      <div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                          <StatusDot status={inst.status} />
                          <span style={{ fontWeight: 600 }}>{inst.fileName}</span>
                          <span style={{ fontSize: '10px', color: 'var(--text3)', fontFamily: 'var(--mono)' }}>
                            #{inst.idConfiguration}
                          </span>
                        </div>
                        <div className="instance-path">
                          Serwer: {inst.serverName}<br />
                          Katalog: {inst.path}
                        </div>
                      </div>
                      <LangBadge lang={inst.lang} />
                    </div>

                    {/* Input + Run */}
                    <div className="command-input-group" style={{ marginTop: '15px' }}>
                      <input
                        className="form-input"
                        placeholder="Wpisz komendę..."
                        value={customCommands[inst.id] ?? ''}
                        onChange={e => handleCmdChange(inst.id, e.target.value)}
                        onKeyDown={e => handleKeyDown(e, inst)}
                      />
                      <button
                        className="nav-btn primary"
                        onClick={() => handleRun(inst)}
                        disabled={!(customCommands[inst.id] ?? '').trim()}
                      >
                        Run
                      </button>
                    </div>

                    {/* Presety — toggle */}
                    <div style={{ marginTop: '10px' }}>
                      <button
                        onClick={() => togglePresets(inst.id)}
                        style={{
                          background: 'transparent',
                          border: '1px solid var(--border)',
                          color: 'var(--text2)',
                          fontFamily: 'var(--mono)',
                          fontSize: '11px',
                          padding: '3px 10px',
                          borderRadius: '4px',
                          cursor: 'pointer',
                          display: 'flex',
                          alignItems: 'center',
                          gap: '5px',
                        }}
                      >
                        <span style={{ transition: 'transform .2s', display: 'inline-block', transform: isOpen ? 'rotate(90deg)' : 'rotate(0deg)' }}>▶</span>
                        Szybkie komendy
                      </button>

                      {isOpen && (
                        <div style={{ marginTop: '8px', display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
                          {presets.map((p) => (
                            <button
                              key={p.label}
                              className="preset-btn"
                              onClick={() => handlePreset(inst, p.cmd(inst))}
                              title={p.cmd(inst)}
                            >
                              {p.label}
                            </button>
                          ))}
                          <button
                            className="preset-btn"
                            style={{ marginLeft: 'auto', borderColor: 'var(--red)', color: 'var(--red)' }}
                            onClick={() => handlePreset(inst, `kill $(lsof -t -i)`)}
                          >
                            Kill
                          </button>
                        </div>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </main>

        {/* Terminal */}
        <aside style={{
          width: '350px',
          background: '#000',
          borderLeft: '1px solid var(--border)',
          padding: '15px',
          display: 'flex',
          flexDirection: 'column',
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
            <div style={{ color: 'var(--green)', fontFamily: 'var(--mono)', fontSize: '11px', opacity: 0.7 }}>
              TERMINAL OUTPUT
            </div>
            <button
              onClick={vm.clearTerminal}
              style={{
                background: 'transparent',
                border: '1px solid #333',
                color: '#666',
                fontFamily: 'var(--mono)',
                fontSize: '10px',
                padding: '2px 7px',
                borderRadius: '3px',
                cursor: 'pointer',
              }}
            >
              clear
            </button>
          </div>

          <div style={{ flex: 1, fontFamily: 'var(--mono)', fontSize: '12px', overflowY: 'auto' }}>
            {vm.terminalLines.map(line => (
              <div
                key={line.id}
                style={{ color: lineColor(line.type), lineHeight: '1.6', whiteSpace: 'pre-wrap', wordBreak: 'break-all' }}
              >
                {line.text}
              </div>
            ))}
            <div ref={terminalEndRef} />
            <span className="terminal-cursor" />
          </div>
        </aside>
      </div>
    </div>
  );
};