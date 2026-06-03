// src/editor/components/CodeView.tsx
import React, { useMemo } from 'react';
import { ProjectFile, SAMPLE_CODE } from '@/editor/model';

interface CodeViewProps {
  file: ProjectFile | null;
}

export const CodeView: React.FC<CodeViewProps> = ({ file }) => {
  const code = useMemo(() => {
    if (!file) return '';
    return file.content || SAMPLE_CODE[file.ext] || SAMPLE_CODE['ts'];
  }, [file]);

  const lineCount = code.split('\n').length;
  const lineNumbers = Array.from({ length: lineCount }, (_, i) => i + 1).join('\n');

  if (!file) {
    return (
      <div className="code-area">
        <div style={{ padding: 32, color: 'var(--text3)', fontSize: 13 }}>
          Wybierz plik z listy po lewej stronie.
        </div>
      </div>
    );
  }

  return (
    <div className="code-area">
      <div className="line-nums">{lineNumbers}</div>
      <pre className="code-content">{code}</pre>
    </div>
  );
};
