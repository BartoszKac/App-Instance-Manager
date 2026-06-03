// src/editor/components/Sidebar.tsx
import React, { useState, useRef } from 'react';
import { Project, ProjectFile } from '@/editor/model';

interface SidebarProps {
  project: Project;
  activeFile: ProjectFile | null;
  onFileClick: (file: ProjectFile) => void;
  onFileRemove: (fileName: string) => void;
  onFileAdd: (file: ProjectFile) => void;
}

export const Sidebar: React.FC<SidebarProps> = ({
  project,
  activeFile,
  onFileClick,
  onFileRemove,
  onFileAdd,
}) => {
  const [isAdding, setIsAdding] = useState(false);
  const [newFileName, setNewFileName] = useState('');
  const inputRef = useRef<HTMLInputElement>(null);

  const handleToggleAdd = () => {
    setIsAdding((prev) => !prev);
    setTimeout(() => inputRef.current?.focus(), 50);
  };

  const handleConfirmAdd = () => {
    const val = newFileName.trim();
    if (!val) return;
    const parts = val.split('.');
    const ext = parts.length > 1 ? parts[parts.length - 1] : 'txt';
    onFileAdd({ name: val, ext, content: '' });
    setNewFileName('');
    setIsAdding(false);
  };

  return (
    <aside className="sidebar">
      <div className="sidebar-header">
        <span className="sidebar-project-name">{project.name}</span>
        <button className="icon-btn" onClick={handleToggleAdd} title="Dodaj plik">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
            <path d="M12 5v14M5 12h14" />
          </svg>
        </button>
      </div>

      <div className="sidebar-section">Pliki</div>

      <div className="file-list">
        {project.files.map((file) => (
          <div
            key={file.name}
            className={`file-item ${activeFile?.name === file.name ? 'active' : ''}`}
            onClick={() => onFileClick(file)}
          >
            <span className={`file-icon file-ext-${file.ext}`}>.{file.ext}</span>
            <span className="file-name">{file.name}</span>
            <span
              className="file-remove"
              onClick={(e) => {
                e.stopPropagation();
                onFileRemove(file.name);
              }}
            >
              <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                <path d="M18 6 6 18M6 6l12 12" />
              </svg>
            </span>
          </div>
        ))}
      </div>

      {isAdding && (
        <div className="add-file-form open">
          <input
            ref={inputRef}
            className="add-file-input"
            value={newFileName}
            onChange={(e) => setNewFileName(e.target.value)}
            placeholder="np. index.ts"
            onKeyDown={(e) => {
              if (e.key === 'Enter') handleConfirmAdd();
              if (e.key === 'Escape') setIsAdding(false);
            }}
          />
          <button className="add-file-confirm" onClick={handleConfirmAdd}>Dodaj</button>
        </div>
      )}

      <button className="btn-add-file" onClick={handleToggleAdd}>
        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
          <path d="M12 5v14M5 12h14" />
        </svg>
        Dodaj plik
      </button>
    </aside>
  );
};
