import { useMemo } from 'react';
import Editor from 'react-simple-code-editor';
import Prism from 'prismjs';
import 'prismjs/components/prism-clike';
import 'prismjs/components/prism-c';
import 'prismjs/components/prism-cpp';
import 'prismjs/components/prism-java';
import 'prismjs/components/prism-python';
import 'prismjs/components/prism-bash';
import 'prismjs/components/prism-json';
import type { ProjectFile } from '@/editor/model';
import { EmptyState } from '@/shared/ui/atoms';
import { Icon } from '@/shared/ui/Icon';

const EditorModule = Editor as unknown as { default?: typeof Editor };
const EditorComponent = EditorModule.default ?? Editor;

const GRAMMAR_OF: Record<string, string> = {
  java: 'java',
  py: 'python',
  cpp: 'cpp',
  cc: 'cpp',
  c: 'c',
  h: 'cpp',
  sh: 'bash',
  json: 'json',
};

interface CodeViewProps {
  file: ProjectFile | null;
  onContentChange: (content: string) => void;
  onClear: () => void;
}

export const CodeView = ({ file, onContentChange, onClear }: CodeViewProps) => {
  const grammarName = file ? GRAMMAR_OF[file.ext] ?? 'clike' : 'clike';
  const grammar = Prism.languages[grammarName] ?? Prism.languages.clike;
  const code = file?.content ?? '';
  const gutter = useMemo(() => Array.from({ length: code.split('\n').length }, (_, i) => i + 1).join('\n'), [code]);

  if (!file) {
    return <div className="code"><EmptyState title="Żaden plik nie jest otwarty" hint="Wybierz plik z listy po lewej albo dodaj nowy." /></div>;
  }

  return (
    <div className="code">
      <div className="code__scroll">
        <pre className="code__gutter" aria-hidden="true">{gutter}</pre>
        <EditorComponent
          className="code__editor"
          textareaClassName="code__textarea"
          textareaId="code-editor"
          aria-label={`Treść pliku ${file.name}`}
          value={code}
          onValueChange={onContentChange}
          highlight={(value: string) => Prism.highlight(value, grammar, grammarName)}
          padding={{ top: 16, bottom: 48, left: 16, right: 24 }}
          tabSize={4}
          insertSpaces
        />
      </div>
      <button className="btn btn--ghost btn--sm code__clear" onClick={onClear} disabled={!code}>
        <Icon name="erase" size={15} />Wyczyść plik
      </button>
    </div>
  );
};
