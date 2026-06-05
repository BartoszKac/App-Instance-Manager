// src/editor/components/CodeView.tsx
import React, { useMemo } from 'react';
import Editor from 'react-simple-code-editor';
import Prism from 'prismjs';

import 'prismjs/components/prism-clike';
import 'prismjs/components/prism-javascript';
import 'prismjs/components/prism-typescript';
import 'prismjs/components/prism-python';
import 'prismjs/components/prism-rust';
import 'prismjs/components/prism-css';
import 'prismjs/components/prism-json';

import 'prismjs/themes/prism-tomorrow.css';
import { ProjectFile, SAMPLE_CODE } from '@/editor/model';

// Vite/ESM compatibility fix for react-simple-code-editor
const EditorModule = Editor as unknown as { default: React.ComponentType };
const EditorComponent = EditorModule.default || Editor;

interface CodeViewProps {
  file: ProjectFile | null;
  onContentChange?: (content: string) => void;
}

export const CodeView: React.FC<CodeViewProps> = ({ file, onContentChange }) => {
  const prismLanguage = useMemo(() => {
    if (!file) return Prism.languages.clike;
    const langMap: Record<string, Prism.Grammar> = {
      ts:   Prism.languages.typescript,
      js:   Prism.languages.javascript,
      py:   Prism.languages.python,
      rs:   Prism.languages.rust,
      css:  Prism.languages.css,
      json: Prism.languages.json,
    };
    return langMap[file.ext] || Prism.languages.clike;
  }, [file]);

  const displayCode = useMemo(() => {
    if (!file) return '';
    const content = file.content ?? SAMPLE_CODE[file.ext] ?? SAMPLE_CODE['ts'] ?? '';
    return content;
  }, [file]);

  const lineCount = displayCode.split('\n').length;
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
      <div className="code-editor-container" style={{ flex: 1, position: 'relative' }}>
        <EditorComponent
          className="code-content code-editor-root"
          value={displayCode}
          onValueChange={(content: string) => {
            onContentChange?.(content);
          }}
          highlight={(code: string) => Prism.highlight(code, prismLanguage, file.ext)}
          padding={10}
          style={{
            fontFamily: 'var(--mono)',
            fontSize: 14,
            minHeight: '100%',
            outline: 'none',
          }}
        />
      </div>
    </div>
  );
};