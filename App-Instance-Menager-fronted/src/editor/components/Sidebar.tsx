import { useState } from 'react';
import type { ProjectFile } from '@/editor/model';
import { EXT_TO_LANGUAGE } from '@/editor/model';
import { Icon } from '@/shared/ui/Icon';
import { langClass } from '@/shared/lang';

interface SidebarProps {
  files: ProjectFile[];
  activeFile: ProjectFile | null;
  onFileClick: (file: ProjectFile) => void;
  onFileRemove: (fileName: string) => void;
  onFileAdd: (file: ProjectFile) => void;
}

const SAFE_NAME = /^[A-Za-z0-9_-][A-Za-z0-9_.-]*$/;

const toFile = (name: string): ProjectFile => {
  const ext = name.includes('.') ? name.split('.').pop() ?? 'txt' : 'txt';
  return { id: 0, name, ext, content: '', language: EXT_TO_LANGUAGE[ext] ?? 'UNKNOWN' };
};

const AddFileForm = ({ existing, onAdd, onCancel }: { existing: string[]; onAdd: (f: ProjectFile) => void; onCancel: () => void }) => {
  const [name, setName] = useState('');
  const trimmed = name.trim();
  const error = !trimmed ? '' : !SAFE_NAME.test(trimmed) ? 'Tylko litery, cyfry, „.”, „_” i „-”' : existing.includes(trimmed) ? 'Taki plik już jest' : '';
  const submit = () => { if (trimmed && !error) onAdd(toFile(trimmed)); };

  return (
    <div className="file-panel__add">
      <input
        className="input input--mono"
        autoFocus
        value={name}
        placeholder="Nazwa pliku, np. Utils.java"
        aria-label="Nazwa nowego pliku"
        aria-invalid={Boolean(error)}
        onChange={(e) => setName(e.target.value)}
        onKeyDown={(e) => { if (e.key === 'Enter') submit(); if (e.key === 'Escape') onCancel(); }}
      />
      {error && <span className="file-panel__error">{error}</span>}
    </div>
  );
};

export const Sidebar = ({ files, activeFile, onFileClick, onFileRemove, onFileAdd }: SidebarProps) => {
  const [adding, setAdding] = useState(false);

  return (
    <aside className="file-panel" aria-label="Pliki projektu">
      <div className="file-panel__head">
        <span>Pliki</span>
        <button className="icon-btn" onClick={() => setAdding((v) => !v)} aria-label="Dodaj plik" aria-expanded={adding}><Icon name="add" /></button>
      </div>
      {adding && <AddFileForm existing={files.map((f) => f.name)} onAdd={(f) => { onFileAdd(f); setAdding(false); }} onCancel={() => setAdding(false)} />}
      <ul className="file-panel__list">
        {files.map((file) => (
          <li key={file.name} className="file-row" aria-current={activeFile?.name === file.name ? 'true' : undefined}>
            <button className={`file-row__open ${langClass(file.ext)}`} onClick={() => onFileClick(file)}>
              <span className="file-row__swatch" aria-hidden="true" />
              <span className="file-row__name">{file.name}</span>
              {!file.id && <span className="file-row__new">nowy</span>}
            </button>
            <button className="icon-btn icon-btn--danger file-row__remove" onClick={() => onFileRemove(file.name)} aria-label={`Usuń ${file.name}`}>
              <Icon name="trash" size={16} />
            </button>
          </li>
        ))}
      </ul>
    </aside>
  );
};
