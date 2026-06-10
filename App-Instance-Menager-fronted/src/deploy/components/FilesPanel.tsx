// src/deploy/components/FilesPanel.tsx
import React from 'react';
import { LANG_COLOR, LANG_EXT, UPLOAD_STRATEGIES, STRATEGY_LABEL } from '../model/DeployState';
import { DeployViewModel } from '../viewmodel/useDeployViewModel';
import { Checkbox } from './DeployAtoms';

interface FilesPanelProps {
  vm: DeployViewModel;
}

const STRATEGY_COLOR = {
  SOURCE_CODE:       '#4f7ef7',
  COMPILED_CODE:     '#f59e0b',
  REMOTE_EXECUTABLE: '#22c55e',
} as const;

export const FilesPanel: React.FC<FilesPanelProps> = ({ vm }) => {
  const {
    files,
    selectedFiles,
    toggleFile,
    toggleAllFiles,
    setFileStrategy,
    loading,
    loadError,
  } = vm;

  const allChecked  = files.length > 0 && selectedFiles.size === files.length;
  const someChecked = selectedFiles.size > 0 && !allChecked;

  const sourceFiles  = files.filter((f) => f.source === 'source');
  const processFiles = files.filter((f) => f.source === 'process');

  const renderFileRow = (file: ReturnType<typeof files[0]['source'] extends 'source' ? never : never> extends never ? typeof files[0] : typeof files[0]) => {
    const checked = selectedFiles.has(file.key);
    const color   = LANG_COLOR[file.language] ?? '#94a3b8';
    const ext     = LANG_EXT[file.language]   ?? file.language.toLowerCase();
    const stratColor = STRATEGY_COLOR[file.strategy] ?? '#94a3b8';

    return (
      <div
        key={file.key}
        className={`file-item file-item-deploy ${checked ? 'active' : ''}`}
        style={{ background: checked ? `${color}15` : 'transparent' }}
      >
        {/* checkbox — tylko ta kolumna zaznacza plik */}
        <div onClick={() => toggleFile(file.key)} style={{ display: 'flex', alignItems: 'center', gap: 6, flex: 1, cursor: 'pointer' }}>
          <Checkbox checked={checked} onChange={() => toggleFile(file.key)} />
          <span style={{
            fontFamily: 'var(--mono)', fontSize: 10, fontWeight: 700,
            color, background: color + '20',
            padding: '1px 5px', borderRadius: 3, flexShrink: 0,
          }}>
            .{ext}
          </span>
          <span className="file-name">{file.name}</span>
        </div>

        {/* strategia — dropdown per plik */}
        <select
          value={file.strategy}
          onChange={(e) => setFileStrategy(file.key, e.target.value as typeof file.strategy)}
          onClick={(e) => e.stopPropagation()}
          style={{
            fontSize: 9, fontFamily: 'var(--mono)',
            color: stratColor,
            background: stratColor + '15',
            border: `1px solid ${stratColor}40`,
            borderRadius: 3, padding: '1px 4px',
            cursor: 'pointer', outline: 'none',
            flexShrink: 0,
          }}
        >
          {UPLOAD_STRATEGIES.map((s) => (
            <option key={s} value={s}>{STRATEGY_LABEL[s]}</option>
          ))}
        </select>
      </div>
    );
  };

  return (
    <aside className="sidebar sidebar-deploy">
      <div className="sidebar-header">
        <span className="sidebar-project-name">Pliki źródłowe</span>
        <span className="sidebar-badge">{selectedFiles.size} wybrano</span>
      </div>

      {loading && (
        <div style={{ padding: 16, color: 'var(--text3)', fontSize: 12 }}>
          <span className="deploy-spinner" style={{ marginRight: 8 }} />
          Ładowanie plików…
        </div>
      )}

      {loadError && (
        <div style={{ padding: 12, color: '#ef4444', fontSize: 11, fontFamily: 'var(--mono)' }}>
          ✗ {loadError}
        </div>
      )}

      {!loading && !loadError && (
        <div className="file-list" style={{ flex: 1, overflowY: 'auto' }}>
          {/* zaznacz wszystkie */}
          <div
            className="file-item project-group"
            style={{ background: 'var(--bg3)', borderLeft: '4px solid var(--accent)' }}
          >
            <Checkbox checked={allChecked} indeterminate={someChecked} onChange={toggleAllFiles} />
            <div
              onClick={toggleAllFiles}
              style={{ fontSize: 11, color: 'var(--text2)', cursor: 'pointer', fontWeight: 600 }}
            >
              Wszystkie pliki
            </div>
          </div>

          {sourceFiles.length > 0 && (
            <>
              <div className="sidebar-section">Klasy główne</div>
              {sourceFiles.map(renderFileRow)}
            </>
          )}

          {processFiles.length > 0 && (
            <>
              <div className="sidebar-section">Procesy</div>
              {processFiles.map(renderFileRow)}
            </>
          )}

          {files.length === 0 && (
            <div style={{ padding: 16, color: 'var(--text3)', fontSize: 12, textAlign: 'center' }}>
              Brak dostępnych plików
            </div>
          )}
        </div>
      )}
    </aside>
  );
};