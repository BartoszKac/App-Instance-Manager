// src/execution/views/ExecutionView.tsx
import React, { useState } from 'react';
import { Topbar } from '@/editor/components';
import { useExecutionViewModel } from '@/execution/model/useExecutionViewModel';
import { LangBadge, StatusDot } from '@/deploy/components';
import { DeployedInstance } from '@/editor/model/EditorState';

interface ExecutionViewProps {
  onProjects: () => void;
  onDeploy: () => void;
}

export const ExecutionView: React.FC<ExecutionViewProps> = ({ onProjects, onDeploy }) => {
  const vm = useExecutionViewModel();
  const [customCommands, setCustomCommands] = useState<Record<string, string>>({});

  const handleCmdChange = (id: string, val: string) => {
    setCustomCommands(prev => ({ ...prev, [id]: val }));
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
        <main className="execution-main" style={{ flex: 1 }}>
          <h2 style={{ marginBottom: '20px', fontSize: '18px' }}>Zdalne Instancje</h2>
          
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(400px, 1fr))', gap: '20px' }}>
            {vm.instances.map((inst: DeployedInstance) => (
              <div key={inst.id} className="instance-card">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                      <StatusDot status={inst.status} />
                      <span style={{ fontWeight: 600 }}>{inst.fileName}</span>
                    </div>
                    <div className="instance-path">
                      Serwer: {inst.serverName}<br />
                      Katalog: {inst.path}
                    </div>
                  </div>
                  <LangBadge lang={inst.lang} />
                </div>

                <div className="command-input-group" style={{ marginTop: '15px' }}>
                  <input 
                    className="form-input" 
                    placeholder="Wpisz komendę..." 
                    value={customCommands[inst.id] || ''}
                    onChange={e => handleCmdChange(inst.id, e.target.value)}
                  />
                  <button className="nav-btn primary" onClick={() => vm.executeCommand(inst.id, customCommands[inst.id])}>Run</button>
                </div>

                <div className="command-presets">
                  {inst.lang === 'java' && (
                    <>
                      <button className="preset-btn" onClick={() => vm.executeCommand(inst.id, "javac " + inst.fileName)}>javac</button>
                      <button className="preset-btn" onClick={() => vm.executeCommand(inst.id, "java " + inst.fileName.split('.')[0])}>java Run</button>
                    </>
                  )}
                  {inst.lang === 'py' && (
                    <button className="preset-btn" onClick={() => vm.executeCommand(inst.id, "python3 " + inst.fileName)}>python3</button>
                  )}
                  <button className="preset-btn" style={{ marginLeft: 'auto', borderColor: 'var(--red)', color: 'var(--red)' }}>Kill</button>
                </div>
              </div>
            ))}
          </div>
        </main>

        <aside style={{ width: '350px', background: '#000', borderLeft: '1px solid var(--border)', padding: '15px', display: 'flex', flexDirection: 'column' }}>
           <div style={{ color: 'var(--green)', fontFamily: 'var(--mono)', fontSize: '11px', marginBottom: '10px', opacity: 0.7 }}>
             TERMINAL OUTPUT
           </div>
           <div style={{ flex: 1, fontFamily: 'var(--mono)', fontSize: '12px', color: '#eee', overflowY: 'auto' }}>
             <div style={{ color: 'var(--text3)' }}>[10:42:01] Connected to SSH...</div>
             <div>$ java Main</div>
             <div style={{ color: 'var(--green)' }}>Hello from remote server!</div>
             <div style={{ color: 'var(--text3)' }}>[10:42:05] Process finished.</div>
             <span className="terminal-cursor" />
           </div>
        </aside>
      </div>
    </div>
  );
};