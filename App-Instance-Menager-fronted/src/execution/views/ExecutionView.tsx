// src/execution/views/ExecutionView.tsx
import React, { useState } from 'react';
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

interface PresetGroup {
  group: string;
  items: Preset[];
}

const PRESET_GROUPS: PresetGroup[] = [
  {
    group: 'Java',
    items: [
      { label: 'javac',        cmd: (i) => `javac ${i.fileName}` },
      { label: 'java run',     cmd: (i) => `java ${i.fileName.replace('.java', '')}` },
      { label: 'java -jar',    cmd: (i) => `java -jar ${i.fileName.replace('.java', '.jar')}` },
      { label: 'mvn package',  cmd: () =>  'mvn clean package' },
      { label: 'mvn run',      cmd: () =>  'mvn spring-boot:run' },
      { label: 'gradle build', cmd: () =>  './gradlew build' },
      { label: 'gradle run',   cmd: () =>  './gradlew run' },
      { label: 'rm .class',    cmd: (i) => `rm -f ${i.fileName.replace('.java', '.class')}` },
    ],
  },
  {
    group: 'Python',
    items: [
      { label: 'python3',       cmd: (i) => `python3 ${i.fileName}` },
      { label: 'python',        cmd: (i) => `python ${i.fileName}` },
      { label: 'pip install',   cmd: () =>  'pip3 install -r requirements.txt' },
      { label: 'venv create',   cmd: () =>  'python3 -m venv venv' },
      { label: 'venv activate', cmd: () =>  'source venv/bin/activate' },
      { label: 'pytest',        cmd: () =>  'pytest' },
      { label: 'rm .pyc',       cmd: (i) => `rm -f ${i.fileName.replace('.py', '.pyc')}` },
    ],
  },
  {
    group: 'Node / TS',
    items: [
      { label: 'ts-node',    cmd: (i) => `npx ts-node ${i.fileName}` },
      { label: 'tsc',        cmd: (i) => `npx tsc ${i.fileName}` },
      { label: 'node run',   cmd: (i) => `node ${i.fileName}` },
      { label: 'npm start',  cmd: () =>  'npm start' },
      { label: 'npm build',  cmd: () =>  'npm run build' },
      { label: 'npm install',cmd: () =>  'npm install' },
      { label: 'npm test',   cmd: () =>  'npm test' },
    ],
  },
  {
    group: 'C / C++',
    items: [
      { label: 'gcc',        cmd: (i) => `gcc -o ${i.fileName.replace(/\.[^.]+$/, '')} ${i.fileName}` },
      { label: 'g++',        cmd: (i) => `g++ -o ${i.fileName.replace(/\.[^.]+$/, '')} ${i.fileName}` },
      { label: 'run binary', cmd: (i) => `./${i.fileName.replace(/\.[^.]+$/, '')}` },
      { label: 'make',       cmd: () =>  'make' },
      { label: 'make clean', cmd: () =>  'make clean' },
    ],
  },
  {
    group: 'System',
    items: [
      { label: 'ls',    cmd: () => 'ls -la' },
      { label: 'pwd',   cmd: () => 'pwd' },
      { label: 'df',    cmd: () => 'df -h' },
      { label: 'ps',    cmd: () => 'ps aux' },
      { label: 'top',   cmd: () => 'top -bn1 | head -20' },
      { label: 'free',  cmd: () => 'free -h' },
      { label: 'mkdir', cmd: () => 'mkdir -p ' },
      { label: 'rm -f', cmd: () => 'rm -f ' },
    ],
  },
];

export const ExecutionView: React.FC<ExecutionViewProps> = ({ onProjects, onDeploy }) => {
  const vm = useExecutionViewModel();
  const [customCommands, setCustomCommands] = useState<Record<string, string>>({});
  const [openPresets, setOpenPresets]       = useState<Record<string, boolean>>({});
  const [activeGroup, setActiveGroup]       = useState<Record<string, string>>({});

  const handleCmdChange = (id: string, val: string) => {
    setCustomCommands(prev => ({ ...prev, [id]: val }));
  };

  const handleRun = (inst: DeployedInstance) => {
    const cmd = customCommands[inst.id] ?? '';
    if (!cmd.trim()) return;
    vm.executeCommand(inst.idConfiguration, cmd);
  };

  const handlePreset = (inst: DeployedInstance, cmd: string) => {
    setCustomCommands(prev => ({ ...prev, [inst.id]: cmd }));
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>, inst: DeployedInstance) => {
    if (e.key === 'Enter') handleRun(inst);
  };

  const togglePresets = (id: string) => {
    setOpenPresets(prev => ({ ...prev, [id]: !prev[id] }));
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
                const isOpen = openPresets[inst.id] ?? false;

                return (
                  <div key={inst.id} className="instance-card">
                    {/* Header */}
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                      <div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                          <StatusDot status={inst.status} />
                          <span style={{ fontFamily: 'var(--mono)', fontSize: '12px' }}>
                            <span style={{ color: 'var(--text2)', fontWeight: 600 }}>{inst.serverName}</span>
                            <span style={{ margin: '0 4px', opacity: 0.4 }}>:</span>
                            <span style={{ color: 'var(--text3)' }}>{inst.path}</span>
                          </span>
                          <span style={{ fontSize: '10px', color: 'var(--text3)', fontFamily: 'var(--mono)' }}>
                            #{inst.idConfiguration}
                          </span>
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

                    {/* Presety — grupy */}
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
                        <div style={{ marginTop: '8px' }}>
                          {/* Zakładki grup */}
                          <div style={{ display: 'flex', gap: '4px', marginBottom: '8px', flexWrap: 'wrap' }}>
                            {PRESET_GROUPS.map((g) => (
                              <button
                                key={g.group}
                                onClick={() => setActiveGroup(prev => ({ ...prev, [inst.id]: g.group }))}
                                style={{
                                  fontSize: '10px',
                                  padding: '2px 8px',
                                  borderRadius: '4px',
                                  border: '1px solid var(--border)',
                                  cursor: 'pointer',
                                  fontFamily: 'var(--mono)',
                                  background: (activeGroup[inst.id] ?? PRESET_GROUPS[0].group) === g.group
                                    ? 'var(--accent, #3b82f6)' : 'transparent',
                                  color: (activeGroup[inst.id] ?? PRESET_GROUPS[0].group) === g.group
                                    ? '#fff' : 'var(--text2)',
                                }}
                              >
                                {g.group}
                              </button>
                            ))}
                          </div>

                          {/* Przyciski aktywnej grupy */}
                          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
                            {(PRESET_GROUPS.find(g => g.group === (activeGroup[inst.id] ?? PRESET_GROUPS[0].group))?.items ?? [])
                              .map((p) => (
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
                        </div>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </main>


      </div>
    </div>
  );
};