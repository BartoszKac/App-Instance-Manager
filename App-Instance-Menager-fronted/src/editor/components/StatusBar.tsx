// src/editor/components/StatusBar.tsx
import React from 'react';
import { ProjectFile } from '@/editor/model';

const LANG_LABELS: Record<string, string> = {
  ts: 'TypeScript',
  py: 'Python',
  js: 'JavaScript',
  rs: 'Rust',
  css: 'CSS',
  json: 'JSON',
  txt: 'Text',
};

interface StatusBarProps {
  file: ProjectFile | null;
  lineCount: number;
}

export const StatusBar: React.FC<StatusBarProps> = ({ file, lineCount }) => {
  const lang = file ? (LANG_LABELS[file.ext] ?? file.ext) : '—';
  const lines =
    lineCount === 1 ? '1 linia' : lineCount < 5 ? `${lineCount} linie` : `${lineCount} linii`;

  return (
    <div className="statusbar">
      <div className="status-item">
        <div className="status-dot" />
        <span>Gotowy</span>
      </div>
      <span>{lang}</span>
      <div className="status-spacer" />
      <span>{file ? lines : '—'}</span>
      <span>·</span>
      <span>UTF-8</span>
    </div>
  );
};
